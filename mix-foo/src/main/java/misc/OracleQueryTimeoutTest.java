package misc;

import oracle.jdbc.pool.OracleDataSource;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Properties;

/**
 * 测试访问 Oracle setQueryTimeout 是否生效
 */
public class OracleQueryTimeoutTest {
    public static void main(String[] args) throws IOException, SQLException {
        System.out.println("开始时间: " + LocalDateTime.now());

        // try (Connection conn = createConnectionByOds()) {
        try (Connection conn = createConnectionByDriver()) {
            int queryTimeout = Integer.parseInt(CONFIG.getProperty("query-timeout"));
            String sqlPrefix = String.format("/* ndt-%s */ ", LocalTime.now());
            String sql = sqlPrefix + CONFIG.getProperty("sql");

            System.out.println("超时时间: " + queryTimeout);
            System.out.println("开始执行 SQL 语句: " + sql);

            Statement stmt = conn.createStatement();
            stmt.setQueryTimeout(queryTimeout); // 设置语句执行的超时时间
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                System.out.println(rs.getObject(1));
            }
        } finally {
            System.out.println("结束时间: " + LocalDateTime.now());
        }
    }

    /**
     * 访问 Oracle 的配置
     */
    private static final Properties CONFIG;

    // 加载配置
    static {
        CONFIG = new Properties();
        try {
            CONFIG.load(ClassLoader.getSystemResourceAsStream("oracle.properties"));
        } catch (IOException e) {
            e.printStackTrace();

            throw new ExceptionInInitializerError("加载配置文件 oracle.properties 错误");
        }
    }

    /**
     * 创建数据库连接
     *
     * @return 返回数据库连接
     */
    public static Connection createConnectionByDriver() throws SQLException {
        int readTimeout = Integer.parseInt(CONFIG.getProperty("read-timeout")) * 1000;
        int connectTimeout = Integer.parseInt(CONFIG.getProperty("connect-timeout")) * 1000;

        Properties props = new Properties();
        props.setProperty("user", CONFIG.getProperty("username"));
        props.setProperty("password", CONFIG.getProperty("password"));
        props.setProperty("oracle.jdbc.ReadTimeout", readTimeout + "");
        props.setProperty("oracle.net.CONNECT_TIMEOUT", connectTimeout + "");

        String jdbcUrl = CONFIG.getProperty("jdbc-url");
        return DriverManager.getDriver(jdbcUrl).connect(jdbcUrl, props);
    }

    /**
     * 创建数据库连接
     *
     * @return 返回数据库连接
     */
    public static Connection createConnectionByOds() throws SQLException {
        // 设置 Oracle 连接信息，创建数据源
        OracleDataSource ods = new OracleDataSource();

        Properties props = new Properties();
        props.setProperty("user", CONFIG.getProperty("username"));
        props.setProperty("password", CONFIG.getProperty("password"));
        ods.setConnectionProperties(props);

        String jdbcUrl = CONFIG.getProperty("jdbc-url");
        ods.setURL(jdbcUrl);

        return ods.getConnection();
    }
}
