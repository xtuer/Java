package util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
     * 超时时间: 30 分钟。
     */
    private static final long TIMEOUT = TimeUnit.MINUTES.toMillis(30);

    /**
     * 执行命令。
     *
     * @param command 要执行的命令。
     * @return 返回命令的结果
     * @throws RuntimeException 执行失败时抛出异常
     */
    public static Result executeCommand(String command) {
        /*
         逻辑:
         1. 把要执行的命令 command 保存到临时 shell 文件。
         2. 执行 sh 脚本，获取结果。
         3. 删除生成的临时文件。
         */

        Path shPath = null;

        try {
            // [1] 把要执行的命令 command 保存到临时 shell 文件。
            shPath =  Files.createTempFile("shell-script-", ".sh");
            Files.write(shPath, command.getBytes(StandardCharsets.UTF_8));

            // [2] 执行 sh 脚本，获取结果。
            log.info("[本地执行命令] [{}]，把命令写入临时脚本进行执行 [{}]", command, shPath);
            return LocalCommander.executeShellScript(shPath.toString());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            // [3] 删除生成的临时文件。
            if (shPath != null) {
                try {
                    log.debug("[删除临时脚本] {}", shPath);
                    Files.delete(shPath);
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * 执行 shell 脚本
     *
     * @param path 脚本路径
     * @return 返回脚本的结果
     * @throws IOException 执行失败时抛出异常
     */
    public static Result executeShellScript(String path) throws IOException {
        /*
         逻辑:
         1. 构造执行脚本的命令，创建执行器。
         2. 创建一个 watchdog，当脚本的进程超时后自动被 watchdog 关闭。
         3. 设置命令的输入输出流获取命令的执行结果。
         4. 执行命令。
         5. 根据命令的返回码，处理命令结果。
         */
        log.info("[本地执行脚本]: sh {}", path);

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
        String ok  = outputStream.toString("UTF-8");
        String err = errorStream.toString("UTF-8");

        // [5] 根据命令的返回码，处理命令结果。
        // Shell 里使用 echo $? 查看命令进程的返回值 return code。
        // 返回值 -1 表示状态未知，0 为命令执行成功，其他值为错误。
        // 当 rc 为 -1 且错误输出不为空则说明命令执行错误。
        // 参考 SSH command in Java/JSch giving exit code -1:
        // https://stackoverflow.com/questions/40896820/ssh-command-in-java-jsch-giving-exit-code-1
        if (rc == -1 && !"".equals(err)) {
            // 有的脚本出错的时候，正确输出和错误输出都会有。把正确的输出也作为错误的内容，输出到日志时有利于判断问题。
            log.warn("命令状态未知，return code [{}]，正确输出: [{}]\n错误信息: [{}]", rc, ok, err);
            return new Result(-1, ok + "\n" + err);
        } else if (rc != 0) {
            log.warn("命令执行错误，return code [{}]，正确输出: [{}]\n错误信息 [{}]", rc, ok, err);
            return new Result(rc, ok + "\n" + err);
        }

        return new Result(0, ok);
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

    /**
     * Ssh 执行结果。
     */
    @Data
    public static class Result {
        public Result(int code, String content) {
            this.code = code;
            this.content = content;
        }

        /**
         * sh 命令的 return code。
         * code 为 0 表示执行成功，code 非 0 表示执行失败。
         */
        private int code;

        /**
         * 命令执行结果。
         */
        private String content;

        /**
         * 判断操作是否成功。
         *
         * @return 成功返回 true，失败返回 false。
         */
        public boolean isSuccess() {
            return code == 0;
        }
    }

    /**
     * 构建日志搜索命令，如果 grepSearchPattern 为空则获取文件最后 1000 行。
     *
     * @param logPath 日志的绝对路径。
     * @param grepSearchPattern Grep 使用的搜索 pattern。
     * @return 返回搜索日志的命令。
     */
    public static String buildSearchLogCommand(String logPath, String grepSearchPattern) {
        grepSearchPattern = StringUtils.trimWhitespace(grepSearchPattern);

        if (StringUtils.hasLength(grepSearchPattern)) {
            grepSearchPattern = grepSearchPattern.replace("\"", "\\\"");
            return String.format("grep -in \"%s\" %s | tail -1000", grepSearchPattern, logPath);
        } else {
            return String.format("tail -1000 %s", logPath);
        }
    }

    public static void main(String[] args) {
        // System.out.println(LocalCommander.executeCommand("ls /usr/local/bin"));
        // System.out.println(LocalCommander.executeCommand("tail -10 ~/java_error_in_idea_80668.log"));
        System.out.println(buildSearchLogCommand("/Users/biao/Documents/temp/fe/vue-temp-2/vue.config.js", ""));
        System.out.println(buildSearchLogCommand("/Users/biao/Documents/temp/fe/vue-temp-2/vue.config.js", "'*'"));
    }
}
