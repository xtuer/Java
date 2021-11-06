package com.xtuer.ws.msg;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ErrorMessage extends Message {
    private String error;

    public ErrorMessage() {
        super.setType(MessageType.ERROR);
    }
}
