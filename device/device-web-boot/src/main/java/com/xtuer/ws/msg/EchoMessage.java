package com.xtuer.ws.msg;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Echo 消息，原样返回的消息
 */
@Getter
@Setter
@Accessors(chain = true)
public class EchoMessage extends Message {
    /**
     * 消息内容
     */
    private String content;

    public EchoMessage() {
        super.setType(MessageType.ECHO);
    }
}
