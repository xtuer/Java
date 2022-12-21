package misc.auto.agent;

import lombok.extern.slf4j.Slf4j;

/**
 * 使用 Agent 执行 shell 脚本、复制文件等。
 */
@Slf4j
public class AgentRunner {
    /**
     * 在 Agent 所在目标主机 nodeIp 上执行脚本。
     * 脚本的多个参数拼成一个字符串，参数以键值对的形式出现，每队参数以 - 开头，例如 '-username "Alice" -password "P@ssw0rd"' (不包含单引号)。
     * 脚本的执行命令如 '/root/foo.sh -username "Alice" -password "P@ssw0rd" (不包含单引号)。
     *
     * @param nodeIp 执行脚本主机的 IP
     * @param scriptName 脚本名称
     * @param args 脚本参数
     * @return 返回执行结果
     */
    public AgentJob executeScript(String nodeIp, String scriptName, String args) throws Exception {
        /*
         执行逻辑:
         1. 通过脚本名获取脚本内容。
         2. 执行脚本。
         */
        return null;
    }
}
