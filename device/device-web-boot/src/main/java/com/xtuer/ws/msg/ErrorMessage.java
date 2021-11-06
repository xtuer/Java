package com.xtuer.ws.msg;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 错误消息
 */
@Getter
@Setter
@Accessors(chain = true)
public class ErrorMessage extends Message {
    /**
     * 错误信息
     */
    private String error;

    public ErrorMessage() {
        super.setType(MessageType.ERROR);
    }
}
