package com.xtuer.template;

import com.xtuer.bean.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Template 的控制器。
 */
@RestController
public class TemplateController {
    @Autowired
    private TemplateService service;

    @GetMapping("/api/template")
    public Result<String> getTemplate() {
        return Result.ok();
    }
}
