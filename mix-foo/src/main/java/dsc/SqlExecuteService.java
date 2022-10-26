package dsc;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * SQL 执行服务
 */
@Slf4j
public class SqlExecuteService {
    static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/gateway?useSSL=false";
    static final String USER   = "root";
    static final String PASS   = "root";

    /**
     * 查询 SQL 返回结果。
     *
     * @param meta 数据库信息对象
     * @param sql  要执行的 SQL 语句
     * @return SQL 语句的查询结果: 每一行是一个 Map，Map 的 key 为列名，value 为列值
     */
    public List<Map<String, Object>> select(MetaVo meta, String sql) {
        log.info("SQL: {}", sql);

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            return new QueryRunner().query(conn, sql, new MapListHandler());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
