package misc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trilead.ssh2.Connection;
import lombok.extern.slf4j.Slf4j;
import util.SshUtils;
import util.SshUtils.SshResult;
import util.SshUtils.SshResultType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static util.SshUtils.runCommand;

/**
 * SaltStack 执行 shell 脚本和传输文件。
 *
 * 最重要的方法有:
 * A. executeScript: 执行脚本
 * B. transferFileFromMasterToMinion: 从 SaltMaster 传输文件到 SaltMinion
 * C. transferDirFromMasterToMinion: 从 SaltMaster 传输目录到 SaltMinion
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
public class SaltStackRun {
    /**
     * SaltMaster 默认的文件系统根路径。
     */
    private static final String DEFAULT_SALT_FILE_SYSTEM_BASE = "/srv/salt/base";

    /**
     * Minion FileSystem 的文件根路径。
     * Minion 复制到 Master 文件的路径如 /var/cache/salt/master/192.168.12.102/root/test.txt。
     */
    private static final String MINION_FILE_SYSTEM_BASE = "/var/cache/salt/master";

    /**
     * SaltStack 文件系统的根路径，脚本文件和介质文件都放在这个路径下，建议使用默认值。
     */
    private String saltFileSystemBase = DEFAULT_SALT_FILE_SYSTEM_BASE;

    /**
     * Salt 执行 shell 脚本的命令模板。
     * 例如: salt '192.168.12.102' cmd.run 'sh /srv/salt/base/scripts-temp/x.sh-16702780281337228849.sh' --out=json
     */
    private static final String SALT_CMD_RUN_FORMAT = "salt '%s' cmd.run '%s' --out=json";

    /**
     * SaltMaster 传输文件到 SaltMinion 的命令模板，使用 salt:// 协议。
     * 例如: salt '192.168.12.102' cp.get_file 'salt://a.txt' '/root/foo/bar/' makedirs=True --out=json
     */
    private static final String SALT_TRANSFER_FILE_FORMAT = "salt '%s' cp.get_file '%s' '%s' makedirs=True --out=json";

    /**
     * SaltMaster 传输目录到 SaltMinion 的命令模板，使用 salt:// 协议。
     * 例如: salt '192.168.12.102' cp.get_dir 'salt://scripts-encrypted' '/root/foo/bar' makedirs=True --out=json
     */
    private static final String SALT_TRANSFER_DIR_FORMAT = "salt '%s' cp.get_dir '%s' '%s' makedirs=True --out=json";

    /**
     * 使用 SaltStack 在 SaltMinion 上执行脚本。
     *
     * @param minionIp 执行脚本机器的 IP
     * @param scriptName 脚本名称
     * @param args 脚本参数
     * @return 返回执行结果
     */
    public SshResult executeScript(String minionIp, String scriptName, String args) throws IOException {
        /*
         逻辑:
         1. Web 服务器从 SaltMaster 获取 shell 脚本
         2. 解密 shell 脚本，并保存到本地临时文件
         3. 复制临时 shell 脚本到 SaltMaster
         4. 复制 SaltMaster 上的 shell 脚本到 SaltMinion
         5. 执行 SaltMinion 上的脚本
         */

        // 建立到 SaltMaster 的 ssh 连接
        Connection conn = connectToSaltMaster();
        Script script = new Script(saltFileSystemBase, scriptName);

        try {
            // [1] Web 服务器从 SaltMaster 获取 shell 脚本
            log.info("获取脚本: {}", script.masterEncryptedScriptPath);

            String cmd = "cat " + script.masterEncryptedScriptPath;
            SshResult result = runCommand(conn, cmd);
            if (!result.isSuccess()) {
                return result;
            }

            String encryptedScriptContent = result.getContent();

            // [2] 解密 shell 脚本，并保存到本地临时文件
            String scriptContent = encryptedScriptContent; // TODO: 解密 shell 脚本
            log.info("脚本内容:\n{}", scriptContent);

            // 保存到本地临时文件，文件名格式为 <scriptName>-<random>.sh，例如 init.sh-1058843542831845790.sh
            Files.write(Paths.get(script.localTempPath), scriptContent.getBytes(StandardCharsets.UTF_8));

            // [3] 复制临时 shell 脚本到 SaltMaster
            SshUtils.scpTo(conn, script.localTempPath, script.masterTempScriptDir);

            // [4] 复制 SaltMaster 上的 shell 脚本到 SaltMinion
            result = transferFileFromMasterToMinion(conn, minionIp, script.masterTempScriptPath, script.minionTempScriptDir);
            if (!result.isSuccess()) {
                return result;
            }

            // [5] 执行 SaltMinion 上的脚本
            cmd = String.format("sh %s %s", script.minionTempScriptPath, Objects.toString(args, "")); // 执行 shell 脚本: sh x.sh [args]
            result = saltCmdRun(conn, minionIp, cmd);

            return result;
        } finally {
            // 关闭 ssh 连接
            conn.close();

            // TODO: 清理临时文件 (可配置是否清理，调试时不清理)
        }
    }

    /**
     * 使用 SaltStack cmd.run 执行命令。
     *
     * @param conn ssh 连接
     * @param minionIp 要执行命令的 Minion 的 IP
     * @param cmd 要执行的命令 (注意: cmd 中最好不要有但引号，如果有必须转义为 \' 且要成对出现)
     * @return 返回执行结果
     * @throws IOException 执行可能抛出 IOException
     */
    public SshResult saltCmdRun(Connection conn, String minionIp, String cmd) throws IOException {
        // salt '192.168.12.102' cmd.run 'sh x.sh [args]' --out=json
        String saltCmd = String.format(SALT_CMD_RUN_FORMAT, minionIp, cmd);
        SshResult result = runCommand(conn, saltCmd);
        handleSaltResult(minionIp, result);

        return result;
    }

    /**
     * 从 SaltMaster 传输文件到 SaltMinion，目标文件夹不存在会自动创建。
     *
     * @param conn 服务到 SaltMaster 的 ssh 连接
     * @param minionIp SaltMinion 的 IP
     * @param sourcePath SaltMaster 上的源文件
     * @param destinationDir 文件保存到 SaltMinion 上的 destinationDir 目录下
     * @return 返回执行结果
     */
    public SshResult transferFileFromMasterToMinion(Connection conn, String minionIp, String sourcePath, String destinationDir) throws IOException {
        /*
         逻辑:
         1. 把操作系统文件路径转为 Salt 文件系统的路径，以 salt:// 开头，如 salt://foo/bar.txt
         2. 确保 destinationDir 以 / 结尾，否则不会作为目录处理
         3. 构造 Salt 传输文件命令，并执行
         4. 处理 Salt 命令结果

         Salt 传输文件示例: salt '192.168.12.102' cp.get_file 'salt://scripts-temp/x.sh' '/root/foo/bar/' makedirs=True --out=json
         传输后得到 /root/foo/bar/x.sh
         */

        // [1] 把操作系统文件路径转为 Salt 文件系统的路径，以 salt:// 开头，如 salt://a.txt
        String sourcePathInSaltFileSystem = SaltStackRun.convertPathToSaltFileSystemPath(saltFileSystemBase, sourcePath);
        if (sourcePathInSaltFileSystem == null) {
            return new SshResult(10000, "文件不在 Salt 文件系统下: " + sourcePath);
        }

        // [2] 确保 destinationDir 以 / 结尾，否则不会作为目录处理
        if (!destinationDir.endsWith("/")) {
            destinationDir += "/";
        }

        // [3] 构造 Salt 传输文件命令，并执行
        // [4] 处理 Salt 命令结果
        log.info("传输文件: Minion [{}], Src [{}], DestDir [{}]", minionIp, sourcePathInSaltFileSystem, destinationDir);
        String cmd = String.format(SALT_TRANSFER_FILE_FORMAT, minionIp, sourcePathInSaltFileSystem, destinationDir);
        SshResult result = runCommand(conn, cmd);
        handleSaltResult(minionIp, result);

        if (!result.isSuccess()) {
            log.warn("传输文件失败: Minion [{}], Src [{}], DestDir [{}]", minionIp, sourcePathInSaltFileSystem, destinationDir);
        }

        return result;
    }

    /**
     * transferFileFromMasterToMinion 的重载函数，自动创建到 SaltMaster 的连接。
     */
    public SshResult transferFileFromMasterToMinion(String minionIp, String sourcePath, String destinationDir) throws IOException {
        Connection conn = connectToSaltMaster();

        try {
            return transferFileFromMasterToMinion(conn, minionIp, sourcePath, destinationDir);
        } finally {
            conn.close();
        }
    }

    /**
     * SaltMaster 传输目录到 SaltMinion，目标文件夹不存在会自动创建，会把目录下的文件以及子目录等都传输过去 (递归)。
     *
     * @param conn 服务到 SaltMaster 的 ssh 连接
     * @param minionIp SaltMinion 的 IP
     * @param sourceDir SaltMaster 上的源目录
     * @param destinationDir 源目录保存到 SaltMinion 上的 destinationDir 目录下
     * @return 返回执行结果
     * @throws IOException 传输文件如网络错误等时抛出 IOException 异常
     */
    public SshResult transferDirFromMasterToMinion(Connection conn, String minionIp, String sourceDir, String destinationDir) throws IOException {
        /*
         逻辑:
         1. 把操作系统文件路径转为 Salt 文件系统的路径，以 salt:// 开头，如 salt://a.txt
         2. 构造 Salt 传输文件命令，并执行
         3. 处理 Salt 命令结果

         Salt 传输目录示例: salt '192.168.12.102' cp.get_dir 'salt://scripts-encrypted' '/root/foo/bar' makedirs=True --out=json
         传输后得到 /root/foo/bar/scripts-encrypted
         */

        // [1] 把操作系统文件路径转为 Salt 文件系统的路径，以 salt:// 开头，如 salt://a.txt
        String sourceDirInSaltFileSystem = SaltStackRun.convertPathToSaltFileSystemPath(saltFileSystemBase, sourceDir);
        if (sourceDirInSaltFileSystem == null) {
            return new SshResult(10001, "目录不在 Salt 文件系统下: " + sourceDir);
        }

        // [2] 构造 Salt 传输文件命令，并执行
        // [3] 处理 Salt 命令结果
        log.info("传输目录: Minion [{}], SrcDir [{}], DestDir [{}]", minionIp, sourceDirInSaltFileSystem, destinationDir);
        String cmd = String.format(SALT_TRANSFER_DIR_FORMAT, minionIp, sourceDirInSaltFileSystem, destinationDir);
        SshResult result = runCommand(conn, cmd);
        handleSaltResult(minionIp, result);

        if (!result.isSuccess()) {
            log.warn("传输目录失败: Minion [{}], SrcDir [{}], DestDir [{}]", minionIp, sourceDirInSaltFileSystem, destinationDir);
        }

        return result;
    }

    /**
     * transferDirFromMasterToMinion 的重载方法。
     */
    public SshResult transferDirFromMasterToMinion(String minionIp, String sourceDir, String destinationParentDir) throws IOException {
        Connection conn = connectToSaltMaster();

        try {
            return transferDirFromMasterToMinion(conn, minionIp, sourceDir, destinationParentDir);
        } finally {
            conn.close();
        }
    }

    /**
     * 把文件从一个 SaltMinion fromMinion 传输到另一个 SaltMinion toMinion。
     * 提示: 需要 SaltMaster 开启 Minion FS。
     *
     * @param conn 服务到 SaltMaster 的 ssh 连接
     * @param sourceMinionIp 文件所在的 SaltMinion IP
     * @param destinationMinionIp 文件传输到的 SaltMinion IP
     * @param sourcePath 源文件路径
     * @param destinationPath 目标文件路径
     * @return 返回执行结果
     * @throws IOException 传输文件如网络错误等时抛出 IOException 异常
     */
    public SshResult transferFileFromMinionToMinion(Connection conn, String sourceMinionIp, String destinationMinionIp, String sourcePath, String destinationPath) throws IOException {
        throw new UnsupportedOperationException(); // TODO: 以后做
    }

    public SshResult transferFileFromMinionToMinion(String sourceMinionIp, String destinationMinionIp, String sourcePath, String destinationPath) throws IOException {
        Connection conn = connectToSaltMaster();

        try {
            return transferFileFromMinionToMinion(conn, sourceMinionIp, destinationMinionIp, sourcePath, destinationPath);
        } finally {
            conn.close();
        }
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
     * 失败:
     * {
     *     "192.168.12.102": "ls: cannot access /root/foo: No such file or directory"
     * }
     * ERROR: Minions returned with non-zero exit code
     *
     * @param minionIp SaltMinion IP
     * @param result 执行结果
     */
    private void handleSaltResult(String minionIp, SshResult result) {
        result.setType(SshResultType.SALT_STACK);

        // 命令执行成功，提取命令结果
        if (result.isSuccess()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode node = objectMapper.readTree(result.getContent());
                String directResult = node.get(minionIp).toString();
                result.setContent(directResult);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
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
     * @throws IOException 连接异常
     */
    public Connection connectToSaltMaster() throws IOException {
        // TODO: 根据情况修改
        return SshUtils.sshConnect("192.168.1.164", 22, "root", "Newdt@cn");
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

            localTempPath = tempFile.toString();
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
