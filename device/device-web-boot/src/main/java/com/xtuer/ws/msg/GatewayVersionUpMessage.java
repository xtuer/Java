package com.xtuer.ws.msg;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 上行网关版本消息
 */
@Getter
@Setter
@Accessors(chain = true)
public class GatewayVersionUpMessage extends Message {
    /**
     * 网关软件版本信息
     */
    private String version;

    public GatewayVersionUpMessage() {
        super.setType(MessageType.GATEWAY_VERSION_UP);
    }
}
