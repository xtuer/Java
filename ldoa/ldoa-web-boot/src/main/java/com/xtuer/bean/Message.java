package com.xtuer.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 消息
 */
@Getter
@Setter
public class Message {
    /**
     * 消息 ID
     */
    private long messageId;

    /**
     * 发送者 ID
     */
    private long senderId;

    /**
     * 接收者 ID
     */
    private long receiverId;

    /**
     * 目标 ID
     */
    private long targetId;

    /**
     * 消息类型
     */
    private Type type;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 是否已读
     */
    private boolean read;

    /**
     * 创建时间
     */
    private Date createdAt;

    public Message() {

    }

    public Message(long messageId, long senderId, long receiverId, long targetId, Type type, String content) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.targetId = targetId;
        this.type = type;
        this.content = content;
    }

    /**
     * 消息类型的枚举
     */
    public enum Type {
        CREATE_ORDER("创建生产订单"),
        UPDATE_ORDER("更新生产订单");

        private final String label;

        Type(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
