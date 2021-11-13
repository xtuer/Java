package com.xtuer.ws.msg;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 下行心跳消息
 */
@Getter
@Setter
@Accessors(chain = true)
public class HeartbeatUpMessage extends Message {
    /**
     * 设备通道类型
     */
    private String chanType;

    /**
     * 设备通信地址
     */
    private int address;

    /**
     * 设备电压
     */
    private Float voltage;

    /**
     * 设备的类型: pro3-x/pro6-x/单孔PCR/单孔核酸/快速PCR/双通道冻干/真空度计…
     */
    private int deviceType;

    /**
     * 设备工作状态: idle/work/real
     */
    private String status;

    public HeartbeatUpMessage() {
        super.setType(MessageType.HEARTBEAT_UP);
    }
}
