import com.trilead.ssh2.Connection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import util.SshUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static util.SshUtils.SshResult;
import static util.SshUtils.runCommand;

/**
 * 测试 ssh 远程执行命令
 */
public class SshTest {
    private static Connection conn;

    @BeforeClass
    public static void setup() throws IOException {
        conn = SshUtils.sshConnect("192.168.1.164", 22, "foo", "*#Passw0rd#");
    }

    @AfterClass
    public static void clean() {
       conn.close();
    }

    /**
     * 简单执行命令
     */
    @Test
    public void testHello() throws IOException {
        SshResult result = runCommand(conn, "echo $HOME");
        dumpResult(result);
    }

    @Test
    public void testOkAndErrorCmd() throws IOException {
        SshResult result = runCommand(conn, "salt '192.168.11.21' cmd.run 'sh /dmp/scripts-temp/2022-09-05/DB_MYSQL_start_stop.sh-2981533508231937652.sh  -HOST 192.168.11.21 -USER sys_admin -PASSWORD manager -PORT 3306 -INSPATH /opt -MODE stop' --out=json");
        dumpResult(result);
    }

    /**
     * 执行 Saltstack Ping 命令
     */
    @Test
    public void testSaltPing() throws IOException {
        String cmd = "salt '192.168.12.101' test.ping --out=json";
        SshResult result = runCommand(conn, cmd);
        dumpResult(result);
    }

    /**
     * 执行 Saltstack 成功命令
     */
    @Test
    public void testSaltOkCmd() throws IOException {
        String cmd = "salt '192.168.12.102' cmd.run 'ls /root' --out=json";
        SshResult result = runCommand(conn, cmd);
        dumpResult(result);
    }

    /**
     * 执行 Saltstack 失败命令
     */
    @Test
    public void testSaltFailCmd1() throws IOException {
        String cmd = "salt '192.168.12.102' cmd.run 'ls /root/xxx/xxx' --out=json";
        SshResult result = runCommand(conn, cmd);
        dumpResult(result);
    }

    /**
     * 执行 Saltstack 失败命令
     */
    @Test
    public void testSaltFailCmd2() throws IOException {
        String cmd = "salt '192.168.12.102' cmd.run 'monaco' --out=json";
        SshResult result = runCommand(conn, cmd);
        dumpResult(result);
    }

    /**
     * 执行 Saltstack 复制文件成功，文件存在
     */
    @Test
    public void testSaltCopyFileOk1() throws IOException {
        String cmd = "salt-cp '192.168.12.102' -C '/etc/hosts' '/root/' --out=json";
        SshResult result = runCommand(conn, cmd);
        dumpResult(result);
    }

    /**
     * 执行 Saltstack 复制文件成功，Minion 上文件夹不存在会自动创建
     */
    @Test
    public void testSaltCopyFileOk2() throws IOException {
        String cmd = "salt-cp '192.168.12.102' -C '/etc/hosts' '/root/x/y/z/' --out=json";
        SshResult result = runCommand(conn, cmd);
        dumpResult(result);
    }

    /**
     * 执行 Saltstack 复制文件失败，Master 上文件不存在
     */
    @Test
    public void testSaltCopyFileFail1() throws IOException {
        String cmd = "salt-cp '192.168.12.102' -C '/etc/hosts2' '/root/' --out=json";
        SshResult result = runCommand(conn, cmd);
        dumpResult(result);
    }

    /**
     * 执行 Saltstack 复制文件成功，Minion 上 z 是文件夹，把
     */
    @Test
    public void testSaltCopyFileFail2() throws IOException {
        // 注意: z 因为是文件夹，salt-cp 则命令中则认为是文件，所以复制失败，但是 return code 为 0，这个情况比较特性
        String cmd = "salt-cp '192.168.12.102' -C '/etc/hosts' '/root/x/y/z' --out=json";
        SshResult result = runCommand(conn, cmd);
        dumpResult(result);
    }

    /**
     * 测试复制大文件: salt-cp 复制大文件很慢，102M 耗时 +100S
     */
    @Test
    public void testCopyLargeFileTooSlow() throws IOException {
        long start = System.currentTimeMillis();
        String cmd = "salt-cp '192.168.12.102' -C '/usr/lib/locale/locale-archive' '/root/' --out=json";
        SshResult result = runCommand(conn, cmd);
        dumpResult(result);

        long duration = (System.currentTimeMillis() - start) / 1000;
        System.out.println("执行耗时: " + duration + "秒");
    }

    /**
     * 测试复制大文件：cp.get_file 速度很快，102M 耗时 4S，1.4G 耗时 38G
     */
    @Test
    public void testCopyLargeFileFast() throws IOException {
        long start = System.currentTimeMillis();
        String cmd = "salt '192.168.12.102' cp.get_file 'salt://a.txt' '/root/a/b/c/' makedirs=True --out=json";
        SshResult result = runCommand(conn, cmd);
        dumpResult(result);

        long duration = (System.currentTimeMillis() - start) / 1000;
        System.out.println("执行耗时: " + duration + "秒");
    }

    /**
     * 测试复制文件夹: cp.get_dir
     */
    @Test
    public void testCopyDir() throws IOException {
        long start = System.currentTimeMillis();
        String cmd = "salt '192.168.12.102' cp.get_dir 'salt://sub' '/root/a/b/c/' makedirs=True --out=json";
        SshResult result = runCommand(conn, cmd);
        dumpResult(result);

        long duration = (System.currentTimeMillis() - start) / 1000;
        System.out.println("执行耗时: " + duration + "秒");
    }

    /**
     * 测试 Web 服务器使用 Echo 输出文件到 Salt Master，有 Bug，应该使用 scp 复制文件
     */
    @Test
    public void testEchoShToSaltMaster() throws IOException {
        // DMP Ansible 使用的复制脚本方案："echo '" + scriptContent + "' > " + tmppath + separator + scriptName;
        // 注意：如果脚本文件中有单引号 ' 时导致错误复制脚本失败
        // 报错例子: echo 'hello ' world'
        String script = new String(Files.readAllBytes(Paths.get("/Users/biao/Documents/temp/test-2.sh")), StandardCharsets.UTF_8);
        String cmd = "echo '" + script + "' > /root/test-1.sh";
        SshResult result = runCommand(conn, cmd);
        dumpResult(result);
    }

    /**
     * 测试 Web 服务器使用 Scp 复制文件到 Salt Master
     */
    @Test
    public void testScpShToSaltMaster() throws IOException {
        String script = "/Users/biao/Documents/temp/test-1.sh";
        SshUtils.scpTo(conn, script, "/root/");
    }

    /**
     * 测试从 Salt Master 获取脚本处理好后再复制回去
     */
    @Test
    public void testHandleShScript() throws IOException {
        // [1] 获取脚本
        String cmd = "cat /root/a.sh";
        SshResult result = runCommand(conn, cmd);
        System.out.println("脚本内容:\n" + result.getContent());

        // [2] 处理脚本 (例如解密脚本)，内容保存到临时脚本里
        String scriptContent = result.getContent() + "\n" + System.currentTimeMillis() + "\n";
        Path shPath = Files.createTempFile("temp-script-", ".sh");
        Files.write(shPath, scriptContent.getBytes(StandardCharsets.UTF_8));
        System.out.println("生成临时脚本: " + shPath);

        // [3] 复制脚本到 Salt master
        SshUtils.scpTo(conn, shPath.toString(), "/root/");
    }

    /**
     * 测试非 root 用户执行 salt 命令。
     * 此用户已经添加到 publisher_acl 中。
     */
    @Test
    public void testSaltViaUserFoo() throws IOException {
        Connection conn = SshUtils.sshConnect("192.168.1.164", 22, "foo", "Passw0rd");
        String cmd = "salt '192.168.12.102' cmd.run 'whoami' --out=json"; // 输出 root
        SshResult result = runCommand(conn, cmd);
        dumpResult(result);

        conn.close();
    }

    /**
     * 测试确保远程机器上的目录存在
     */
    @Test
    public void testMakeSureDirectory() throws IOException {
        Connection conn = SshUtils.sshConnect("192.168.1.164", 22, "foo", "Passw0rd");
        SshUtils.makeSureDirectory(conn, "/srv/salt/base/scripts-temp/2022-06-21");
    }

    private void dumpResult(SshResult result) {
        System.out.printf("Code: %d, Result: \n%s", result.getCode(), result.getContent());
    }
}
