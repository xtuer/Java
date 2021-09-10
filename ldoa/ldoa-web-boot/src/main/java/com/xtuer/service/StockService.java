package com.xtuer.service;

import com.xtuer.bean.Const;
import com.xtuer.bean.Page;
import com.xtuer.bean.Result;
import com.xtuer.bean.User;
import com.xtuer.bean.product.BatchCount;
import com.xtuer.bean.product.ProductItem;
import com.xtuer.bean.stock.StockFilter;
import com.xtuer.bean.stock.StockOutRequestFormBean;
import com.xtuer.bean.stock.StockRecord;
import com.xtuer.bean.stock.StockRequest;
import com.xtuer.exception.ApplicationException;
import com.xtuer.mapper.ProductMapper;
import com.xtuer.mapper.StockMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 库存的服务
 */
@Service
@Slf4j
public class StockService extends BaseService {
    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private AuditServiceHelper auditServiceHelper;

    /**
     * 查询指定 ID 的库存操作申请
     *
     * @param requestId 库存操作申请 ID
     * @return 返回查询到的库存操作申请，查询不到返回 null
     */
    public StockRequest findStockRequest(long requestId) {
        // 1. 查询库存操作申请基本信息
        // 2. 查询库存操作记录
        // 3. 查询操作的产品项
        // 4. 合并产品项到操作记录相关的产品项

        // [1] 查询库存操作申请基本信息
        StockRequest request = stockMapper.findStockRequestById(requestId);

        if (request == null) {
            return null;
        }

        // [2] 查询库存操作记录
        List<StockRecord> records = stockMapper.findStockRecordsByStockRequestId(requestId);
        request.setRecords(records);

        // [3] 查询操作的产品项
        List<ProductItem> productItems = stockMapper.findProductItemsByStockRequestId(requestId);

        // [4] 合并产品项到操作记录相关的产品项
        for (ProductItem item : productItems) {
            for (StockRecord record : records) {
                if (record.getProductItemId() == item.getProductItemId()) {
                    record.setProductItem(item);
                }
            }
        }

        return request;
    }

    /**
     * 入库
     *
     * @param record 库存记录
     * @param user   操作员
     * @return payload 为更新后的库记录
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<StockRecord> stockIn(StockRecord record, User user) {
        // 1. 数据校验
        // 2. 设置入库的相关数据
        // 3. 创建入库记录
        // 4. 入库: 增加产品项的数量
        // 5. 查询新创建的记录

        // [1] 数据校验
        if (record.getCount() <= 0) {
            return Result.fail("入库数量必须大于 0");
        }
        if (StringUtils.isBlank(record.getBatch())) {
            return Result.fail("批次不能为空");
        }

        // [2] 设置入库的相关数据
        record.setStockRecordId(super.nextId());
        record.setType(StockRecord.Type.IN);
        record.setUserId(user.getUserId());
        record.setStockRequestId(0);
        record.setComplete(true);

        // [3] 创建入库记录
        stockMapper.insertStockRecord(record);

        // [4] 入库
        stockMapper.increaseStock(record.getProductItemId(), record.getBatch(), record.getCount());

        // [5] 查询新创建的记录
        record = stockMapper.findStockRecordById(record.getStockRecordId());

        return Result.ok(record);
    }

    /**
     * 删除入库操作记录及其入库数量，如果入库操作超过 1 个小时，则不允许删除
     *
     * @param stockRecordId 库存操作记录 ID
     * @return 返回操作的结果，payload 为无
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> deleteStockRecord(long stockRecordId) {
        // 1. 查询入库记录
        // 2. 删除入库记录
        // 3. 如果是入库操作
        //    3.1 如果入库操作超过 1 个小时，则不允许删除
        //    3.2 删除入库物料此次入库的数量

        // [1] 查询入库记录
        StockRecord record = stockMapper.findStockRecordById(stockRecordId);

        if (record == null) {
            return Result.fail("出库操作记录不存在");
        }

        // [2] 删除入库记录
        stockMapper.deleteStockRecord(stockRecordId);

        // [3] 如果是入库操作
        if (record.getType().equals(StockRecord.Type.IN)) {
            // [3.1] 如果入库操作超过 24 个小时，则不允许删除
            long createdAt = record.getCreatedAt().getTime();
            long now = new Date().getTime();
            double delta = (now - createdAt) / 3600_000.0; // 相差的小时

            // Feature: 您帮我把物料管理和库存部分的编辑和删除权限开一下 (质量保证, 或者您给管理员开通也行)
            // if (delta >= 24) {
            //     return Result.fail("入库时间超过 1 小时，不允许删除");
            // }

            // [3.2] 删除入库物料此次入库的数量
            stockMapper.decreaseStock(record.getProductItemId(), record.getBatch(), record.getCount());
        }

        return Result.ok();
    }

    /**
     * 出库申请
     *
     * @param out  出库信息
     * @param user 操作员
     * @return payload 为新创建的出库请求
     * @exception ApplicationException 审批配置无效时抛异常
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<StockRequest> stockOutRequest(StockOutRequestFormBean out, User user) {
        // 1. 如果没有出库物料，则返回
        // 2. 如果订单已经有出库申请，则不允许继续申请
        // 3. 创建出库请求
        // 4. 创建出库记录，物料的每个批次一个出库记录，但标记为未完成，等待审批通过后才能领取物料，从库存中减去相应的数量
        // 5. 校验物料库存，库存不足则不允许出库
        // 6. 保存出库申请和其出库记录到数据库
        // 7. 创建出库申请的审批，失败则不继续处理

        // [1] 如果没有出库物料，则返回
        if (CollectionUtils.isEmpty(out.getBatchCounts())) {
            return Result.fail("没有出库物料");
        }

        // 订单 ID
        long orderId = out.getOrderId();

        // [2] 如果订单已经有出库申请，则不允许继续申请
        if (orderId > 0 && stockMapper.hasOrderStockOutRequest(orderId)) {
            log.warn("订单 [{}] 已经存在出库申请，请不要重复申请", orderId);
            return Result.fail("订单已经存在出库申请，请不要重复申请");
        }

        // 出库描述: 物料名称拼接在一起
        String desc = out.getBatchCounts().stream().map(BatchCount::getProductItemName).collect(Collectors.joining(", "));
        String models = out.getBatchCounts().stream().map(BatchCount::getProductItemModel).collect(Collectors.joining(", "));

        // [3] 创建出库请求
        StockRequest request = new StockRequest();
        request.setStockRequestId(super.nextId());
        request.setStockRequestSn(this.nextStockOutSn());
        request.setType(StockRecord.Type.OUT);
        request.setOrderId(orderId);
        request.setState(StockRequest.STATE_AUDITING); // 状态为审批中
        request.setApplicantId(user.getUserId());
        request.setApplicantUsername(user.getNickname());
        request.setDesc(desc);
        request.setCreatedAt(new Date());
        request.setCurrentAuditorId(out.getCurrentAuditorId());
        request.setProductItemNames(desc);
        request.setProductItemModels(models);

        // [4] 创建出库记录，物料的每个批次一个出库记录，但标记为未完成，等待审批通过后才能领取物料，从库存中减去相应的数量
        List<StockRecord> stockRecords = new LinkedList<>();
        for (BatchCount bc : out.getBatchCounts()) {
            StockRecord record = new StockRecord();
            record.setStockRecordId(super.nextId());
            record.setType(StockRecord.Type.OUT);
            record.setUserId(user.getUserId());
            record.setStockRequestId(request.getStockRequestId());
            record.setStockRequestSn(request.getStockRequestSn());
            record.setProductId(bc.getProductId());
            record.setProductItemId(bc.getProductItemId());
            record.setBatch(bc.getBatch());
            record.setCount(bc.getCount());
            record.setComplete(false); // 标记为未完成
            record.setProductItem(new ProductItem());
            record.getProductItem().setName(bc.getProductItemName()); // 设置物料名字，库存不足时提示使用

            stockRecords.add(record);
        }

        // [5] 校验物料库存，库存不足则不允许出库
        Result<Boolean> validateResult = this.checkProductItemsStock(stockRecords);
        if (!validateResult.isSuccess()) {
            return Result.fail(validateResult.getMessage());
        }

        // [6] 保存出库申请和其出库记录到数据库
        stockMapper.insertStockRequest(request);

        for (StockRecord record : stockRecords) {
            stockMapper.insertStockRecord(record);
        }

        // [7] 创建出库申请的审批，失败则不继续处理，抛异常是为了事务回滚
        auditServiceHelper.insertStockRequestAudit(user, request);

        return Result.ok(request);
    }

    /**
     * 生成出库请求的单号，格式为 PTD-20200806-0001
     *
     * @return 返回单号
     */
    private String nextStockOutSn() {
        return super.nextSnByYear(Const.SN_PREFIX_STOCK_OUT);
    }

    /**
     * 库存操作审批通过
     *
     * @param requestId 库存操作 ID
     */
    public void acceptStockRequest(long requestId) {
        stockMapper.updateStockRequestState(requestId, StockRequest.STATE_ACCEPTED);
    }

    /**
     * 库存操作审批被拒绝
     *
     * @param requestId 库存操作 ID
     */
    public void rejectStockRequest(long requestId) {
        stockMapper.updateStockRequestState(requestId, StockRequest.STATE_REJECTED);
    }

    /**
     * 物料出库
     *
     * @param requestId 库存操作 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> stockOut(long requestId) {
        // 1. 查询出库申请的物料出库记录
        // 2. 校验物料库存，库存不足则不允许出库
        // 3. 把相同物料同一批次的出库合并到一起，count 为所有相同物料的 count 之和，stockBatchCount 为此批次的库存
        // 4. 出库: 更新库存，每个物料的库存减去对应的数量
        // 5. 更新物料出库记录的状态为完成
        // 6. 更新出库申请的状态为完成

        // [1] 查询出库申请的物料出库记录
        List<StockRecord> records = stockMapper.findStockRecordsByStockRequestId(requestId);

        // [2] 校验物料库存，库存不足则不允许出库
        Result<Boolean> validateResult = this.checkProductItemsStock(records);
        if (!validateResult.isSuccess()) {
            return Result.fail(validateResult.getMessage());
        }

        // [3] 把相同物料同一批次的出库合并到一起，count 为所有相同物料的 count 之和，stockBatchCount 为此批次的库存
        List<BatchCount> batchCounts = mergeToBatchCount(records);

        // [4] 出库: 更新库存，每个物料批次的库存减去对应的数量
        for (BatchCount bc : batchCounts) {
            stockMapper.decreaseStock(bc.getProductItemId(), bc.getBatch(), bc.getCount());
        }

        // [5] 更新物料出库记录的状态为完成
        stockMapper.completeStockRecordByRequestId(requestId);

        // [6] 更新出库申请的状态为完成
        stockMapper.updateStockRequestState(requestId, StockRequest.STATE_COMPLETE);

        return Result.ok();
    }

    /**
     * 把相同物料同一批次的出库合并到一起，count 为所有相同物料的 count 之和，stockBatchCount 为此批次的库存
     *
     * @param records 出库记录
     * @return 返回物料的批次数量数组
     */
    private List<BatchCount> mergeToBatchCount(Collection<StockRecord> records) {
        // 1. 创建 Map，key 为物料 ID + 批次，value 为物料出库和库存信息的对象 BatchCount
        // 2. 遍历库存操作记录 records，把相同物料同一批次的出库合并到一起放到 map 中
        //    2.1 如果物料的 BatchCount bc 不存在，则创建并且插入到 map 中
        //    2.2 出库数量为同一批次的所有出库数量之和
        // 3. 返回 BatchCount 的 List

        // [1] 创建 Map，key 为物料 ID + 批次，value 为物料出库和库存信息的对象 BatchCount
        Map<String, BatchCount> map = new HashMap<>(); // key 为 productItemId + batch

        // [2] 遍历库存操作记录 records，把相同物料同一批次的出库合并到一起放到 map 中
        for (StockRecord record : records) {
            String key = record.getProductItemId() + record.getBatch();
            BatchCount bc = map.get(key);

            // [2.1] 如果物料的 BatchCount bc 不存在，则创建并且插入到 map 中
            if (bc == null) {
                bc = new BatchCount();
                bc.setProductItemId(record.getProductItemId());
                bc.setProductItemName(record.getProductItem().getName());
                bc.setBatch(record.getBatch());
                bc.setCount(0);
                bc.setStockBatchCount(record.getStockBatchCount()); // 库存数量
                map.put(key, bc);
            }

            // [2.2] 出库数量为同一批次的所有出库数量之和
            bc.setCount(bc.getCount() + record.getCount());
        }

        // [3] 返回 BatchCount 的 List
        return new LinkedList<>(map.values());
    }

    /**
     * 检查库存是否足够
     *
     * @param records 出库操作记录
     * @return 库存足够返回 true，否则返回 false
     */
    private Result<Boolean> checkProductItemsStock(Collection<StockRecord> records) {
        // 1. 查询出库记录相关的库存，放到出库记录里，方便操作
        // 2. 把相同物料同一批次的出库合并到一起，count 为所有相同物料的 count 之和，stockBatchCount 为此批次的库存
        // 3. 当所有物料的库存都满足条件时则返回 true，否则返回 false

        // [1] 查询出库记录相关的库存，放到出库记录里，方便操作
        for (StockRecord record : records) {
            int count = stockMapper.findStockBatchCount(record.getProductItemId(), record.getBatch());
            record.setStockBatchCount(count);
        }

        // [2] 把相同物料同一批次的出库合并到一起，count 为所有相同物料的 count 之和，stockBatchCount 为此批次的库存
        List<BatchCount> batchCounts = mergeToBatchCount(records);

        // [3] 当所有物料的库存都满足条件时则返回 true，否则返回 false
        for (BatchCount bc : batchCounts) {
            if (bc.getCount() > bc.getStockBatchCount()) {
                return Result.fail("库存不足: 物料[{}]的库存为 {}，需要 {}",
                        bc.getProductItemName(),
                        bc.getStockBatchCount() + "",
                        bc.getCount() + "");
            }
        }

        return Result.ok();
    }

    /**
     * 导出库存
     *
     * @param filter 过滤条件
     * @return 返回导出的 Excel 的 URL
     */
    public String exportStocks(StockFilter filter) throws IOException {
        Page page = Page.of(1, Integer.MAX_VALUE);
        List<ProductItem> items = stockMapper.findStocks(filter, page);
        return super.exportExcel("库存", ProductItem.class, items);
    }
}
