package com.xtuer.controller;

import com.xtuer.bean.*;
import com.xtuer.mapper.OrderMapper;
import com.xtuer.service.OrderService;
import com.xtuer.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 订单的控制器
 */
@RestController
public class OrderController extends BaseController {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderService orderService;

    /**
     * 查询指定 ID 的订单
     *
     * 网址: http://localhost:8080/api/orders/{orderId}
     * 参数: 无
     *
     * @param orderId 订单 ID
     * @return payload 为查询到的订单，查询不到返回 null
     */
    @GetMapping(Urls.API_ORDERS_BY_ID)
    public Result<Order> findOrderById(@PathVariable long orderId) {
        return Result.single(orderService.findOrder(orderId));
    }

    /**
     * 查询符合条件的订单
     *
     * 网址: http://localhost:8080/api/orders
     * 参数:
     *      orderSn      [可选]: 订单编号
     *      productCodes [可选]: 产品编号
     *      state        [可选]: 状态
     *      pageNumber   [可选]: 页码
     *      pageSize     [可选]: 数量
     *
     * @param filter 过滤器
     * @param page   分页对象
     * @return payload 为订单数组
     */
    @GetMapping(Urls.API_ORDERS)
    public Result<List<Order>> findOrders(Order filter, Page page) {
        return Result.ok(orderMapper.findOrders(filter, page));
    }

    /**
     * 插入或者更新订单
     *
     * 网址: http://localhost:8080/api/orders/{orderId}
     * 参数: 无
     * 请求体: 为订单的 JSON 字符串
     *      orderId         (必要): 订单 ID，为 0 时创建订单，非 0 时更新订单
     *      customerCompany (必要): 客户单位
     *      customerContact (必要): 客户联系人
     *      customerAddress (必要): 客户收件地址
     *      orderDate       (必要): 订单日期
     *      deliveryDate    (必要): 交货日期
     *      calibrated      [可选]: 是否校准
     *      calibrationInfo [可选]: 校准信息
     *      requirement     [可选]: 要求
     *      attachmentId    [可选]: 附件 ID
     *      items           (必要): 订单项
     *
     * @param order 订单
     * @return payload 为更新后的订单
     */
    @PutMapping(Urls.API_ORDERS_BY_ID)
    public Result<Order> upsertOrder(@RequestBody @Valid Order order, BindingResult bindingResult) {
        // 如有参数错误，则返回错误信息给客户端
        if (bindingResult.hasErrors()) {
            return Result.fail(Utils.getBindingMessage(bindingResult));
        }

        User salesperson = super.getCurrentUser();
        return orderService.upsertOrder(order, salesperson);
    }
}
