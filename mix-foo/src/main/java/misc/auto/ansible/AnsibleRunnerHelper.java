package misc.auto.ansible;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import misc.SshHelper;
import misc.SshHelper.SshResult;
import org.springframework.util.StringUtils;

import java.nio.file.Paths;
import java.util.regex.Matcher;

import static com.google.common.collect.ImmutableMap.of;
import static org.apache.commons.text.StringSubstitutor.replace;

/**
 * Ansible 服务的工具类。
 */
@Slf4j
public class AnsibleRunnerHelper {
    /**
     * 处理 Ansible 复制操作的结果。
     * 即使命令执行成功，但是操作的业务可能是失败的，那么操作结果也要作为失败处理。
     *
     * @param result Ansible 复制命令
     */
    public static void handleAnsibleResultForCopy(SshResult result) {
        // 命令失败不用继续处理。
        if (!result.isSuccess()) {
            return;
        }

        // 命令执行成功，但是业务逻辑错误时处理。
        String copyOutput = result.getContent();

        if (!AnsibleRunnerHelper.judgeCopySuccess(copyOutput)) {
            result.setCode(AnsibleRunnerConst.ERROR_COPY_ACTION);
        }
    }

    /**
     * 判断复制操作的结果是成功还是失败。
     *
     * @param copyOutput Ansible 复制操作的输出日志
     * @return 复制操作成功则返回 true，否则返回 false
     */
    public static boolean judgeCopySuccess(String copyOutput) {
        /*
         复制操作的输出样例:
         ...
         TASK [copy file or directory located at remote into Ansible Host] ************************************
         changed: [192.168.12.101]

         PLAY RECAP *******************************************************************************************
         192.168.12.101 : ok=2    changed=1    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0

         说明:
         - PLAY RECAP 的下一行表示操作成功失败的信息。
         - 使用 copy  模块从 Ansible 复制文件或者目录到 Node，ok=2 时操作成功。
         - 使用 fetch 模块从 Node 复制文件到 Ansible，ok=2 时操作成功。
         - 使用 fetch 模块从 Node 复制目录到 Ansible，ok=3 时操作成功。
         */
        String ok = null;

        Matcher matcher = AnsibleRunnerConst.PLAYBOOK_COPY_RESULT_PATTERN.matcher(copyOutput);

        if (matcher.find()) {
            // 获取 ok 的值
            ok = matcher.group(1);
        }

        return AnsibleRunnerConst.PLAYBOOK_COPY_SUCCESS_CODE_2.equals(ok) || AnsibleRunnerConst.PLAYBOOK_COPY_SUCCESS_CODE_3.equals(ok);
    }

    /**
     * 在 Ansible 主机上移动文件或目录。
     *
     * @param ssh 服务到 Ansible 的 ssh 连接
     * @param sourcePath 要移动的文件或目录路径
     * @param destinationDir 移动到的目录
     * @return 返回移动结果
     */
    public static SshResult moveFileOrDirInAnsible(SshHelper ssh, String sourcePath, String destinationDir) throws Exception {
        /*
         逻辑:
         1. 构建 sourcePath 移动到 destinationDir 里的路径 destPath
         2. 删除目录 destPath 和创建目录 destinationDir
         3. 移动 sourcePath 到目录 destinationDir 里
         */
        log.info("移动文件或目录: SrcPath [{}], DestDir [{}]", sourcePath, destinationDir);

        // [1] 构建 sourcePath 移动到 destinationDir 里的路径 destPath
        String destPath = destinationDir + Paths.get(sourcePath).getFileName();

        // [2] 删除目录 destPath 和创建目录 destinationDir
        String mkdirCmd = String.format("rm -rf %s && mkdir -p %s", destPath, destinationDir);
        SshResult result = ssh.executeCommand(mkdirCmd);

        if (!result.isSuccess()) {
            return result;
        }

        // [3] 移动 sourcePath 到目录 destinationDir 里
        String mvCmd = String.format("mv %s %s", sourcePath, destinationDir);
        result = ssh.executeCommand(mvCmd);

        if(!result.isSuccess()) {
            log.warn("移动文件或目录失败: SrcPath [{}], DestDir [{}]", sourcePath, destinationDir);
        }

        return result;
    }

    /**
     * 删除 Ansible 主机上复制生成的临时目录。
     *
     * @param ssh 服务到 Ansible 的 ssh 连接
     * @param tempPath 复制时使用的临时文件或者目录的路径
     */
    public static void deleteCopyGeneratedTempDirectoryInAnsible(SshHelper ssh, String tempPath) throws Exception {
        /*
         逻辑:
         1. 从路径 path 中查找到 AUTO_TEMP_DIR_NAME 的直接子目录
         2. 删除此子目录
         */

        // [1] 从路径 path 中查找到 AUTO_TEMP_DIR_NAME 的直接子目录
        Matcher matcher = AnsibleRunnerConst.TEMP_ROOT_DIR_PATTERN_OF_COPY.matcher(tempPath);

        if (!matcher.find()) {
            return;
        }

        String tempRootDir = tempPath.substring(0, matcher.end());
        deleteFileOrDirectoryInAnsible(ssh, tempRootDir);
    }

    /**
     * 删除 Ansible 上的文件或者目录。
     *
     * @param ssh 服务到 Ansible 的 ssh 连接
     * @param path 要被删除的文件或目录路径
     */
    public static void deleteFileOrDirectoryInAnsible(SshHelper ssh, String path) throws Exception {
        String del = "rm -rf " + path;
        ssh.executeCommand(del);
    }

    /**
     * 检查文件或者目录在指定的主机上是否存在。
     *
     * @param ssh 自动化 Web 服务到 Ansible 的 ssh 连接
     * @param destinationIp 文件所在主机 IP，为 null 时表示 Ansible 所在主机
     * @param path 文件路径
     * @return 文件或者目录存在返回成功的结果，否则返回失败的结果。
     */
    public static boolean fileOrDirExists(SshHelper ssh, String destinationIp, String path) {
        /*
         逻辑: 如果目标主机是 null 表示查询在 Ansible 主机上 path 指向的文件是否存在，否则使用 Ansible 命令查询目标主机上的文件是否存在。
         1. 判断是否在 Ansible 所在主机还是使用 Ansible 命令去其他主机检查文件是否存在
         2. 生成检查文件存在的命令
         3. 执行命令，返回的结果包含 file-exists 则表明文件存在，否则不存在
         */

        // [1] 判断是否在 Ansible 所在主机还是使用 Ansible 命令去其他主机检查文件是否存在
        boolean runAtAnsible = destinationIp == null;
        String cmd;

        // [2] 生成检查文件存在的命令
        if (runAtAnsible) {
            cmd = replace(AnsibleRunnerConst.FILE_OR_DIR_EXISTS_IN_CMD, of(
                    "path", path
            ));
        } else {
            cmd = replace(AnsibleRunnerConst.FILE_OR_DIR_EXISTS_VIA_ANSIBLE, of(
                    "nodeIp", destinationIp,
                    "path", path,
                    "inventory", AnsibleRunner.INVENTORY.get()
            ));
        }

        try {
            // [3] 执行命令，返回的结果包含 file-exists 则表明文件存在，否则不存在
            SshResult result = ssh.executeCommand(cmd);
            return result.isSuccess() && result.getContent().contains("file-exists");
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 获取指定主机的 Home 目录。
     *
     * @param ssh ssh 服务到 Ansible 的 ssh 连接
     * @param destinationIp 目标主机 IP，为 null 时表示 Ansible 所在主机
     * @return 返回 Home 路径。
     */
    public static String getHome(SshHelper ssh, String destinationIp) throws Exception {
        /*
         逻辑:
         1. 判断是否在 Ansible 所在主机还是使用 Ansible 命令去其他主机查询 Home 目录
         2. 生成命令
         3. 执行命令
         4. 从命令的响应中解析 Home 路径:
            4.1 本机查询 Home 命令直接返回 Home 路径
            4.2 Ansible 查询远程主机 Home 命令返回的结果中以 / 开头的行即是 Home 的路径

         示例: Ansible 返回的 Home 结果 (找到以 / 开头的行)
         192.168.12.101 | CHANGED | rc=0 >>
         /root
         */

        // [1] 判断是否在 Ansible 所在主机还是使用 Ansible 命令去其他主机查询 Home 目录
        boolean runAtAnsible = destinationIp == null;
        String cmd;

        // [2] 生成命令
        if (runAtAnsible) {
            cmd = AnsibleRunnerConst.HOME_IN_CMD;
        } else {
            cmd = replace(AnsibleRunnerConst.HOME_VIA_ANSIBLE, of(
                    "nodeIp", destinationIp,
                    "inventory", AnsibleRunner.INVENTORY.get()
            ));
        }

        // [3] 执行命令
        SshResult result = ssh.executeCommand(cmd);
        String response = result.getContent();

        if (!result.isSuccess()) {
            throw new RuntimeException(String.format("查询主机命令失败: DestinationIp [%s], Cause [%s]", destinationIp, response));
        }

        // [4] 从命令的响应中解析 Home 路径:
        if (runAtAnsible) {
            // [4.1] 本机查询 Home 命令直接返回 Home 路径
            return StringUtils.trimWhitespace(result.getContent());
        } else {
            // [4.2] Ansible 查询远程主机 Home 命令返回的结果中以 / 开头的行即是 Home 的路径
            String home = null;
            for (String str : Splitter.on("\n").trimResults().split(response)) {
                if (str.startsWith("/")) {
                    home = str;
                    break;
                }
            }

            return home;
        }
    }

    /**
     * 使用 Ansible 给 nodeIp 上的文件增加可执行权限。
     *
     * @param ssh ssh 服务到 Ansible 的 ssh 连接
     * @param nodeIp 文件所在主机 IP
     * @param path 文件路径
     */
    public static void addExecutablePermissionToFileViaAnsible(SshHelper ssh, String nodeIp, String path) throws Exception {
        String cmd = replace(AnsibleRunnerConst.FILE_EXECUTABLE_VIA_ANSIBLE, of(
                "nodeIp", nodeIp,
                "path", path,
                "inventory", AnsibleRunner.INVENTORY.get()
        ));

        ssh.executeCommand(cmd);
    }

    /**
     * 从 Ansible 执行 Node 上的脚本。
     *
     * @param ssh ssh 服务到 Ansible 的 ssh 连接
     * @param nodeIp 脚本所在主机 IP
     * @param scriptPath 脚本路径
     * @param args 脚本参数
     * @return 返回执行结果
     */
    public static SshResult executeScriptViaAnsible(SshHelper ssh, String nodeIp, String scriptPath, String args) throws Exception {
        String cmd = replace(AnsibleRunnerConst.EXECUTE_SCRIPT_VIA_ANSIBLE, of(
                "nodeIp", nodeIp,
                "script", scriptPath,
                "args", args,
                "inventory", AnsibleRunner.INVENTORY.get()
        ));

        return ssh.executeCommand(cmd);
    }

    /**
     * 生成在 Ansible 主机上的临时目录路径 (此时不会创建临时目录)。
     *
     * @return 返回临时目录的路径。
     */
    public static String generateTempDirPathInAnsible(SshHelper ssh) throws Exception {
        /*
         逻辑:
         1. 查询用户目录
         2. 构建在用户目录下的临时目录路径，例如 /root/shindata-temp-auto/1665737329979/
         */

        // [1] 查询用户目录
        String home = AnsibleRunnerHelper.getHome(ssh, null);
        log.info("用户目录: [{}]", home);

        // [2] 使用时间戳构建在用户目录下的临时目录路径，例如 /root/shindata-temp-auto/1665737329979/
        String tempDestinationDir = replace(AnsibleRunnerConst.TEMP_DIR_OF_ANSIBLE, of(
                "home", home,
                "timestamp", System.currentTimeMillis()
        ));

        return tempDestinationDir;
    }

    /**
     * 检查目录路径格式，必须以 / 结尾。
     *
     * @param path 路径
     */
    public static void checkDir(String path) {
        Preconditions.checkArgument(path.endsWith("/"), "目录的路径必须以 / 结尾");
    }
}
