import org.junit.Test;
import util.JschService;

import java.nio.file.Path;
import java.nio.file.Paths;

public class JschServiceTest {
    private static final String HOST = "192.168.12.101";
    private static final String USER = "root";
    private static final String PASS = "Newdt@cn";

    @Test
    public void testExecuteCommandSuccess() throws Exception {
        JschService jschService = new JschService(HOST, USER, PASS);
        String res = jschService.executeCommand("cat /root/SaltGUI-master/README.md");
        System.out.println(res);
        jschService.close();
    }

    @Test
    public void testExecuteCommandFailure() throws Exception {
        JschService jschService = new JschService(HOST, USER, PASS);
        String res = jschService.executeCommand("ls /root/xyz/b/c");
        System.out.println(res);
        jschService.close();
    }

    @Test
    public void testSftpPutFile() throws Exception {
        try (JschService jschService = new JschService(HOST, USER, PASS)) {
        jschService.sftpPut("/Users/biao/Downloads/test.js", "~/foo/bar/");
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
