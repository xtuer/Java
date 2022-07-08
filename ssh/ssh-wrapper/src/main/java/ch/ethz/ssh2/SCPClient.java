package ch.ethz.ssh2;

import com.jcraft.jsch.ChannelSftp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 使用 Scp 上传文件。
 */
public class SCPClient {
    /**
     * 底层的 jsch Session。
     */
    private final com.jcraft.jsch.Session jschSession;

    public SCPClient(com.jcraft.jsch.Session jschSession) {
        this.jschSession = jschSession;
    }

    /**
     * 上传本地文件到远程机器，远程机器上文件路径为传入的 remoteTargetFile。
     *
     * @param localFile 本地文件路径
     * @param remoteTargetFile 远程文件路径
     * @throws IOException 上传错误时抛 IOException 异常
     */
    public void put(String localFile, String remoteTargetFile) throws IOException {
        put(Files.newInputStream(Paths.get(localFile)), remoteTargetFile);
    }

    /**
     * 上传多个文件到远程机器的 remoteTargetDirectory 目录中。
     *
     * @param localFiles 本地文件路径数组
     * @param remoteTargetDirectory 远程目录
     * @throws IOException 上传错误时抛 IOException 异常
     */
    public void put(String[] localFiles, String remoteTargetDirectory) throws IOException {
        for (String localFile : localFiles) {
            String filename = Paths.get(localFile).getFileName().toString();
            put(localFile, remoteTargetDirectory + "/" + filename);
        }
    }

    /**
     * 使用输入流上传文件到远程机器，远程文件路径为传入的 remoteTargetFile。
     *
     * @param srcIn 输入流
     * @param remoteTargetFile 远程文件路径
     * @throws IOException 上传出错时抛出 IOException
     */
    public void put(InputStream srcIn, String remoteTargetFile) throws IOException {
        try {
            ChannelSftp channel = (ChannelSftp) jschSession.openChannel("sftp");
            channel.connect();
            channel.put(srcIn, remoteTargetFile);
            channel.disconnect();
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }
}
