package com.xtuer.mapper;

import com.xtuer.bean.Page;
import com.xtuer.bean.sales.SalesOrder;
import com.xtuer.bean.sales.SalesOrderFilter;
import org.apache.ibatis.annotations.Mapper;

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
     * 订单收款
     *
     * @param orderId    销售订单 ID
     * @param paidAmount 收款金额
     */
    void pay(long orderId, double paidAmount);

    /**
     * 完成订单
     *
     * @param orderId 销售订单 ID
     */
    void completeSalesOrder(long orderId);
}
