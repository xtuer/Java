import misc.SaltStackConfig;
import misc.SaltStackRunner;
import misc.SshHelper;
import static misc.SshHelper.SshResult;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 测试内容:
 * X. 执行脚本
 * A. Master -> Minion: File and Dir
 * B. Minion -> Master: File and Dir
 * C. Minion -> Minion: File and Dir
 */
public class SaltStackTest {
    private static SaltStackRunner salt;

    private static final String MINION_IP_1 = "192.168.1.51";
    private static final String MINION_IP_2 = "192.168.12.102";

    // 准备数据: mkdir -p /dmp && chmod 777 /dmp && mkdir -p /root/test && echo 'hostname' > /root/test/get_hostname.sh

    private static final String MINION_DIR         = "/root/test";
    private static final String MINION_FILE_BIG    = "/data/dmp-release-8.1.1-mysql-20220310.tar.gz"; // 1.2G, 在 192.168.1.51 上
    private static final String MINION_FILE_LITTLE = "/root/test/get_hostname.sh";
    private static final String MASTER_SALT_FILE   = "/srv/salt/base/x.sls";
    private static final String MASTER_SALT_DIR    = "/srv/salt/base/scripts-encrypted";
    private static final String DEST_DIR           = "/dmp/2022-08"; // 保存复制文件的目录, Master+Minion 上都要创建好，目录 /dmp 权限为 777

    @BeforeClass
    public static void setup() {
        SaltStackConfig config = new SaltStackConfig();
        config.setMasterIp("192.168.12.101");
        config.setMasterSshUsername("foo");
        config.setMasterSshPassword("Passw0rd");
        salt = new SaltStackRunner(config);
    }

    /**
     * 测试执行 Salt Master 上的脚本
     */
    @Test
    public void testExecuteSh() throws Exception {
        SshResult result = salt.executeScript(MINION_IP_1, "x.sh", "-A 阿布 -B Bob -C Carry");
        dumpResult(result);
    }

    /**
     * 测试复制文件: Master -> Minion
     */
    @Test
    public void testCopyFileFromMasterToMinion() throws Exception {
        SshResult result = salt.copyFile(null, MINION_IP_1, MASTER_SALT_FILE, DEST_DIR);
        dumpResult(result);
    }

    /**
     * 测试复制目录: Master -> Minion
     */
    @Test
    public void testCopyDirFromMasterToMinion() throws Exception {
        SshResult result = salt.copyDir(null, MINION_IP_1, MASTER_SALT_DIR, DEST_DIR);
        dumpResult(result);
    }

    /**
     * 测试复制文件: Minion -> Master
     */
    @Test
    public void testCopyFileFromMinionToMaster() throws Exception {
        SshResult result = salt.copyFile(MINION_IP_1, null, MINION_FILE_LITTLE, DEST_DIR);
        dumpResult(result);
    }

    /**
     * 测试复制文件: Minion -> Master 大文件
     */
    @Test
    public void testCopyFileFromMinionToMasterBigFile() throws Exception {
        SshResult result = salt.copyFile(MINION_IP_1, null, MINION_FILE_BIG, DEST_DIR);
        dumpResult(result);
    }

    /**
     * 测试复制目录: Minion -> Master
     */
    @Test
    public void testCopyDirFromMinionToMaster() throws Exception {
        SshResult result = salt.copyDir(MINION_IP_1, null, MINION_DIR, DEST_DIR);
        dumpResult(result);
    }

    /**
     * 测试复制文件: Minion -> Minion
     */
    @Test
    public void testCopyFileFromMinionToMinion() throws Exception {
        SshResult result = salt.copyFile(MINION_IP_1, MINION_IP_2, MINION_FILE_LITTLE, DEST_DIR);
        dumpResult(result);
    }

    /**
     * 测试复制文件: Minion -> Minion 大文件
     */
    @Test
    public void testCopyFileFromMinionToMinionBigFile() throws Exception {
        SshResult result = salt.copyFile(MINION_IP_1, MINION_IP_2, MINION_FILE_BIG, DEST_DIR);
        dumpResult(result);
    }

    /**
     * 测试复制目录: Minion -> Minion
     */
    @Test
    public void testCopyDirFromMinionToMinion() throws Exception {
        SshResult result = salt.copyDir(MINION_IP_1, MINION_IP_2, MINION_DIR, DEST_DIR);
        dumpResult(result);
    }

    /**
     * 打印出结果
     */
    private void dumpResult(SshHelper.SshResult result) {
        System.out.printf("Code: %d, Result: \n%s\n", result.getCode(), result.getContent());
    }
}
