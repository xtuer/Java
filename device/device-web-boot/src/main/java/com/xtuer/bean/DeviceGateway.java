package com.xtuer.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 设备网关
 */
@Getter
@Setter
@Accessors(chain = true)
public class DeviceGateway {
    private String name;
    private String id;
}
