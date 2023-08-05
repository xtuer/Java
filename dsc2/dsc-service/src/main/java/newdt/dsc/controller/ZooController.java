package newdt.dsc.controller;

import lombok.Data;
import newdt.dsc.bean.Response;
import newdt.dsc.bean.Urls;
import newdt.dsc.bean.db.DatabaseType;
import newdt.dsc.mapper.ZooMapper;
import newdt.dsc.service.db.TestConnections;
import org.javatuples.Quartet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class ZooController {
    @Autowired
    private ZooMapper zooMapper;

    private AtomicInteger count = new AtomicInteger(0);

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

    static final String DB_URL  = "jdbc:mysql://127.0.0.1:3306/meta_test_catalog?useSSL=false";
    static final String DB_USER = "root";
    static final String DB_PASS = "root";

    /**
     * 数据库连接压力测试。
     * 测试:
     *     curl http://localhost:8080/api/demo/pt/jdbc
     *     wrk -c 32 -t 20 -d 20 'http://localhost:8080/api/demo/pt/jdbc'
     */
    @GetMapping("/api/demo/pt/jdbc")
    public Response<String> ptJdbc() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            Random rand = new Random();
            int id = rand.nextInt(20);
            String sql = "SELECT id, name, age FROM meta_test_table WHERE id = " + id;

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return Response.ok(rs.getString("name"));
            }
        }

        return Response.fail();
    }

    /**
     * 使用 MyBatis 访问数据库。
     * 测试:
     *    curl http://localhost:8080/api/demo/pt/pool
     *    wrk -c 32 -t 20 -d 20 'http://localhost:8080/api/demo/pt/pool'
     */
    @GetMapping("/api/demo/pt/pool")
    public Response<String> dbTest() {
        Random rand = new Random();
        int id = rand.nextInt(20);
        return Response.ok(zooMapper.test(id));
    }

    /**
     * 获取测试的数据库信息。
     *
     * 测试: curl 'http://localhost:8080/api/moc/dsc/databases/instances?type=MYSQL'
     *
     * @param type 数据库类型。
     * @return payload 为数据库信息数组。
     */
    @GetMapping(Urls.API_MOC_DATABASE_INSTANCES)
    public Response<List<Integer>> findDatabaseInstances(@RequestParam DatabaseType type) {
        List<Integer> dbids = new LinkedList<>();

        TestConnections.CONNECT_INFO.forEach((t, dbs) -> {
            if (t.equals(type)) {
                for (Quartet<Integer, String, String, String> info : dbs) {
                    dbids.add(info.getValue0());
                }
            }
        });

        return Response.ok(dbids);
    }
}
