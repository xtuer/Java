package com.xtuer.ws;

import com.google.common.base.Preconditions;
import com.xtuer.bean.DeviceGateway;
import com.xtuer.util.Utils;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;

import java.util.LinkedList;
import java.util.List;

/**
 * 创建消息的工具类
 */
public final class WsMessageUtils {
    /**
     * 创建 Echo 消息
     */
    public static WsMessage createEchoMessage(String content) {
        return new WsMessage().setType(WsMessageType.ECHO).setContent(content);
    }

    /**
     * 创建不支持的消息
     *
     * @return 返回消息对象
     */
    public static WsMessage createUnsupportedMessage() {
        return createErrorMessage("不支持的消息格式");
    }

    /**
     * 创建错误消息
     *
     * @param  error 错误信息
     * @return 返回消息对象
     */
    public static WsMessage createErrorMessage(String error) {
        WsMessage msg = new WsMessage();
        msg.setContent(error);
        msg.setType(WsMessageType.ERROR);

        return msg;
    }

    /**
     * 踢掉用户的消息
     *
     * @return 返回消息对象
     */
    public static WsMessage createKickOutMessage() {
        WsMessage message = new WsMessage();
        message.setType(WsMessageType.KICK_AWAY);

        return message;
    }

    /**
     * 获取所有连接的设备网关的消息
     *
     * @return 返回设备网关的消息对象
     */
    public static WsMessage createGatewaysMessage() {
        Preconditions.checkNotNull(WsServer.tioConfig, "服务器 TioConfig 未初始化");

        List<DeviceGateway> gateways = new LinkedList<>();

        for (ChannelContext ctx : Tio.getAll(WsServer.tioConfig).getObj()) {
            DeviceGateway g = (DeviceGateway) ctx.getAttribute(Const.KEY_GATEWAY);
            gateways.add(g);
        }

        WsMessage message = new WsMessage();
        message.setType(WsMessageType.GATEWAYS);
        message.setContent(Utils.toJson(gateways, false));

        return message;
    }

    /**
     * 创建总共有多少连接数量的消息
     *
     * @return 返回连接数的消息对象
     */
    public static WsMessage createConnectionCountMessage() {
        Preconditions.checkNotNull(WsServer.tioConfig, "服务器 TioConfig 未初始化");

        int count = Tio.getAll(WsServer.tioConfig).getObj().size();

        WsMessage message = new WsMessage();
        message.setType(WsMessageType.CONNECTION_COUNT);
        message.setContent(count + "");

        return message;
    }
}
