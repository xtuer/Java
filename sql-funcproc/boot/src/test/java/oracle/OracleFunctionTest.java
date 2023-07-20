package oracle;

import org.junit.jupiter.api.Test;
import xtuer.sp.DatabaseType;
import xtuer.sp.Result;
import xtuer.sp.function.Function;
import xtuer.sp.function.FunctionExecutors;
import xtuer.sp.function.FunctionFetcher;
import xtuer.util.Utils;

import java.sql.*;

import static xtuer.util.FunctionPrinter.print;

public class OracleFunctionTest {
    static final String DB_URL  = "jdbc:oracle:thin:@//192.168.12.16:31001/orcl";
    static final String USER    = "system";
    static final String PASS    = "system";
    static final String CATALOG = "";
    static final String SCHEMA  = "BIAO";
    static final DatabaseType DB_TYPE = DatabaseType.ORACLE;

    @Test
    public void execute() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Function func = FunctionExecutors.findFunction(DB_TYPE, conn, CATALOG, SCHEMA, "FUNC_RETURN_CURSOR"); // 函数名默认需要大写: CIRCLE_AREA, FUNC_RETURN_CURSOR
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

            CallableStatement cstmt = conn.prepareCall("{ ? = call FUNC_RETURN_VOID() }");
            cstmt.registerOutParameter(1, Types.INTEGER); // 值为 -10
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
            boolean exists = FunctionFetcher.checkFunctionExists(conn, CATALOG, SCHEMA, "PROC_NO_ARG");
            System.out.println(exists);
        }
    }

    // 列出所有函数名。
    @Test
    public void testFindFunctions() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            System.out.println(FunctionExecutors.findFunctionNames(DB_TYPE, conn, CATALOG, SCHEMA));
        }
    }
}
