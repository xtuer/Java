package oracle;
import oracle.jdbc.OracleTypes;
import org.junit.*;
import xtuer.funcproc.DatabaseType;
import xtuer.funcproc.Result;
import xtuer.funcproc.function.Function;
import xtuer.funcproc.function.FunctionExecutors;
import xtuer.funcproc.function.FunctionFetcher;
import xtuer.util.Utils;

import static xtuer.util.FunctionPrinter.print;

import java.sql.*;

public class OracleFunctionTest {
    static final String DB_URL  = "jdbc:oracle:thin:@//192.168.12.16:31001/orcl";
    static final String USER    = "system";
    static final String PASS    = "system";
    static final String CATALOG = "";
    static final String SCHEMA  = "BIAO";
    static final DatabaseType DB_TYPE = DatabaseType.Oracle;

    @Test
    public void execute() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Function func = FunctionExecutors.findFunction(DB_TYPE, conn, CATALOG, SCHEMA, "GET_TEST_CURSOR"); // 函数名默认需要大写
            print(func);

            Result result = FunctionExecutors.executeFunction(DB_TYPE, conn, func, 3);
            Utils.dump(result);
        }
    }

    @Test
    public void executeFunction() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog(CATALOG);
            conn.setSchema(SCHEMA);
            conn.setAutoCommit(false);

            CallableStatement cstmt = conn.prepareCall("{ ? = call GET_TEST_CURSOR() }");
            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            // cstmt.setObject(2, "3");
            cstmt.execute();

            // 获取返回结果
            ResultSet rs = (ResultSet) cstmt.getObject(1);
            while (rs != null && rs.next()) {
                System.out.println(rs.getObject(1));
            }

            conn.commit();
        }
    }

    // 检查函数是否存在。
    @Test
    public void checkFunctionExists() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            boolean exists = FunctionFetcher.checkFunctionExists(conn, CATALOG, SCHEMA, "circle_area");
            System.out.println(exists);
        }
    }
}
