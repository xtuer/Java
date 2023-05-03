package xtuer.controller;

import org.springframework.web.bind.annotation.*;
import xtuer.bean.User;

import javax.validation.Valid;

@RestController
public class ValidController {
    /**
     * 异常: org.springframework.validation.BindException
     *
     * 测试: curl 'localhost:8080/api/valid/users/1?password=Passw0rd'
     */
    @GetMapping("/api/valid/users/{userId}")
    public User getUser(@Valid User user) {
        return user;
    }

    /**
     * 异常: org.springframework.validation.BindException
     *
     * 测试: curl -X POST 'localhost:8080/api/valid/users/1' -d 'username=&password=Passw0rd'
     */
    @PostMapping("/api/valid/users/{userId}")
    public User postUser(@Valid User user) {
        return user;
    }

    /**
     * 异常: org.springframework.web.bind.MethodArgumentNotValidException
     *
     * 测试: curl -X PUT 'localhost:8080/api/valid/users/1' -d '{"username":"", "password":"Passw0rd", "userId": "1"}' --header 'Content-Type: application/json'
     */
    @PutMapping("/api/valid/users/{userId}")
    public User putUser(@RequestBody @Valid User user) {
        return user;
    }
}
