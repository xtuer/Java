package com.xtuer.controller;

import com.xtuer.bean.Page;
import com.xtuer.bean.Result;
import com.xtuer.bean.Urls;
import com.xtuer.bean.User;
import com.xtuer.bean.sales.SalesOrder;
import com.xtuer.bean.sales.SalesOrderFilter;
import com.xtuer.mapper.SalesOrderMapper;
import com.xtuer.service.SalesOrderService;
import com.xtuer.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 销售订单的控制器
 */
@RestController
public class SalesOrderController extends BaseController {
    @Autowired
    private SalesOrderService salesOrderService;

    @Autowired
    private SalesOrderMapper salesOrderMapper;

    /**
     * 查询指定 ID 的销售订单
     *
     * 网址: http://localhohst:8080/api/sales/salesOrders/{salesOrderId}
     * 参数: 无
     *
     * @param salesOrderId 销售订单 ID
     * @return payload 为销售订单，查询不到时为 null
     */
    @GetMapping(Urls.API_SALES_ORDERS_BY_ID)
    public Result<SalesOrder> findSalesOrder(@PathVariable long salesOrderId) {
        return Result.single(salesOrderService.findSalesOrder(salesOrderId));
    }

    /**
     * 查询符合条件的销售订单
     *
     * 网址: http://localhohst:8080/api/sales/salesOrders
     * 参数:
     *      customerName [可选]: 客户
     *      business     [可选]: 行业
     *      topic        [可选]: 主题
     *      pageNumber   [可选]: 页码
     *      pageSize     [可选]: 数量
     *
     * @return payload 为销售订单数组
     */
    @GetMapping(Urls.API_SALES_ORDERS)
    public Result<List<SalesOrder>> findSalesOrders(SalesOrderFilter filter, Page page) {
        return Result.ok(salesOrderService.findSalesOrders(filter, page));
    }

    /**
     * 更新或者插入销售订单
     *
     * 网址: http://localhohst:8080/api/sales/salesOrders/{salesOrderId}
     * 参数: 无
     * 请求体: 参数 SalesOrder 的属性
     *
     * @param salesOrder 销售订单
     * @param bindingResult 校验结果
     * @return payload 为销售订单
     */
    @PutMapping(Urls.API_SALES_ORDERS_BY_ID)
    public Result<SalesOrder> upsertSalesOrder(@Valid @RequestBody SalesOrder salesOrder, BindingResult bindingResult) {
        // 如有参数错误，则返回错误信息给客户端
        if (bindingResult.hasErrors()) {
            return Result.fail(Utils.getBindingMessage(bindingResult));
        }

        User salesperson = super.getCurrentUser();
        return salesOrderService.upsertSalesOrder(salesOrder, salesperson);
    }
}