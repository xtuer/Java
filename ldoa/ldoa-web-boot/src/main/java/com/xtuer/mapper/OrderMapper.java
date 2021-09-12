package com.xtuer.mapper;

import com.xtuer.bean.Page;
import com.xtuer.bean.order.Order;
import com.xtuer.bean.order.OrderFilter;
import com.xtuer.bean.order.OrderItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 订单的 Mapper
 */
@Mapper
public interface OrderMapper {
    /**
     * 查询指定 ID 的订单
     *
     * @param orderId 订单 ID
     * @return 返回查询到的订单，查询不到返回 null
     */
    Order findOrderById(long orderId);

    /**
     * 查询符合条件的订单
     *
     * @param filter 过滤器
     * @param page   分页对象
     * @return 返回查询到的订单，查询不到返回空 List
     */
    List<Order> findOrders(OrderFilter filter, Page page);

    /**
     * 插入或者更新订单
     *
     * @param order 订单
     */
    void upsertOrder(Order order);

    /**
     * 插入订单项
     *
     * @param items 订单项
     */
    void insertOrderItems(List<OrderItem> items);

    /**
     * 删除订单的订单项
     *
     * @param orderId 订单 ID
     */
    void deleteOrderItems(long orderId);

    /**
     * 修改订单的状态
     *
     * @param orderId 订单 ID
     * @param state   状态
     */
    void updateOrderState(long orderId, int state);

    /**
     * 更新订单的进度
     *
     * @param orderId  订单 ID
     * @param progress 进度
     */
    void updateOrderProgress(long orderId, String progress);
}
