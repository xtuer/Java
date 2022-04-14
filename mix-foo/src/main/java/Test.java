import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Data
@Slf4j
public class Test {
    static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/ldoa?useSSL=false";
    static final String USER   = "root";
    static final String PASS   = "root";

    public static void main(String[] args) {
        try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Statement stmt = conn.createStatement();
            // stmt.setQueryTimeout(5); // 设置语句执行的超时时间
            stmt.setMaxRows(10);

            ResultSet rs = stmt.executeQuery("select * from message");

            while (rs.next()) {
                System.out.println("message_id: " + rs.getLong(1));
            }

            // query(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void query(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        // stmt.setQueryTimeout(5); // 设置语句执行的超时时间

        ResultSet rs = stmt.executeQuery("select sleep(10)");

        while (rs.next()) {
            System.out.println("In Thread: " + rs.getInt(1));
        }
    }
}
