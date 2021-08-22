/**
 * 消息 Dao
 */
export default class MessageDao {
    /**
     * 查询指定接收者的消息
     *
     * 网址: http://localhost:8080/api/messages/receivers/{receiverId}/messages
     * 参数:
     *      pageNumber [可选]: 页码
     *      pageSize   [可选]: 数量
     *
     * @param {JSON} filter 过滤条件，属性有 receiverId, pageNumber, pageSize
     * @return {Promise} 返回 Promise 对象，resolve 的参数为消息数组，reject 的参数为错误信息
     */
    static findMessagesByReceiverId(filter) {
        return Rest.get(Urls.API_MESSAGES_OF_RECEIVER, {
            params: { receiverId: filter.receiverId },
            data: filter
        }).then(({ data: messages, success, message }) => {
            return Utils.response(messages, success, message);
        });
    }

    /**
     * 统计指定接收者的未读消息
     *
     * 网址: http://localhost:8080/api/messages/receivers/{receiverId}/messages/unreadCount
     * 参数: 无
     *
     * @param {Long} receiverId 接收者 ID
     * @return {Promise} 返回 Promise 对象，resolve 的参数为读消息数量，reject 的参数为错误信息
     */
    static countUnreadMessagesByReceiverId(receiverId) {
        return Rest.get(Urls.API_MESSAGES_UNREAD_COUNT_OF_RECEIVER, {
            params: { receiverId }
        }).then(({ data: count, success, message }) => {
            return Utils.response(count, success, message);
        });
    }

    /**
     * 标记消息为已读
     *
     * 网址: http://localhost:8080/api/messages/{messageId}/read
     * 参数: 无
     *
     * @param {Long} messageId 消息 ID
     * @return {Promise} 返回 Promise 对象，resolve 的参数为无，reject 的参数为错误信息
     */
    static markMessageRead(messageId) {
        return Rest.update(Urls.API_MESSAGES_AS_READ, {
            params: { messageId }
        }).then(({ success, message }) => {
            return Utils.response(true, success, message);
        });
    }
}
