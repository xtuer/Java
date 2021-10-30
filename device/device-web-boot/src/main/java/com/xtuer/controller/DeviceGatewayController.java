package com.xtuer.controller;

import com.xtuer.bean.Result;
import com.xtuer.bean.Urls;
import com.xtuer.ws.WsMessage;
import com.xtuer.ws.WsMessageUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备网关的控制器
 */
@RestController
public class DeviceGatewayController extends BaseController {
    /**
     * 获取当前连接的所有设备网关
     *
     * 网址: http://localhost:8080/api/gateways
     * 参数: 无
     *
     * @return payload 为 WsMessage 对象
     */
    @GetMapping(Urls.API_GATEWAYS)
    public Result<WsMessage> getAllGateways() {
        return Result.ok(WsMessageUtils.createGatewaysMessage());
    }
}
