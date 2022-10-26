import misc.SshHelper;
import misc.SshHelper.SshResult;
import org.junit.Test;

public class SshHelperTest {
    private static final String HOST     = "192.168.12.101";
    private static final String USERNAME = "foo";
    private static final String PASSWORD = "*#Passw0rd#";
    private static final int    PORT     = 22;

    @Test
    public void testSimpleCommand() throws Exception {
        try (SshHelper ssh = new SshHelper(HOST, USERNAME, PASSWORD, PORT)) {
            SshResult result = ssh.executeCommand("echo $HOME");
            dumpResult(result);
        }
    }

    /**
     * 打印出结果
     */
    private void dumpResult(SshResult result) {
        System.out.printf("Code: %d, Result: \n%s\n", result.getCode(), result.getContent());
    }
}
