package com.xtuer.controller;

import com.xtuer.bean.Page;
import com.xtuer.bean.Result;
import com.xtuer.bean.Urls;
import com.xtuer.bean.User;
import com.xtuer.bean.product.ProductItem;
import com.xtuer.bean.stock.*;
import com.xtuer.exception.ApplicationException;
import com.xtuer.mapper.StockMapper;
import com.xtuer.service.StockService;
import com.xtuer.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * 库存的控制器
 */
@RestController
public class StockController extends BaseController {
    @Autowired
    private StockService stockService;

    @Autowired
    private StockMapper stockMapper;

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
     *      endAt        [可选]: 结束时间
     *      pageNumber   [可选]: 页码
     *      pageSize     [可选]: 数量
     *
     * @param filter 过滤条件
     * @param page   分页对象
     * @return payload 为库存操作记录的数组
     */
    @GetMapping(Urls.API_STOCKS_RECORDS)
    public Result<List<StockRecord>> findStockRecords(StockRecordFilter filter, Page page) {
        // 设置查询时间范围
        filter.setStartAt(Utils.dayStart(filter.getStartAt()));
        filter.setEndAt(Utils.dayEnd(filter.getEndAt()));

        return Result.ok(stockMapper.findStockRecords(filter, page));
    }

    /**
     * 查询库存操作申请
     *
     * 网址: http://localhost:8080/api/stocks/requests?type=OUT
     * 参数:
     *      type              (必要): IN (入库)、OUT (出库)
     *      applicantId       [可选]: 小于 1 时查询所有的，否则查询指定申请人的
     *      applicantUsername [可选]: 申请人名字
     *      productItemName   [可选]: 物料名称
     *      productItemModel  [可选]: 物料规格型号
     *      stockRequestSn    [可选]: 出库申请单号
     *      state             [可选]: 状态, 为 -1 时表示查询所有的
     *      startAt           [可选]: 开始时间
     *      endAt             [可选]: 结束时间
     *      pageNumber        [可选]: 页码
     *      pageSize          [可选]: 数量
     *
     * @param filter 过滤条件
     * @param page   分页对象
     * @return payload 为库存操作申请的数组
     */
    @GetMapping(Urls.API_STOCKS_REQUESTS)
    public Result<List<StockRequest>> findStockRequests(StockRequestFilter filter, Page page) {
        // 设置查询时间范围
        filter.setStartAt(Utils.dayStart(filter.getStartAt()));
        filter.setEndAt(Utils.dayEnd(filter.getEndAt()));

        return Result.ok(stockMapper.findStockRequests(filter, page));
    }

    /**
     * 统计库存操作数量
     *
     * 网址: http://localhost:8080/api/stocks/requests?type=OUT
     * 参数: 参考 findStockRequests
     *
     * @param filter 过滤条件
     * @return payload 为数量
     */
    @GetMapping(Urls.API_STOCKS_REQUESTS_COUNT)
    public Result<Integer> countStockRequests(StockRequestFilter filter) {
        // 设置查询时间范围
        filter.setStartAt(Utils.dayStart(filter.getStartAt()));
        filter.setEndAt(Utils.dayEnd(filter.getEndAt()));

        Utils.dump(filter);

        return Result.ok(stockMapper.countStockRequests(filter, Page.of(1, 20)));
    }

    /**
     * 查询指定 ID 的库存操作申请
     *
     * 网址: http://localhost:8080/api/stocks/requests/{requestId}
     * 参数: 无
     *
     * @param requestId 库存操作申请 ID
     * @return payload 为库存操作申请，查询不到则为 null
     */
    @GetMapping(Urls.API_STOCKS_REQUESTS_BY_ID)
    public Result<StockRequest> findStockRequest(@PathVariable long requestId) {
        return Result.single(stockService.findStockRequest(requestId));
    }

    /**
     * 入库
     *
     * 网址: http://localhost:8080/api/stocks/in
     * 参数:
     *      productItemId (必要): 物料 ID
     *      count         (必要): 入库数量
     *      warehouse     [可选]: 仓库
     *      productItemType [可选]: 类型
     *
     * @param record 库存操作记录
     * @return payload 为更新后的入库记录
     */
    @PostMapping(Urls.API_STOCKS_IN)
    public Result<StockRecord> stockIn(StockRecord record) {
        User user = super.getCurrentUser();
        return stockService.stockIn(record, user);
    }

    /**
     * 删除入库操作记录及其入库数量，如果入库操作超过 1 个小时，则不允许删除
     *
     * 网址: http://localhost:8080/api/stocks/records/{recordId}
     * 参数: 无
     *
     * @param recordId 库存操作记录 ID
     */
    @DeleteMapping(Urls.API_STOCKS_RECORDS_BY_ID)
    public Result<Boolean> deleteStockRecord(@PathVariable long recordId) {
        return stockService.deleteStockRecord(recordId);
    }

    /**
     * 出库申请
     *
     * 网址: http://localhost:8080/api/stocks/out/requests
     * 参数: 无
     * 请求体:
     *      targetId:   出库对象的 ID: 产品 ID、订单 ID (产品项出库时为 0，因可能有多个产品项)
     *      targetType: 出库类型: 1 (产品项出库)、2 (产品出库)、3 (订单出库)
     *      currentAuditorId: 当前审批员 ID
     *      desc: 描述: 产品项出库 (物料出库)、产品出库 (产品名字) 、订单出库 (订单 SN)
     *      comment: 出库备注
     *      productItemNames: 物料名字拼起来的字符串
     *      records: 出库项
     *          productId:     物料所属产品 ID
     *          productItemId: 物料 ID
     *          count:         物料出库数量
     *
     * @param request 出库信息
     * @return payload 为服务器创建的出库申请
     */
    @PostMapping(Urls.API_STOCKS_OUT_REQUESTS)
    public Result<StockRequest> stockOutRequest(@RequestBody StockRequest request) {
        User user = super.getCurrentUser();

        try {
            return stockService.stockOutRequest(request, user);
        } catch (ApplicationException ex) {
            // 没有配置出库审批流程的异常
            if (ex.getCode() == 10) {
                return Result.fail(ex.getMessage());
            } else {
                throw ex;
            }
        }
    }

    /**
     * 完成出库申请，物料领取
     *
     * 网址: http://localhost:8080/api/stocks/out/requests/{requestID}
     * 参数: 无
     *
     * @param requestId 出库申请 ID
     */
    @PutMapping(Urls.API_STOCKS_OUT_REQUESTS_BY_ID)
    public Result<Boolean> stockOut(@PathVariable long requestId) {
        return stockService.stockOut(requestId);
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
     * @param page   分页
     *
     * @return payload 为物料数组，其中包含了出库信息
     */
    @GetMapping(Urls.API_STOCKS)
    public Result<List<ProductItem>> findStocks(StockFilter filter, Page page) {
        return Result.ok(stockMapper.findStocks(filter, page));
    }

    /**
     * 导出物料的库存
     *
     * 网址: http://localhost:8080/api/stocks/export
     * 参数:
     *      productItemId [可选]: 物料 ID
     *      name          [可选]: 物料名称
     *      code          [可选]: 物料编码
     *      batch         [可选]: 入库批次
     *      count         [可选]: 数量 (大于 0 时查询小于等于 count 的产品项)
     *
     * @param filter 过滤条件
     *
     * @return payload 为导出的 Excel 的 URL
     */
    @GetMapping(Urls.API_STOCKS_EXPORT)
    public Result<String> exportStocks(StockFilter filter) throws IOException {
        return Result.ok(stockService.exportStocks(filter));
    }

    /**
     * 查询物料的出库申请
     *
     * 网址: http://localhost:8080/api/stocks/product-items/{productItemId}/out/requests
     * 参数: 无
     *
     * @param productItemId 物料 ID
     * @return payload 为物料的出库申请数组
     */
    @GetMapping(Urls.API_STOCKS_PRODUCT_ITEM_OUT_REQUESTS)
    public Result<List<StockRequest>> findStockRequestsByProductItemId(@PathVariable long productItemId) {
        return Result.ok(stockMapper.findStockRequestsByProductItemId(productItemId));
    }
}
