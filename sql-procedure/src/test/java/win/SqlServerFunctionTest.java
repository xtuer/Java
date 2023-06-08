package win;
import org.junit.*;
import xtuer.funcproc.DatabaseType;
import xtuer.funcproc.Result;
import xtuer.funcproc.function.Function;
import xtuer.funcproc.function.FunctionExecutors;
import xtuer.util.Utils;

import java.sql.*;

import static xtuer.util.FunctionPrinter.print;

public class SqlServerFunctionTest {
    static final String DB_URL  = "jdbc:sqlserver://192.168.1.28:1533;encrypt=true;trustServerCertificate=true;";
    static final String USER    = "sa";
    static final String PASS    = "Newdt@cn";
    static final String CATALOG = "TEST";
    static final String SCHEMA  = "test"; // 可以为 null, "", "test", 或者任意值，也就是这个值不生效。
    static final DatabaseType DB_TYPE = DatabaseType.SqlServer;

    @Test
    public void execute() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            // 查询的时候不能带 schema，执行的时候需要。
            Function func = FunctionExecutors.findFunction(DB_TYPE, conn, CATALOG, SCHEMA, "func_return_multi_table"); // func_return_inline_table, func_return_multi_table, AddNumbers, ISOweek ("2022-12-31")
            print(func);

            Result result = FunctionExecutors.executeFunction(DB_TYPE, conn, func, 2, 3);
            Utils.dump(result);
        }
    }

    @Test
    public void executeFunction() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog(CATALOG);
            conn.setSchema(SCHEMA);
            conn.setAutoCommit(false);

            CallableStatement cstmt = conn.prepareCall("{ ? = call test.ufn_SalesByStore(?) }");
            cstmt.registerOutParameter(1, Types.OTHER); // 注册的类型不能使用 Types.OTHER
            cstmt.setObject(2, 1);
            // cstmt.setObject(3, 3);
            cstmt.execute();

            // 获取返回结果
            System.out.println(cstmt.getObject(1));
            ResultSet rs = cstmt.getResultSet();
            while (rs != null && rs.next()) {
                Utils.dump(rs);
            }

            conn.commit();
        }
    }

    @Test
    public void executeSql() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog(CATALOG);
            conn.setSchema(SCHEMA);
            conn.setAutoCommit(false);

            PreparedStatement pstmt = conn.prepareStatement("select * from test.func_return_multi_table(?)");
            pstmt.setObject(1, 1);
            pstmt.execute();

            ResultSet rs = pstmt.getResultSet();
            while (rs != null && rs.next()) {
                Utils.dump(rs);
            }

            conn.commit();
        }
    }
}
