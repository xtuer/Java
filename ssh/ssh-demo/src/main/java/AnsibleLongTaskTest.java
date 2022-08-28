import lombok.extern.slf4j.Slf4j;
import util.JschService;

@Slf4j
public class AnsibleLongTaskTest {
    private static final String HOST = "192.168.1.158";
    private static final String USER = "root";
    private static final String PASS = "Newdt@cn";

    public static void main(String[] args) {
        try (JschService jschService = new JschService(HOST, USER, PASS)) {
            String res = jschService.executeCommand("ansible 192.168.12.102 -f 5 -T 10 -m shell -B 7200 -P 15 -a 'sh /root/long-task.sh 9000'");
            log.info("任务结果: {}", res);
        } catch (Exception ex) {
            log.warn("异常: {}", ex.getMessage());
        }
    }
}
