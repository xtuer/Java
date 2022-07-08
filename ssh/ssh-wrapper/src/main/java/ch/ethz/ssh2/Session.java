package ch.ethz.ssh2;

import com.jcraft.jsch.ChannelExec;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * 代理 com.jcraft.jsch.Session 的 Session，用于执行命令。
 */
public class Session {
    /**
     * 底层的 jsch Session。
     */
    private final com.jcraft.jsch.Session jschSession;

    /**
     * 命令执行成功的结果。
     */
    private String okResult;

    /**
     * 命令执行失败的结果。
     */
    private String errResult;

    public Session(com.jcraft.jsch.Session jschSession) {
        this.jschSession = jschSession;
    }

    /**
     * 执行命令。
     *
     * @param command 要执行的命令
     * @throws IOException 命令被执行前出现问题抛出 IOException 异常，例如网络异常
     * @throws RuntimeException 命令被执行，但执行出错时抛出 RuntimeException 异常，例如命令不存在，目录不存在等
     */
    public void execCommand(String command) throws IOException {
        /*
         逻辑:
         1. 创建执行命令的 channel，并设置 channel 要执行的命令为传入的 command
         2. 连接 channel 执行命令
         3. 获取命令的执行结果: 命令成功的输出和失败的输出
         4. 读取完成后关闭 channel
         */
        try {
            ChannelExec channel = (ChannelExec) jschSession.openChannel("exec");
            channel.setCommand(command);
            InputStream okOutput = channel.getInputStream();
            InputStream errOutput = channel.getErrStream();

            channel.connect();
            okResult = Session.readString(okOutput);
            errResult = Session.readString(errOutput);
            channel.disconnect();

            // 命令执行出错时抛出异常。
            if (errResult != null && !"".equals(errResult)) {
                throw new RuntimeException(errResult);
            }
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }

    /**
     * 从输入流 in 中读取所有内容为字符串。
     *
     * @param in 输入流
     * @return 返回输入流中的字符串
     */
    private static String readString(InputStream in) {
        // 读取输入流中的内容转为字符串。
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取命令执行成功时的输入流。
     *
     * @return 返回 InputStream 对象。
     */
    public InputStream getStdout() {
        return new ByteArrayInputStream(okResult.getBytes());
    }

    /**
     * 获取命令执行失败时的输入流。
     * @return 返回 InputStream 对象。
     */
    public InputStream getStderr() {
        return new ByteArrayInputStream(errResult.getBytes());
    }

    public OutputStream getStdin() {
        throw new UnsupportedOperationException("SESSION 不支持 stdin");
    }

    public String getOkResult() {
        return okResult;
    }

    public String getErrorResult() {
        return errResult;
    }

    /**
     * 本类的 Session 对象不能在此关闭，此方法是为了适配 ch.ethz.ssh2。
     */
    public void close() {
    }
}
