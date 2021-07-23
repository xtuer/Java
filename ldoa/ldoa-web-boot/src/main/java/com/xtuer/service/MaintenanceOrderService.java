package com.xtuer.service;

import com.xtuer.bean.Page;
import com.xtuer.bean.Result;
import com.xtuer.bean.User;
import com.xtuer.bean.impex.ImpexMaintenanceOrder;
import com.xtuer.bean.order.MaintenanceOrder;
import com.xtuer.bean.order.MaintenanceOrderFilter;
import com.xtuer.bean.order.MaintenanceOrderItem;
import com.xtuer.mapper.ImpexMapper;
import com.xtuer.mapper.MaintenanceOrderMapper;
import com.xtuer.util.Utils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 维保订单服务
 */
@Service
public class MaintenanceOrderService extends BaseService {
    @Autowired
    private MaintenanceOrderMapper orderMapper;

    @Autowired
    private ImpexMapper impexMapper;

    @Autowired
    private AuditServiceHelper auditServiceHelper;

    /**
     * 查询维保订单，同时查询出它的订单项
     *
     * @param orderId 维保订单 ID
     * @return 返回查询到的维保订单，查询不到返回 null
     */
    public MaintenanceOrder findMaintenanceOrder(long orderId) {
        MaintenanceOrder order = orderMapper.findMaintenanceOrderById(orderId);

        if (order != null) {
            List<MaintenanceOrderItem> items = orderMapper.findMaintenanceOrderItemsByMaintenanceOrderId(orderId);
            order.setItems(items);
        }

        return order;
    }

    /**
     * 插入或者更新维保订单
     *
     * @param order         维保订单
     * @param servicePerson 售后服务人员
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<MaintenanceOrder> upsertMaintenanceOrder(MaintenanceOrder order, User servicePerson) {
        // 0. 聚合产品名称、编码、型号
        // 1. 参数校验: 客户名称、销售人员、收货日期、反馈的问题不能为空
        // 2. 如果 ID 无效，则是新创建，为其分配 ID 和 SN
        // 3. 设置维保订单的其他属性
        // 4. 保存维保订单到数据库
        // 5. 删除已有维保订单项
        // 6. 创建新的维保订单项
        // 7. 创建审批

        // [0] 聚合产品名称、编码、型号
        String productNames  = order.getItems().stream().map(MaintenanceOrderItem::getProductName).collect(Collectors.joining(", "));
        String productCodes  = order.getItems().stream().map(MaintenanceOrderItem::getProductCode).collect(Collectors.joining(", "));
        String productModels = order.getItems().stream().map(MaintenanceOrderItem::getProductModel).collect(Collectors.joining(", "));
        order.setProductName(productNames);
        order.setProductCode(productCodes);
        order.setProductModel(productModels);

        // [1] 参数校验: 客户名称、销售人员、收货日期、反馈的问题不能为空
        if (StringUtils.isBlank(order.getCustomerName())) {
            return Result.fail("客户名称不能为空");
        }
        if (StringUtils.isBlank(order.getSalespersonName())) {
            return Result.fail("销售人员不能为空");
        }
        if (order.getReceivedDate() == null) {
            return Result.fail("收货日期不能为空");
        }
        if (StringUtils.isBlank(order.getProblem())) {
            return Result.fail("反馈的问题不能为空");
        }

        // [2] 如果 ID 无效，则是新创建，为其分配 ID 和 SN
        if (Utils.isInvalidId(order.getMaintenanceOrderId())) {
            order.setMaintenanceOrderId(super.nextId());
            order.setMaintenanceOrderSn(super.nextSnByYear("SHDD"));
        }

        // [3] 设置维保订单的其他属性
        order.setServicePersonId(servicePerson.getUserId());
        order.setServicePersonName(servicePerson.getNickname());

        // [New]
        // 提交时，修改订单状态为审批中
        // 暂存时，如果订单已经存在，则不修改它的状态，否则状态为初始化
        if (order.isCommitted()) {
            order.setState(MaintenanceOrder.STATE_AUDITING);
        } else {
            MaintenanceOrder exitsOrder = orderMapper.findMaintenanceOrderById(order.getMaintenanceOrderId());

            if (exitsOrder != null) {
                order.setState(exitsOrder.getState());
            } else {
                order.setState(MaintenanceOrder.STATE_INIT);
            }
        }

        // [4] 保存维保订单到数据库
        orderMapper.upsertMaintenanceOrder(order);

        // [5] 删除已有维保订单项
        orderMapper.deleteMaintenanceOrderItemsByMaintenanceOrderId(order.getMaintenanceOrderId());

        // [6] 创建新的维保订单项
        for (MaintenanceOrderItem item : order.getItems()) {
            item.setMaintenanceOrderId(order.getMaintenanceOrderId());
            item.setMaintenanceOrderItemId(super.nextId());
            orderMapper.insertMaintenanceOrderItem(item);
        }

        // [7] 创建审批
        if (order.isCommitted()) {
            orderMapper.commitMaintenanceOrder(order.getMaintenanceOrderId());
            auditServiceHelper.upsertMaintenanceOrderAudit(servicePerson, order);
        }

        return Result.ok(order);
    }

    /**
     * 订单审批通过
     *
     * @param orderId 订单 ID
     */
    public void acceptOrder(long orderId) {
        orderMapper.updateMaintenanceOrderState(orderId, MaintenanceOrder.STATE_ACCEPTED);
    }

    /**
     * 订单审批被拒绝
     *
     * @param orderId 订单 ID
     */
    public void rejectOrder(long orderId) {
        orderMapper.updateMaintenanceOrderState(orderId, MaintenanceOrder.STATE_REJECTED);
    }

    /**
     * 完成订单
     *
     * @param orderId 订单 ID
     */
    public void completeOrder(long orderId) {
        orderMapper.updateMaintenanceOrderState(orderId, MaintenanceOrder.STATE_COMPLETE);
    }

    /**
     * 导出维保订单
     *
     * @param filter 过滤条件
     * @return 返回导出的 Excel 的 URL
     */
    public String exportMaintenanceOrders(MaintenanceOrderFilter filter) throws IOException {
        Page page = Page.of(1, Integer.MAX_VALUE);
        List<ImpexMaintenanceOrder> items = impexMapper.findMaintenanceOrders(filter, page);
        return super.exportExcel("维保订单", ImpexMaintenanceOrder.class, items);
    }
}
