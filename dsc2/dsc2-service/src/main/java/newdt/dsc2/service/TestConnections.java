package newdt.dsc2.service;

import newdt.dsc2.bean.DatabaseType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 测试库的连接。
 */
public class TestConnections {
    /**
     * 创建获取数据库元数据的连接 (使用 SQL 管控用户)。
     *
     * @param type 数据库类型。
     * @param dbid 数据库的 DBID。
     * @return 返回数据库连接。
     */
    public static Connection openConnection(DatabaseType type, int dbid) throws SQLException {
        if (dbid == 1) {
            // MySQL: catalog
            String url = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false";
            String user = "root";
            String pass = "root";
            return DriverManager.getConnection(url, user, pass);
        } else if (dbid == 2) {
            // Oracle: schema
            String url = "jdbc:oracle:thin:@//192.168.12.16:31001/orcl";
            String user = "system";
            String pass = "system";
            return DriverManager.getConnection(url, user, pass);
        } else if (dbid == 3) {
            // Postgres: catalog + schema
            String url = "jdbc:postgresql://192.168.12.19:33005/postgres";
            String user = "postgres";
            String pass = "123456";
            return DriverManager.getConnection(url, user, pass);
        }

        return null;
    }
}
