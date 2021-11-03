import java.sql.*;

public class Test {
    static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/gbk?useSSL=false";
    static final String USER = "root";
    static final String PASS = "root";
    static final String QUERY = "SELECT id, name FROM test";
    static final String INSERT = "INSERT INTO test(id, name) VALUES (2, '你好')";

    public static void main(String[] args) {
        query();
    }

    public static void query() {
        try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(QUERY);) {
            while (rs.next()) {
                System.out.print("ID: " + rs.getInt("id"));
                System.out.print(", Name: " + rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insert() {
        try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(INSERT);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
