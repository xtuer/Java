package com.xtuer.controller;

import com.xtuer.bean.Page;
import com.xtuer.bean.Result;
import com.xtuer.bean.Urls;
import com.xtuer.bean.User;
import com.xtuer.bean.sales.CustomerFinance;
import com.xtuer.bean.sales.SalesOrder;
import com.xtuer.bean.sales.SalesOrderFilter;
import com.xtuer.mapper.SalesOrderMapper;
import com.xtuer.service.SalesOrderService;
import com.xtuer.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Date;
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
     * 网址: http://localhost:8080/api/sales/salesOrders/{salesOrderId}
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
     * 网址: http://localhost:8080/api/sales/salesOrders
     * 参数:
     *      customerName   [可选]: 客户
     *      business       [可选]: 行业
     *      topic          [可选]: 主题
     *      searchType     [可选]: 搜索类型: 0 (所有订单)、1 (应收款订单)、2 (本月已收款订单)、3 (本年已收款订单)
     *      paidAtStart    [可选]: 开始支付时间: 搜索类型为 2 或者 3 时使用
     *      paidAtEnd      [可选]: 结束支付时间: 搜索类型为 2 或者 3 时使用
     *      agreementStart [可选]: 开始签约时间: 搜索类型非 2 或者 3 时使用
     *      agreementEnd   [可选]: 结束签约时间: 搜索类型非 2 或者 3 时使用
     *      pageNumber     [可选]: 页码
     *      pageSize       [可选]: 数量
     *
     * @return payload 为销售订单数组
     */
    @GetMapping(Urls.API_SALES_ORDERS)
    public Result<List<SalesOrder>> findSalesOrders(SalesOrderFilter filter, Page page) {
        return Result.ok(salesOrderService.findSalesOrders(filter, page));
    }

    /**
     * 导出销售订单
     *
     * 网址: http://localhost:8080/api/sales/salesOrders/export
     * 参数: 参考 findSalesOrders
     *
     * @param filter 过滤条件
     * @return payload 为导出的 Excel 的 URL
     */
    @GetMapping(Urls.API_SALES_ORDERS_EXPORT)
    public Result<String> exportSalesOrders(SalesOrderFilter filter) throws IOException {
        return Result.ok(salesOrderService.exportSalesOrders(filter));
    }

    /**
     * 导出支付信息的销售订单
     *
     * 网址: http://localhost:8080/api/sales/salesOrders/export-payment
     * 参数: 参考 findSalesOrders
     *
     * @param filter 过滤条件
     * @return payload 为导出的 Excel 的 URL
     */
    @GetMapping(Urls.API_SALES_ORDERS_EXPORT_PAY)
    public Result<String> exportSalesOrdersForPayment(SalesOrderFilter filter) throws IOException {
        return Result.ok(salesOrderService.exportSalesOrdersForPayment(filter));
    }

    /**
     * 更新或者插入销售订单
     *
     * 网址: http://localhost:8080/api/sales/salesOrders/{salesOrderId}
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

    /**
     * 订单收款
     *
     * 网址: http://localhost:8080/api/sales/salesOrders/{salesOrderId}/payments
     * 参数: paidAmount: 收款金额
     *
     * @param salesOrderId 销售订单 ID
     * @param paidAmount   收款金额
     * @param paidType     收款类型
     * @param paidAt       收款时间
     * @return payload 为订单的状态
     */
    @PutMapping(Urls.API_SALES_ORDERS_PAYMENTS)
    public Result<Integer> pay(@PathVariable long   salesOrderId,
                               @RequestParam double paidAmount,
                               @RequestParam int    paidType,
                               @RequestParam Date   paidAt) {
        return Result.ok(salesOrderService.pay(salesOrderId, paidAmount, paidType, paidAt));
    }

    /**
     * 完成订单
     *
     * 网址: http://localhost:8080/api/sales/salesOrders/{salesOrderId}/complete
     * 参数: 无
     *
     * @param salesOrderId 销售订单 ID
     */
    @PutMapping(Urls.API_SALES_ORDERS_COMPLETE)
    public Result<Boolean> completeSalesOrder(@PathVariable long salesOrderId) {
        return salesOrderService.completeSalesOrder(salesOrderId);
    }

    /**
     * 查询客户的财务信息: 累计订单金额、累计应收款、累计已收款
     *
     * 网址: http://localhost:8080/api/sales/customers/{customerId}/finance
     * 参数: 无
     *
     * @param customerId 客户 ID
     * @return payload 为客户的财务信息
     */
    @GetMapping(Urls.API_SALES_CUSTOMERS_FINANCE)
    public Result<CustomerFinance> findCustomerFinance(@PathVariable long customerId) {
        return Result.ok(salesOrderService.findCustomerFinance(customerId));
    }
}
