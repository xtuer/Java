package com.xtuer.ws.msg;

import com.xtuer.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 每种消息都有自己的类型 type，大多数消息都有网关 ID 和设备 ID
 */
@Getter
@Setter
@Accessors(chain = true)
public class Message {
    private String gatewayId; // 网关 ID
    private String deviceId;  // 设备 ID
    private MessageType type; // 消息类型

    /**
     * 返回消息的 JSON 字符串
     * @return JSON 字符串
     */
    public String toJson() {
        return Utils.toJson(this, false);
    }
}
