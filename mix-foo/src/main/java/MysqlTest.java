import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;

/**
 * 访问 MySQL
 */
public class MysqlTest {
    public static void main(String[] args) throws IOException, SQLException {
        System.out.println("开始时间: " + LocalDateTime.now());

        try (Connection conn = createConnection()) {
            // String sql = CONFIG.getProperty("sql");
            String sql = readAsString("sql.txt");
            System.out.println("开始执行 SQL 语句: " + sql);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            while (rs.next()) {
                for (int i = 1; i <= columnCount; ++i) {
                    if (rsmd.getColumnType(i) == Types.VARCHAR) {
                        System.out.print(rs.getString(i));
                    } else {
                        System.out.print(rs.getObject(i));
                    }

                    System.out.print("\t");
                }
                System.out.println();
            }
        } finally {
            System.out.println("结束时间: " + LocalDateTime.now());
        }
    }

    public static String readAsString(String path) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(MysqlTest.class.getClassLoader().getResourceAsStream(path), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line = null;

            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }

            return sb.toString();
        }
    }

    /**
     * 访问 MySQL 的配置
     */
    private static final Properties CONFIG;

    // 加载配置
    static {
        CONFIG = new Properties();
        try {
            CONFIG.load(ClassLoader.getSystemResourceAsStream("mysql.properties"));
        } catch (IOException e) {
            throw new ExceptionInInitializerError("加载配置文件 mysql.properties 错误");
        }
    }

    /**
     * 创建数据库连接
     *
     * @return 返回数据库连接
     */
    public static Connection createConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", CONFIG.getProperty("username"));
        props.setProperty("password", CONFIG.getProperty("password"));
        String jdbcUrl = CONFIG.getProperty("jdbc-url");

        System.out.println(props);
        System.out.println(jdbcUrl);

        return DriverManager.getConnection(jdbcUrl, props);
    }
}
