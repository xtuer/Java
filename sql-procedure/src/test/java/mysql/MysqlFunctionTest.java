package mysql;

import org.junit.Test;
import xtuer.procfunc.DatabaseType;
import xtuer.procfunc.Result;
import xtuer.procfunc.function.Function;
import xtuer.procfunc.function.FunctionExecutors;
import xtuer.procfunc.function.FunctionFetcher;
import xtuer.util.TablePrinter;
import xtuer.util.Utils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Types;

public class MysqlFunctionTest {
    static final String DB_URL  = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false";
    static final String USER    = "root";
    static final String PASS    = "root";
    static final String CATALOG = "test";
    static final String SCHEMA  = null; // 可以为 null, "", "test", 或者任意值，也就是这个值不生效。
    static final DatabaseType DB_TYPE = DatabaseType.MySQL;

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

    public static void print(Function func) {
        System.out.println("OriginalArgs:");
        TablePrinter.print(func.getOriginalArgs(), "scale", "value", "length", "precision", "dataTypeValue");
        System.out.println("InoutArgs:");
        TablePrinter.print(func.getInoutArgs(), "scale", "value", "length", "precision", "dataTypeValue");
        System.out.println("ReturnArgs:");
        TablePrinter.print(func.getReturnArgs(), "scale", "value", "length", "precision", "dataTypeValue");
        System.out.println("CallableSQL:");
        System.out.println(func.getCallableSql());
        System.out.println("Signature:");
        System.out.println(func.getSignature());
        System.out.println();
    }
}
