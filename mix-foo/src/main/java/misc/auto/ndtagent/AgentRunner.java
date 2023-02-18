package misc.auto.ndtagent;

import lombok.extern.slf4j.Slf4j;

/**
 * 使用 Agent 执行 shell 脚本、复制文件等。
 */
@Slf4j
public class AgentRunner {
    private final AgentConfig config;

    public AgentRunner(AgentConfig config) {
        this.config = config;

        log.debug("Agent 配置: {}", config.toString());
    }

    /**
     * 在 Agent 所在目标主机上执行脚本。
     * 脚本的多个参数拼成一个字符串，参数以键值对的形式出现，每队参数以 - 开头，例如 '-username "Alice" -password "P@ssw0rd"' (不包含单引号)。
     * 脚本的执行命令如 '/root/foo.sh -username "Alice" -password "P@ssw0rd"' (不包含单引号)。
     *
     * @param nodeIp 执行脚本主机的 IP
     * @param scriptName 脚本名称
     * @param args 脚本参数
     * @return 返回执行结果
     */
    public AgentJob executeScript(String nodeIp, String scriptName, String args) {
        /*
         执行逻辑:
         1. 通过脚本名获取脚本内容。
         2. 在 Agent 所在目标主机上执行脚本。
         */
        // [1] 通过脚本名获取脚本内容。
        log.info("获取脚本: {}", scriptName);
        String scriptContent = "for ((i=0;i<20;i++));do echo $i; sleep 1; done; echo $@";

        // [2] 在 Agent 所在目标主机上执行脚本。
        log.info("执行脚本: 在 Agent 所在目标主机上执行脚本，IP:Port [{}:{}]，脚本 [{}]", nodeIp, config.getAgentPort(), scriptName);
        AgentJob job = AgentRunnerHelper.executeScript(nodeIp, config.getAgentPort(), scriptName, scriptContent, args, AgentJob.SCRIPT_TYPE_SHELL, true);

        log.info("脚本结果:\n{}", job);

        return job;
    }

    /**
     * 简单执行 shell 脚本。
     *
     * @param nodeIp 执行脚本主机的 IP
     * @param scriptName 脚本名称
     * @param scriptContent 脚本内容
     * @param args 脚本参数
     * @return 返回执行结果
     */
    public AgentJob executeShellScriptDirectly(String nodeIp, String scriptName, String scriptContent, String args) {
        return AgentRunnerHelper.executeScript(nodeIp, config.getAgentPort(), scriptName, scriptContent, args, AgentJob.SCRIPT_TYPE_SHELL, false);
    }
}
