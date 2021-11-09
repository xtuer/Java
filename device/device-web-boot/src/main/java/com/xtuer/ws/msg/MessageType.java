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

    STATUS_DOWN,          // 下行状态请求消息 (Has class)
    STATUS_UP,            // 上行状态上报消息 (Has class)
    GATEWAY_RESET_DOWN,   // 下行复位网关消息
    GATEWAY_VERSION_DOWN, // 下行获取网关版本消息
    GATEWAY_VERSION_UP,   // 上行网关版本消息 (Has class)
    DEVICE_SEARCH_DOWN,   // 下行设备入网搜索请求消息
    DEVICE_RESET_DOWN,    // 下行复位设备消息
}
