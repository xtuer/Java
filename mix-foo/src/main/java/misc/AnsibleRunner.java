package misc;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import misc.SshHelper.SshResult;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import java.nio.file.Paths;

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
     * 在主机之间复制文件。当 sourceIp 为 null 时则表示 source 为 Ansible，当 destinationIp 为 null 时则表示 destination 为 Ansible。
     */
    public SshResult copyFile(@Nullable String sourceIp, String destinationIp, String sourcePath, String destinationDir) throws Exception {
        try (SshHelper ssh = connectToAnsible()) {
            buildInventory();
            return copy(ssh, sourceIp, destinationIp, sourcePath, destinationDir, true);
        }
    }

    /**
     * 在主机之间复制目录。当 sourceIp 为 null 时则表示 source 为 Ansible，当 destinationIp 为 null 时则表示 destination 为 Ansible。
     */
    public SshResult copyDir(@Nullable String sourceIp, String destinationIp, String sourcePath, String destinationDir) throws Exception {
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

        // [1] 从 Node 先复制目录到 Ansible 临时目录
        SshResult result = copyDirFromNodeToAnsibleTempDir(ssh, nodeIp, sourcePath);
        if (!result.isSuccess()) {
            return result;
        }

        // [2] 把 Ansible 临时目录下要刚刚复制得到的目录移动到 destinationDir 下
        String tempPath = result.getContent();
        result = AnsibleRunnerHelper.moveFileOrDir(ssh, tempPath, destinationDir);

        // [3] 删除复制产生的临时目录
        AnsibleRunnerHelper.deleteCopyTaskTempDirectory(ssh, tempPath);

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
        AnsibleRunnerHelper.deleteCopyTaskTempDirectory(ssh, ansibleTempDir);

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

        // [1] 从 SourceIp 复制目录到 Ansible 临时目录下
        String ansibleTempDir = AnsibleRunnerHelper.generateTempDirPathInAnsible(ssh) + "/";
        SshResult result = copyDirFromNodeToAnsibleTempDir(ssh, sourceIp, sourcePath);
        if (!result.isSuccess()) {
            log.warn("目录复制失败: 从 Node 复制文件到 Ansible 临时目录错误, SourceIp [{}],SourcePath [{}], Cause [{}]", sourceIp, destinationIp, result.getContent());
            return result;
        }

        // [2] 复制 Ansible 临时目录下的目录到 destinationIp 的 destinationDir 下
        String sourceTempPath = result.getContent();
        result = copyFileOrDirFromAnsibleToNode(ssh, destinationIp, sourceTempPath, destinationDir);

        // [3] 删除复制参数的临时目录
        AnsibleRunnerHelper.deleteCopyTaskTempDirectory(ssh, sourceTempPath);

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

        return new SshResult(0, finalDir);
    }

    /**
     * 使用 ssh 连接到 Ansible。
     *
     * @return 返回 ssh 连接
     * @throws Exception 连接异常
     */
    private SshHelper connectToAnsible() throws Exception {
        return new SshHelper(config.getAnsibleIp(), config.getAnsibleSshUsername(), config.getAnsibleSshPassword(), config.getAnsibleSshPort());
    }

    /**
     * 构建 Ansible 用户信息的 inventory.
     */
    private void buildInventory() {
        AnsibleRunner.INVENTORY.set("");
    }
}
