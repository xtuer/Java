package ch.ethz.ssh2;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.IOException;

/**
 * 与远程机器的连接，底层是一个 com.jcraft.jsch.Session。
 */
public class Connection {
    /**
     * 远程机器的 IP 或者域名
     */
    private String hostname;

    /**
     * SSH 端口
     */
    private int port;

    /**
     * 连接超时
     */
    private int timeout = 60000;

    /**
     * 与远程主机的连接 (叫 Session 是为了适配 ch.ethz.ssh2)
     */
    private Session session;

    public Connection(String hostname) {
        this(hostname, 22);
    }

    public Connection(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public synchronized void connect() throws IOException {
        // Do nothing.
    }

    /**
     * 使用账号密码进行登录。
     *
     * @param user 账号
     * @param password 密码
     * @return 登录成功返回 true
     * @throws IOException 登录出现异常如网络异常，不可联通等抛出 IOException 异常
     */
    public synchronized boolean authenticateWithPassword(String user, String password) throws IOException {
        try {
            JSch jsch = new JSch();
            this.session = jsch.getSession(user, hostname, port);
            this.session.setPassword(password);
            this.session.setConfig("StrictHostKeyChecking", "no");
            this.session.setTimeout(this.timeout);
            this.session.connect();

            return true;
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }

    /**
     * 使用 pem 格式的 ssh key 进行登录。
     *
     * 注意:
     * A. 函数名有问题，不是使用 public key，而是使用 private key (否则会报 invalid private key 错误)。
     * B. Jsch 不支持 OpenSSH key，只支持 RSA key，需要执行命令进行转换: ssh-keygen -p -f ~/.ssh/id_rsa -m pem，
     *    由于会覆盖被转换的 id_rsa 源文件，最好复制后再进行转换。
     *    参考 https://mkyong.com/java/jsch-invalid-privatekey-exception/
     *
     * @param user 账号
     * @param privateKey ssh private key
     * @param password 密码 (不知道干啥用)
     * @return 登录成功返回 true
     * @throws IOException 登录出现异常如网络异常，不可联通等抛出 IOException 异常
     */
    public synchronized boolean authenticateWithPublicKey(String user, File privateKey, String password) throws IOException {
        try {
            JSch jsch = new JSch();
            jsch.addIdentity(privateKey.getCanonicalPath());
            this.session = jsch.getSession(user, hostname, port);
            this.session.setConfig("StrictHostKeyChecking", "no");
            this.session.setTimeout(this.timeout);
            this.session.connect();

            return true;
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }

    public synchronized boolean authenticateWithKeyboardInteractive(String user, InteractiveCallback cb) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * 打开一个 session 用于执行命令和传输文件等。
     *
     * @return 返回 Session
     * @throws IOException 打开 Session 失败时抛出异常
     */
    public synchronized ch.ethz.ssh2.Session openSession() throws IOException {
        return new ch.ethz.ssh2.Session(this.session);
    }

    /**
     * 创建 SCPClient 用于传输文件。
     *
     * @return 返回 SCPClient 对象
     * @throws IOException 创建 SCPClient 对象失败时抛出异常
     */
    public synchronized SCPClient createSCPClient() throws IOException {
        return new SCPClient(this.session);
    }

    /**
     * 关闭连接。
     */
    public synchronized void close() {
        if (session != null) {
            session.disconnect();
        }
    }
}
