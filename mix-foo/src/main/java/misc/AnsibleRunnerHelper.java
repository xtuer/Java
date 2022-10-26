package misc;

import lombok.extern.slf4j.Slf4j;
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

        return "2".equals(ok) || "3".equals(ok);
    }

    /**
     * 移动文件或目录。
     *
     * @param ssh 服务到 Ansible 的 ssh 连接
     * @param sourcePath 要移动的文件或目录路径
     * @param destinationDir 移动到的目录
     * @return 返回移动结果
     */
    public static SshResult moveFileOrDir(SshHelper ssh, String sourcePath, String destinationDir) throws Exception {
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
     * 生成在 Ansible 主机上的临时目录路径 (没有创建临时目录)。
     *
     * @return 返回临时目录的路径。
     */
    public static String generateTempDirPathInAnsible(SshHelper ssh) throws Exception {
        /*
         逻辑:
         1. 查询用户目录
         2. 构建在用户目录下的临时目录路径，例如 /root/shindata-temp-auto/1665737329979/
         */

        SshResult homeResult = ssh.executeCommand("echo $HOME");
        if (!homeResult.isSuccess()) {
            throw new RuntimeException("执行命令 [echo $HOME] 失败");
        }

        // [1] 查询用户目录
        String home = StringUtils.trimWhitespace(homeResult.getContent());
        log.info("用户目录: [{}]", homeResult.getContent());

        // [2] 使用时间戳构建在用户目录下的临时目录路径，例如 /root/shindata-temp-auto/1665737329979/
        String tempDestinationDir = replace(AnsibleRunnerConst.TEMP_DIR_OF_ANSIBLE, of(
                "home", home,
                "timestamp", System.currentTimeMillis()
        ));

        return tempDestinationDir;
    }

    /**
     * 删除复制任务的临时目录。
     *
     * @param ssh 服务到 Ansible 的 ssh 连接
     * @param tempPath 复制时使用的临时文件或者目录的路径
     */
    public static void deleteCopyTaskTempDirectory(SshHelper ssh, String tempPath) throws Exception {
        /*
         逻辑:
         1. 从路径 path 中查找到 AUTO_TEMP_DIR_NAME 的直接子目录
         2. 删除此子目录
         */

        // [1] 从路径 path 中查找到 AUTO_TEMP_DIR_NAME 的直接子目录
        Matcher matcher = AnsibleRunnerConst.COPY_TASK_TEMP_ROOT_DIR_PATTERN.matcher(tempPath);

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
     * @return 返回删除结果。
     */
    public static SshResult deleteFileOrDirectoryInAnsible(SshHelper ssh, String path) throws Exception {
        String del = "rm -rf " + path;
        SshResult result = ssh.executeCommand(del);

        return result;
    }

    /**
     * 检查传入的文件或者目录是否在文件系统里存在。
     *
     * @param ssh 自动化 Web 服务到 Ansible 的 ssh 连接
     * @param destinationIp 文件所在主机 IP
     * @param path 文件路径
     * @return 文件或者目录存在返回成功的结果，否则返回失败的结果。
     */
    public static SshResult fileOrDirExists(SshHelper ssh, String destinationIp, String path) {
        throw new UnsupportedOperationException();
    }
}
