package com.xtuer.controller;

import com.xtuer.bean.AgentStats;
import com.xtuer.bean.Result;
import com.xtuer.mapper.AgentManageMapper;
import com.xtuer.util.Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Agent 管理的控制器。
 */
@RestController
public class AgentManageController {
    @Autowired
    private AgentManageMapper mapper;

    /**
     * 接收 agent 状态上报。
     *
     * 链接: http://localhost:8080/api/agents/statsInPartition
     * 参数: 无
     * 请求体: AgentStats 对象序列化为 Json 的字符串。
     *
     * @param stats Agent 状态。
     */
    @PostMapping("/api/agents/statsInPartition")
    public Result<Boolean> receiveAgentStats(@RequestBody AgentStats stats) {
        mapper.upsertAgent(stats);
        mapper.insertAgentStats(stats.getAgentAddr(), Utils.toJson(stats));

        return Result.ok();
    }

    /**
     * 查询 agents。
     *
     * 链接: http://localhost:8080/api/agents
     * 参数: 无
     *
     * @return payload 为 agent 数组。
     */
    @GetMapping("/api/agents")
    public Result<List<AgentStats>> findAgents() {
        return Result.ok(mapper.findAgents());
    }

    /**
     * 查询传入 agentAddr 的 agent 的最新状态。
     *
     * 链接: http://localhost:8080/api/agents/{agentAddr}/stats
     * 参数: 无
     *
     * @return payload 为 agent 状态对象。
     */
    @GetMapping("/api/agents/{agentAddr}/stats")
    public Result<AgentStats> findLatestAgentStatusJsonByAgentAddr(@PathVariable String agentAddr) {
        String json = mapper.findLatestAgentStatusJsonByAgentAddr(agentAddr);

        if (StringUtils.isBlank(json)) {
            return Result.fail();
        }

        AgentStats stats = Utils.fromJson(json, AgentStats.class);
        return Result.ok(stats);
    }
}
