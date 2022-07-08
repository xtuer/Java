package util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Collectors;

/**
 * 使用 Jsch 远程执行命令或者传文件。
 * 命令的 return code 为非 0 时表示命令执行失败，抛出 RuntimeException 异常。
 *
 * session.openChannel("exec") and session.openChannel("shell"):
 * What's the exact differences between jsch ChannelExec and ChannelShell?
 * https://stackoverflow.com/questions/6265278/whats-the-exact-differences-between-jsch-channelexec-and-channelshell
 */
public class JschUtils implements AutoCloseable {
    private final Session session;
    private int timeout = 60000;

    public JschUtils(final String ipAddress, final String username, final String password) throws Exception {
        JSch jsch = new JSch();
        this.session = jsch.getSession(username, ipAddress, 22);
        this.session.setPassword(password);
        this.session.setConfig("StrictHostKeyChecking", "no");
        this.session.setTimeout(this.timeout);
        this.session.connect();
    }

    /**
     * 在远程机器上执行命令。
     *
     * @param command 命令，支持管道等
     * @return 返回命令的结果
     * @throws Exception 命令执行失败时抛出异常
     */
    public String runCommand(String command) throws Exception {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        InputStream okOutput = channel.getInputStream();
        InputStream errOutput = channel.getErrStream();

        channel.connect();
        String ok = JschUtils.readString(okOutput);
        String err = JschUtils.readString(errOutput);
        channel.disconnect();

        // 如果错误输出有内容，则说明命令执行失败，抛出异常
        if (err != null  && !"".equals(err)) {
            throw new RuntimeException(err);
        }

        return ok;
    }

    /**
     * 使用 sftp 复制本地文件到远程机器。
     *
     * @param localPath 本地文件路径
     * @param remoteDirectory 目标机器上的目录
     * @throws Exception scp 复制文件失败时抛异常，例如远程文件夹不存在
     */
    public void sftpPut(String localPath, String remoteDirectory) throws Exception {
        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();

        channel.cd(remoteDirectory);
        File file = new File(localPath);
        channel.put(Files.newInputStream(file.toPath()), file.getName());

        channel.disconnect();
    }

    public void sftpGet() throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * 从 in 中读取所有内容为字符串。
     *
     * @param in 输入流
     * @return 返回输入流中的字符串
     */
    private static String readString(InputStream in) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public void close() {
        this.session.disconnect();
    }
}
