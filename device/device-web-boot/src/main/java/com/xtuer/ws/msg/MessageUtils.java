package com.xtuer.ws.msg;

import com.google.common.base.Preconditions;
import com.xtuer.bean.DeviceGateway;
import com.xtuer.util.Utils;
import com.xtuer.ws.Const;
import com.xtuer.ws.WsServer;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;

import java.util.LinkedList;
import java.util.List;

/**
 * 创建消息的工具类
 */
public final class MessageUtils {
    /**
     * 创建 Echo 消息
     */
    public static Message createEchoMessage(String content) {
        return new EchoMessage().setContent(content);
    }

    /**
     * 创建不支持的消息
     *
     * @return 返回消息对象
     */
    public static Message createUnsupportedMessage() {
        return createErrorMessage("不支持的消息格式");
    }

    /**
     * 创建错误消息
     *
     * @param  error 错误信息
     * @return 返回消息对象
     */
    public static Message createErrorMessage(String error) {
        return new ErrorMessage().setError(error);
    }

    /**
     * 踢掉用户的消息
     *
     * @return 返回消息对象
     */
    public static Message createKickOutMessage() {
        return new Message().setType(MessageType.KICK_AWAY);
    }

    /**
     * 获取所有连接的设备网关的消息
     *
     * @return 返回设备网关的消息对象
     */
    public static Message createGatewaysMessage() {
        Preconditions.checkNotNull(WsServer.tioConfig, "服务器 TioConfig 未初始化");

        List<DeviceGateway> gateways = new LinkedList<>();

        for (ChannelContext ctx : Tio.getAll(WsServer.tioConfig).getObj()) {
            DeviceGateway g = (DeviceGateway) ctx.getAttribute(Const.KEY_GATEWAY);
            gateways.add(g);
        }

        CommonMessage message = new CommonMessage();
        message.setType(MessageType.GATEWAYS);
        message.setContent(Utils.toJson(gateways, false));

        return message;
    }

    /**
     * 创建总共有多少连接数量的消息
     *
     * @return 返回连接数的消息对象
     */
    public static Message createConnectionCountMessage() {
        Preconditions.checkNotNull(WsServer.tioConfig, "服务器 TioConfig 未初始化");

        int count = Tio.getAll(WsServer.tioConfig).getObj().size();

        CommonMessage message = new CommonMessage();
        message.setType(MessageType.CONNECTION_COUNT);
        message.setContent(count + "");

        return message;
    }
}
