package dsc.webterminal;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import misc.LocalCommander;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.ImmutableMap.of;
import static org.apache.commons.text.StringSubstitutor.replace;

/**
 * Web Terminal 启动服务。
 */
@Slf4j
@Data
public class WebTerminalService {
    /**
     * 检查端口是否被使用的命令。
     */
    private static final String CMD_PORT_CHECK = "netstat -an | grep -i listen | grep ${port} | wc -l";

    /**
     * 使用 Gotty 启动  cmd 指定的进程。
     * 例如: nohup /home/xtuer/gotty -w sqlplus mmm/mmm@//192.168.1.50:1521/orcl > /dev/null 2>&1 &
     */
    private static final String CMD_GOTTY_START = "nohup ${gotty} -w --port ${port} --ws-origin '.*' --once ${cmd} > /dev/null 2>&1 &";

    /**
     * WebTerminal 使用的最小端口。
     */
    private int minPort = 30000;

    /**
     * WebTerminal 使用的最大端口。
     */
    private int maxPort = 30010;

    /**
     * 最后启动 WebTerminal 使用的端口。
     */
    private int lastPort = minPort - 1;

    /**
     * Gotty 的路径。
     */
    private String gottyPath = "./gotty";

    /**
     * 启动 WebTerminal。
     *
     * @param cmd Gotty 启动的程序。
     * @return 返回当前启动的 WebTerminal 的端口。
     */
    public int startWebTerminal(String cmd) throws Exception {
        /*
         逻辑:
         1. 生成端口。
         2. 检查端口是否可用。
         3. 使用得到的端口开启 Web Terminal。
         4. 使用端口检查进程是否启动成功。
         */

        // 先检查 gotty 是否存在。
        if (!Files.isExecutable(Paths.get(gottyPath))) {
            throw new RuntimeException("Gotty 不可执行: " + gottyPath);
        }

        // 最大可用端口数。
        final int maxPortCount = maxPort - minPort + 1;

        // 被使用的端口数。
        int usedPortCount = 0;

        int currentPort = lastPort;

        while (usedPortCount++ <= maxPortCount) {
            // [1] 生成端口。
            currentPort++;
            currentPort = currentPort > maxPort ? minPort : currentPort;

            // [2] 检查端口是否可用。
            log.debug("检查 WebTerminal 的端口是否被占用，端口 [{}]", currentPort);
            if (isPortUsed(currentPort, 1)) {
                log.debug("启动 WebTerminal 的端口已经被使用，端口 [{}]", currentPort);
                continue;
            }

            // [3] 使用得到的端口开启 Web Terminal。
            String startCmd = replace(CMD_GOTTY_START, of(
                    "gotty", gottyPath,
                    "port", currentPort,
                    "cmd", cmd));

            log.info("启动 WebTerminal，启动命令 [{}]", startCmd);

            LocalCommander.executeCommand(startCmd);

            // [4] 使用端口检查进程是否启动成功。
            log.info("检查 WebTerminal 是否启动成功，端口 [{}]", currentPort);
            if (isPortUsed(currentPort, 10)) {
                lastPort = currentPort;
                log.info("[成功]: 启动 WebTerminal 成功，端口为 [{}]", currentPort);
                return lastPort;
            }
        }

        throw new RuntimeException(String.format("启动 WebTerminal 失败，[%d] 个端口已被用完", maxPortCount));
    }

    /**
     * 检查端口是否被使用了，最多检查 times 次，如果期间 port 被使用了返回 true，检查次数结束端口仍然未被占用则返回 false。
     *
     * @param port 端口。
     * @param times 检查次数，每次检查之间间隔 1 秒。
     * @return 端口使用了返回 true，没有使用返回 false。
     * @throws Exception 执行命令时抛出异常。
     */
    public static boolean isPortUsed(int port, int times) throws Exception {
        for (int i = 0; i < times; i++) {
            String cmd = replace(CMD_PORT_CHECK, of("port", port));
            String numberResult = LocalCommander.executeCommand(cmd).trim();
            int count = Integer.parseInt(numberResult);

            if (count > 0) {
                return true;
            }

            TimeUnit.SECONDS.sleep(1);
        }

        return false;
    }

    // java -jar -Dpath=/root/gotty app.jar
    public static void main(String[] args) throws Exception {
        WebTerminalService starter = new WebTerminalService();

        String gottyPath = System.getProperty("path");
        if (StringUtils.hasText(gottyPath)) {
            starter.setGottyPath(gottyPath);
        }

        int port = starter.startWebTerminal("bash");
        System.out.println(port);
    }
}
