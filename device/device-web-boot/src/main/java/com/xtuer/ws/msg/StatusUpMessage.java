package com.xtuer.ws.msg;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 上行状态上报消息
 */
@Getter
@Setter
@Accessors(chain = true)
public class StatusUpMessage extends Message {
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

    /**
     * 校准时间
     */
    private int calcTime;

    /**
     * 设备序列号
     */
    private int devSerial;

    /**
     * 本次采样数目
     */
    private int recordNum;

    /**
     * 高温采样数目
     */
    private int highTempNum;

    /**
     * 采样开始时间
     */
    private int startTime;

    /**
     * 采样结束时间
     */
    private int closeTime;

    /**
     * 当前设备时间
     */
    private int currentTime;

    /**
     * 采样间隔时间
     */
    private int interval;

    /**
     * 最小采样时间
     */
    private int miniTime;

    /**
     * 上电时间长度
     */
    private int powerOnTime;

    public StatusUpMessage() {
        super.setType(MessageType.STATUS_UP);
    }
}
