package com.xtuer.mapper;

import com.xtuer.bean.Page;
import com.xtuer.bean.sales.CustomerFinance;
import com.xtuer.bean.sales.SalesOrder;
import com.xtuer.bean.sales.SalesOrderFilter;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

/**
 * 销售订单 Mapper
 */
@Mapper
public interface SalesOrderMapper {
    /**
     * 查询指定 ID 的销售订单
     *
     * @param orderId 订单 ID
     * @return 返回查询到的销售订单，查询不到返回 null
     */
    SalesOrder findSalesOrderById(long orderId);

    /**
     * 查询符合条件的销售订单
     *
     * @return 返回销售订单的数组
     */
    List<SalesOrder> findSalesOrders(SalesOrderFilter filter, Page page);

    /**
     * 更新或者插入销售订单
     *
     * @param order 销售订单
     */
    void upsertSalesOrder(SalesOrder order);

    /**
     * 更新销售订单的状态
     *
     * @param orderId 销售订单 ID
     * @param state   状态
     */
    void updateSalesOrderState(long orderId, int state);

    /**
     * 更新销售订单的临时生产订单
     *
     * @param orderId 销售订单 ID
     * @param produceOrderTemp 临时生产订单
     */
    void updateProduceOrderTemp(long orderId, String produceOrderTemp);

    /**
     * 订单收款
     *
     * @param orderId    销售订单 ID
     * @param paidAmount 收款金额
     * @param paidType   收款类型
     * @param paidAt     收款时间
     */
    void pay(long orderId, double paidAmount, int paidType, Date paidAt);

    /**
     * 完成订单
     *
     * @param orderId 销售订单 ID
     */
    void completeSalesOrder(long orderId);

    /**
     * 查询客户的财务信息: 累计订单金额、累计应收款、累计已收款
     *
     * @param customerId 客户 ID
     * @return 返回交易的财务信息
     */
    CustomerFinance findFinanceByCustomerId(long customerId);
}
