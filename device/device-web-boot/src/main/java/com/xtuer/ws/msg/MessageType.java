package com.xtuer.ws.msg;

/**
 * 消息类型
 */
public enum MessageType {
    ECHO,             // Echo 消息
    GATEWAYS,         // 获取所有设备网关消息
    HEARTBEAT,        // 心跳消息
    ERROR,            // 错误消息
    KICK_AWAY,        // 被踢掉的消息
    CONNECTION_COUNT, // 查询连接数

    METRICS,          // 设备监控数据
    CMD_CONFIG,       // 配置命令

    HEARTBEAT_UP,     // 上行心跳消息
    HEARTBEAT_DOWN,   // 下行心跳消息
}
