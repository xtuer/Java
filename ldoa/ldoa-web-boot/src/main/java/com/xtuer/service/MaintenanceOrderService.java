package com.xtuer.service;

import com.xtuer.bean.Result;
import com.xtuer.bean.User;
import com.xtuer.bean.order.MaintenanceOrder;
import com.xtuer.mapper.MaintenanceOrderMapper;
import com.xtuer.util.Utils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 维保订单服务
 */
@Service
public class MaintenanceOrderService extends BaseService {
    @Autowired
    private MaintenanceOrderMapper orderMapper;

    @Autowired
    private AuditServiceHelper auditServiceHelper;

    /**
     * 插入或者更新维保订单
     *
     * @param order         维保订单
     * @param servicePerson 售后服务人员
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<MaintenanceOrder> upsertMaintenanceOrder(MaintenanceOrder order, User servicePerson) {
        // 1. 参数校验: 产品名称、规格/型号、产品序列号不能为空
        // 2. 如果 ID 无效，则是新创建，为其分配 ID 和 SN
        // 3. 设置维保订单的其他属性
        // 4. 保存维保订单到数据库
        // 5. 创建审批

        // [1] 参数校验: 产品名称、规格/型号、产品序列号不能为空
        if (StringUtils.isBlank(order.getProductName())) {
            return Result.fail("产品名称不能为空");
        }
        if (StringUtils.isBlank(order.getModel())) {
            return Result.fail("规格/型号不能为空");
        }
        if (StringUtils.isBlank(order.getProductSn())) {
            return Result.fail("产品序列号不能为空");
        }

        // [2] 如果 ID 无效，则是新创建，为其分配 ID 和 SN
        if (Utils.isInvalidId(order.getMaintenanceOrderId())) {
            order.setMaintenanceOrderId(super.nextId());
            order.setMaintenanceOrderSn(super.nextSnByYear("SHDD"));
        }

        // [3] 设置维保订单的其他属性
        order.setServicePersonId(servicePerson.getUserId());
        order.setServicePersonName(servicePerson.getNickname());

        // [4] 保存维保订单到数据库
        orderMapper.upsertMaintenanceOrder(order);

        // [5] 创建审批
        auditServiceHelper.upsertMaintenanceOrderAudit(servicePerson, order);

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
}