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
                order.returnDate = Utils.stringToDate(order.returnDate);
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
     *      productNames [可选]: 产品名称
     *      state        [可选]: 状态
     *      pageNumber   [可选]: 页码
     *      pageSize     [可选]: 数量
     *      notInStockRequest [可选]: 是否在出库请求中有记录
     *
     * @param {JSON} filter 过滤器
     * @return {Promise} 返回 Promise 对象，resolve 的参数为订单的数组，reject 的参数为错误信息
     */
    static findOrders(filter) {
        return Rest.get(Urls.API_ORDERS, { data: filter }).then(({ data: orders, success, message }) => {
            return Utils.response(orders, success, message);
        });
    }

    /**
     * 导出符合条件的订单
     *
     * 网址: http://localhost:8080/api/orders
     * 参数:
     *      orderSn      [可选]: 订单编号
     *      productCodes [可选]: 产品编号
     *      productNames [可选]: 产品名称
     *      state        [可选]: 状态
     *
     * @param {JSON} filter 过滤器
     * @return {Promise} 返回 Promise 对象，resolve 的参数为导出的 Excel URL，reject 的参数为错误信息
     */
    static exportOrders(filter) {
        return Rest.get(Urls.API_ORDERS_EXPORT, { data: filter }).then(({ data: url, success, message }) => {
            return Utils.response(url, success, message);
        });
    }

    /**
     * 插入或者更新订单
     *
     * 网址: http://localhost:8080/api/orders/{orderId}
     * 参数: 无
     * 请求体: 为订单的 JSON 字符串
     *      orderId         (必要): 订单 ID，为 0 时创建订单，非 0 时更新订单
     *      type            [可选]: 订单类型: 0 (生产订单)、1 (样品订单)
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

    /**
     * 更新订单的进度
     *
     * 网址: http://localhost:8080/api/orders/{orderId}/progress
     * 参数: value 进度
     *
     * @param {Long} orderId  订单 ID
     * @param {String} progress 进度
     * @return {Promise} 返回 Promise 对象，resolve 的参数为无，reject 的参数为错误信息
     */
    static updateProgress(orderId, progress) {
        return Rest.update(Urls.API_ORDERS_PROGRESS, {
            params: { orderId },
            data: { value: progress }
        }).then((success, message) => {
            return Utils.response(null, success, message);
        });
    }

    /**
     * 查询订单的产品
     *
     * 网址: http://localhost:8080/api/orders/{orderId}/products
     * 参数: 无
     *
     * @param {Long} orderId 订单 ID
     *  @return {Promise} 返回 Promise 对象，resolve 的参数为订单的参数数组，reject 的参数为错误信息
     */
    static findProductsByOrderId(orderId) {
        return Rest.get(Urls.API_ORDERS_PRODUCTS, { params: { orderId } }).then(({ data: products, success, message }) => {
            return Utils.response(products, success, message);
        });
    }
}
