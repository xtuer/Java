package util;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 使用 Jsch 远程执行命令或者传文件。
 * 命令的 return code 为非 0 时表示命令执行失败，抛出 RuntimeException 异常。
 *
 * session.openChannel("exec") and session.openChannel("shell"):
 * What's the exact differences between jsch ChannelExec and ChannelShell?
 * https://stackoverflow.com/questions/6265278/whats-the-exact-differences-between-jsch-channelexec-and-channelshell
 */
@Slf4j
public class JschService implements AutoCloseable {
    /**
     * Session 对应 TCP Socket，使用后需要关闭，否则会造成连接泄漏。
     *
     * 提示:
     * A. Session 打开的 Channel 不会建立新的 Socket 连接。
     * B. 一个 Session 只支持 10 个 channel 并发执行命令，可以使用 Semaphore(10) 进行优化控制并发量。
     */
    private final Session session;

    /**
     * 建立连接的超时时间。
     */
    private int timeout = 6000;

    public JschService(final String host, final String username, final String password) throws Exception {
        JSch jsch = new JSch();
        this.session = jsch.getSession(username, host, 22);
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
    public String executeCommand(String command) throws Exception {
        log.info("执行命令: 主机 [{}]，命令 [{}]", session.getHost(), command);

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        InputStream okOutput  = channel.getInputStream();
        InputStream errOutput = channel.getErrStream();

        channel.connect();
        String ok  = JschService.readAsString(okOutput);
        String err = JschService.readAsString(errOutput);
        channel.disconnect();

        // 命令 return code 不为 0 则说明执行命令失败
        if (channel.getExitStatus() != 0) {
            throw new RuntimeException(err);
        }

        return ok;
    }

    /**
     * 使用 sftp 复制本地文件到远程机器，如果远程目录不存在则会自动创建。
     *
     * @param localPath 本地文件路径
     * @param remoteDirectory 目标机器上的目录
     * @throws Exception scp 复制文件失败时抛异常，例如远程文件夹不存在
     */
    public void sftpPut(String localPath, String remoteDirectory) throws Exception {
        log.info("上传文件: 主机 [{}]，本地文件 [{}]，远程目录 [{}]", session.getHost(), localPath, remoteDirectory);

        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();

        // 如果目录以 ~ 开头，则把其转换为用户目录的路径，JSch 上传文件时路径不支持 ~
        if (remoteDirectory.startsWith("~")) {
            remoteDirectory = channel.getHome() + "/" + remoteDirectory.substring(1);
        }

        // 路径需要以 / 开头
        if (!remoteDirectory.startsWith("/")) {
            throw new RuntimeException("路径需要以 / 开头，不能使用相对路径: " + remoteDirectory);
        }

        // 确保目录存在
        JschService.makeSureDirectories(channel, remoteDirectory);

        channel.cd(remoteDirectory);
        Path path = Paths.get(localPath);
        channel.put(Files.newInputStream(path), path.getFileName().toString());

        channel.disconnect();
    }

    /**
     * 确保远程机器上的目录存在，不存在则创建。
     *
     * @param channel Sftp 通道
     * @param remoteDirectory 远程目录
     * @throws RuntimeException 创建目录出错跑运行时异常
     */
    private static void makeSureDirectories(ChannelSftp channel, String remoteDirectory) {
        /*
         逻辑: 倒序测试目录是否存在，把需要创建的目录路径加入 newDirs
         1. 获取文件属性，如果不存在则抛 SftpException 异常
         2. 获取文件属性成功，是目录则结束循环，非目录抛异常
         3. 获取文件属性出错，异常信息里包含 "No such file" 则说明目录不存在，加入要创建目录的列表
         4. 创建需要的目录
         */
        List<String> newDirs = new LinkedList<>();
        Path path = Paths.get(remoteDirectory);

        while (path != null) {
            try {
                // [1] 获取文件属性，如果不存在则抛 SftpException 异常
                SftpATTRS attrs = channel.lstat(path.toString());

                // [2] 获取文件属性成功，是目录则结束循环，非目录抛异常
                if (attrs.isDir()) {
                    break;
                } else {
                    throw new RuntimeException("文件存在，但不是目录: " + path);
                }
            } catch (SftpException ex) {
                String message = ex.getMessage();

                // [3] 获取文件属性出错，异常信息里包含 "No such file" 则说明目录不存在，加入要创建目录的列表
                if (message.contains("No such file")) {
                    newDirs.add(0, path.toString());
                    path = path.getParent();
                } else {
                    throw new RuntimeException(ex);
                }
            }
        }

        // [4] 创建需要的目录
        for (String dir : newDirs) {
            try {
                String host = channel.getSession().getHost();
                log.info("创建目录: 主机 [{}]，远程目录 [{}]", host, dir);
                channel.mkdir(dir);
            } catch (SftpException | JSchException ex) {
                throw new RuntimeException(ex);
            }
        }
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
    private static String readAsString(InputStream in) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public void close() {
        this.session.disconnect();
    }
}