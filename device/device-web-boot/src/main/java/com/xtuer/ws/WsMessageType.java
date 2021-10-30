package com.xtuer.ws;

/**
 * 消息类型
 */
public enum WsMessageType {
    ECHO,             // Echo 消息
    GATEWAYS,         // 获取所有设备网关消息
    HEARTBEAT,        // 心跳消息
    ERROR,            // 错误消息
    CONNECTION_COUNT, // 查询连接数
    KICK_AWAY,        // 被踢掉的消息

    METRICS,          // 设备监控数据
    CMD_CONFIG,       // 配置命令
}
