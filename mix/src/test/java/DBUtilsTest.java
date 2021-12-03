import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.junit.Test;

import java.sql.*;
import java.util.Date;
import java.util.List;

public class DBUtilsTest {
    /**
     * 测试访问 Postgres
     */
    @Test
    public void testPg() throws Exception {
        String url  = "jdbc:postgresql://192.168.12.205/postgres?ssl=false";
        String user = "postgres";
        String pass = "testpassword";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String sql = "select * from test where id=?";
            query(conn, sql);
        }
    }

    /**
     * 测试访问 openGauss
     * openGauss 使用用户模式: 默认访问用户 (schema) dbpaasop 下的表
     * postgres 默认访问 schema public
     */
    @Test
    public void testOg() throws Exception {
        String url  = "jdbc:opengauss://192.168.1.73:30223/postgres?ssl=false";
        String user = "dbpaasop";
        String pass = "K8S@admin";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String sql = "select * from test where id = ?";
            query(conn, sql);
        }
    }

    /**
     * 使用修改过的 postgresql-og.jar 测试访问 openGauss
     * openGauss 使用用户模式: 默认访问用户 (schema) dbpaasop 下的表
     * postgres 默认访问 schema public
     */
    @Test
    public void testOgWithPgDriver() throws Exception {
        String url  = "jdbc:postgresql://192.168.1.73:30223/postgres?ssl=false";
        String user = "dbpaasop";
        String pass = "K8S@admin";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            // 切换 schema，默认是用户名的 schema
            conn.setSchema("public");
            String sql = "\\dt";
            query(conn, sql);
        }
    }

    /**
     * 测试访问 MySQL
     */
    @Test
    public void testMysql() throws Exception {
        String url  = "jdbc:mysql://127.0.0.1:3306/gateway?useUnicode=true&characterEncoding=UTF-8&useSSL=false";
        String user = "root";
        String pass = "root";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String sql = "show tables";
            query(conn, sql);
        }
    }

    /**
     * 执行查询语句
     */
    public void query(Connection conn, String sql) throws Exception {
        QueryRunner qr = new QueryRunner();
        List<Object[]> result = qr.query(conn, sql, new ArrayListHandler());

        for (Object[] objs : result) {
            //遍历对象数组
            for (Object obj : objs) {
                System.out.print(obj + ",");
            }
            System.out.println();
        }
    }

    /**
     * 测试访问 MySQL
     */
    @Test
    public void testMysql2() throws Exception {
        String url  = "jdbc:mysql://127.0.0.1:3306/gateway?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowMultiQueries=true";
        String user = "root";
        String pass = "root";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String sql = "SELECT dt FROM test";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                System.out.println(rs.getTimestamp(1).getTime());
            }
        }
    }

    @Test
    public void testMySQLInsertTime() throws Exception {
        String url  = "jdbc:mysql://127.0.0.1:3306/gateway?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowMultiQueries=true";
        String user = "root";
        String pass = "root";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String sql = "INSERT INTO test(dt) VALUES(?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            Date date = new Date(); // 1638335652963
            System.out.println(date.getTime());
            stmt.setObject(1, date);
            stmt.executeUpdate();
        }
    }
}
