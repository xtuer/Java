import xtuer.util.Utils;

import java.sql.*;

public class Test {
    static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false";
    static final String USER   = "root";
    static final String PASS   = "root";

    public static void main(String[] args) {
        try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            CallableStatement cstmt = conn.prepareCall("{ call get_employee_data() }");
            cstmt.execute();

            ResultSet rs = cstmt.getResultSet();
            while (rs != null && rs.next()) {
                Utils.dump(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Main end");
    }
}
