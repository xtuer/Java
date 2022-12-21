import misc.SshHelper;
import misc.SshHelper.SshResult;
import misc.auto.ansible.AnsibleConfig;
import misc.auto.ansible.AnsibleRunner;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 数据准备: echo "noops-file" > /root/test-file.txt; mkdir -p /root/test-dir/x/y && echo "noops-dir" > /root/test-dir/x/y/nemo.txt
 */
public class AnsibleTest {
    private static AnsibleRunner ansible;

    private static final String NODE_IP_1 = "192.168.12.101";
    private static final String NODE_IP_2 = "192.168.12.102";
    private static final String ANSIBLE_IP = "192.168.1.158";

    @BeforeClass
    public static void setup() {
        AnsibleConfig config = new AnsibleConfig();
        config.setAnsibleIp(ANSIBLE_IP);
        config.setAnsibleSshUsername("root");
        config.setAnsibleSshPassword("Newdt@cn");
        config.setAnsibleSshPort(22);
        config.setAnsibleAutoBase("/etc/ansible/auto-base");
        ansible = new AnsibleRunner(config);
    }

    /**
     * 测试执行脚本。
     */
    @Test
    public void testExecuteScript() throws Exception {
        SshResult result = ansible.executeScript(NODE_IP_1, "x.sh", "-username=Alice -password=P@ssw0rd");
        dumpResult(result);
    }

    /**
     * 测试从 Ansible 复制文件到 Node。
     */
    @Test
    public void testCopyFileFromAnsibleToNode() throws Exception {
        SshResult result = ansible.copyFile(null, NODE_IP_1, "/root/test-file.txt", "/root/a/b/c");
        dumpResult(result);
    }

    /**
     * 测试从 Ansible 复制大文件到 Node。
     *
     * 查找大文件的命令: find . -size +100M
     */
    @Test
    public void testCopyBigFileFromAnsibleToNode() throws Exception {
        SshResult result = ansible.copyFile(null, NODE_IP_1, "/root/test/dmp-release-8.1.1-mysql-20220310.tar.gz", "/root/a/b/c");
        dumpResult(result);
    }

    /**
     * 测试从 Ansible 复制目录到 Node。
     */
    @Test
    public void testCopyDirFromAnsibleToNode() throws Exception {
        SshResult result = ansible.copyDir(null, NODE_IP_1, "/root/test-dir", "/root/a/b/c");
        dumpResult(result);
    }

    /**
     * 测试从 Node 复制文件到 Ansible。
     */
    @Test
    public void testCopyFileFromNodeToAnsible() throws Exception {
        // 已测: 复制大文件也支持。
        SshResult result = ansible.copyFile(NODE_IP_1, null, "/root/test-file.txt", "/root/a/b/c");
        dumpResult(result);
    }

    /**
     * 测试从 Node 复制目录到 Ansible。
     */
    @Test
    public void testCopyDirFromNodeToAnsible() throws Exception {
        // 已测: 复制大文件也支持。
        SshResult result = ansible.copyDir(NODE_IP_1, null, "/root/test-dir", "/root/a/b/c");
        dumpResult(result);
    }

    /**
     * 测试从 Node 复制文件到 Node。
     */
    @Test
    public void testCopyFileFromNodeToNode() throws Exception {
        SshResult result = ansible.copyFile(NODE_IP_1, NODE_IP_2, "/root/test-file.txt", "/root/a/b/c");
        dumpResult(result);
    }

    /**
     * 测试从 Node 复制目录到 Node。
     */
    @Test
    public void testCopyDirFromNodeToNode() throws Exception {
        SshResult result = ansible.copyDir(NODE_IP_1, NODE_IP_2, "/root/test-dir", "/root/a/b/c");
        dumpResult(result);
    }

    /**
     * 测试从 Node 复制目录到 Ansible。
     */
    @Test
    public void testCopyDirFromNodeToNodeAsAnsible() throws Exception {
        SshResult result = ansible.copyDir(NODE_IP_1, ANSIBLE_IP, "/root/test-dir", "/root/a/b/c");
        dumpResult(result);
    }

    /**
     * 测试生成 Ansible inventory 文件。
     */
    @Test
    public void testCreateInventory() throws Exception {
       ansible.buildInventory();
    }

    /**
     * 打印出结果。
     */
    private void dumpResult(SshHelper.SshResult result) {
        System.out.printf("Code: %d, Result: \n%s\n", result.getCode(), result.getContent());
    }
}
