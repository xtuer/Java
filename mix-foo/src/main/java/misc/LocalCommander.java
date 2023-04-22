package misc;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

/**
 * 执行本地命令。
 */
@Slf4j
public class LocalCommander {
    /**
     * 超时时间: 3 小时
     */
    private static final long TIMEOUT = TimeUnit.HOURS.toMillis(3);

    /**
     * 执行命令。
     *
     * @param command 要执行的命令。
     * @return 返回命令的结果
     * @throws Exception 执行失败时抛出异常
     */
    public static String executeCommand(String command) throws Exception {
        /*
         逻辑:
         1. 把要执行的命令 command 保存到临时 shell 文件
         2. 执行 sh 脚本，获取结果
         3. 删除生成的临时文件
         */

        // [1] 把要执行的命令 command 保存到临时 shell 文件
        Path shPath = Files.createTempFile("shell-script-", ".sh");
        Files.write(shPath, command.getBytes(StandardCharsets.UTF_8));

        // [2] 执行 sh 脚本，获取结果
        try {
            log.info("执行命令 [{}]，把命令写入脚本进行执行 [{}]", command, shPath);
            String result = LocalCommander.execSh(shPath.toString());
            return result;
        } finally {
            // [3] 删除生成的临时文件
            Files.delete(shPath);
        }
    }

    /**
     * 执行 sh 脚本
     *
     * @param path 脚本路径
     * @return 返回脚本的结果
     * @throws Exception 执行失败时抛出异常
     */
    public static String execSh(String path) throws Exception {
        /*
         逻辑:
         1. 构造执行脚本的命令，创建执行器。
         2. 创建一个 watchdog，当脚本的进程超时后自动被 watchdog 关闭。
         3. 设置命令的输入输出流获取命令的执行结果。
         4. 执行命令。
         5. 根据命令的返回码，处理命令结果。
         */
        log.info("执行脚本: sh {}", path);

        // [1] 构造执行脚本的命令，创建执行器。
        // Unix like 执行 sh x.sh
        CommandLine cmdLine = CommandLine.parse("sh " + path);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValues(null);

        // [2] 创建一个 watchdog，当脚本的进程超时后自动被 watchdog 关闭。
        ExecuteWatchdog watchdog = new ExecuteWatchdog(TIMEOUT);
        executor.setWatchdog(watchdog);

        // [3] 设置命令的输入输出流获取命令的执行结果。
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream,errorStream);

        // [4] 执行命令。
        executor.setStreamHandler(streamHandler);
        int rc = executor.execute(cmdLine);

        // 获取程序外部程序执行结果
        String out = outputStream.toString("UTF-8");
        String err = errorStream.toString("UTF-8");

        // 5] 根据命令的返回码，处理命令结果。
        // Shell 里使用 echo $? 查看命令进程的返回值 return code。
        // 返回值 -1 表示状态未知，0 为命令执行成功，其他值为错误。
        // 当 rc 为 -1 且错误输出不为空则说明命令执行错误。
        // 参考 SSH command in Java/JSch giving exit code -1:
        // https://stackoverflow.com/questions/40896820/ssh-command-in-java-jsch-giving-exit-code-1
        if (rc == -1 && err != null && err.equals("")) {
            log.warn("命令状态未知，return code [{}]", rc);
            throw new RuntimeException(err);
        } else if (rc != 0) {
            log.warn("命令执行错误，return code [{}]", rc);
            throw new RuntimeException(err);
        }

        return out;
    }

    /**
     * 复制文件到指定目录。
     *
     * @param path 要复制的文件路径
     * @param directory 文件复制到的目录
     * @throws Exception 复制错误时抛出异常
     */
    public static void copyFile(String path, String directory) throws Exception {
        /*
         逻辑:
         1. 如果 path 和 directory 以 ~ 开头，替换为用户目录
         1. 创建文件和目录对象。
         2. 如果目录不存在则创建。
         3. 复制文件到目录。
         */

        log.info("复制文件: 文件 [{}]，目录 [{}]", path, directory);

        // [1] 如果 path 和 directory 以 ~ 开头，替换为用户目录
        if (path.startsWith("~")) {
            path = System.getProperty("user.home") + path.substring(1);
        }
        if (directory.startsWith("~")) {
            directory = System.getProperty("user.home") + directory.substring(1);
        }

        // [1] 创建文件和目录对象。
        Path src = Paths.get(path);
        Path dir = Paths.get(directory);

        // [2] 如果目录不存在则创建。
        if (Files.notExists(dir)) {
            Files.createDirectories(dir);
        }

        // [3] 如果目录不存在则创建。
        // 文件已经存在则覆盖 (优化: 文件已经存在，进行判断 2 个文件的 MD5 是否一致，一致则不继续复制，不一致则覆盖已存在的文件，这种处理对于复制大文件比较友好)。
        Files.copy(src, dir.resolve(src.getFileName()), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void main(String[] args) throws Exception {
        String result = LocalCommander.executeCommand("ls /");
        System.out.println(result);

        // System.out.println("==> " + System.getProperty("user.home"));
        // LocalCommander.copyFile("~/Desktop/mongo-test.js", "~/Desktop/a/b/c");
    }
}
