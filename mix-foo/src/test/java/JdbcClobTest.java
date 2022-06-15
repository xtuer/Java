import org.junit.Test;

import java.io.FileReader;
import java.sql.*;

/**
 * 测试 JDBC Clob, MySQL 驱动的 Clob 底层存储的是 String 类型的 charData,大字段效率比较低.
 *
 * MySQL 数据类型 mediumtext:
 * * JDBC 使用 Clob 从文件读取内容插入数据: OK, setClob 的底层是把文件读取为 String 或者 bytes 再插入
 * * JDBC 使用 String 插入数据: OK
 * * Clob 可以获取 Stream,然后把其内容写入文件
 */
public class JdbcClobTest {
    static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false";
    static final String USER   = "root";
    static final String PASS   = "root";

    /**
     * 使用 clob 写到 mediumtext
     */
    @Test
    public void testWriteViaString() throws Exception {
        try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "INSERT INTO clob_test (content) VALUES(?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "你好昨天"); // OK
            pstmt.execute();
        }
    }

    /**
     * 使用 string 写到 mediumtext
     */
    @Test
    public void testWriteViaClob() throws Exception {
        try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "INSERT INTO clob_test (content) VALUES(?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            FileReader reader = new FileReader("/Users/biao/Downloads/java.txt"); // pstmt 关闭的时候，reader 会被自动关闭
            pstmt.setClob(1, reader); // OK
            pstmt.execute();
        }
    }

    /**
     * 使用 clob 读取 mediumtext
     */
    @Test
    public void testReadViaClob() throws Exception {
        try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "SELECT * FROM clob_test";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Clob clob = rs.getClob(1);
                String content = clobToString(clob);
                System.out.println(content);
                System.out.println("-----------------------------");
            }
        }
    }

    /**
     * 使用 clob 读取 mediumtext
     */
    @Test
    public void testReadViaString() throws Exception {
        try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "SELECT * FROM clob_test";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                System.out.println(rs.getString(1));
                System.out.println("-----------------------------");
            }
        }
    }

    private static String clobToString(Clob clob) throws SQLException {
        return clob.getSubString(1, (int) clob.length());
    }
}
