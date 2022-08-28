import org.junit.Test;
import util.JschService;

/**
 * 测试 Ansible 耗时任务
 */
public class AnsibleLongTaskTest {
    private static final String HOST = "192.168.1.158";
    private static final String USER = "root";
    private static final String PASS = "Newdt@cn";

    @Test
    public void testAsyncTask() throws Exception {
        JschService jschService = new JschService(HOST, USER, PASS);
        String res = jschService.executeCommand("ansible 192.168.12.102 -f 5 -T 10 -m shell -B 7200 -P 15 -a 'sh /root/long-task.sh 100'");
        System.out.println(res);
        jschService.close();
    }
}
