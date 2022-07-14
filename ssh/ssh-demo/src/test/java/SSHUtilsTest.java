import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import cn.newdt.autop.ansible.SSHUtils;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SSHUtilsTest {
    /**
     * 测试执行命令成功。
     */
    @Test
    public void testExecuteCommandSuccess() throws Exception {
        SSHUtils sshUtils = new SSHUtils();
        Connection conn = sshUtils.SSHConnect();
        String result = sshUtils.runCommand(conn, "cat /root/test.js", false, null);
        System.out.println(result);

        System.out.println("-----------------------------");
        result = sshUtils.runCommand(conn, "ls ~", false, null);
        System.out.println(result);

        conn.close();
    }

    /**
     * 测试执行命令失败 (抛出异常)。
     */
    @Test
    public void testExecuteCommandFailure() throws Exception {
        SSHUtils sshUtils = new SSHUtils();
        Connection conn = sshUtils.SSHConnect();
        String result = sshUtils.runCommand(conn, "ls /root/xyz/b/c", false, null);
        System.out.println(result);

        conn.close();
    }

    /**
     * 测试上传单个文件。
     */
    @Test
    public void testScpClientSingleFile() throws Exception {
        SSHUtils sshUtils = new SSHUtils();
        Connection conn = sshUtils.SSHConnect();
        SCPClient scp = conn.createSCPClient();
        scp.put("/Users/biao/Downloads/test.js", "/root/temp");

        conn.close();
    }

    /**
     * 测试上传多个文件。
     */
    @Test
    public void testScpClientMultiFiles() throws Exception {
        SSHUtils sshUtils = new SSHUtils();
        Connection conn = sshUtils.SSHConnect();
        SCPClient scp = conn.createSCPClient();
        scp.put(new String[]{"/Users/biao/Downloads/test.js", "/Users/biao/Downloads/PAT.ppt"}, "/root/temp");

        conn.close();
    }

    /**
     * 测试使用流上传文件 (sqlexe 里上传 MultipartFile 使用)。
     */
    @Test
    public void testScpClientViaStream() throws Exception {
        SSHUtils sshUtils = new SSHUtils();
        Connection conn = sshUtils.SSHConnect();
        SCPClient scp = conn.createSCPClient();
        InputStream in = Files.newInputStream(Paths.get("/Users/biao/Downloads/test.js"));
        scp.put(in, "/root/temp/x.js");

        conn.close();
    }

    /**
     * 测试使用 ssh private key 进行登录并执行命令。
     */
    @Test
    public void testSshKey() throws Exception {
        Connection conn = new Connection("192.168.1.70", 22);
        conn.connect();
        // 使用 pem ssh key 进行登录
        boolean isAuthenticated = conn.authenticateWithPublicKey("root", new File("/Users/biao/.ssh/id_rsa-pem"), "Newdt@cn");
        System.out.println(isAuthenticated);

        Session session = conn.openSession();
        session.execCommand("ls /root");
        System.out.println(session.getOkResult());

        conn.close();
    }
}
