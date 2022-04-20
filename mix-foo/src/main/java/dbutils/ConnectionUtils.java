package dbutils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionUtils {
    static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false";
    static final String USER   = "root";
    static final String PASS   = "root";

    /**
     * 获取数据库连接
     *
     * @return 返回数据库连接
     * @throws SQLException 连接失败时抛出异常
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
}
