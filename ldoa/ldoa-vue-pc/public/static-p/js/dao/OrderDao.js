/**
 * 订单 Dao
 */
export default class {
    /**
     * 查询指定 ID 的订单
     *
     * 网址: http://localhost:8080/api/orders/{orderId}
     * 参数: 无
     *
     * @param {Long} orderId 订单 ID
     * @return {Promise} 返回 Promise 对象，resolve 的参数为订单，reject 的参数为错误信息
     */
    static findOrderById(orderId) {
        return Rest.get(Urls.API_ORDERS_BY_ID, { params: { orderId } }).then(({ data: order, success, message }) => {
            if (success) {
                order.orderDate = Utils.stringToDate(order.orderDate);
                order.deliveryDate = Utils.stringToDate(order.deliveryDate);
                return Promise.resolve(order);
            } else {
                Message.error(message);
                return Promise.reject(message);
            }
        });
    }

    /**
     * 查询符合条件的订单
     *
     * 网址: http://localhost:8080/api/orders
     * 参数: filter 的属性包括:
     *      orderSn      [可选]: 订单编号
     *      productCodes [可选]: 产品编号
     *      state        [可选]: 状态
     *      pageNumber   [可选]: 页码
     *      pageSize     [可选]: 数量
     *      notInStockRequest [可选]: 是否在出库请求中有记录
     *
     * @param filter 过滤器
     * @param page   分页对象
     * @return {Promise} 返回 Promise 对象，resolve 的参数为订单的数组，reject 的参数为错误信息
     */
    static findOrders(filter) {
        return Rest.get(Urls.API_ORDERS, { data: filter }).then(({ data: orders, success, message }) => {
            return Utils.response(orders, success, message);
        });
    }

    /**
     * 插入或者更新订单
     *
     * 网址: http://localhost:8080/api/orders/{orderId}
     * 参数: 无
     * 请求体: 为订单的 JSON 字符串
     *      orderId         (必要): 订单 ID，为 0 时创建订单，非 0 时更新订单
     *      customerCompany (必要): 客户单位
     *      customerContact (必要): 客户联系人
     *      customerAddress (必要): 客户收件地址
     *      orderDate       (必要): 订单日期
     *      deliveryDate    (必要): 交货日期
     *      calibrated      [可选]: 是否校准
     *      calibrationInfo [可选]: 校准信息
     *      requirement     [可选]: 要求
     *      attachmentId    [可选]: 附件 ID
     *      items           (必要): 订单项
     *
     * @param {JSON} order 订单
     * @return {Promise} 返回 Promise 对象，resolve 的参数为更新后的订单，reject 的参数为错误信息
     */
    static upsertOrder(order) {
        return Rest.update(Urls.API_ORDERS_BY_ID, { params: { orderId: order.orderId }, data: order, json: true })
            .then(({ data: newOrder, success, message }) => {
                return Utils.response(newOrder, success, message);
            });
    }

    /**
     * 完成订单
     *
     * 网址: http://localhost:8080/api/orders/{orderId}/complete
     * 参数: 无
     *
     * @param {Long} orderId 订单 ID
     * @return {Promise} 返回 Promise 对象，resolve 的参数为无，reject 的参数为错误信息
     */
    static completeOrder(orderId) {
        return Rest.update(Urls.API_ORDERS_COMPLETE, { params: { orderId } }).then(({ success, message }) => {
            return Utils.response(null, success, message);
        });
    }
}
