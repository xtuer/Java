package newdt.dsc2.controller;

import newdt.dsc2.bean.DatabaseMetaDataConfig;
import newdt.dsc2.config.DatabaseMetaDataConfigs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ZooController {
    @Autowired
    private DatabaseMetaDataConfigs configs;

    /**
     * 测试: curl http://localhost:8080/api/demo/configs
     */
    @GetMapping("/api/demo/configs")
    public List<DatabaseMetaDataConfig> getConfigs() {
        return configs.getDatabaseMetaDataConfigs();
    }
}
