package com.xtuer.ws.msg;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 通用消息
 */
@Getter
@Setter
@Accessors(chain = true)
public class CommonMessage extends Message {
    /**
     * 消息内容
     */
    private String content;
}
