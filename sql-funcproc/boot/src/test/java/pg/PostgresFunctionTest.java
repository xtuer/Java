package pg;

import org.junit.jupiter.api.Test;
import xtuer.sp.DatabaseType;
import xtuer.sp.Result;
import xtuer.sp.function.Function;
import xtuer.sp.function.FunctionExecutors;
import xtuer.sp.function.FunctionFetcher;
import xtuer.sp.function.spec.PostgresFunction;
import xtuer.util.Utils;

import java.sql.*;

import static xtuer.util.FunctionPrinter.print;

public class PostgresFunctionTest {
    static final String DB_URL  = "jdbc:postgresql://192.168.12.19:33005/postgres";
    static final String USER    = "postgres";
    static final String PASS    = "123456";
    static final String CATALOG = "postgres";
    static final String SCHEMA  = "biao";
    static final DatabaseType DB_TYPE = DatabaseType.POSTGRES;

    @Test
    public void execute() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Function func = FunctionExecutors.findFunction(DB_TYPE, conn, CATALOG, SCHEMA, "func_pl_out_arg_cursor");
            print(func);

            Result result = FunctionExecutors.executeFunction(DB_TYPE, conn, func, 1, 2, 3);
            Utils.dump(result);
        }
    }

    @Test
    public void executeJson() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Function func = FunctionFetcher.fetchFunction(conn, CATALOG, SCHEMA, "func_has_arg_return_setof_record");
            PostgresFunction pFunc = Function.newFunction(func, PostgresFunction.class);

            // 传给前端。
            String json = Utils.toJson(pFunc);
            System.out.println(json);

            // 前端处理完后提交给后端。
            pFunc = Utils.fromJson(json, PostgresFunction.class);
            print(pFunc);

            Result result = FunctionExecutors.findFunctionExecutor(DB_TYPE).execute(conn, pFunc, 1, 2, 3);
            Utils.dump(result);
        }
    }

    @Test
    public void executeFunction() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            // 获取 cursor 需要关闭自动提交，否则报错 cursor "<unnamed portal 1>" does not exist，
            // 会报 cursor "<unnamed portal 1>" does not exist 错误。
            conn.setCatalog(CATALOG);
            conn.setSchema(SCHEMA);
            conn.setAutoCommit(false);

            CallableStatement cstmt = conn.prepareCall("{ call func_has_arg_return_setof_record(?) } ");
            cstmt.setObject(1, 1);
            // cstmt.setObject(2, 2);
            // cstmt.registerOutParameter(1, Types.REF_CURSOR);
            cstmt.execute();

            ResultSet rs = cstmt.getResultSet();
            while (rs != null && rs.next()) {
                Utils.dump(rs);
            }

            conn.commit();
        }
    }

    // 检查函数是否存在。
    @Test
    public void checkFunctionExists() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            boolean exists = FunctionFetcher.checkFunctionExists(conn, CATALOG, SCHEMA, "func_pl_out_arg_cursor");
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
