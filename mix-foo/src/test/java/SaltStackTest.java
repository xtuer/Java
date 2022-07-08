import misc.SaltStackConfig;
import misc.SaltStackRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import util.SshUtils;
import util.SshUtils.*;

import java.io.IOException;

public class SaltStackTest {
    private static SaltStackRunner saltRun;

    private static final String MINION_IP = "192.168.12.102";

    @BeforeClass
    public static void setup() {
        SaltStackConfig config = new SaltStackConfig();
        config.setSaltMasterIp("192.168.12.101");
        config.setSaltMasterUsername("foo");
        config.setSaltMasterPassword("Passw0rd");
        saltRun = new SaltStackRunner(config);
    }

    /**
     * 测试执行 Salt Master 上的脚本
     */
    @Test
    public void testExecuteSh() throws IOException {
        SshResult result = saltRun.executeScript(MINION_IP, "x.sh", "a=阿布 b=Bob c=Carry");
        dumpResult(result);
    }

    /**
     * 测试复制文件
     */
    @Test
    public void testTransferFile() throws IOException {
        SshResult result = saltRun.transferFileFromMasterToMinion(MINION_IP, "/srv/salt/base/scripts-encrypted/y.sh", "/dmp/temp");
        dumpResult(result);
    }

    /**
     * 测试复制文件夹
     */
    @Test
    public void testTransferDir() throws IOException {
        SshResult result = saltRun.transferDirFromMasterToMinion(MINION_IP, "/srv/salt/base/foo", "/dmp/files");
        dumpResult(result);
    }

    private void dumpResult(SshUtils.SshResult result) {
        System.out.printf("Code: %d, Result: \n%s\n", result.getCode(), result.getContent());
    }
}
