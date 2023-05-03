package xtuer.controller;

import org.springframework.web.bind.annotation.*;
import xtuer.bean.User;

/**
 * 参数绑定测试的控制器。
 */
@RestController
public class BindController {
    /**
     * 缺少参数异常: org.springframework.web.bind.MissingServletRequestParameterException
     *
     * 测试: curl 'localhost:8080/api/bind/param-not-present'
     */
    @GetMapping("/api/bind/param-not-present")
    public String paramNotPresent(@RequestParam String username) {
        return username;
    }

    /**
     * 类型转换异常: org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
     *
     * 测试: curl 'localhost:8080/api/bind/cast-exception?count=abc'
     */
    @GetMapping("/api/bind/cast-exception")
    public Integer toInteger(@RequestParam int count) {
        return count;
    }

    /**
     * 接收为对象时 (没有使用注解 @Valid 或者 @Validated 时进行参数校验):
     * 1. 缺少参数不会报异常
     * 2. 参数不少，属性类型转换失败时抛异常: org.springframework.validation.BindException
     *
     * 提示: 属性 userId 使用 PathVariable 中的，参数里的 userId 会被忽略。
     *
     * 测试:
     *   正确: curl 'localhost:8080/api/bind/users/1'
     *   正确: curl 'localhost:8080/api/bind/users/1?username=Alice&password=Passw0rd'
     *
     *   异常: curl 'localhost:8080/api/bind/users/1x?username=Alice&password=Passw0rd'
     */
    @GetMapping("/api/bind/users/{userId}")
    public User getUser(User user) {
        return user;
    }

    /**
     * 同 getUser 情况一样。
     *
     * 提示: Form 表单里获取参数。
     *
     * 测试: curl -X POST 'localhost:8080/api/bind/users/1x' -d 'username=Alice&password=Passw0rd'
     */
    @PostMapping("/api/bind/users/{userId}")
    public User postUser(User user) {
        return user;
    }

    /**
     * 属性类型转换错误时抛异常: org.springframework.http.converter.HttpMessageNotReadableException
     *
     * 提示: 属性 userId 使用 request body 里的，PathVariable 中的会被忽略。
     *
     * 测试: curl -X PUT 'localhost:8080/api/bind/users/1x' -d '{"username":"Alice", "password":"Passw0rd", "userId": "1x"}' --header 'Content-Type: application/json'
     */
    @PutMapping("/api/bind/users/{userId}")
    public User putUser(@RequestBody User user) {
        return user;
    }
}
