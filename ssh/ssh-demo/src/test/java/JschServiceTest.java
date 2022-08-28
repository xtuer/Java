import org.junit.Test;
import util.JschService;

import java.nio.file.Path;
import java.nio.file.Paths;

public class JschServiceTest {
    private static final String HOST = "192.168.12.102";
    private static final String USER = "root";
    private static final String PASS = "Newdt@cn";

    // private static final String HOST = "118.195.181.149";
    // private static final String USER = "root";
    // private static final String PASS = "*#Passw0rd#";

    @Test
    public void testRealtimeOutput() throws Exception {
        JschService jschService = new JschService(HOST, USER, PASS);
        String res = jschService.executeCommand("sh /root/long-task.sh 30");
        System.out.println(res);
        jschService.close();
    }

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
        String res = jschService.executeCommand("ls /root/z");
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
