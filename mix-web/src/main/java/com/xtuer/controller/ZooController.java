package com.xtuer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.xtuer.bean.Page;
import com.xtuer.bean.Result;
import com.xtuer.service.BaseService;
import com.xtuer.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.*;

@Slf4j
@RestController
public class ZooController extends BaseController {
    /**
     * 自动注入在 Spring IoC 容器中类型为 BaseService 的对象 (instanceof)
     */
    @Autowired
    private BaseService[] baseServices;

    /**
     * 获取所有继承自 BaseService 的服务类的对象名称。
     *
     * 网址: http://localhost:8080/api/demo/bean-array
     * 参数: 无
     *
     * @return payload 为以逗号分隔的对象名称拼接的字符串。
     */
    @GetMapping("/api/demo/bean-array")
    public String getAllBaseServiceNames() {
        List<String> names = new LinkedList<>();

        for (BaseService bs : baseServices) {
            names.add(bs.getClass().getName());
        }

        return String.join("\n", names);
    }

    @GetMapping("/api/demo/jackson-date")
    public String jacksonDate() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<>();
        map.put("time", new Timestamp(new Date().getTime()));
        return objectMapper.writeValueAsString(map);
    }

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
     * 获取请求头
     */
    @GetMapping("/api/demo/headers")
    public Result<String> getHeaders(HttpServletRequest request) {
        String token = request.getHeader("token");
        return Result.ok(token);
    }

    /**
     * 测试 URL 和 URI 的区别 (都不带参数)
     * 访问: http://localhost:8080/api/demo/url-uri?name=biao&password=P@ssw0rd
     * URI: /api/demo/url-uri
     * URL: http://localhost:8080/api/demo/url-uri
     */
    @RequestMapping("/api/demo/url-uri")
    public Result<Map<String, Object>> differenceBetweenUrlAndUri(HttpServletRequest request) {
        log.info("URI: {}", request.getRequestURI());
        log.info("URL: {}", request.getRequestURL());
        Utils.dump(request.getParameterMap());

        return Result.ok(ImmutableMap.of(
                "URI", request.getRequestURI(),
                "URL", request.getRequestURL()
        ));
    }
}
