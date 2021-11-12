package com.xtuer.ws;

import com.google.common.base.Preconditions;
import com.xtuer.bean.DeviceGateway;
import com.xtuer.ws.msg.Message;
import com.xtuer.ws.msg.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.http.common.HttpRequest;
import org.tio.websocket.common.WsResponse;

import java.util.LinkedList;
import java.util.List;

/**
 * 群消服务:
 *     加入小组
 *     离开小组
 *     离开所有小组
 *     获取小组成员
 *     获取小组历史消息
 *     获取私有聊天历史消息
 *     发送小组消息调用 sendToGroup()
 *     发送私人消息调用 sendToUser()
 *
 * 提示: IP:Port 的连接对应一个 ChannelContext 对象
 */
@Service
@Slf4j
public class WsMessageService {
    /**
     * 设备网关连接进来, 验证绑定设备网关
     * 网址: ws://im.xtuer.com:9321?gatewayId=1&gatewayName=bob
     *
     * @param request 请求 HttpRequest 对象
     * @param channelContext ChannelContext 对象
     * @return 连接成功返回 true, 否则返回 false
     */
    public boolean login(HttpRequest request, ChannelContext channelContext) {
        // 1. 获取设备网关信息
        // 2. 如果 userId 或者 username 为空则返回 false, 不允许建立连接
        // 3. 如果 userId 已经绑定过其他的 channelContext, 则踢掉前一个, 同一个 userId 不允许重复重复登录
        // 4. 绑定设备网关和 channelContext: 在 channelContext 中存储设备网关对象

        // [1] 获取设备网关信息
        String gatewayId   = StringUtils.trim(request.getParam("gatewayId"));
        String gatewayName = StringUtils.trim(request.getParam("gatewayName"));
        DeviceGateway gateway = new DeviceGateway().setId(gatewayId).setName(gatewayName);

        // [2] 如果 gatewayId 或者 gatewayName 为空则返回 false, 不允许建立连接
        if (StringUtils.isBlank(gatewayId) || StringUtils.isBlank(gatewayName)) {
            log.warn("[错误] gatewayId 或 gatewayName 不能为空: {}", channelContext.getClientNode());
            return false;
        }

        // [3] 如果 gatewayId 已经绑定过其他的 channelContext, 则踢掉前一个, 同一个 gatewayId 不允许重复重复登录
        ChannelContext previousChannelContext = Tio.getByBsId(channelContext.tioConfig, gatewayId);
        if (!channelContext.equals(previousChannelContext) && previousChannelContext != null) {
            log.info("[重复登录] 踢掉 [{}: {}] 已经登录的连接 {}", gatewayName, gatewayId, previousChannelContext.getClientNode());

            // 给用设备网关送一条他被踢掉的消息: 阻塞消息
            sendToGateway(gatewayId, MessageUtils.createKickOutMessage(), previousChannelContext, true);

            // 踢掉设备网关
            previousChannelContext.setAttribute(Const.KEY_KICK_AWAY, true); // 踢掉的标志
            Tio.unbindBsId(previousChannelContext);
            Tio.remove(previousChannelContext, "服务器断开客户端连接");
        }

        // [4] 绑定设备网关和 channelContext: 在 channelContext 中存储设备网关对象,
        //     以便使用 Tio.sendToBsId(groupContext, gatewayId, response) 给指定 ID 的设备网关发送信息
        //     Tio.bindBsId() 内部会调用 channelContext.setBsId(userid), 其他地方可以使用 channelContext.getBsId() 获取设备网关 ID
        bindGateway(gateway, channelContext);

        return true;
    }

    /**
     * 设备网关断开连接离开
     *
     * @param channelContext ChannelContext 对象
     */
    public void logout(ChannelContext channelContext) {
        // 1. 如果 isKickAway() 为 true, 则说明是被重复登录踢掉的, 不需要发送离开消息, 也不需要重复解绑
        // 2. 离开所有小组
        // 3. 与 channelContext 解绑

        // [1] 如果 isKickOut() 为 true, 则说明是被重复登录踢掉的, 不需要发送离开消息, 也不需要重复解绑
        if (isKickAway(channelContext)) {
            return;
        }

        // [2] 离开所有小组
        // DeviceGateway gateway = getGateway(channelContext);
        // user.getGroups().forEach(groupName -> {
        //     this.leaveGroup(user.getId(), groupName, channelContext);
        // });

        // [3] 与 channelContext 解绑
        Tio.unbindBsId(channelContext);
    }

    /**
     * 绑定设备网关到 channelContext, channelContext 存储设备网关对象
     *
     * @param gateway 设备网关
     * @param channelContext ChannelContext 对象
     */
    private void bindGateway(DeviceGateway gateway, ChannelContext channelContext) {
        channelContext.setAttribute(Const.KEY_GATEWAY, gateway);
        Tio.bindBsId(channelContext, gateway.getId()); // BS ID 和 ChannelContext 是一对一的
    }

    /**
     * 解绑设备网关
     *
     * @param gateway 设备网关
     * @param channelContext ChannelContext 对象
     */
    private void unbindGateway(DeviceGateway gateway, ChannelContext channelContext) {
        channelContext.removeAttribute(Const.KEY_GATEWAY);
        Tio.unbindBsId(channelContext);
    }

    /**
     * 获取 channelContext 绑定的设备网关
     *
     * @param channelContext ChannelContext 对象
     * @return 返回设备网关对象
     */
    public DeviceGateway getGateway(ChannelContext channelContext) {
        return ((DeviceGateway) channelContext.getAttribute(Const.KEY_GATEWAY));
    }

    /**
     * 判断 channelContext 是否被踢掉线的
     *
     * @param channelContext ChannelContext 对象
     * @return 被踢掉线返回 true, 否则返回 false
     */
    public boolean isKickAway(ChannelContext channelContext) {
        return BooleanUtils.toBoolean((Boolean) channelContext.getAttribute(Const.KEY_KICK_AWAY));
    }

    /**
     * 给网关发送消息
     *
     * @param gatewayId 设备网关 ID
     * @param message 消息
     */
    public void sendToGateway(String gatewayId, Message message) {
        sendToGateway(gatewayId, message.toJson());
    }

    /**
     * 给网关发送消息
     *
     * @param gatewayId 设备网关 ID
     * @param message   JSON 格式的消息
     */
    public void sendToGateway(String gatewayId, String message) {
        ChannelContext ctx = Tio.getByBsId(WsServer.tioConfig, gatewayId);

        if (ctx != null) {
            WsResponse response = WsResponse.fromText(message, WsServerConfig.CHARSET);
            Tio.sendToBsId(WsServer.tioConfig, gatewayId, response);
        } else {
            log.warn("[注意] 不能发送消息给设备网关，设备网关不存在或者未连接: {}", gatewayId);
            throw new RuntimeException("设备网关不存在或者未连接: " + gatewayId);
        }
    }

    /**
     * 给指定设备网关发送消息，以异步的方式发送
     *
     * @param gatewayId 设备网关 ID
     * @param message 消息
     */
    public void sendToGateway(String gatewayId, Message message, ChannelContext channelContext) {
        sendToGateway(gatewayId, message, channelContext, false);
    }

    /**
     * 给指定设备网关发送消息，isBlock 为 true 时以阻塞的方式发送，为 false 以异步的方式发送
     *
     * @param gatewayId  设备网关 ID
     * @param message 消息
     * @param isBlock 是否阻塞
     */
    public void sendToGateway(String gatewayId, Message message, ChannelContext channelContext, boolean isBlock) {
        WsResponse response = WsResponse.fromText(message.toJson(), WsServerConfig.CHARSET);

        if (isBlock) {
            Tio.bSendToBsId(channelContext.tioConfig, gatewayId, response);
        } else {
            Tio.sendToBsId(channelContext.tioConfig, gatewayId, response);
        }
    }

    /**
     * 获取所有连接到服务器的设备网关
     *
     * @return 返回设备网关的数组
     */
    public List<DeviceGateway> findActiveGateways() {
        Preconditions.checkNotNull(WsServer.tioConfig, "服务器 TioConfig 未初始化");

        List<DeviceGateway> gateways = new LinkedList<>();

        for (ChannelContext ctx : Tio.getAll(WsServer.tioConfig).getObj()) {
            DeviceGateway g = (DeviceGateway) ctx.getAttribute(Const.KEY_GATEWAY);
            gateways.add(g);
        }

        return gateways;
    }
}
