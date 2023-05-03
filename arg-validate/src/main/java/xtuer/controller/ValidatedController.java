package xtuer.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xtuer.bean.User;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;

/**
 * 参数验证的控制器。
 * 1. @validated 校验单个参数
 * 2. @Validated 校验对象: Form + Json
 * 3. @Valid 校验对象: Form + Json
 */
@RestController
@Validated
public class ValidatedController {
    /**
     * 校验异常: javax.validation.ConstraintViolationException
     *
     * 提示:
     * A. 先绑定参数，然后才校验参数。
     * B. Controller 方法上要对单个参数进行校验时，需要把 @Validated 注解放到 Controller 类上，而不是方法上。
     *
     * 测试: curl 'localhost:8080/api/validated/param-not-blank?username=&count=20'
     */
    @GetMapping("/api/validated/param-not-blank")
    public String paramNotBlank(@RequestParam @NotBlank(message = "Username 不能为空") String username,
                                @RequestParam @Max(value = 10, message = "Count 不能大于 10") int count) {
        return username;
    }

    /**
     * 异常: org.springframework.validation.BindException
     *
     * 测试: curl 'localhost:8080/api/validated/users/1?password=Passw0rd'
     */
    @GetMapping("/api/validated/users/{userId}")
    public User getUser(@Validated User user) {
        return user;
    }

    /**
     * 异常: org.springframework.validation.BindException
     *
     * 测试: curl -X POST 'localhost:8080/api/validated/users/1' -d 'username=&password=Passw0rd'
     */
    @PostMapping("/api/validated/users/{userId}")
    public User postUser(@Validated User user) {
        return user;
    }

    /**
     * 异常: org.springframework.web.bind.MethodArgumentNotValidException
     *
     * 测试: curl -X PUT 'localhost:8080/api/validated/users/1' -d '{"username":"", "password":"Passw0rd", "userId": "1"}' --header 'Content-Type: application/json'
     */
    @PutMapping("/api/validated/users/{userId}")
    public User putUser(@RequestBody @Validated User user) {
        return user;
    }
}

