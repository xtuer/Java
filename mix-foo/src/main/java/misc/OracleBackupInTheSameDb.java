package misc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Oracle 同库备份
 */
public class OracleBackupInTheSameDb {
    static final String DB_URL = "jdbc:oracle:thin:@//192.168.12.16:31002/orcl";
    static final String USER   = "system";
    static final String PASS   = "system";

    public static void main(String[] args) throws Exception {
        backup("dbmon", "update test set name='Hello'");
    }

    /**
     * 对传入的 dmlSql 影响的数据进行备份
     *
     * @param dmlSql 更新的 SQL 语句，包含 UPDATE 和 DELETE
     */
    public static void backup(String schema, String dmlSql) throws Exception {
        /*
         逻辑:
         1. 构造备份语句: create table test4 as select * from test where id > 1
         2. 执行备份语句
         */
        try (Connection conn = createConnection()) {
            conn.setSchema(schema);

            String backupSql = OracleBackupInTheSameDbUtils.generateBackupSql(dmlSql);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(backupSql);

            System.out.println("完成同库备份， 备份 SQL: " + backupSql);
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
