package util;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SCPClient;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 使用 ssh 远程执行命令。
 * 依赖: 'com.trilead:trilead-ssh2:1.0.0-build222'
 */
public final class SshUtils {
    public static Connection sshConnect(String ip, int port, String user, String password) throws IOException {
        // 创建链接
        Connection conn = new Connection("192.168.1.164", 22);
        conn.connect();

        // 添加信任
        boolean isAuthenticated = conn.authenticateWithPassword("root", "Newdt@cn");

        if (!isAuthenticated) {
            throw new IOException("Authentication failed");
        }

        return conn;
    }

    /**
     * 远程执行 shell 命令。
     *
     * @param conn    ssh 连接
     * @param command 命令
     * @return 返回执行结构的 Map: { code: 0|other, result: "${command output}" }
     *         远程命令执行成功是 code 为 0，失败时，code 为非 0
     */
    public static SshResult runCommand(Connection conn, String command) throws IOException {
        StringBuilder result = new StringBuilder();
        InputStream stdout = null;
        InputStream stderr = null;
        BufferedReader br = null;
        Session session = null;
        Map<String, String> retMap = new HashMap<>();

        System.out.println("=> 执行命令: " + command);

        // 执行命令
        try {
            session = conn.openSession();
            session.execCommand(command);

            // 读取正确输出
            stdout = new StreamGobbler(session.getStdout());
            br = new BufferedReader(new InputStreamReader(stdout));
            while (true) {
                String line = br.readLine();
                if (line == null){
                    break;
                }
                result.append(line).append("\n");
            }

            // 读取错误输出
            stderr = new StreamGobbler(session.getStderr());
            br = new BufferedReader(new InputStreamReader(stderr));
            while (true) {
                String line = br.readLine();
                if (line == null){
                    break;
                }
                result.append(line).append("\n");
            }

            return new SshResult(session.getExitStatus(), result.toString());
        } finally {
            if(session != null){
                session.close();
            }
            if(br != null){
                br.close();
            }
            if(stdout != null){
                stdout.close();
            }
            if (stderr != null) {
                stderr.close();
            }
        }
    }

    /**
     * 复制文件到远程机器。
     * API: https://javadoc.io/static/com.trilead/trilead-ssh2/1.0.0-build222/com/trilead/ssh2/SCPClient.html
     *
     * @param conn ssh 连接
     * @param localFile 本地文件路径
     * @param remoteTargetDirectory 远程目录
     */
    public static void scpTo(Connection conn, String localFile, String remoteTargetDirectory) throws IOException {
        Objects.requireNonNull(conn, "Ssh 连接 conn 不能为空");
        SCPClient scpClient = conn.createSCPClient();
        scpClient.put(localFile, remoteTargetDirectory, "0644");
    }

    /**
     * Ssh 执行结果
     */
    public static class SshResult {
        public SshResult(int code, String content) {
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

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
