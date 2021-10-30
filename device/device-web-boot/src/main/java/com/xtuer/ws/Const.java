package com.xtuer.ws;

public interface Const {
	/**
	 * 用于群聊的 group id
	 */
	String GROUP_ID = "device-group";

	/**
	 * 网关的 key，用于把用户绑定到 channelContext 上
	 */
	String KEY_GATEWAY = "ws_gateway";

	/**
	 * 重复登录时被踢掉标志的 key
	 */
	String KEY_KICK_AWAY = "ws_kick_away";
}
