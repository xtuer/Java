package dbutils;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.StatementConfiguration;
import org.apache.commons.dbutils.handlers.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 作用:
 * 1. 写 SQL
 * 2. QueryRunner 执行 SQL 并用 ResultSetHandler 处理为目标类型，不需要自己创建 Statement 和直接处理 ResultSet
 *    核心类是 QueryRunner 和 ResultSetHandler
 *    query 方法返回 ResultSetHandler#handle 的结果
 *    batch 可以批量更新或插入
 *
 * 主要的 ResultSetHandler 有:
 * * BeanHandler      : 只取第一行转为 Bean
 * * BeanListHandler  : 每行转为一个 Bean
 * * MapListHandler   : 每行转为一个 Map
 * * ScalarHandler    : 只有一行返回值
 * * ColumnListHandler: 每行取一列
 *
 * 参考: https://www.yiibai.com/dbutils/dbutils_maplisthandler_class.html
 *      https://commons.apache.org/proper/commons-dbutils/examples.html
 */
public class QueryRunnerTest {
    public static void main(String[] args) throws SQLException {
        // testBean();
        // testBeanList();
        // testScalar();
        // testScalar2();
        testStmtConfig();
        // testCustomResultSetHandler();
        // testMapList();
        // testColumnList();
    }

    public static void testBean() throws SQLException {
        try (Connection conn = ConnectionUtils.getConnection()) {
            // A. 泛型 <User> 表示要处理的类型
            // B. 构造函数里传入 User.class 是因为要使用 Class 在内部创建 User 对象，泛型 <User> 不能创建对象
            ResultSetHandler<User> handler = new BeanHandler<>(User.class);
            // 有行匹配时只取第一行，handler 后面是 varargs 参数
            User user = new QueryRunner().query(conn, "SELECT id, name FROM test WHERE id=?", handler, 2);
            System.out.println(user);
        }
    }

    public static void testBeanList() throws SQLException {
        try (Connection conn = ConnectionUtils.getConnection()) {
            ResultSetHandler<List<User>> handler = new BeanListHandler<>(User.class);
            List<User> users = new QueryRunner().query(conn, "SELECT id, name FROM test", handler);
            System.out.println(users);
        }
    }

    public static void testScalar() throws SQLException {
        try (Connection conn = ConnectionUtils.getConnection()) {
            ResultSetHandler<Long> handler = new ScalarHandler<>();
            long count = new QueryRunner().query(conn, "SELECT count(id) FROM test", handler);
            System.out.println(count);
        }
    }

    public static void testScalar2() throws SQLException {
        try (Connection conn = ConnectionUtils.getConnection()) {
            ResultSetHandler<Integer> handler = new ScalarHandler<>();
            int id = new QueryRunner().query(conn, "SELECT id FROM test", handler);
            System.out.println(id);
        }
    }

    /**
     * 自定义 ResultSetHandler
     */
    public static void testCustomResultSetHandler() throws SQLException {
        try (Connection conn = ConnectionUtils.getConnection()) {
            // handle 方法返回一行对应的对象
            ResultSetHandler<Integer> handler = rs -> {
                if (!rs.next()) {
                    return 0;
                }

                return rs.getInt(1);
            };

            int count = new QueryRunner().query(conn, "SELECT count(id) FROM test", handler);
            System.out.println(count);
        }
    }

    public static void testMapList() throws SQLException {
        try (Connection conn = ConnectionUtils.getConnection()) {
            // 一行转换为一个 key value 的 map
            List<Map<String, Object>> users = new QueryRunner().query(conn, "SELECT id, name FROM test", new MapListHandler());
            System.out.println(users);
        }
    }

    public static void testColumnList() throws SQLException {
        try (Connection conn = ConnectionUtils.getConnection()) {
            // 获取某一列的数据
            ResultSetHandler<List<String>> handler = new ColumnListHandler<>("name");
            List<String> names = new QueryRunner().query(conn, "SELECT name FROM test", handler);
            System.out.println(names);
        }
    }

    public static void testStmtConfig() throws SQLException {
        try (Connection conn = ConnectionUtils.getConnection()) {
            // 可配置 fetchSize, maxRows, queryTimeout 等
            StatementConfiguration config = new StatementConfiguration.Builder().queryTimeout(5).build();
            ResultSetHandler<Long> handler = new ScalarHandler<>(1);
            long r = new QueryRunner(config).query(conn, "SELECT sleep(10)", handler);
            System.out.println(r);
        }
    }
}
