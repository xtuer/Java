package mysql;

import org.junit.jupiter.api.Test;
import xtuer.funcproc.DatabaseType;
import xtuer.funcproc.Result;
import xtuer.funcproc.function.Function;
import xtuer.funcproc.function.FunctionExecutors;
import xtuer.funcproc.function.FunctionFetcher;
import xtuer.util.Utils;

import java.sql.*;
import java.util.List;

import static xtuer.util.FunctionPrinter.print;

public class MysqlFunctionTest {
    static final String DB_URL  = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false";
    static final String USER    = "root";
    static final String PASS    = "root";
    static final String CATALOG = "test";
    static final String SCHEMA  = null; // 可以为 null, "", "test", 或者任意值，也就是这个值不生效。
    static final DatabaseType DB_TYPE = DatabaseType.Mysql;

    @Test
    public void execute() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Function func = FunctionExecutors.findFunction(DB_TYPE, conn, CATALOG, SCHEMA, "func_dateToStr");
            print(func);

            Result result = FunctionExecutors.executeFunction(DB_TYPE, conn, func, "2023-06-02");
            Utils.dump(result);
        }
    }

    @Test
    public void executeFunction() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog(CATALOG);
            conn.setSchema(SCHEMA);
            conn.setAutoCommit(false);

            CallableStatement cstmt = conn.prepareCall("{ ? = call fun_dateToStr(?) }");
            cstmt.setObject(2, "2023-06-02");
            cstmt.registerOutParameter(1, Types.OTHER);
            cstmt.execute();

            // 获取返回结果
            System.out.println(cstmt.getObject(1));

            conn.commit();
        }
    }

    // 检查函数是否存在。
    @Test
    public void checkFunctionExists() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            boolean exists = FunctionFetcher.checkFunctionExists(conn, CATALOG, SCHEMA, "func_dateToStr2");
            System.out.println(exists);
        }
    }

    @Test
    public void testListProcedures() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog(CATALOG);
            conn.setSchema(SCHEMA);

            List<String> names = FunctionFetcher.fetchFunctionNames(conn, CATALOG, SCHEMA);
            System.out.println(names);
        }
    }
}