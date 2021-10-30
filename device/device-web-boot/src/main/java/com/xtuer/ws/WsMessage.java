package com.xtuer.ws;

import com.xtuer.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 每种消息都有自己的类型 type，如果此类型不够用，那么需要根据业务场景，使用 JSON 格式的消息内容 content，在 content 中再定义需要的类型。
 */
@Getter
@Setter
@Accessors(chain = true)
public final class WsMessage {
    private String from;        // 消息发送者, 一般为消息发送者 ID
    private String fromName;    // 消息发送者的名字
    private String to;          // 消息接收者, 例如私聊信息的目标 ID, 小组消息时为小组名字
    private String content;     // 消息内容
    private WsMessageType type; // 消息类型
    private Date   createdAt = new Date(); // 消息创建时间

    /**
     * 返回消息的 JSON 字符串
     * @return JSON 字符串
     */
    public String toJson() {
        return Utils.toJson(this, false);
    }
}
