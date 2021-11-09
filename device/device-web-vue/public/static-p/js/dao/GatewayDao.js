/**
 * 网关的 Dao
 */
export default class GatewayDao {
    /**
     * 获取当前连接的所有设备网关
     *
     * 网址: http://localhost:8080/api/gateways?connected=true
     * 参数: 无
     *
     * @return {Promise} 返回 Promise 对象，resolve 的参数为网关数组，reject 的参数为错误信息
     */
    static findActiveGateways() {
        return Rest.url(Urls.API_GATEWAYS).data({ connected: true }).get()
            .then(({ data: gateways, success, message }) => {
                return Utils.response(gateways, success, message);
            });
    }

    /**
     * 给设备网关发送消息
     *
     * 网址: http://localhost:8080/api/gateways/messages
     * 参数: message (必要): JSON 格式的消息内容
     *
     * @param {JSON} message 消息内容
     * @return {Promise} 返回 Promise 对象，resolve 的参数为无，reject 的参数为错误信息
     */
    static sendMessageToGateway(message) {
        return Rest.url(Urls.API_GATEWAYS_MESSAGES).data(message).json(true).create()
            .then(({ success, message: msg }) => {
                return Utils.response(null, success, msg);
            });
    }
}
