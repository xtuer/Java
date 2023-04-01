import java.sql.*;
import org.junit.Test;

public class JdbcTest {
    static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false";
    static final String USER   = "root";
    static final String PASS   = "root";

    @Test
    public void test() {
        try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            // conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            stmt.setQueryTimeout(5); // 设置语句执行的超时时间
            String sql = "-- Comments\n"
                    + "insert into test(id, name) values(5, /* comments */'Biao''s Huang');";
            // sql = "sleep 1";
            sql += "SET NAMES utf8mb4;";
            stmt.execute(sql);
            // System.out.println(ar);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
