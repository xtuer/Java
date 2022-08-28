package misc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import misc.SshHelper.SshResult;
import misc.SshHelper.SshResultType;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.of;
import static org.apache.commons.text.StringSubstitutor.replace;

/**
 * SaltStack 执行 shell 脚本、复制文件和目录。
 *
 * 最重要的方法有:
 * A. executeScript: 执行脚本
 * B. copyFile : 复制文件
 * C. copyDir  : 复制目录
 *
 * 复制文件和目录的方向有:
 * A. Master -> Minion
 * B. Minion -> Master
 * C. Minion -> Minion
 *
 * SaltMaster 上的文件位置:
 * A. 加密脚本: 在 {saltFileSystemBase}/scripts-encrypted 目录下
 * B. 临时脚本: 在 {saltFileSystemBase}/scripts-temp/{yyyy-MM-dd} 目录下
 * C. 介质文件: 在 {saltFileSystemBase}/files 目录下
 *
 * SaltMinion 上的文件位置:
 * A. 临时脚本: 在 /dmp/scripts-temp/{yyyy-MM-dd} 目录下
 * B. 介质文件: 自动化编排时指定位置
 */
@Slf4j
public class SaltStackRunner {
    /**
     * SaltStack 配置
     */
    private final SaltStackConfig config;

    /**
     * Salt 执行 shell 脚本的命令模板。
     * 例如: salt '192.168.12.102' cmd.run 'sh /srv/salt/base/scripts-temp/x.sh-16702780281337228849.sh' --out=json
     */
    private static final String SALT_CMD_RUN = "salt '${minion}' cmd.run '${cmd}' --out=json";

    /**
     * SaltMaster 复制文件到 SaltMinion 的命令模板，使用 salt:// 协议。
     * 例如: salt '192.168.12.102' cp.get_file 'salt://a.txt' '/root/foo/bar/' makedirs=True --out=json
     */
    private static final String SALT_COPY_FILE = "salt '${minion}' cp.get_file '${srcSaltPath}' '${destDir}' makedirs=True --out=json";

    /**
     * SaltMaster 复制目录到 SaltMinion 的命令模板，使用 salt:// 协议。
     * 例如: salt '192.168.12.102' cp.get_dir 'salt://scripts-encrypted' '/root/foo/bar' makedirs=True --out=json
     */
    private static final String SALT_COPY_DIR = "salt '${minion}' cp.get_dir '${srcSaltDir}' '${destDir}' makedirs=True --out=json";

    /**
     * 从 Minion 到 Minion 复制文件时，把文件从 Minion 下载到 Master 的命令。
     * 例如: salt '192.168.12.102' cp.push '/root/dmp/get_hostname.sh' --out=json
     */
    private static final String SALT_MINIONFS_FILE_MINION_TO_MASTER = "salt '${minion}' cp.push '${srcPath}' --out=json";

    /**
     * 从 Minion 到 Minion 复制文件时，把文件从 Master 复制到 Minion。
     * 例如: salt '192.168.12.101' cp.get_file 'salt://192.168.12.102/root/dmp/get_hostname.sh' '/dmp/temp/minion/' makedirs=True --out=json
     */
    private static final String SALT_MINIONFS_FILE_MASTER_TO_MINION = "salt '${destMinion}' cp.get_file 'salt://${srcMinion}${srcPath}' '${destDir}' makedirs=True --out=json";

    /**
     * 从 Minion 到 Minion 复制目录时，把目录从 Minion 下载到 Master 的命令。
     * 例如: salt '192.168.12.102' cp.push_dir '/root/dmp' --out=json
     */
    private static final String SALT_MINIONFS_DIR_MINION_TO_MASTER = "salt '${minion}' cp.push_dir '${srcDir}' --out=json";

    /**
     * 从 Minion 到 Minion 复制文件时，把目录从 Master 复制到 Minion。
     * 例如: salt '192.168.12.101' cp.get_dir 'salt://192.168.12.102/root/dmp' '/dmp/temp/minion' makedirs=True --out=json
     */
    private static final String SALT_MINIONFS_DIR_MASTER_TO_MINION = "salt '${destMinion}' cp.get_dir 'salt://${srcMinion}${srcDir}' '${destDir}' makedirs=True --out=json";

    /**
     * 从 Minion 到 Minion 复制文件时，在 Master 上暂存文件的路径。
     * 格式: /var/cache/salt/master/minions/<minion>/files/<abs-path-at-minion>
     * 例如: /var/cache/salt/master/minions/192.168.12.102/files/dmp/temp/foo.sh
     *
     * 注意: 有的文档介绍的路径没有 files 部分，我测试的时候又有。需要以实际情况为准，如果不同版本有多种情况的话，此 pattern 变量需要放到配置文件中。
     */
    private static final String SALT_MINIONFS_MASTER_CACHE_PATH = "/var/cache/salt/master/minions/${minion}/files${srcPath}";

    /**
     * 目录不存在时创建目录的命令。
     */
    private static final String MKDIR_WHEN_NON_EXIST = "[ -d \"${dir}\" ] && echo '目录存在不需要创建' || mkdir -p ${dir}";

    /**
     * Salt 相关的错误码。
     */
    private static final int ERROR_FILE_NOT_IN_SALT_BASE = 10001;
    private static final int ERROR_DIR_NOT_IN_SALT_BASE  = 10002;
    private static final int ERROR_FILE_NOT_EXIST        = 10003;
    private static final int ERROR_DIR_NOT_EXIST         = 10004;

    /**
     * 使用配置创建对象。
     *
     * @param config 自动化使用的 SaltStack 配置
     */
    public SaltStackRunner(SaltStackConfig config) {
        this.config = config;

        if (log.isDebugEnabled()) {
            log.debug("SaltStack 配置: {}", config.toString());
        }
    }

    /**
     * 使用 SaltStack 在 SaltMinion 上执行脚本。脚本被加密存储在 SaltMaster 的 {saltFileSystemBase}/scripts-encrypted 目录下。
     *
     * @param minionIp 执行脚本主机的 IP
     * @param scriptName 脚本名称
     * @param args 脚本参数
     * @return 返回执行结果
     */
    public SshResult executeScript(String minionIp, String scriptName, String args) throws Exception {
        /*
         逻辑:
         1. Web 服务器从 SaltMaster 获取 shell 脚本
         2. 解密 shell 脚本，并保存到本地临时文件
         3. 复制临时 shell 脚本到 SaltMaster
         4. 复制 SaltMaster 上的 shell 脚本到 SaltMinion
         5. 执行 SaltMinion 上的脚本
         */

        Script script = new Script(config.getSaltFileSystemBase(), scriptName);

        // 建立到 SaltMaster 的 ssh 连接
        try (SshHelper ssh = connectToSaltMaster()) {
            // [1] Web 服务器从 SaltMaster 获取 shell 脚本
            log.info("获取脚本: {}", script.masterEncryptedScriptPath);

            String cmd = "cat " + script.masterEncryptedScriptPath;
            SshResult result = ssh.executeCommand(cmd);
            if (!result.isSuccess()) {
                return result;
            }

            String encryptedScriptContent = result.getContent();
            log.debug("加密的脚本:\n{}", encryptedScriptContent);

            // [2] 解密 shell 脚本，并保存到本地临时文件
            String scriptContent = encryptedScriptContent; // TODO: 解密 shell 脚本 (自动化的脚本都是加密保存的，需要解密后才能使用)
            log.debug("解密的脚本:\n{}", scriptContent);

            // 保存到本地临时文件，文件名格式为 <scriptName>-<random>.sh，例如 init.sh-1058843542831845790.sh
            Files.write(Paths.get(script.localTempPath), scriptContent.getBytes(StandardCharsets.UTF_8));

            // [3] 复制临时 shell 脚本到 SaltMaster
            log.info("复制脚本: 脚本解密后复制到 SaltMaster 的临时目录");
            ssh.sftpPut(script.localTempPath, script.masterTempScriptDir);

            // [4] 复制 SaltMaster 上的 shell 脚本到 SaltMinion
            log.info("复制脚本: 从 SaltMaster 到 SaltMinion");
            result = copyFileFromMasterToMinion(ssh, minionIp, script.masterTempScriptPath, script.minionTempScriptDir);
            if (!result.isSuccess()) {
                return result;
            }

            // [5] 执行 SaltMinion 上的脚本
            log.info("执行脚本: 从 SaltMaster 上执行 Minion 的脚本 [{}]", script.minionTempScriptPath);
            cmd = String.format("sh %s %s", script.minionTempScriptPath, Objects.toString(args, "")); // 执行 shell 脚本: sh x.sh [args]
            result = saltCmdRun(ssh, minionIp, cmd);

            log.info("脚本结果:\n{}", result.getContent());

            return result;
        }

        // TODO: 清理临时文件 (可配置是否清理，调试时不清理非常有用，方便定位问题):
        // A. 本地临时文件不需要删除，操作系统会自动删除
        // B. SaltMaster 上会产生大量的临时脚本，按天存储，可以使用 crontab 定时任务处理，没必要在程序里删除
        // C. SaltMinion 上会产生少量的临时脚本，按天存储，其实不清理也没事
    }

    /**
     * 使用 SaltStack cmd.run 执行命令。
     *
     * @param ssh ssh 连接
     * @param minionIp 要执行命令的 Minion 的 IP
     * @param cmd 要执行的命令 (注意: cmd 中最好不要有但引号，如果有必须转义为 \' 且要成对出现)
     * @return 返回执行结果
     * @throws Exception 执行可能抛出 Exception
     */
    public SshResult saltCmdRun(SshHelper ssh, String minionIp, String cmd) throws Exception {
        // salt '192.168.12.102' cmd.run 'sh x.sh [args]' --out=json
        String saltCmd = replace(SALT_CMD_RUN, of("minion", minionIp, "cmd", cmd));
        SshResult result = ssh.executeCommand(saltCmd);
        SaltStackRunner.handleSaltResult(minionIp, result);

        return result;
    }

    /**
     * 在主机之间复制文件。当 sourceIp 为 null 时则表示 source 为 SaltMaster，当 destinationIp 为 null 时则表示 destination 为 SaltMaster。
     *
     * 当 sourceIp 为 null 时表示源主机为 Master。
     */
    public SshResult copyFile(@Nullable String sourceIp, String destinationIp, String sourcePath, String destinationDir) throws Exception {
        try (SshHelper ssh = connectToSaltMaster()) {
            return copy(ssh, sourceIp, destinationIp, sourcePath, destinationDir, true);
        }
    }

    /**
     * 在主机之间复制目录。当 sourceIp 为 null 时则表示 source 为 SaltMaster，当 destinationIp 为 null 时则表示 destination 为 SaltMaster。
     *
     * 当 sourceIp 为 null 时表示源主机为 Master。
     */
    public SshResult copyDir(@Nullable String sourceIp, String destinationIp, String sourceDir, String destinationDir) throws Exception {
        try (SshHelper ssh = connectToSaltMaster()) {
            return copy(ssh, sourceIp, destinationIp, sourceDir, destinationDir, false);
        }
    }

    /**
     * 在主机之间复制文件或者目录。当 sourceIp 为 null 时则表示 source 为 SaltMaster，当 destinationIp 为 null 时则表示 destination 为 SaltMaster。
     *
     * @param ssh 服务到 SaltMaster 的 ssh 连接
     * @param sourceIp 源文件所在主机 IP
     * @param destinationIp 目标目录所在主机 IP
     * @param source 源文件或者目录的路径
     * @param destinationDir 目标目录
     * @param isFile 为 true 表示传文件，false 表示传目录
     * @return 返回执行结果
     * @throws Exception 执行可能抛出 Exception
     */
    private SshResult copy(SshHelper ssh, String sourceIp, String destinationIp, String source, String destinationDir, boolean isFile) throws Exception {
        /*
         逻辑:
         1. 获取 SaltMaster IP，如果 sourceIp 为 null 则表示文件在 SalterMaster 上
         2. 根据 sourceIp 和 destinationIp 判断复制目录的方向:
            A. Master 到 Minion
            B. Minion 到 Master
            C. Minion 到 Minion
         3. 根据不同的复制方向调用对应的方法进行复制
         */

        // SaltStack Master IP
        String masterIp = this.config.getMasterIp();

        // 如果源 IP 为 null 则表示源在 SaltMaster 上。
        // 如果目标 IP 为 null 则表示目标在 SaltMaster 上。
        sourceIp = Optional.ofNullable(sourceIp).orElse(masterIp);
        destinationIp = Optional.ofNullable(destinationIp).orElse(masterIp);

        if (sourceIp.equals(destinationIp)) {
            // Source -> Source
            throw new RuntimeException("源 IP 和目标 IP 相等，不支持在同一个主机上移动目录");
        } else if (masterIp.equals(sourceIp)) {
            // Master -> Minion
            return isFile
                    ? copyFileFromMasterToMinion(ssh, destinationIp, source, destinationDir)
                    : copyDirFromMasterToMinion(ssh, destinationIp, source, destinationDir);
        } else if (masterIp.equals(destinationIp)) {
            // Minion -> Master
            // 传到 Master Cache 后再移动到 destinationDir 下
            return isFile
                    ? copyFileFromMinionToMaster(ssh, sourceIp, source, destinationDir)
                    : copyDirFromMinionToMaster(ssh, sourceIp, source, destinationDir);
        } else {
            // Minion -> Minion
            return isFile
                    ? copyFileFromMinionToMinion(ssh, sourceIp, destinationIp, source, destinationDir)
                    : copyDirFromMinionToMinion(ssh, sourceIp, destinationIp, source, destinationDir);
        }
    }

    /**
     * 从 SaltMaster 复制文件到 SaltMinion，目标文件夹不存在会自动创建。
     *
     * @param ssh 服务到 SaltMaster 的 ssh 连接
     * @param minionIp SaltMinion 的 IP
     * @param sourcePath SaltMaster 上的源文件，文件需要在 SaltMaster 配置的 file_roots.base 目录下
     * @param destinationDir 文件保存到 SaltMinion 上的 destinationDir 目录下
     * @return 返回执行结果
     */
    public SshResult copyFileFromMasterToMinion(SshHelper ssh, String minionIp, String sourcePath, String destinationDir) throws Exception {
        /*
         逻辑:
         1. 把操作系统文件路径转为 Salt 文件系统的路径，以 salt:// 开头，如 salt://foo/bar.txt
         2. 确保 destinationDir 以 / 结尾，否则不会作为目录处理
         3. 构造 Salt 复制文件命令，并执行
         4. 处理 Salt 命令结果

         Salt 复制文件示例: salt '192.168.12.102' cp.get_file 'salt://scripts-temp/x.sh' '/root/foo/bar/' makedirs=True --out=json
         复制后得到 /root/foo/bar/x.sh
         */

        // [1] 把操作系统文件路径转为 Salt 文件系统的路径，以 salt:// 开头，如 salt://a.txt
        String saltBase = config.getSaltFileSystemBase();
        String sourcePathInSaltFileSystem = SaltStackRunner.convertPathToSaltFileSystemPath(saltBase, sourcePath);
        if (sourcePathInSaltFileSystem == null) {
            return new SshResult(ERROR_FILE_NOT_IN_SALT_BASE, String.format("文件不在 Salt 文件系统下: Salt 文件系统根目录 [%s]，文件 [%s]", saltBase, sourcePath));
        }

        // [2] 确保 destinationDir 以 / 结尾，否则不会作为目录处理
        destinationDir = SaltStackRunner.ensureDirPattern(destinationDir);

        // [3] 构造 Salt 复制文件命令，并执行
        // [4] 处理 Salt 命令结果
        log.info("复制文件: Minion [{}], 系统路径 [{}], SaltSrcPath [{}], DestDir [{}]", minionIp, sourcePath, sourcePathInSaltFileSystem, destinationDir);
        String cmd = replace(SALT_COPY_FILE, of("minion", minionIp, "srcSaltPath", sourcePathInSaltFileSystem, "destDir", destinationDir));
        SshResult result = ssh.executeCommand(cmd);
        SaltStackRunner.handleSaltResult(minionIp, result);
        SaltStackRunner.handleSaltResultForCopy(result);

        if (!result.isSuccess()) {
            log.warn("复制文件失败: Minion [{}], Src [{}], DestDir [{}]", minionIp, sourcePathInSaltFileSystem, destinationDir);
        }

        return result;
    }

    /**
     * SaltMaster 复制目录到 SaltMinion，目标文件夹不存在会自动创建，会把目录下的文件以及子目录等都复制过去 (递归)。
     *
     * @param ssh 服务到 SaltMaster 的 ssh 连接
     * @param minionIp SaltMinion 的 IP
     * @param sourceDir SaltMaster 上的源目录，目录需要在 SaltMaster 配置的 file_roots.base 目录下
     * @param destinationDir 源目录保存到 SaltMinion 上的 destinationDir 目录下
     * @return 返回执行结果
     * @throws Exception 复制文件如网络错误等时抛出 Exception 异常
     */
    public SshResult copyDirFromMasterToMinion(SshHelper ssh, String minionIp, String sourceDir, String destinationDir) throws Exception {
        /*
         逻辑:
         1. 把操作系统文件路径转为 Salt 文件系统的路径，以 salt:// 开头，如 salt://a.txt
         2. 构造 Salt 复制文件命令，并执行
         3. 处理 Salt 命令结果

         Salt 复制目录示例: salt '192.168.12.102' cp.get_dir 'salt://scripts-encrypted' '/root/foo/bar' makedirs=True --out=json
         复制后得到 /root/foo/bar/scripts-encrypted
         */

        // [1] 把操作系统文件路径转为 Salt 文件系统的路径，以 salt:// 开头，如 salt://a.txt
        String saltBase = config.getSaltFileSystemBase();
        String sourceDirInSaltFileSystem = SaltStackRunner.convertPathToSaltFileSystemPath(saltBase, sourceDir);
        if (sourceDirInSaltFileSystem == null) {
            return new SshResult(ERROR_DIR_NOT_IN_SALT_BASE, String.format("目录不在 Salt 文件系统下: Salt 文件系统根目录 [%s]，目录 [%s]", saltBase, sourceDir));
        }

        // [2] 构造 Salt 复制文件命令，并执行
        // [3] 处理 Salt 命令结果
        log.info("复制目录: Minion [{}], 系统路径 [{}], SaltSrcDir [{}], DestDir [{}]", minionIp, sourceDir, sourceDirInSaltFileSystem, destinationDir);
        String cmd = replace(SALT_COPY_DIR, of("minion", minionIp, "srcSaltDir", sourceDirInSaltFileSystem, "destDir", destinationDir));
        SshResult result = ssh.executeCommand(cmd);
        SaltStackRunner.handleSaltResult(minionIp, result);
        SaltStackRunner.handleSaltResultForCopy(result);

        if (!result.isSuccess()) {
            log.warn("复制目录失败: Minion [{}], SrcDir [{}], DestDir [{}]", minionIp, sourceDirInSaltFileSystem, destinationDir);
        }

        return result;
    }

    /**
     * 把文件从一个 SaltMinion fromMinion 复制到另一个 SaltMinion toMinion。
     * 提示: 需要 SaltMaster 开启 Minion FS。
     *
     * @param ssh 服务到 SaltMaster 的 ssh 连接
     * @param sourceMinionIp 文件所在的 SaltMinion IP
     * @param destinationMinionIp 文件复制到的 SaltMinion IP
     * @param sourcePath 源文件路径
     * @param destinationDir 目标目录
     * @return 返回执行结果
     * @throws Exception 复制文件如网络错误等时抛出 Exception 异常
     */
    public SshResult copyFileFromMinionToMinion(SshHelper ssh, String sourceMinionIp, String destinationMinionIp, String sourcePath, String destinationDir) throws Exception {
        /*
         逻辑:
         1. 确保 destinationDir 以 / 结尾，否则不会作为目录处理
         2. 把文件从 SourceMinion 下载到 SaltMaster
         3. 把文件从 SaltMaster 复制到 DestinationMinion
         */

        log.info("复制文件: 源 Minion [{}]，目标 Minion [{}]，源文件路径 [{}]，目标目录 [{}]", sourceMinionIp, destinationMinionIp, sourcePath, destinationDir);

        // [1] 确保 destinationDir 以 / 结尾，否则不会作为目录处理
        destinationDir = SaltStackRunner.ensureDirPattern(destinationDir);

        // [2] 把文件从 SourceMinion 下载到 SaltMaster
        SshResult result = copyFileFromMinionToMasterCache(ssh, sourceMinionIp, sourcePath);
        if (!result.isSuccess()) {
            log.warn("{}: 不继续执行从 Master 复制文件到 Minion，源文件 [{}]", result.getContent(), sourcePath);
            return result;
        }

        // [3] 把文件从 SaltMaster 复制到 DestinationMinion
        log.info("复制文件: 使用 MinionFS 把文件从 Master 复制到 Minion，MinionIP [{}], 目标目录 [{}]", destinationMinionIp, destinationDir);
        String toCmd = replace(SALT_MINIONFS_FILE_MASTER_TO_MINION, of("srcMinion", sourceMinionIp, "destMinion", destinationMinionIp, "srcPath", sourcePath, "destDir", destinationDir));
        result = ssh.executeCommand(toCmd);
        SaltStackRunner.handleSaltResult(destinationMinionIp, result);
        SaltStackRunner.handleSaltResultForCopy(result);

        return result;
    }

    /**
     * 从 Minion 复制文件到 Master，文件保存到 MinionFS 在 Master 的 cache 目录中。
     *
     * @param ssh 服务到 SaltMaster 的 ssh 连接
     * @param sourceMinionIp 源文件所在 Minion IP
     * @param sourcePath 源文件路径
     * @return 返回执行结果
     */
    private SshResult copyFileFromMinionToMasterCache(SshHelper ssh, String sourceMinionIp, String sourcePath) throws Exception {
        /*
         逻辑:
         1. 把文件从 Minion 复制到 Master
         2. 处理命令结果
         */
        log.info("复制文件: 使用 MinionFS 把文件从 Minion 复制到 Master Cache，MinionIP [{}], 源文件路径 [{}]", sourceMinionIp, sourcePath);

        // [1] 把文件从 Minion 复制到 Master
        String cmd = replace(SALT_MINIONFS_FILE_MINION_TO_MASTER, of("minion", sourceMinionIp, "srcPath", sourcePath));
        SshResult result = ssh.executeCommand(cmd);

        // [2] 处理命令结果
        SaltStackRunner.handleSaltResult(sourceMinionIp, result);
        SaltStackRunner.handleSaltResultForCopy(result);

        return result;
    }

    /**
     * 把目录从一个 SaltMinion fromMinion 复制到另一个 SaltMinion toMinion。
     * 提示: 需要 SaltMaster 开启 Minion FS。
     *
     * @param ssh 服务到 SaltMaster 的 ssh 连接
     * @param sourceMinionIp 目录所在的 SaltMinion IP
     * @param destinationMinionIp 目录复制到的 SaltMinion IP
     * @param sourceDir 源目录
     * @param destinationDir 目标目录
     * @return 返回执行结果
     * @throws Exception 复制文件如网络错误等时抛出 Exception 异常
     */
    public SshResult copyDirFromMinionToMinion(SshHelper ssh, String sourceMinionIp, String destinationMinionIp, String sourceDir, String destinationDir) throws Exception {
        /*
         逻辑:
         1. 把目录从 SourceMinion 下载到 SaltMaster
         2. 把目录从 SaltMaster 复制到 DestinationMinion
         */

        log.info("复制目录: 源 Minion [{}]，目标 Minion [{}]，源目录 [{}]，目标目录 [{}]", sourceMinionIp, destinationMinionIp, sourceDir, destinationDir);

        // [1] 把目录从 SourceMinion 下载到 SaltMaster
        SshResult result = copyDirFromMinionToMasterCache(ssh, sourceMinionIp, sourceDir);
        if (!result.isSuccess()) {
            log.warn("{}: 不继续执行从 Master 复制目录到 Minion，源目录 [{}]", result.getContent(), sourceDir);
            return result;
        }

        // [2] 把目录从 SaltMaster 复制到 DestinationMinion
        log.info("复制目录: 使用 MinionFS 把目录从 Master 复制到 Minion，MinionIP [{}], 目标目录 [{}]", destinationMinionIp, destinationDir);
        String toCmd = replace(SALT_MINIONFS_DIR_MASTER_TO_MINION, of("srcMinion", sourceMinionIp, "destMinion", destinationMinionIp, "srcDir", sourceDir, "destDir", destinationDir));
        result = ssh.executeCommand(toCmd);
        SaltStackRunner.handleSaltResult(destinationMinionIp, result);
        SaltStackRunner.handleSaltResultForCopy(result);

        return result;
    }

    /**
     * 从 Minion 复制目录到 Master，文件保存到 MinionFS 在 Master 的 cache 目录中。
     *
     * @param ssh 服务到 SaltMaster 的 ssh 连接
     * @param sourceMinionIp 源文件所在 Minion IP
     * @param sourceDir 源目录
     * @return 返回执行结果
     */
    private SshResult copyDirFromMinionToMasterCache(SshHelper ssh, String sourceMinionIp, String sourceDir) throws Exception {
        /*
         逻辑:
         1. 把目录从 Minion 复制到 Master
         2. 处理命令结果
         */
        log.info("复制目录: 使用 MinionFS 把目录从 Minion 复制到 Master Cache，MinionIP [{}], 源目录 [{}]", sourceMinionIp, sourceDir);

        // [1] 把目录从 Minion 复制到 Master
        String cmd = replace(SALT_MINIONFS_DIR_MINION_TO_MASTER, of("minion", sourceMinionIp, "srcDir", sourceDir));
        SshResult result = ssh.executeCommand(cmd);

        // [2] 处理命令结果
        SaltStackRunner.handleSaltResult(sourceMinionIp, result);
        SaltStackRunner.handleSaltResultForCopy(result);

        return result;
    }

    /**
     * 从 Minion 复制文件到 Master。
     *
     * @param ssh 服务到 SaltMaster 的 ssh 连接
     * @param minionIp 源文件所在 Minion IP
     * @param sourcePath 源文件路径
     * @param destinationDir 目标目录
     * @return 返回执行结果
     */
    public SshResult copyFileFromMinionToMaster(SshHelper ssh, String minionIp, String sourcePath, String destinationDir) throws Exception {
        /*
         逻辑:
         1. 确保 destinationDir 以 / 结尾，否则不会作为目录处理
         2. 把文件从 Minion 下载到 Master Cache 目录
         3. 删除 Master 上对应位置可能存在的文件
         4. 目标目录如果不存在则创建
         5. 把文件从 Master Cache 复制到目标文件夹下 (因为 Salt Cache 中的文件是全局只读，如果使用移动的话 ssh user 有可能没有操作权限)
         */

        log.info("复制文件: 把文件从 Minion 复制到 Master，源 Minion [{}]，源文件 [{}]，目标目录 [{}]", minionIp, sourcePath, destinationDir);

        // [1] 确保 destinationDir 以 / 结尾，否则不会作为目录处理
        destinationDir = SaltStackRunner.ensureDirPattern(destinationDir);

        // [2] 把文件从 Minion 下载到 Master Cache 目录
        SshResult result = copyFileFromMinionToMasterCache(ssh, minionIp, sourcePath);
        if (!result.isSuccess()) {
            log.warn("{}: 不继续执行从 Master Cache 复制文件到目录 [{}]", result.getContent(), destinationDir);
            return result;
        }

        // 构造文件相关的路径
        String fileName = Paths.get(sourcePath).getFileName().toString();
        String destPath = destinationDir + fileName;
        String cachePath = replace(SALT_MINIONFS_MASTER_CACHE_PATH, of("minion", minionIp, "srcPath", sourcePath));

        // [3] 删除 Master 上对应位置原来可能存在的文件
        log.info("删除文件: 删除 Master 上的文件 [{}]", destPath);
        ssh.executeCommand("rm -rf " + destPath);

        // [4] 目标目录如果不存在则创建
        log.info("创建目录: 目标目录如果不存在则创建，避免复制文件失败，目录 [{}]", destinationDir);
        result = ensureDirInMaster(ssh, destinationDir);
        if (!result.isSuccess()) {
            return result;
        }

        // [5] 把文件从 Master Cache 复制到目标文件夹下 (因为 Salt Cache 中的文件是全局只读，如果使用移动的话 ssh user 有可能没有操作权限)
        log.info("复制文件: 复制 Master Cache 中的文件到目标目录，cachePath [{}], destDir [{}]", cachePath, destinationDir);
        result = ssh.executeCommand(String.format("cp %s %s", cachePath, destinationDir));

        return result;
    }

    /**
     * 从 Minion 复制目录到 Master。
     *
     * @param ssh 服务到 SaltMaster 的 ssh 连接
     * @param minionIp 源目录所在 Minion IP
     * @param sourceDir 源目录
     * @param destinationDir 目标目录
     * @return 返回执行结果
     */
    public SshResult copyDirFromMinionToMaster(SshHelper ssh, String minionIp, String sourceDir, String destinationDir) throws Exception {
        /*
         逻辑:
         1. 确保 destinationDir 以 / 结尾，否则不会作为目录处理
         2. 把目录从 Minion 下载到 Master Cache 目录
         3. 删除 Master 上对应位置可能存在的目录
         4. 目标目录如果不存在则创建
         5. 把目录从 Master Cache 复制到目标目录下 (因为 Salt Cache 中的文件是全局只读，如果使用移动的话 ssh user 有可能没有操作权限)
         */

        log.info("复制目录: 把目录从 Minion 复制到 Master，源 Minion [{}]，源目录 [{}]，目标目录 [{}]", minionIp, sourceDir, destinationDir);

        // [1] 确保 destinationDir 以 / 结尾，否则不会作为目录处理
        destinationDir = SaltStackRunner.ensureDirPattern(destinationDir);

        // [2] 把目录从 Minion 下载到 Master Cache 目录
        SshResult result = copyDirFromMinionToMasterCache(ssh, minionIp, sourceDir);
        if (!result.isSuccess()) {
            log.warn("{}: 不继续执行从 Master Cache 复制目录到目录 [{}]", result.getContent(), destinationDir);
            return result;
        }

        // 构造文件相关的路径
        String fileName = Paths.get(sourceDir).getFileName().toString();
        String destDir = destinationDir + fileName;
        String cacheDir = replace(SALT_MINIONFS_MASTER_CACHE_PATH, of("minion", minionIp, "srcPath", sourceDir));

        // [3] 删除 Master 上对应位置可能存在的目录
        log.info("删除目录: 删除 Master 上的目录 [{}]", destDir);
        ssh.executeCommand("rm -rf " + destDir);

        // [4] 目标目录如果不存在则创建
        log.info("创建目录: 目标目录如果不存在则创建，避免复制目录失败，目录 [{}]", destinationDir);
        result = ensureDirInMaster(ssh, destinationDir);
        if (!result.isSuccess()) {
            return result;
        }

        // [5] 把文件从 Master Cache 复制到目标文件夹下 (因为 Salt Cache 中的文件是全局只读，如果使用移动的话 ssh user 有可能没有操作权限)
        log.info("复制目录: 复制 Master Cache 中的目录到目标目录，cacheDir [{}], destDir [{}]", cacheDir, destinationDir);
        result = ssh.executeCommand(String.format("cp -R %s %s", cacheDir, destinationDir));

        return result;
    }

    /**
     * 确保目录路径以 "/" 结尾
     *
     * @param dir 目录路径
     * @return 返回以 "/" 结尾的路径
     */
    public static String ensureDirPattern(String dir) {
        return dir.endsWith("/") ? dir : dir + "/";
    }

    /**
     * 在 SaltMaster 上创建目录
     *
     * @param ssh 服务到 SaltMaster 的 ssh 连接
     * @param dir 要创建的目录
     * @return 返回执行结果
     */
    public SshResult ensureDirInMaster(SshHelper ssh, String dir) throws Exception {
        SshResult result = ssh.executeCommand(replace(MKDIR_WHEN_NON_EXIST, of("dir", dir)));

        if (!result.isSuccess()) {
            log.warn("在 SaltMaster 创建目录失败: {}", result.getContent());
        }

        return result;
    }

    /**
     * 处理 Salt 命令执行的结果。
     *
     * Salt 使用 JSON 格式输出命令结果，成功时是有效的 JSON 格式，失败时除了 JSON 部分还有其他内容。
     * 命令执行成功后自动化需要把结果提取出来进行业务处理，所以把命令的结果 (JSON 中 IP 属性对应的值) 提取出来放到 SshResult.content，
     * 命令执行失败时不进行任何处理，只需要原样保存到日志系统。
     *
     * 成功:
     * {
     *     "192.168.12.102": "Fri Jun 24 08:07:56 CST 2022"
     * }
     *
     * 结果: "Fri Jun 24 08:07:56 CST 2022" 会被提取出来作为 SshResult.content (不包括双引号)。
     *
     * 失败:
     * {
     *     "192.168.12.102": "ls: cannot access /root/foo: No such file or directory"
     * }
     * ERROR: Minions returned with non-zero exit code
     *
     * @param minionIp SaltMinion IP
     * @param result 执行结果
     */
    private static void handleSaltResult(String minionIp, SshResult result) {
        result.setType(SshResultType.SALT_STACK);

        // 命令执行成功，提取命令结果
        if (result.isSuccess()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode node = objectMapper.readTree(result.getContent());
                String directResult = node.get(minionIp).toString();

                if (directResult != null) {
                    // 去掉前后的双引号，把字符 \n 换为回车
                    directResult = directResult.replaceAll("^\"|\"$", "");
                    directResult = directResult.replace("\\n", "\n");
                    result.setContent(directResult);
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 处理 Salt 复制文件的结果 (即使命令执行成功，但是复制操作可能是失败的)。
     *
     * @param result 已经调用 handleSaltResult 处理过的结果，在文件复制时进一步处理结果
     */
    private static void handleSaltResultForCopy(SshResult result) {
        /*
         A. 复制文件:
         成功:
         {
             "192.168.12.102": true
         }
         失败: 源文件不存在、文件大小超过了 file_recv_max_size
         {
             "192.168.12.102": false
         }

         B. 复制目录:
         成功:
         {
             "192.168.12.102": [
                 "/dmp/files/foo/bar/non/x.sh",
                 "/dmp/files/foo/placeholder.txt"
             ]
         }
         失败: 源目录不存在
         {
             "192.168.12.102": []
         }
         */

        String copyResult = result.getContent();

        if ("false".equals(copyResult)) {
            result.setCode(ERROR_FILE_NOT_EXIST);
            result.setContent("源文件不存在，或者大小超过了 SaltMaster 中配置的 file_recv_max_size");
        } else if ("[]".equals(copyResult)) {
            result.setCode(ERROR_DIR_NOT_EXIST);
            result.setContent("源目录不存在或者源目录下没有文件，或者大小超过了 SaltMaster 中配置的 file_recv_max_size");
        }
    }

    /**
     * 本地文件系统路径转为 Salt 的文件系统路径。
     *
     * 示例:
     * /srv/salt/base/scripts-temp/2022-06-23/x.sh-13217088053930812980.sh -> salt://scripts-temp/2022-06-23/x.sh-13217088053930812980.sh
     *
     * @param saltFileSystemBase SaltMaster 文件系统根路径
     * @param absolutePath 本地文件系统的绝对路径
     * @return 返回 Salt 的文件系统路径，如果路径 absolutePath 不在 saltFileSystemBase 指定的目录下则返回 null
     */
    public static String convertPathToSaltFileSystemPath(String saltFileSystemBase, String absolutePath) {
        if (!absolutePath.startsWith(saltFileSystemBase)) {
            return null;
        }

        return "salt://" + absolutePath.substring(saltFileSystemBase.length() + 1);
    }

    /**
     * 使用 ssh 连接到 SaltMaster。
     *
     * @return 返回 ssh 连接
     * @throws Exception 连接异常
     */
    private SshHelper connectToSaltMaster() throws Exception {
        return new SshHelper(config.getMasterIp(), config.getMasterSshUsername(), config.getMasterSshPassword(), config.getMasterSshPort());
    }

    /**
     * Shell 脚本类，计算出脚本相关的各种路径:
     * A. SaltMaster 上保存文件的目录为 /srv/salt/base
     * B. SaltMaster 上临时脚本的路径为 /srv/salt/base/scripts-temp/{yyyy-MM-dd}/{script-name.sh}
     * C. 临时脚本的 Salt 文件系统路径为 salt://scripts-temp/{yyyy-MM-dd}/{script-name.sh}
     *
     * 脚本涉及到 3 个系统:
     * A. Web 服务器所在系统
     * B. SaltMaster 所在系统
     * C. SaltMinion 所在系统
     */
    public static class Script {
        /**
         * 脚本的本地临时文件路径 (Web 服务器所在系统)。
         */
        public String localTempPath;

        /**
         * 脚本在 SaltMaster 上的绝对路径。
         */
        public String masterEncryptedScriptPath;

        /**
         * 脚本在 SaltMaster 上的临时目录的绝对路径。
         */
        public String masterTempScriptDir;

        /**
         * 脚本在 SaltMaster 上的临时文件的绝对路径。
         */
        public String masterTempScriptPath;

        /**
         * 脚本在 SaltMinion 上的绝对路径。
         */
        public String minionTempScriptPath;

        /**
         * 脚本在 SaltMinion 上的目录绝对路径。
         */
        public String minionTempScriptDir;

        /**
         * 使用 SaltMaster 的文件存储位置和脚本名创建脚本对象。
         *
         * @param saltFileSystemBase SaltMaster 的文件存储位置
         * @param scriptFileName 脚本名
         * @throws IOException 创建临时文件错误时抛出 IOException 异常
         */
        public Script(String saltFileSystemBase, String scriptFileName) throws IOException {
            // 创建脚本的临时文件，系统会自动回收
            Path tempFile = Files.createTempFile(scriptFileName + "-", ".sh");
            String tempFileName = tempFile.getFileName().toString();
            String date = today();

            localTempPath             = tempFile.toString();
            masterEncryptedScriptPath = String.format("%s/scripts-encrypted/%s", saltFileSystemBase, scriptFileName);
            masterTempScriptDir       = String.format("%s/scripts-temp/%s", saltFileSystemBase, date);
            masterTempScriptPath      = String.format("%s/%s", masterTempScriptDir, tempFileName);
            minionTempScriptPath      = String.format("/dmp/scripts-temp/%s/%s", date, tempFileName);
            minionTempScriptDir       = String.format("/dmp/scripts-temp/%s", date);
        }

        private static final SimpleDateFormat TODAY_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

        /**
         * 获取今天的日期，格式为 yyyy-MM-dd
         *
         * @return 返回日期的字符串
         */
        public static String today() {
            return TODAY_FORMATTER.format(new Date());
        }
    }
}
