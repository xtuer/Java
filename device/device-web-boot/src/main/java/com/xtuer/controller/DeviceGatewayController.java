package com.xtuer.controller;

import com.xtuer.bean.Result;
import com.xtuer.bean.Urls;
import com.xtuer.ws.WsMessage;
import com.xtuer.ws.WsMessageService;
import com.xtuer.ws.WsMessageType;
import com.xtuer.ws.WsMessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 设备网关的控制器
 */
@RestController
public class DeviceGatewayController extends BaseController {
    @Autowired
    private WsMessageService msgService;

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

    /**
     * 给设备网关发送消息
     *
     * 网址: http://localhost:8080/api/gateways/{gatewayId}/messages
     * 参数: message (必要): 消息内容
     *
     * @param gatewayId 设备网关 ID
     * @param content   消息内容
     */
    @PostMapping(Urls.API_GATEWAYS_MESSAGES)
    public Result<Boolean> sendMessageToGateway(@PathVariable String gatewayId, @RequestParam String content) {
        WsMessage msg = new WsMessage().setContent(content).setType(WsMessageType.CMD_CONFIG);
        boolean ret = msgService.sendToGateway(gatewayId, msg);

        if (ret) {
            return Result.ok();
        } else {
            return Result.fail("设备网关 [{}] 不存在或者未连接", gatewayId);
        }
    }
}
