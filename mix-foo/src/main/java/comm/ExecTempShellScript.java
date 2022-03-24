package comm;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 生成临时 shell 脚本并执行
 */
public class ExecTempShellScript {
    public static void main(String[] args) throws IOException {
        // 1. 生成临时脚本文件
        // 2. 命令写入脚本文件
        // 3. 执行脚本
        // 4. 删除临时脚本文件

        String command = "ls -l /Users/biao";
        Path path = Files.createTempFile("mongo-", ".sh");
        Files.write(path, command.getBytes(StandardCharsets.UTF_8));
        System.out.println(path);

        try {
            execSh(path.toString());
        } finally {
            Files.delete(path);
        }
    }

    public static void execSh(String path) throws IOException {
        CommandLine cmdLine = CommandLine.parse("sh " + path);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValues(null);

        ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
        executor.setWatchdog(watchdog);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream,errorStream);

        executor.setStreamHandler(streamHandler);
        executor.execute(cmdLine);

        // 获取程序外部程序执行结果
        String out = outputStream.toString("UTF-8");
        String error = errorStream.toString("UTF-8");

        // 处理结果
        System.out.println("==== ok ====");
        System.out.println(out);
        System.out.println("==== error ====");
        System.out.println(error);
    }
}
