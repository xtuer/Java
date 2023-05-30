import java.sql.*;
import org.junit.Test;

public class JdbcTest {
    static final String DB_URL = "jdbc:mysql://192.168.1.163:3306/ndtmdb?useSSL=false";
    static final String USER   = "sys_admin";
    static final String PASS   = "manager";

    @Test
    public void test() {
        try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            // conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            String sql = "select * from db_monconf_interactive where funcname='ORA_DATA_TBSPTABLE'";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                System.out.println(rs.getObject(1));
            }
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
        // String url = "jdbc:oracle:thin:@//192.168.11.49:1521/orcl";
        // String user = "system";
        // String pass = "sys";

        String url = "jdbc:mysql://192.168.12.21:35004/test?useSSL=false";
        String user = "root";
        String pass = "mypass";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            // conn.setSchema("ANONYMOUS");
            String sql = "create table mm_table%d(id int, name varchar(128))";

            conn.setAutoCommit(false);
            for (int i = 1; i < 3000; i++) {
                System.out.println(i);
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(String.format(sql, i));
                stmt.close();

                if (i % 300 == 0) {
                    conn.commit();
                }
            }

            conn.commit();
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
