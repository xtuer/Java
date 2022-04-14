package com.xtuer.mapper;

import com.xtuer.bean.Page;
import com.xtuer.bean.product.ProductItem;
import com.xtuer.bean.stock.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 库存的 Mapper
 */
@Mapper
public interface StockMapper {
    /**
     * 查询库存操作记录
     *
     * @param filter 过滤器
     * @param page   分页对象
     * @return 返回库存操作记录的数组，如果没有记录则返回空的 list
     */
    List<StockRecord> findStockRecords(StockRecordFilter filter, Page page);

    /**
     * 查询出库申请的库存操作记录
     *
     * @param requestId 出库申请 ID
     * @return 返回库存操作记录的数组，如果没有记录则返回空的 list
     */
    List<StockRecord> findStockRecordsByStockRequestId(long requestId);

    /**
     * 查询指定 ID 的库存记录
     *
     * @param recordId 库存记录 ID
     * @return 返回查询到的库存记录，查询不到返回 null
     */
    StockRecord findStockRecordById(long recordId);

    /**
     * 查询库存操作申请
     *
     * @param filter 过滤器
     * @param page   分页对象
     * @return 返回库存操作申请的数组，如果没有记录则返回空的 list
     */
    List<StockRequest> findStockRequests(StockRequestFilter filter, Page page);

    /**
     * 统计库存操作申请
     *
     * @param filter 过滤器
     * @return 返回库存操作申请数量
     */
    int countStockRequests(StockRequestFilter filter, Page page);

    /**
     * 查询指定 ID 的库存操作申请
     *
     * @param requestId 操作申请 ID
     * @return 返回查询到的库存操作，查询不到返回 null
     */
    StockRequest findStockRequestById(long requestId);

    /**
     * 查询指定 ID 的库存操作的产品项
     *
     * @param requestId 操作申请 ID
     * @return 返回查询到的产品项
     */
    List<ProductItem> findProductItemsByStockRequestId(long requestId);

    /**
     * 创建库存操作记录
     *
     * @param record 库存操作记录
     */
    void insertStockRecord(StockRecord record);

    /**
     * 删除入库操作记录
     *
     * @param recordId 库存操作记录 ID
     */
    void deleteStockRecord(long recordId);

    /**
     * 创建库存操作申请
     *
     * @param request 操作申请
     */
    void insertStockRequest(StockRequest request);

    /**
     * 更新库存操作申请的状态
     *
     * @param requestId 操作申请 ID
     * @param state     状态
     */
    void updateStockRequestState(long requestId, int state);

    /**
     * 标记库存操作完成
     *
     * @param requestId 库存操作申请 ID
     */
    void completeStockRecordByRequestId(long requestId);

    /**
     * 判断订单是否已经存在出库操作申请
     *
     * @param orderId 订单 ID
     * @return 订单已经存在出库操作申请返回 true，否则返回 false
     */
    boolean hasOrderStockOutRequest(long orderId);

    /**
     * 增加库存
     *
     * @param productItemId 物料 ID
     * @param batch 批次
     * @param count 数量
     */
    void increaseStock(long productItemId, String batch, int count);

    /**
     * 减少库存
     *
     * @param productItemId 物料 ID
     * @param batch 批次
     * @param count 数量
     */
    void decreaseStock(long productItemId, String batch, int count);

    /**
     * 查询物料的库存
     *
     * @return 返回物料数组，其中包含了出库信息
     */
    List<ProductItem> findStocks(StockFilter filter, Page page);

    /**
     * 查询物料批次的库存数量
     *
     * @param productItemId 物料 ID
     * @param batch 批次
     * @return 返回批次的库存数量
     */
    int findStockBatchCount(long productItemId, String batch);

    /**
     * 查询物料的出库申请
     *
     * @param productItemId 物料 ID
     * @return 返回出库申请的数组
     */
    List<StockRequest> findStockRequestsByProductItemId(long productItemId);
}
