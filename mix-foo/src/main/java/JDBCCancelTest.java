import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

public class JDBCCancelTest {
    static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false";
    static final String USER   = "root";
    static final String PASS   = "root";

    public static void main(String[] args) {
        try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Statement stmt = conn.createStatement();

            // [2] 线程中模拟手动点击取消执行。
            new Timer(true).schedule(new TimerTask() {
                public void run() {
                    try {
                        System.out.println("CLOSE");
                        stmt.cancel();
                        // stmt.close(); // 不生效
                        // conn.close(); // 不生效
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }, 3000);

            // [1] 模拟耗时 SQL。
            ResultSet rs = stmt.executeQuery("select sleep(10)");

            while (rs.next()) {
                System.out.println(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
