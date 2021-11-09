package com.xtuer.ws.msg;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 下行状态请求消息
 */
@Getter
@Setter
@Accessors(chain = true)
public class StatusDownMessage extends Message {
    /**
     * 设备通信地址，广播地址 0xFFFF 或者单播地址
     */
    private int address;

    public StatusDownMessage() {
        super.setType(MessageType.STATUS_DOWN);
    }
}
