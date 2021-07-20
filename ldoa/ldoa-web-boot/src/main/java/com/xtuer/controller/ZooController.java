package com.xtuer.controller;

import com.github.wujun234.uid.impl.CachedUidGenerator;
import com.xtuer.bean.Page;
import com.xtuer.bean.Result;
import com.xtuer.bean.order.Order;
import com.xtuer.exception.ApplicationException;
import com.xtuer.mapper.CommonMapper;
import com.xtuer.service.CommonService;
import com.xtuer.service.OrderService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
public class ZooController extends BaseController {
    @Autowired
    private CommonService commonService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CommonMapper commonMapper;

    /**
     * 把字符串自动转为日期
     *
     * 网址:
     *      http://localhost:8080/api/demo/string2date?date=2020-01-01
     *      http://localhost:8080/api/demo/string2date?date=2020-10-04%2012:10:00
     *
     * 参数: date [String]: 字符串格式的日期
     */
    @GetMapping("/api/demo/string2date")
    public Date convertStringToDate(@RequestParam Date date) {
        return date;
    }

    /**
     * 查看异常响应
     *
     * 网址: http://localhost:8080/api/demo/exception
     */
    @GetMapping("/api/demo/exception")
    public String exception() {
        throw new RuntimeException();
    }

    /**
     * 测试 POST 请求中有中文 (默认应该使用了 UTF-8)
     *
     * 网址: http://localhost:8080/api/demo/encoding
     * 参数: name [String]: 中文字符串
     */
    @PostMapping("/api/demo/encoding")
    public Result<String> encoding(@RequestParam String name) {
        return Result.ok(name);
    }

    /**
     * 测试 POST 请求中有中文 (默认应该使用了 UTF-8)
     *
     * 网址:
     *      http://localhost:8080/api/demo/page
     *      http://localhost:8080/api/demo/page?pageNumber=2&pageSize=5
     * 参数:
     *      pageNumber [可选]: 页码，默认为 1
     *      pageSize   [可选]: 数量，默认为 10
     */
    @GetMapping("/api/demo/page")
    public Result<Page> paging(Page page) { // 不要使用 @RequestParam Page page
        return Result.ok(page);
    }

    /**
     * 获取分布式唯一 ID
     *
     * 网址: http://localhost:8080/api/uid
     * 参数: 无
     *
     * @return payload 为 ID
     */
    @GetMapping("/api/uid")
    public Result<Long> uid() {
        return Result.ok(nextId());
    }

    /**
     * 前端传数组到服务器端
     *
     * 前端发送请求:
     * const ids = [1, 2, 3];
     * Rest.get({ url: '/api/demo/array', data: { ids } })
     *
     * 网址: http://localhost:8080/api/demo/array?ids=1&ids=2&ids=3
     * 参数: ids: 数组
     */
    @GetMapping("/api/demo/array")
    public Result<List<Integer>> array(@RequestParam List<Integer> ids) {
        return Result.ok(ids);
    }

    /**
     * 获取下一个序列号
     *
     * 网址: http://localhost:8080/api/demo/nextSequence
     * 参数: 无
     *
     * @return payload 为序列号
     */
    @GetMapping("/api/demo/nextSequence")
    public Result<Integer> nextSequence() {
        // XSDD-20200806-0001
        String date = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now());
        String name = "DEMO-" + date;
        System.out.println(StringUtils.leftPad("23", 4, "0"));

        return Result.ok(commonService.nextSequence(name));
    }

    /**
     * 获取下一个订单号
     *
     * 网址: http://localhost:8080/api/demo/nextOrderSn
     * 参数: 无
     *
     * @return payload 为序列号
     */
    @GetMapping("/api/demo/nextOrderSn")
    public Result<String> nextOrderSn() {
        return Result.ok(orderService.nextOrderSn());
    }

    /**
     * 测试 XA 命令
     *
     * 网址: http://localhost:8080/api/xa-test
     * 参数: 无
     */
    @SuppressWarnings("rawtypes")
    @GetMapping("/api/xa-test")
    public Result<Map> xaTest() {
        return Result.ok(commonMapper.xaTest());
    }

    /**
     * 测试 @Valid 校验不通过时抛出异常
     *
     * 网址: http://localhost:8080/api/demo/order-invalid
     * 参数: 可有可无
     *
     * @param order 订单
     */
    @GetMapping("/api/demo/order-invalid")
    public Result<String> validDemo(@Valid Order order) {
        return Result.ok();
    }

    /**
     * 参数无效时抛出 IllegalArgumentException 异常，验证响应的消息
     *
     * 网址: http://localhost:8080/api/demo/illegal-argument
     */
    @GetMapping("/api/demo/illegal-argument")
    public Result<String> illegalArgument() {
        throw new IllegalArgumentException("参数无效");
    }

    /**
     * Application 异常
     *
     * 网址: http://localhost:8080/api/demo/application-exception
     */
    @GetMapping("/api/demo/application-exception")
    public Result<String> applicationException() {
        throw new ApplicationException("应用程序异常", 505);
    }

    /**
     * 类加载器判断
     *
     * 网址: http://localhost:8080/api/demo/class-loader
     */
    @GetMapping("/api/demo/class-loader")
    public Result<String> testClassLoader() {
        return Result.ok(CachedUidGenerator.class.getClassLoader().getClass().getName());
    }
}
