package newdt.dsc.controller;

import newdt.dsc.bean.DatabaseMetaDataConfig;
import newdt.dsc.bean.Response;
import newdt.dsc.config.DatabaseMetaDataConfigs;
import newdt.dsc.mapper.ZooMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class ZooController {
    @Autowired
    private DatabaseMetaDataConfigs configs;

    @Autowired
    private ZooMapper zooMapper;

    private AtomicInteger count = new AtomicInteger(0);

    /**
     * 测试: curl http://localhost:8080/api/demo/configs
     */
    @GetMapping("/api/demo/configs")
    public List<DatabaseMetaDataConfig> getConfigs() {
        return configs.getDatabaseMetaDataConfigs();
    }

    /**
     * 测试: curl http://localhost:8080/api/demo/dbtest
     */
    @GetMapping("/api/demo/dbtest")
    public Response<String> dbTest() {
        return Response.ok(zooMapper.test(1));
    }

    /**
     * 压力测试 (同样的条件下 wrk 能产生更多请求)。
     * 测试:
     *    curl http://localhost:8080/api/demo/pt
     *    wrk -c 32 -t 10 'http://localhost:8080/api/demo/pt'
     *    ab -c 32 -t 10 'http://localhost:8080/api/demo/pt'
     */
    @GetMapping("/api/demo/pt")
    public Response<Integer> pt() {
        return Response.ok(count.getAndAdd(1));
    }

    /**
     * 获取压测的计数。
     * 测试: curl http://localhost:8080/api/demo/ptCount
     */
    @GetMapping("/api/demo/ptCount")
    public Response<Integer> ptCount() {
        return Response.ok(count.get());
    }
}
