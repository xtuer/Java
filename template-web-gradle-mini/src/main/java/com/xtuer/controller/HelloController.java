package com.xtuer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {
    @RequestMapping("/")
    @ResponseBody
    public String index() {
        return "Welcome";
    }

    @RequestMapping("/hello")
    public String hello(ModelMap model) {
//        model.put("name", "Biao");

        return "hello.htm";
    }
}