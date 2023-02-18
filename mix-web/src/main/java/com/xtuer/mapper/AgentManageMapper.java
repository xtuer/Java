package com.xtuer.mapper;

import com.xtuer.bean.AgentStats;

import java.util.List;

/**
 * Agent 管理的 Mapper。
 *
 * 提示: agent 对象和 agent stats 对象都使用 AgentStats 的对象表示。
 */
public interface AgentManageMapper {
    /**
     * 插入或者更新 agent。
     *
     * @param agent Agent 对象。
     */
    void upsertAgent(AgentStats agent);

    /**
     * 插入 agent 的状态。
     *
     * @param agentAddr Agent 的访问地址，Ip:Port。
     * @param json Agent 状态对象序列化的 Json 字符串。
     */
    void insertAgentStats(String agentAddr, String json);

    /**
     * 查询满足条件的 agents。
     *
     * @return 返回 agent 数组。
     */
    List<AgentStats> findAgents();

    /**
     * 查询传入 agentAddr 的 agent 状态。
     *
     * @param agentAddr Agent 的访问地址。
     * @return 返回 agent 状态对象的 Json 字符串。
     */
    String findAgentStatusJsonByAgentAddr(String agentAddr);
}
