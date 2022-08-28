import com.trilead.ssh2.Connection;
import lombok.extern.slf4j.Slf4j;
import util.SshUtils;

@Slf4j
public class AnsibleLongTaskWithTrileadTest {
    public static void main(String[] args) throws Exception {
        String cmd = "ansible 192.168.12.102 -f 5 -T 10 -m shell -B 7300 -P 15 -a 'sh /root/long-task.sh 9000'";
        Connection conn = SshUtils.sshConnect("192.168.1.158", 22, "root", "Newdt@cn");
        SshUtils.SshResult result = SshUtils.runCommand(conn, cmd);

        log.info("Return code [{}], Content:\n{}", result.getCode(), result.getContent());

        conn.close();
    }
}
