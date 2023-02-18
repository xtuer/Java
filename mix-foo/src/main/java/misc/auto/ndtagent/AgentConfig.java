package misc.auto.ndtagent;

import lombok.Data;

/**
 * Agent 的配置，主要有 Agent 的端口。
 */
@Data
public class AgentConfig {
    /**
     * Agent 运行的端口。
     */
    private int agentPort;
}
