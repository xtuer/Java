package misc.auto.ansible;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import misc.SshHelper;
import misc.SshHelper.SshResult;
import misc.auto.AutoConst;
import misc.auto.AutoScript;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static com.google.common.collect.ImmutableMap.of;
import static org.apache.commons.text.StringSubstitutor.replace;

/**
 * Ansible 执行 shell 脚本、复制文件和目录。
 *
 * 术语:
 * - 主机分 2 种类型，Ansible 和 Node。使用 Ansible 操作的其他主机都叫 Node。
 */
@Slf4j
public class AnsibleRunner {
    private final AnsibleConfig config;

    /**
     * Ansible 临时用户信息的 inventory。
     * 由于是 Web 服务，每个请求都是在自己独立的线程里运行，使用 ThreadLocal 传递 inventory 比较方便，也不会导致数据错乱。
     */
    public static final ThreadLocal<String> INVENTORY = new ThreadLocal<>();

    /**
     * 使用配置创建 Ansible 复制文件工具对象。
     *
     * @param config 自动化使用的 Ansible 配置
     */
    public AnsibleRunner(AnsibleConfig config) {
        this.config = config;

        if (log.isDebugEnabled()) {
            log.debug("Ansible 配置: {}", config);
        }
    }

    /**
     * 使用 Ansible 在目标主机 nodeIp 上执行脚本。
     * 脚本的多个参数拼成一个字符串，参数以键值对的形式出现，每队参数以 - 开头，例如 "-username=Alice -password=P@ssw0rd" (不包含引号)。
     * 脚本的执行命令如 "/root/foo.sh -username=Alice -password=P@ssw0rd" (不包含引号)。
     *
     * @param nodeIp 执行脚本主机的 IP
     * @param scriptName 脚本名称
     * @param args 脚本参数
     * @return 返回执行结果
     */
    public SshResult executeScript(String nodeIp, String scriptName, String args) throws Exception {
        /*
         逻辑:
         1. 创建临时认证的 inventory
         2. 构建脚本的各种路径信息
         3. Web 服务器从 Ansible 获取 shell 脚本
         4. 解密 shell 脚本，并保存到本地临时文件
         5. 复制本地临时 shell 脚本到 Ansible
         6. 复制 Ansible 上的 shell 脚本到 Node
         7. 给脚本增加可执行权限
         8. 从 Ansible 执行 Node 上的脚本
         */

        log.info("执行脚本: NodeIP [{}], ScriptName: [{}], Args: [{}]", nodeIp, scriptName, args);

        try (SshHelper ssh = connectToAnsible()) {
            // [1] 创建临时认证的 inventory
            buildInventory();

            // [2] 构建脚本的各种路径信息
            String nodeHome = AnsibleRunnerHelper.getHome(ssh, nodeIp);
            AutoScript script = new AutoScript(config.getAnsibleAutoBase(), nodeHome, scriptName);

            // [3] Web 服务器从 Ansible 获取 shell 脚本
            log.info("获取脚本: {}", script.encryptedScriptPathInEngine);

            String cmd = "cat " + script.encryptedScriptPathInEngine;
            SshResult result = ssh.executeCommand(cmd);
            if (!result.isSuccess()) {
                return result;
            }

            String encryptedScriptContent = result.getContent();
            log.debug("加密的脚本:\n{}", encryptedScriptContent);

            // [4] 解密 shell 脚本，并保存到本地临时文件
            String scriptContent = encryptedScriptContent; // TODO: 解密 shell 脚本 (自动化的脚本都是加密保存的，需要解密后才能使用)
            log.debug("解密的脚本:\n{}", scriptContent);

            // 保存到本地临时文件，文件名格式为 <scriptName>-<random>.sh，例如 init.sh-1058843542831845790.sh
            Files.write(Paths.get(script.tempScriptPathInLocal), scriptContent.getBytes(StandardCharsets.UTF_8));

            // [5] 复制本地临时 shell 脚本到 Ansible
            log.info("复制脚本: 脚本解密后复制到 Ansible 的临时目录");
            ssh.sftpPut(script.tempScriptPathInLocal, script.tempScriptDirInEngine);

            // [6] 复制 Ansible 上的 shell 脚本到 Node
            log.info("复制脚本: 从 Ansible 到 Node");
            result = copyFileOrDirFromAnsibleToNode(ssh, nodeIp, script.tempScriptPathInEngine, script.tempScriptDirInTarget + "/");
            if (!result.isSuccess()) {
                return result;
            }

            // [7] 给脚本增加可执行权限
            log.info("脚本增加可执行权限: 脚本 [{}]", script.tempScriptPathInTarget);
            AnsibleRunnerHelper.addExecutablePermissionToFileViaAnsible(ssh, nodeIp, script.tempScriptPathInTarget);

            // [8] 从 Ansible 执行 Node 上的脚本
            log.info("执行脚本: 从 Ansible 上执行 Node 的脚本 [{}]", script.tempScriptPathInTarget);
            result = AnsibleRunnerHelper.executeScriptViaAnsible(ssh, nodeIp, script.tempScriptPathInTarget, Objects.toString(args, ""));
            log.info("脚本结果:\n{}", result.getContent());

            return result;
        }
    }

    /**
     * 在主机之间复制文件。当 sourceIp 为 null 时则表示 source 为 Ansible，当 destinationIp 为 null 时则表示 destination 为 Ansible。
     */
    public SshResult copyFile(String sourceIp, String destinationIp, String sourcePath, String destinationDir) throws Exception {
        try (SshHelper ssh = connectToAnsible()) {
            buildInventory();
            return copy(ssh, sourceIp, destinationIp, sourcePath, destinationDir, true);
        }
    }

    /**
     * 在主机之间复制目录。当 sourceIp 为 null 时则表示 source 为 Ansible，当 destinationIp 为 null 时则表示 destination 为 Ansible。
     */
    public SshResult copyDir(String sourceIp, String destinationIp, String sourcePath, String destinationDir) throws Exception {
        try (SshHelper ssh = connectToAnsible()) {
            buildInventory();
            return copy(ssh, sourceIp, destinationIp, sourcePath, destinationDir, false);
        }
    }

    /**
     * 在主机之间复制文件或者目录。当 sourceIp 为 null 时则表示 source 为 Ansible 所在主机，当 destinationIp 为 null 时则表示 destination 为 Ansible 所在主机。
     *
     * @param ssh 服务到 Ansible 的 ssh 连接
     * @param sourceIp 源文件所在主机 IP
     * @param destinationIp 目标目录所在主机 IP
     * @param sourcePath 源文件或者目录的路径
     * @param destinationDir 目标目录
     * @param isFile 为 true 表示复制文件，false 表示复制目录
     * @return 返回执行结果
     * @throws Exception 执行出问题时可能抛出 Exception
     */
    public SshResult copy(SshHelper ssh, String sourceIp, String destinationIp, String sourcePath, String destinationDir, boolean isFile) throws Exception {
        /*
         逻辑:
         1. 参数校验: 路径不能为空，路径不能包含空格，源和目标 IP 不能同时为空
         2. 路径处理:
            2.1. 把 source 结尾的 / 去掉，避免复制文件夹的时候造成复制文件夹下的内容
            2.2. destinationDir 以 / 结尾，确保 source 复制到 destinationDir 下
         3. 源和目标处理:
            3.1 如果源 IP 为空则表示源在 Ansible 上。
            3.2 如果目标 IP 为空则表示目标在 Ansible 上。
         4. 根据 sourceIp 和 destinationIp 判断复制目录的方向，根据不同的复制方向调用对应的方法进行复制:
            4.1 Ansible 到 Node
            4.2 Node 到 Ansible
            4.3 Node 到 Node
         */

        // 去掉前后的空白字符
        sourceIp       = StringUtils.trimWhitespace(sourceIp);
        destinationIp  = StringUtils.trimWhitespace(destinationIp);
        sourcePath     = StringUtils.trimWhitespace(sourcePath);
        destinationDir = StringUtils.trimWhitespace(destinationDir);

        // [1] 参数校验: 路径不能为空，路径不能包含空格，源和目标 IP 不能同时为空
        Preconditions.checkArgument(StringUtils.hasText(sourcePath), "源路径不能为空");
        Preconditions.checkArgument(StringUtils.hasText(destinationDir), "目标目录不能为空");
        Preconditions.checkArgument(!StringUtils.containsWhitespace(sourcePath), "源路径不能包含空白字符");
        Preconditions.checkArgument(!StringUtils.containsWhitespace(destinationDir), "目标目录不能包含空白字符");
        Preconditions.checkArgument(!StringUtils.isEmpty(sourceIp) || !StringUtils.isEmpty(destinationIp), "源 IP 和目标 IP 不能同时为空");

        // [2] 路径处理
        sourcePath = sourcePath.replaceAll("/+$", "");
        destinationDir = destinationDir.endsWith("/") ? destinationDir : destinationDir + "/";

        // [3] 源和目标处理:
        // [3.1] 如果源 IP 为空则表示源在 Ansible 上。
        // [3.2] 如果目标 IP 为空则表示目标在 Ansible 上。
        String ansibleIp = this.config.getAnsibleIp();
        sourceIp = StringUtils.isEmpty(sourceIp) ? ansibleIp : sourceIp;
        destinationIp = StringUtils.isEmpty(destinationIp) ? ansibleIp : destinationIp;

        // [4] 根据 sourceIp 和 destinationIp 判断复制目录的方向，根据不同的复制方向调用对应的方法进行复制
        if (sourceIp.equals(destinationIp)) {
            // Source -> Source
            throw new RuntimeException("源 IP 和目标 IP 相等，不支持在同一个主机上移动文件或目录");
        } else if (ansibleIp.equals(sourceIp)) {
            // Ansible -> Node
            return copyFileOrDirFromAnsibleToNode(ssh, destinationIp, sourcePath, destinationDir);
        } else if (ansibleIp.equals(destinationIp)) {
            // Node -> Ansible
            return isFile
                    ? copyFileFromNodeToAnsible(ssh, sourceIp, sourcePath, destinationDir)
                    : copyDirFromNodeToAnsible(ssh, sourceIp, sourcePath, destinationDir);
        } else {
            // Node -> Node
            return isFile
                    ? copyFileFromNodeToNode(ssh, sourceIp, destinationIp, sourcePath, destinationDir)
                    : copyDirFromNodeToNode(ssh, sourceIp, destinationIp, sourcePath, destinationDir);
        }
    }

    /**
     * 从 Ansible 复制文件或目录到 Node，目标文件夹不存在会自动创建。
     *
     * @param ssh 服务到 Ansible 的 ssh 连接
     * @param nodeIp Node 的 IP
     * @param sourcePath Ansible 上的源文件或源目录路径
     * @param destinationDir 文件和目录复制到 Node 的 destinationDir 目录下
     * @return 返回操作结果
     */
    private SshResult copyFileOrDirFromAnsibleToNode(SshHelper ssh, String nodeIp, String sourcePath, String destinationDir) throws Exception {
        /*
         逻辑:
         1. 构造 Ansible 复制文件命令，并执行
         2. 处理 Ansible 复制操作结果
         */

        log.info("复制文件或目录: Ansible -> Node, Node [{}], Src [{}], DestDir [{}]", nodeIp, sourcePath, destinationDir);

        // 验证目录格式。
        AnsibleRunnerHelper.checkDir(destinationDir);

        // [1] 构造 Ansible 复制文件命令，并执行
        // [2] 处理 Ansible 复制操作结果
        String cmd = replace(AnsibleRunnerConst.PLAYBOOK_COPY_FILE_OR_DIR_FROM_ANSIBLE_TO_NODE, of(
                "nodeIp", nodeIp,
                "src", sourcePath,
                "destDir", destinationDir,
                "inventory", INVENTORY.get()
        ));
        SshResult result = ssh.executeCommand(cmd);
        AnsibleRunnerHelper.handleAnsibleResultForCopy(result);

        // 复制操作失败时输出错误信息到日志
        if (!result.isSuccess()) {
            log.warn("复制失败: Node [{}], Src [{}], DestDir [{}]", nodeIp, sourcePath, destinationDir);
        }

        return result;
    }

    /**
     * 从 Node 复制文件到 Ansible，目标文件夹不存在会自动创建。
     *
     * @param ssh 服务到 Ansible 的 ssh 连接
     * @param nodeIp Node 的 IP
     * @param sourcePath Node 上的源文件路径
     * @param destinationDir 文件复制到 Ansible 的 destinationDir 目录下
     * @return 返回操作结果
     */
    private SshResult copyFileFromNodeToAnsible(SshHelper ssh, String nodeIp, String sourcePath, String destinationDir) throws Exception {
        /*
         逻辑:
         1. 构造 Ansible 复制文件命令，并执行
         2. 处理 Ansible 复制操作结果
         */

        log.info("复制文件: Node -> Ansible, Node [{}], Src [{}], DestDir [{}]", nodeIp, sourcePath, destinationDir);

        // 验证目录格式。
        AnsibleRunnerHelper.checkDir(destinationDir);

        // [1] 构造 Ansible 复制文件命令，并执行
        // [2] 处理 Ansible 复制操作结果
        String cmd = replace(AnsibleRunnerConst.PLAYBOOK_COPY_FILE_FROM_NODE_TO_ANSIBLE, of(
                "nodeIp", nodeIp,
                "src", sourcePath,
                "destDir", destinationDir,
                "inventory", INVENTORY.get()
        ));
        SshResult result = ssh.executeCommand(cmd);
        AnsibleRunnerHelper.handleAnsibleResultForCopy(result);

        // 复制操作失败时输出错误信息到日志
        if (!result.isSuccess()) {
            log.warn("复制失败: Node [{}], Src [{}], DestDir [{}]", nodeIp, sourcePath, destinationDir);
        }

        return result;
    }

    /**
     * 从 Node 复制目录到 Ansible，目标文件夹不存在会自动创建。
     *
     * @param ssh 服务到 Ansible 的 ssh 连接
     * @param nodeIp Node 的 IP
     * @param sourcePath Node 上的源目录路径
     * @param destinationDir 目录复制到 Ansible 的 destinationDir 目录下
     * @return 返回操作结果
     */
    private SshResult copyDirFromNodeToAnsible(SshHelper ssh, String nodeIp, String sourcePath, String destinationDir) throws Exception {
        /*
         逻辑 (先复制到临时目录再移动是因为要去掉 Ansible 复制结果中的一些干扰路径):
         1. 从 Node 先复制目录到 Ansible 临时目录
         2. 把 Ansible 临时目录下要刚刚复制得到的目录移动到 destinationDir 下
         3. 删除复制产生的临时目录

         说明:
            从 Node 复制目录到 Ansible 临时目录比较特别，被复制目录在 Ansible 中的路径是 ${destinationDir}/${nodeIp}/${sourcePath}。
            例如 nodeIp 为 192.168.12.101，sourcePath 为 /node-src/test-dir，destinationDir 为 /root/ansible-dest，
            那么被复制的目录 test-dir 在 Ansible 的路径为 /root/ansible-dest/192.168.12.101/node-src/test-dir，而不是 /root/ansible-dest/test-dir。
            所以先把目录复制到 Ansible 的临时文件夹下，复制结束后再移动到目标目录。
         */

        log.info("复制目录: Node -> Ansible, Node [{}], Src [{}], DestDir [{}]", nodeIp, sourcePath, destinationDir);

        // 验证目录格式。
        AnsibleRunnerHelper.checkDir(destinationDir);

        // [1] 从 Node 先复制目录到 Ansible 临时目录
        SshResult result = copyDirFromNodeToAnsibleTempDir(ssh, nodeIp, sourcePath);
        if (!result.isSuccess()) {
            return result;
        }

        // [2] 把 Ansible 临时目录下要刚刚复制得到的目录移动到 destinationDir 下
        String tempPath = result.getContent();
        result = AnsibleRunnerHelper.moveFileOrDirInAnsible(ssh, tempPath, destinationDir);

        // [3] 删除复制产生的临时目录
        AnsibleRunnerHelper.deleteCopyGeneratedTempDirectoryInAnsible(ssh, tempPath);

        return result;
    }

    /**
     * 从 sourceNodeIp 复制文件 sourcePath 到 destinationNodeIp 的 destinationDir 目录下。
     *
     * @param ssh ssh 服务到 Ansible 的 ssh 连接
     * @param sourceIp 源文件所在主机 IP
     * @param destinationIp 目标目录所在主机 IP
     * @param sourcePath 源文件路径
     * @param destinationDir 目标目录路径
     * @return 返回操作结果
     */
    private SshResult copyFileFromNodeToNode(SshHelper ssh, String sourceIp, String destinationIp, String sourcePath, String destinationDir) throws Exception {
        /*
         逻辑:
         1. 从 SourceIp 复制文件到 Ansible 临时目录下
         2. 复制 Ansible 临时目录下的文件到 destinationIp 的 destinationDir 下
         3. 删除复制参数的临时目录
         */

        log.info("复制文件: Node -> Node, SourceIp [{}], DestinationIp [{}], SourcePath [{}], DestinationDir [{}]",
                sourceIp, destinationIp, sourcePath, destinationDir);

        // 验证目录格式。
        AnsibleRunnerHelper.checkDir(destinationDir);

        // [1] 从 sourceNodeIp 复制文件到 Ansible 临时目录下
        String ansibleTempDir = AnsibleRunnerHelper.generateTempDirPathInAnsible(ssh) + "/";
        SshResult result = copyFileFromNodeToAnsible(ssh, sourceIp, sourcePath, ansibleTempDir);
        if (!result.isSuccess()) {
            log.warn("文件复制失败: 从 Node 复制文件到 Ansible 临时目录错误, SourceIp [{}],SourcePath [{}], Cause [{}]", sourceIp, destinationIp, result.getContent());
            return result;
        }

        // [2] 复制 Ansible 临时目录下的文件到 destinationNodeIp 的 destinationDir 下
        String sourceFileName = Paths.get(sourcePath).getFileName().toString();
        String sourcePathInAnsibleTempDir = ansibleTempDir + sourceFileName;
        result = copyFileOrDirFromAnsibleToNode(ssh, destinationIp, sourcePathInAnsibleTempDir, destinationDir);
        if (!result.isSuccess()) {
            log.warn("文件复制失败，从 Ansible 临时目录复制文件到 Node，DestinationIp [{}], TempSourcePath [{}], Cause [{}]",
                    destinationIp, sourcePathInAnsibleTempDir, result.getContent());
        }

        // [3] 删除复制参数的临时目录
        AnsibleRunnerHelper.deleteCopyGeneratedTempDirectoryInAnsible(ssh, ansibleTempDir);

        return result;
    }

    /**
     * 从 sourceNodeIp 复制目录 sourcePath 到 destinationNodeIp 的 destinationDir 目录下。
     *
     * @param ssh ssh 服务到 Ansible 的 ssh 连接
     * @param sourceIp 源目录所在主机 IP
     * @param destinationIp 目标目录所在主机 IP
     * @param sourcePath 源文件路径
     * @param destinationDir 目标目录路径
     * @return 返回操作结果
     */
    private SshResult copyDirFromNodeToNode(SshHelper ssh, String sourceIp, String destinationIp, String sourcePath, String destinationDir) throws Exception {
        /*
         逻辑:
         1. 从 SourceIp 复制目录到 Ansible 临时目录下
         2. 复制 Ansible 临时目录下的目录到 destinationIp 的 destinationDir 下
         3. 删除复制参数的临时目录
         */

        log.info("复制目录: Node -> Node, SourceIp [{}], DestinationIp [{}], SourcePath [{}], DestinationDir [{}]",
                sourceIp, destinationIp, sourcePath, destinationDir);

        // 验证目录格式。
        AnsibleRunnerHelper.checkDir(destinationDir);

        // [1] 从 SourceIp 复制目录到 Ansible 临时目录下
        SshResult result = copyDirFromNodeToAnsibleTempDir(ssh, sourceIp, sourcePath);
        if (!result.isSuccess()) {
            log.warn("目录复制失败: 从 Node 复制文件到 Ansible 临时目录错误, SourceIp [{}],SourcePath [{}], Cause [{}]", sourceIp, destinationIp, result.getContent());
            return result;
        }

        // [2] 复制 Ansible 临时目录下的目录到 destinationIp 的 destinationDir 下
        String sourceTempPath = result.getContent(); // 成功时 SshResult.content 为被复制目录在 Ansible 中的临时目录下的路径
        result = copyFileOrDirFromAnsibleToNode(ssh, destinationIp, sourceTempPath, destinationDir);

        // [3] 删除复制参数的临时目录
        AnsibleRunnerHelper.deleteCopyGeneratedTempDirectoryInAnsible(ssh, sourceTempPath);

        return result;
    }

    /**
     * 从 Node 复制目录到 Ansible 用户下的临时目录里。
     *
     * @param ssh 服务到 Ansible 的 ssh 连接
     * @param nodeIp Node 的 IP
     * @param sourcePath Node 上的源目录路径
     * @return 返回操作结果，成功时 SshResult.content 为被复制目录在 Ansible 中的临时目录下的路径。
     */
    private SshResult copyDirFromNodeToAnsibleTempDir(SshHelper ssh, String nodeIp, String sourcePath) throws Exception {
        /*
         逻辑:
         1. 生成 Ansible 主机上的临时目录路径
         2. 构建 Ansible 复制目录命令，并执行
         3. 返回被复制目录的临时路径，其模式为 ${ansibleTempDir}/${nodeIp}/${sourcePath}，
            例如 /root/shindata-temp-auto/1665737329979/192.168.12.101/root/test-dir
         4. 因为源目录不存在时 playbook 复制不报错，需要检查 Ansible 上文件是否存在，不存在则报错
         */

        // [1] 生成 Ansible 主机上的临时目录路径
        String tempDestinationDir = AnsibleRunnerHelper.generateTempDirPathInAnsible(ssh);

        // [2] 构建 Ansible 复制目录命令，并执行
        String cmd = replace(AnsibleRunnerConst.PLAYBOOK_COPY_DIR_FROM_NODE_TO_ANSIBLE, of(
                "nodeIp", nodeIp,
                "src", sourcePath,
                "destDir", tempDestinationDir,
                "inventory", INVENTORY.get()
        ));
        SshResult result = ssh.executeCommand(cmd);
        AnsibleRunnerHelper.handleAnsibleResultForCopy(result);

        // Ansible 复制操作失败时输出错误信息到日志并返回
        if (!result.isSuccess()) {
            log.warn("复制失败: Node [{}], Src [{}], DestDir [{}]\nCause [{}]", nodeIp, sourcePath, tempDestinationDir, result.getContent());
            return result;
        }

        // [3] 返回被复制目录的临时路径，其模式为 ${ansibleTempDir}/${nodeIp}/${sourcePath}，
        //     例如 /root/shindata-temp-auto/1665737329979/192.168.12.101/root/test-dir
        String finalDir = replace(AnsibleRunnerConst.TEMP_PATH_OF_COPIED_DIR_FROM_NODE_TO_ANSIBLE, of(
                "ansibleTempDir", tempDestinationDir,
                "nodeIp", nodeIp,
                "srcPath", sourcePath
        ));

        // [4] 因为源目录不存在时 playbook 复制不报错，需要检查 Ansible 上文件是否存在，不存在则报错
        if (!AnsibleRunnerHelper.fileOrDirExists(ssh, null, finalDir)) {
            log.warn("源目录不存在: NodeIp [{}], SourcePath [{}]", nodeIp, sourcePath);
            return new SshResult(AnsibleRunnerConst.ERROR_FILE_NOT_FOUND, String.format("源目录不存在: NodeIp [%s], SourcePath [%s]", nodeIp, sourcePath));
        }

        return new SshResult(0, finalDir);
    }

    /**
     * 使用 ssh 连接到 Ansible。
     *
     * @return 返回 ssh 连接
     * @throws Exception 连接异常
     */
    public SshHelper connectToAnsible() throws Exception {
        return new SshHelper(config.getAnsibleIp(), config.getAnsibleSshUsername(), config.getAnsibleSshPassword(), config.getAnsibleSshPort());
    }

    /**
     * 构建 Ansible 用户信息的 inventory.
     */
    public void buildInventory() throws Exception {
        AnsibleRunner.INVENTORY.set("");

        /*
        逻辑:
        1. 生成 inventory item。
        2. 把 inventory item 内容写入本地临时文件。
        3. 把本地临时 inventory 文件复制到 Ansible 上自动化使用的 inventory 目录下。
        */

        // Inventory 文件内容。
        StringBuilder inventory = new StringBuilder();

        // [1] 生成 inventory item。
        String inv1 = replace(AnsibleRunnerConst.INVENTORY_ITEM, of(
                "ip", "192.168.12.101",
                "port", 22,
                "user", "root",
                "password", "Newdt@cn"
        ));
        String inv2 = replace(AnsibleRunnerConst.INVENTORY_ITEM_SUDO, of(
                "ip", "192.168.12.101",
                "port", 22,
                "user", "root",
                "password", "Newdt@cn",
                "becomeUser", "root",
                "becomePassword", "Passw0rd"
        ));

        inventory.append(inv1).append("\n");
        inventory.append(inv2).append("\n");

        // [2] 把 inventory item 内容写入本地临时文件。
        Path tempInventory = Files.createTempFile("inventory-", ".inv");
        Files.write(tempInventory, inventory.toString().getBytes(StandardCharsets.UTF_8));

        // [3] 把本地临时 inventory 文件复制到 Ansible 上自动化使用的 inventory 目录下。
        try (SshHelper ssh = connectToAnsible()) {
            String ansibleInventoryDir = config.getAnsibleAutoBase() + "/" + AutoConst.DIR_INVENTORY;
            log.info("生成 Inventory 文件并复制到 Ansible: LocalPath [{}], AnsibleInventoryDir [{}]", tempInventory, ansibleInventoryDir);
            ssh.sftpPut(tempInventory.toString(), ansibleInventoryDir);

            String inventoryPath = ansibleInventoryDir + "/" + tempInventory.getFileName().toString();
            AnsibleRunner.INVENTORY.set("-i " + inventoryPath);
            log.info("Inventory: " + AnsibleRunner.INVENTORY.get());
        }
    }
}
