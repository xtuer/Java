import com.google.common.base.Stopwatch;
import com.google.common.io.Files;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.concurrent.TimeUnit;

public class Test {
    static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false";
    static final String USER   = "root";
    static final String PASS   = "root";

    public static void main(String[] args) throws Exception {
        Stopwatch watch = Stopwatch.createStarted();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Statement stmt = conn.createStatement();
            String sql = Files.asCharSource(new File("/Users/biao/Desktop/bs-1.sql"), StandardCharsets.UTF_8).read();
            int count = stmt.executeUpdate(sql);
            System.out.println(count);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(watch.elapsed(TimeUnit.MILLISECONDS));
    }
}
