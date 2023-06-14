import com.google.common.io.Files;
import org.junit.Test;
import util.Utils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.*;

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

    // 测试创建存储函数 (不能带 DELIMITER)。
    @Test
    public void testCreateFunction1() {
        String url = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false";
        String user = "root";
        String pass = "root";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String sql = Files.asCharSource(new File("/Users/biao/Documents/temp/sqls/mysql_func_2.sql"), StandardCharsets.UTF_8).read();
            System.out.println(sql);
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 测试创建存储函数 (不能带 DELIMITER)。
    @Test
    public void testCreateProcedure() {
        String url = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false";
        String user = "root";
        String pass = "root";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String sql = Files.asCharSource(new File("/Users/biao/Documents/temp/sqls/mysql_proc_2.sql"), StandardCharsets.UTF_8).read();
            System.out.println(sql);
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMultipleSqls() {
        String url = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false";
        String user = "root";
        String pass = "root";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String sql = "insert into sp_test(id, name, age) values(11, 'eleven', 101); select * from sp_test;";
            Statement stmt = conn.createStatement();
            stmt.execute(sql);

            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                Utils.dump(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFunctionExist() throws Exception {
        String url = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false&allowMultiQueries=true";
        String user = "root";
        String pass = "root";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            DatabaseMetaData meta = conn.getMetaData();

            try (ResultSet rs = meta.getFunctionColumns("test", null, "func_no_arg", null)) {
                System.out.println(111);

                while (rs.next()) {
                    String argName         = rs.getString("COLUMN_NAME");   // 参数名称
                    int originalPosition   = rs.getInt("ORDINAL_POSITION"); // 参数原始位置
                    int argTypeValue       = rs.getInt("COLUMN_TYPE");      // 入参出参:  1 (IN), 4 (OUT), 2 (INOUT)
                    int argDataTypeValue   = rs.getInt("DATA_TYPE");        // 参数的数据类型值: SQL type from java.sql.Types
                    String argDataTypeName = rs.getString("TYPE_NAME");     // 参数的数据类型名: SQL type name, for a UDT type the type name is fully qualified
                    int length             = rs.getInt("LENGTH");           // 长度
                    int precision          = rs.getInt("PRECISION");        // 精度
                    short scale            = rs.getShort("SCALE");          // 标度

                    System.out.println("x:" + argName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
