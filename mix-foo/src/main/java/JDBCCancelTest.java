import java.sql.*;

public class JDBCCancelTest {
    static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false";
    static final String USER   = "root";
    static final String PASS   = "root";

    public static void main(String[] args) {
        try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            CallableStatement stmt = conn.prepareCall("{call mix_demo(?, ?, ?, ?)}");

            // 设置入参。
            stmt.setObject(1, 5);
            stmt.setObject(2, 10);
            stmt.registerOutParameter(3, Types.OTHER);
            stmt.setObject(4, 3);

            // 设置出参，参数类型请参考 java.sql.Types，如果类型不确定则使用 Types.OTHER。
            stmt.registerOutParameter(4, Types.OTHER);

            // 执行然后获取结果。
            stmt.execute();
            int sum = stmt.getInt(3);
            int id = stmt.getInt(4);
            System.out.println("Sum: " + sum + ", ID: " + id);

            // 获取结果集。
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                System.out.printf("ID: %d, Name: %s, Age: %d\n", rs.getInt("id"), rs.getString("name"), rs.getInt("age"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
