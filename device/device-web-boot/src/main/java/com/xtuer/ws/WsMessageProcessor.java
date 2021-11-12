package com.xtuer.ws;

import com.xtuer.util.Utils;
import com.xtuer.ws.msg.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;

/**
 * 消息处理器
 */
@Component
@Slf4j
public class WsMessageProcessor {
    @Autowired
    private WsMessageService msgService;

    /**
     * 处理消息
     *
     * @param text 消息的原始字符串
     * @param channelContext 通道 context，为 null 表示通过业务代码调用，非 null 为 WS 直接调用
     * @return 返回消息处理结果
     */
    public Object processMessage(String text, ChannelContext channelContext) {
        // 逻辑:
        // 1. 转换消息为 Message 对象，
        // 2. 如果转换出错则消息格式不对，return 错误信息告知发送者
        // 3. 处理心跳消息
        // 4. 根据消息的类型分别进行处理:
        //    获取设备网关
        //    获取服务器当前的连接数
        //    不支持的消息
        //    原样返回的 Echo 消息

        // [1] 转换消息为 Message 对象
        Message message = Utils.fromJson(text, Message.class);

        // [2] 如果转换出错则消息格式不对，return 错误信息告知发送者
        if (message == null || message.getType() == null) {
            if (log.isDebugEnabled()) {
                log.debug("[错误] 消息不支持: {}", text);
            }

            if (channelContext == null) {
                throw new RuntimeException("消息不支持");
            } else {
                return MessageUtils.createUnsupportedMessage().toJson();
            }
        }

        // [3] 提前处理心跳消息, 提高效率
        if (MessageType.HEARTBEAT.equals(message.getType())) {
            return null; // 不需要处理, 心跳消息只是为了告知服务器客户端连接仍然是活跃的
        }

        // 打印收到的消息
        if (log.isDebugEnabled()) {
            log.debug("[消息] 收到消息:\n{}", text);
        }

        // [4] 根据消息的类型分别进行处理
        try {
            switch (message.getType()) {
                case GATEWAYS:
                    return MessageUtils.createGatewaysMessage().toJson();
                case CONNECTION_COUNT:
                    return MessageUtils.createConnectionCountMessage().toJson();
                case ECHO:
                    return text;

                // 设备上报的消息
                case STATUS_UP:
                    StatusUpMessage statusUpMsg = Utils.fromJson(text, StatusUpMessage.class);
                    log.info("收到设备状态上报消息:\n{}", statusUpMsg.toJson());
                    return null;
                case GATEWAY_VERSION_UP:
                    GatewayVersionUpMessage gvUpMsg = Utils.fromJson(text, GatewayVersionUpMessage.class);
                    log.info("收到设备状态上报消息:\n{}", gvUpMsg.toJson());
                    return null;

                // 转发消息到设备网关
                case STATUS_DOWN:
                case GATEWAY_RESET_DOWN:
                case GATEWAY_VERSION_DOWN:
                case DEVICE_SEARCH_DOWN:
                case DEVICE_RESET_DOWN:
                    log.info("[操作] 转发消息给设备网关: 消息类型 [{}]，网关 [{}], 消息:\n{}", message.getType(), message.getGatewayId(), text);
                    msgService.sendToGateway(message.getGatewayId(), text);
                    return null;
                default:
                    return null;
            }
        } catch (RuntimeException ex) {
            // 业务调用 processMessage 发生的异常，再次抛出给调用者
            if (channelContext == null) {
                throw ex;
            } else {
                return null;
            }
        }
    }
}
