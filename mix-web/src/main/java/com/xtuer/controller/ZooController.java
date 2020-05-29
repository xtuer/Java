package com.xtuer.controller;

import com.alibaba.fastjson.JSON;
import com.xtuer.bean.EB;
import com.xtuer.bean.Page;
import com.xtuer.bean.Result;
import com.xtuer.bean.User;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
public class ZooController extends BaseController {
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
     * 网址: http://localhost:8080/api/enum/x?id=123&username=alice&role=STUDENT
     */
    @GetMapping("/api/enum/x")
    public Result<EB> enum1(EB user) {
        System.out.println(JSON.toJSONString(user, true));
        return Result.ok(user);
    }

    /**
     * 网址: http://localhost:8080/api/enum/y?role=ADMIN
     */
    @GetMapping("/api/enum/y")
    @ResponseBody
    public Result<EB.Role> enum1(@RequestParam(name = "role") EB.Role role) {
        System.out.println("enum2: " + role);
        return Result.ok(role);
    }
}
