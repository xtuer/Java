/**
 * 库存操作的 Dao
 */
export default class StockDao {
    /**
     * 查询库存操作记录
     *
     * 网址: http://localhost:8080/api/stocks/records?type=IN
     * 参数:
     *      type         (必要): IN (入库)、OUT (出库)
     *      name         [可选]: 名字
     *      code         [可选]: 编码
     *      batch        [可选]: 批次
     *      model        [可选]: 规格型号
     *      manufacturer [可选]: 厂家
     *      startAt      [可选]: 开始时间
     *      enAt         [可选]: 结束时间
     *      pageNumber   [可选]: 页码
     *      pageSize     [可选]: 数量
     *
     * @param {JSON} filter 条件过滤器 (参考上面的参数说明)
     * @return {Promise} 返回 Promise 对象，resolve 的参数为库存操作记录的数组，reject 的参数为错误信息
     */
    static findStockRecords(filter) {
        return Rest.get(Urls.API_STOCKS_RECORDS, { data: filter }).then(({ data: records, success, message }) => {
            return Utils.response(records, success, message);
        });
    }

    /**
     * 查询库存操作申请
     *
     * 网址: http://localhost:8080/api/stocks/requests?type=OUT
     * 参数:
     *      type           (必要): IN (入库)、OUT (出库)
     *      applicantId    [可选]: 小于 1 时查询所有的，否则查询指定申请人的
     *      stockRequestSn [可选]: 出库申请单号
     *      state          [可选]: 状态, 为 -1 时表示查询所有的
     *      startAt        [可选]: 开始时间
     *      endAt          [可选]: 结束时间
     *      pageNumber     [可选]: 页码
     *      pageSize       [可选]: 数量
     *
     * @param {JSON} filter 过滤条件，参考上面的 '参数'
     * @return {Promise} 返回 Promise 对象，resolve 的参数为库存操作申请的数组，reject 的参数为错误信息
     */
    static findStockRequests(filter) {
        return Rest.get(Urls.API_STOCKS_REQUESTS, { data: filter }).then(({ data: requests, success, message }) => {
            return Utils.response(requests, success, message);
        });
    }

    /**
     * 查询指定 ID 的库存操作申请
     *
     * 网址: http://localhost:8080/api/stocks/requests/{requestId}
     * 参数: 无
     *
     * @param {Long} requestId 库存操作申请 ID
     * @return {Promise} 返回 Promise 对象，resolve 的参数为库存操作申请，reject 的参数为错误信息
     */
    static findStockRequestById(requestId) {
        return Rest.get(Urls.API_STOCKS_REQUESTS_BY_ID, { params: { requestId } }).then(({ data: request, success, message }) => {
            return Utils.response(request, success, message);
        });
    }

    /**
     * 入库
     *
     * 网址: http://localhost:8080/api/stocks/in
     * 参数:
     *      productItemId (必要): 物料 ID
     *      count         (必要): 入库数量
     *      batch         (必要): 批次
     *      warehouse     [可选]: 仓库
     *
     * @param {JSON} record 库存操作记录 (属性参考上面)
     * @return {Promise} 返回 Promise 对象，resolve 的参数为更新后的入库记录，reject 的参数为错误信息
     */
    static stockIn(record) {
        return Rest.create(Urls.API_STOCKS_IN, { data: record }).then(({ data: newRecord, success, message }) => {
            return Utils.response(newRecord, success, message);
        });
    }

    /**
     * 删除入库操作记录及其入库数量，如果入库操作超过 1 个小时，则不允许删除
     *
     * 网址: http://localhost:8080/api/stocks/records/{recordId}
     * 参数: 无
     *
     * @param {Long} recordId 库存操作记录 ID
     * @return {Promise} 返回 Promise 对象，resolve 的参数为无，reject 的参数为错误信息
     */
    static deleteStockRecord(recordId) {
        return Rest.del(Urls.API_STOCKS_RECORDS_BY_ID, { params: { recordId } }).then(({ success, message }) => {
            return Utils.response(null, success, message);
        });
    }

    /**
     * 出库申请
     *
     * 网址: http://localhost:8080/api/stocks/out/requests
     * 参数: 无
     * 请求体:
     *      orderId: 订单 ID
     *      batchCounts: 物料出库的批次和数量，其结构为 { productId, productItemId, productItemName, batch, count }
     *      currentAuditorId: 当前审批员 ID
     *
     * @param {JSON} stockOutData 出库信息
     * @return {Promise} 返回 Promise 对象，resolve 的参数为新创建的出库申请，reject 的参数为错误信息
     */
    static stockOutRequest(stockOutData) {
        return Rest.create(Urls.API_STOCKS_OUT_REQUESTS, { data: stockOutData, json: true }).then(({ data: request, success, message }) => {
            return Utils.response(request, success, message);
        });
    }

    /**
     * 完成出库申请，物料领取
     *
     * 网址: http://localhost:8080/api/stocks/out/requests/{requestID}
     * 参数: 无
     *
     * @param {Long} requestId 出库申请 ID
     * @return {Promise} 返回 Promise 对象，resolve 的参数为无，reject 的参数为错误信息
     */
    static stockOut(requestId) {
        return Rest.update(Urls.API_STOCKS_OUT_REQUESTS_BY_ID, { params: { requestId } }).then(({ data, success, message }) => {
            return Utils.response(data, success, message);
        });
    }

    /**
     * 查询物料的库存
     *
     * 网址: http://localhost:8080/api/stocks
     * 参数:
     *      productItemId [可选]: 物料 ID
     *      name          [可选]: 物料名称
     *      code          [可选]: 物料编码
     *      batch         [可选]: 入库批次
     *      count         [可选]: 数量 (大于 0 时查询小于等于 count 的产品项)
     *      pageNumber    [可选]: 页码
     *      pageSize      [可选]: 数量
     *
     * @param filter 过滤条件
     *
     * @return {Promise} 返回 Promise 对象，resolve 的参数为物料数组，其中包含了出库信息，reject 的参数为错误信息
     */
    static findStocks(filter) {
        return Rest.get(Urls.API_STOCKS, { data: filter }).then(({ data: stocks, success, message }) => {
            return Utils.response(stocks, success, message);
        });
    }
}
