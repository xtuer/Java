package dsc.procedure;

import org.apache.commons.dbutils.BasicRowProcessor;
import util.Utils;

import java.sql.*;
import java.util.Properties;

public class ProcedureFetchTestOracle {
    static final String DB_URL = "jdbc:oracle:thin:@//192.168.12.16:31001/orcl";
    static final String USER   = "system";
    static final String PASS   = "system";

    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setSchema("TEST");
            Procedure procedure = ProcedureFetcher.fetch(conn, null, "TEST", "PROC_GET");
            Utils.dump(procedure);

            procedure.getArgs().get(0).setValue(3);
            // procedure.getArgs().get(1).setValue(10);
            Procedure.Result result = ProcedureExecutor.execute(conn, procedure);
            Utils.dump(result);
        }
    }

    /**
     * 创建数据库连接
     *
     * @return 返回数据库连接
     */
    public static Connection createConnectionByDriver() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", "system");
        props.setProperty("password", "system");

        String jdbcUrl = "jdbc:oracle:thin:@//192.168.12.16:31001/orcl";
        return DriverManager.getDriver(jdbcUrl).connect(jdbcUrl, props);
    }
}
