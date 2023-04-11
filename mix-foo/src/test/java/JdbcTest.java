import java.sql.*;
import org.junit.Test;

public class JdbcTest {
    static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/mm_table?useSSL=false";
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

    @Test
    public void testBatchWithSelect() {
        try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            // conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            String sql = "create table xyz(id int)";
            stmt.addBatch(sql);
            stmt.addBatch("insert into xyz(id) values(1)");
            stmt.addBatch("insert into xyz(id) values(2)");
            stmt.executeBatch();
            stmt.clearBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateTables() {
        String url = "jdbc:oracle:thin:@//192.168.11.49:1521/orcl";
        String user = "system";
        String pass = "sys";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            conn.setSchema("ANONYMOUS");
            String sql = "create table mm_table%d(id int, name varchar(128))";

            for (int i = 1; i < 1000; i++) {
                System.out.println(i);
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(String.format(sql, i));
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFoo() {
        String url = "jdbc:oracle:thin:@//192.168.11.49:1521/orcl";
        String user = "system";
        String pass = "sys";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            conn.setSchema("ANONYMOUS");
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getTables(null, "ANONYMOUS", "%", new String[]{"TABLE"});
            rs.setFetchSize(10000);
            System.out.println("start........");

            int count = 0;
            while (rs.next()) {
                count++;
                String tableName = rs.getString("TABLE_NAME");
                System.out.println(tableName);
            }
            System.out.println(count);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
