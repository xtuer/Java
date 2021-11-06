package com.xtuer.ws.msg;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 上行心跳消息
 */
@Getter
@Setter
@Accessors(chain = true)
public class HeartBeatUpMessage extends Message {
    /**
     * 设备通道类型
     */
    private String chanType;

    /**
     * 设备通讯地址
     */
    private int address;

    /**
     * 设备电压
     */
    private float voltage;

    /**
     * 设备类型: pro3-x/pro6-x/单孔 PCR/单孔核酸/快速 PCR/双通道冻干/真空度计
     */
    private int deviceType;

    /**
     * 设备工作状态: idle/work/real
     */
    private String status;

    public HeartBeatUpMessage() {
        super.setType(MessageType.HEARTBEAT_UP);
    }
}
