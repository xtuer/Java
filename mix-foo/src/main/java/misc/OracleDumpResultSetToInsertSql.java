package misc;

import java.sql.*;
import java.util.Properties;

/**
 * 把 ResultSet 导出为 INSERT 语句
 */
public class OracleDumpResultSetToInsertSql {
    static final String DB_URL = "jdbc:oracle:thin:@//192.168.12.16:31002/orcl";
    static final String USER   = "system";
    static final String PASS   = "system";

    public static void main(String[] args) throws Exception {
        String schema = "DBMON";
        String selectSql = "select * from test";
        String tableName = "test_123";
        String insertSqls = OracleDumpResultSetToInsertSql.dumpInsertSqls(schema, selectSql, tableName);
        System.out.println(insertSqls);
    }

    /**
     * 把查询语句 selectSql 的结果集导出为 INSERT 语句。
     *
     * @param selectSql 查询 SQL 语句
     * @param targetTableName 导出的表名
     */
    public static String dumpInsertSqls(String schema, String selectSql, String targetTableName) throws SQLException {
        try (Connection conn = createConnection()) {
            conn.setSchema(schema);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(selectSql);

            String insertSqls = OracleDumpResultSetToInsertSqlUtils.dumpResultSetToString(rs, targetTableName);
            return insertSqls;
        }
    }

    /**
     * 创建数据库连接
     *
     * @return 返回数据库连接
     */
    public static Connection createConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", USER);
        props.setProperty("password", PASS);
        props.setProperty("oracle.jdbc.ReadTimeout", 10_000 + ""); // 10 秒
        props.setProperty("oracle.net.CONNECT_TIMEOUT", 10_000 + "");

        return DriverManager.getDriver(DB_URL).connect(DB_URL, props);
    }
}
