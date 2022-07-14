import util.JschService;

import java.util.concurrent.TimeUnit;

/**
 * 测试连接是否泄漏
 */
public class ConnectionCountTest {
    private static final String IP   = "192.168.12.101";
    private static final String USER = "root";
    private static final String PASS = "Newdt@cn";
    private static final int    LEN  = 1000;

    public static void main(String[] args) throws Exception {
        // 1. Channel 不关闭，关闭 Session
        // 2. Channel 关闭，Session 不关闭
        testChannelDoesNotClose();
    }

    public static void testChannelDoesNotClose() throws Exception {
        JschService jschService = new JschService(IP, USER, PASS);

        for (int i = 0; i < LEN; i++) {
            String res = jschService.executeCommand("cat /root/SaltGUI-master/README.md");
            System.out.println(res);
            System.out.println("-------------------> " + i);
        }

        TimeUnit.SECONDS.sleep(50);
        jschService.close();
    }
}
