import java.sql.*;

public class Test {
    static final String DB_URL = "jdbc:mysql://192.168.12.21:35004/test?useSSL=false";
    static final String USER   = "root";
    static final String PASS   = "mypass";

    public static void main(String[] args) {
        try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select sleep(1)");

            while (rs.next()) {
                System.out.println(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
