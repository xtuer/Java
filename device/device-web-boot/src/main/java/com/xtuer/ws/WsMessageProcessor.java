package com.xtuer.ws;

import com.xtuer.util.Utils;
import com.xtuer.ws.msg.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;

/**
 * 消息处理器
 */
@Component
@Slf4j
public class WsMessageProcessor {
    /**
     * 处理消息
     *
     * @param text 消息的原始字符串
     * @param channelContext ChannelContext 对象
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
        if (message == null) {
            if (log.isDebugEnabled()) {
                log.debug("[错误] 消息不支持: {}", text);
            }

            return MessageUtils.createUnsupportedMessage().toJson();
        }

        // [3] 提前处理心跳消息, 提高效率
        if (MessageType.HEARTBEAT.equals(message.getType())) {
            return null; // 不需要处理, 心跳消息只是为了告知服务器客户端连接仍然是活跃的
        }

        if (log.isDebugEnabled()) {
            log.debug("[消息] 收到消息:\n{}", text);
        }

        // [4] 根据消息的类型分别进行处理
        switch (message.getType()) {
            case GATEWAYS:
                return MessageUtils.createGatewaysMessage().toJson();
            case CONNECTION_COUNT:
                return MessageUtils.createConnectionCountMessage().toJson();
            case ECHO:
                return text;
            case HEARTBEAT_UP:
                HeartBeatUpMessage upMsg = Utils.fromJson(text, HeartBeatUpMessage.class);
                log.info("收到上行心跳消息:\n{}", upMsg.toJson());
                return null;
            case HEARTBEAT_DOWN:
                HeartBeatDownMessage downMsg = Utils.fromJson(text, HeartBeatDownMessage.class);
                log.info("收到下行心跳消息:\n{}", downMsg.toJson());
                return null;
            case METRICS:
                // 保存监控消息到数据库
                return null;
            default:
                return null;
        }
    }
}
