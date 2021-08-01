/**
 * 销售订单的 DAO
 */
export default class SalesOrderDao {
    // 处理时间
    static handleDate(salesOrder) {
        salesOrder.agreementDate = dayjs(salesOrder.agreementDate).toDate();
        salesOrder.deliveryDate = dayjs(salesOrder.deliveryDate).toDate();
        salesOrder.returnDate = dayjs(salesOrder.returnDate).toDate();
    }

    /**
     * 查询指定 ID 的销售订单
     *
     * 网址: http://localhost:8080/api/sales/salesOrders/{salesOrderId}
     * 参数: 无
     *
     * @param salesOrderId 销售订单 ID
     * @return {Promise} 返回 Promise 对象，resolve 的参数为销售订单，reject 的参数为错误信息
     */
    static findSalesOrder(salesOrderId) {
        return Rest.get(Urls.API_SALES_ORDERS_BY_ID, { params: { salesOrderId } }).then(({ data: order, success, message }) => {
            SalesOrderDao.handleDate(order);

            return Utils.response(order, success, message);
        });
    }

    /**
     * 查询符合条件的销售订单
     *
     * 网址: http://localhost:8080/api/sales/salesOrders
     * 参数:
     *      customerName   [可选]: 客户
     *      business       [可选]: 行业
     *      topic          [可选]: 主题
     *      searchType     [可选]: 搜索类型: 0 (所有订单)、1 (应收款订单)、2 (本月已收款订单)、3 (本年已收款订单)
     *      paidAtStart    [可选]: 开始支付时间: 搜索类型为 2 或者 3 时使用
     *      paidAtEnd      [可选]: 结束支付时间: 搜索类型为 2 或者 3 时使用
     *      agreementStart [可选]: 开始签约时间: 搜索类型非 2 或者 3 时使用
     *      agreementEnd   [可选]: 结束签约时间: 搜索类型非 2 或者 3 时使用
     *      pageNumber     [可选]: 页码
     *      pageSize       [可选]: 数量
     *
     * @param {JSON} filter
     * @return {Promise} 返回 Promise 对象，resolve 的参数为销售订单数组，reject 的参数为错误信息
     */
    static findSalesOrders(filter) {
        return Rest.get(Urls.API_SALES_ORDERS, { data: filter }).then(({ data: orders, success, message }) => {
            for (let order of orders) {
                SalesOrderDao.handleDate(order);
            }

            return Utils.response(orders, success, message);
        });
    }

    /**
     * 导出销售订单
     *
     * 网址: http://localhost:8080/api/sales/salesOrders/export
     * 参数: 参考 findSalesOrders
     *
     * @param {JSON} filter 过滤条件
     * @return {Promise} 返回 Promise 对象，resolve 的参数为导出的 Excel 的 URL，reject 的参数为错误信息
     */
    static exportSalesOrders(filter) {
        return Rest.get(Urls.API_SALES_ORDERS_EXPORT, { data: filter }).then(({ data: url, success, message }) => {
            return Utils.response(url, success, message);
        });
    }

    /**
     * 导出支付信息的销售订单
     *
     * 网址: http://localhost:8080/api/sales/salesOrders/export-payment
     * 参数: 参考 findSalesOrders
     *
     * @param {JSON} filter 过滤条件
     * @return {Promise} 返回 Promise 对象，resolve 的参数为导出的 Excel 的 URL，reject 的参数为错误信息
     */
    static exportSalesOrdersForPayment(filter) {
        return Rest.get(Urls.API_SALES_ORDERS_EXPORT_PAY, { data: filter }).then(({ data: url, success, message }) => {
            return Utils.response(url, success, message);
        });
    }

    /**
     * 更新或者插入销售订单
     *
     * 网址: http://localhost:8080/api/sales/salesOrders/{salesOrderId}
     * 参数: 无
     * 请求体: 参数 SalesOrder 的属性
     *
     * @param {JSON} salesOrder 销售订单
     * @param {Int}  saveType   保存类型: 0 (临时保存)、1 (提交生成生产订单)
     * @return {Promise} 返回 Promise 对象，resolve 的参数为销售订单，reject 的参数为错误信息
     */
    static upsertSalesOrder(salesOrder, saveType) {
        return Rest.update(
            Urls.API_SALES_ORDERS_BY_ID + '?saveType=' + saveType,
            { params: { salesOrderId: salesOrder.salesOrderId }, data: salesOrder, json: true }
        ).then(({ data: newOrder, success, message }) => {
            return Utils.response(newOrder, success, message);
        });
    }

    /**
     * 订单收款
     *
     * 网址: http://localhost:8080/api/sales/salesOrders/{salesOrderId}/payments
     * 参数: paidAmount: 收款金额
     *
     * @param {Long} salesOrderId 销售订单 ID
     * @param {Double} paidAmount 收款金额
     * @param {Int}    paidType   收款类型
     * @param {Date}   paidAt     收款时间
     * @return {Promise} 返回 Promise 对象，resolve 的参数为订单状态，reject 的参数为错误信息
     */
    static pay(salesOrderId, paidAmount, paidType, paidAt) {
        return Rest.update(
            Urls.API_SALES_ORDERS_PAYMENTS,
            { params: { salesOrderId }, data: { paidAmount, paidType, paidAt } }
        ).then(({ data: state, success, message }) => {
            return Utils.response(state, success, message);
        });
    }

    /**
     * 完成订单
     *
     * 网址: http://localhost:8080/api/sales/salesOrders/{salesOrderId}/complete
     * 参数: 无
     *
     * @param {Long} salesOrderId 销售订单 ID
     * @return {Promise} 返回 Promise 对象，resolve 的参数为无，reject 的参数为错误信息
     */
    static completeSalesOrder(salesOrderId) {
        return Rest.update(Urls.API_SALES_ORDERS_COMPLETE, { params: { salesOrderId } }).then(({ success, message }) => {
            return Utils.response(null, success, message);
        });
    }

    /**
     * 查询客户的财务信息: 累计订单金额、累计应收款、累计已收款
     *
     * 网址: http://localhost:8080/api/sales/customers/{customerId}/finance
     * 参数: 无
     *
     * @param {Long} customerId 客户 ID
     * @return {Promise} 返回 Promise 对象，resolve 的参数为客户的财务信息，reject 的参数为错误信息
     */
    static findCustomerFinance(customerId) {
        return Rest.get(Urls.API_SALES_CUSTOMERS_FINANCE, { params: { customerId } }).then(({ data: finance, success, message }) => {
            return Utils.response(finance, success, message);
        });
    }
}
