import org.junit.Test;
import util.JschUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class JschUtilsTest {
    private static final String IP = "192.168.12.101";
    private static final String USER = "root";
    private static final String PASS = "Newdt@cn";

    @Test
    public void testExecuteCommandSuccess() throws Exception {
        JschUtils jschUtils = new JschUtils(IP, USER, PASS);
        String res = jschUtils.runCommand("cat /root/SaltGUI-master/README.md");
        System.out.println(res);
        jschUtils.close();
    }

    @Test
    public void testExecuteCommandFailure() throws Exception {
        JschUtils jschUtils = new JschUtils(IP, USER, PASS);
        String res = jschUtils.runCommand("ls /root/xyz/b/c");
        System.out.println(res);
        jschUtils.close();
    }

    @Test
    public void testSftpPutFile() throws Exception {
        try (JschUtils jschUtils = new JschUtils(IP, USER, PASS)) {
        jschUtils.sftpPut("/Users/biao/Downloads/test.js", "~/foo/bar/");
        }
    }

    @Test
    public void testPath() throws Exception {
        Path path = Paths.get("/root/foo/bar/");

        while (path != null) {
            System.out.println(path);
            path = path.getParent();
        }
    }
}
