package com.xtuer.ws.msg;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class EchoMessage extends Message {
    private String content;

    public EchoMessage() {
        super.setType(MessageType.ECHO);
    }
}
