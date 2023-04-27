import java.sql.*;

public class JDBCCancelTest {
    static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false";
    static final String USER   = "root";
    static final String PASS   = "root";

    public static void main(String[] args) {
        try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            CallableStatement stmt = conn.prepareCall("{call get_demo()}");
            stmt.execute();

            System.out.println(stmt.getUpdateCount());
            ResultSet rs = stmt.getResultSet();

            while (rs.next()) {
                System.out.printf("ID: %d, Name: %s, Age: %d\n", rs.getInt("id"), rs.getString("name"), rs.getInt("age"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
