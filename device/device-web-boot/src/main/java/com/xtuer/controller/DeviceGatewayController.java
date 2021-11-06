package com.xtuer.controller;

import com.xtuer.bean.DeviceGateway;
import com.xtuer.bean.Result;
import com.xtuer.bean.Urls;
import com.xtuer.ws.WsMessageProcessor;
import com.xtuer.ws.WsMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设备网关的控制器
 */
@RestController
public class DeviceGatewayController extends BaseController {
    @Autowired
    private WsMessageService msgService;

    @Autowired
    private WsMessageProcessor msgProcessor;

    /**
     * 获取当前连接的所有设备网关
     *
     * 网址: http://localhost:8080/api/gateways
     * 参数: connected [可选]: 为 true 时表示当前连接的网关，为 false 表示所有的网关
     *
     * @param connected 是否和服务器连接上
     * @return payload 为 Message 对象
     */
    @GetMapping(Urls.API_GATEWAYS)
    public Result<List<DeviceGateway>> findGateways(@RequestParam(required = false) boolean connected) {
        return Result.ok(msgService.findActiveGateways());
    }

    /**
     * 给设备网关发送消息
     *
     * 网址: http://localhost:8080/api/gateways/messages
     * 参数: message (必要): JSON 格式的消息内容
     *
     * @param message 消息内容
     */
    @PostMapping(Urls.API_GATEWAYS_MESSAGES)
    public Result<Boolean> sendMessageToGateway(@RequestBody String message) {
        msgProcessor.processMessage(message, null);
        return Result.ok();
    }
}
