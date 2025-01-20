import org.apache.commons.dbutils.BasicRowProcessor;

import java.sql.*;

public class Test {
    static final String DB_URL = "jdbc:oracle:thin:@//192.168.1.50:1521/orcl";
    static final String USER   = "system";
    static final String PASS   = "sys";

    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            DatabaseMetaData meta = conn.getMetaData();

            // ResultSet rs = meta.getColumns(null, null, "AAA", null);
            ResultSet rs = meta.getColumns(null, null, "V$SESSION", null);
            while (rs.next()) {
                System.out.println(new BasicRowProcessor().toMap(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
