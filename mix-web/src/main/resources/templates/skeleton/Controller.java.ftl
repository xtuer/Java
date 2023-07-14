package ${package}.controller;

import com.xtuer.bean.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ${Type} 的控制器。
 */
@RestController
public class ${Type}Controller {
    @Autowired
    private ${Type}Service service;

    @GetMapping("/api/demo")
    public Result<String> getDemo() {
        return Result.ok();
    }
}
