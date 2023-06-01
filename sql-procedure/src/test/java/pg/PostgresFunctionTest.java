package pg;

import org.junit.Test;
import xtuer.procfunc.Result;
import xtuer.procfunc.function.*;
import xtuer.util.TablePrinter;
import xtuer.util.Utils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class PostgresFunctionTest {
    static final String DB_URL  = "jdbc:postgresql://192.168.12.19:33005/postgres";
    static final String USER    = "postgres";
    static final String PASS    = "123456";
    static final String CATALOG = "postgres";
    static final String SCHEMA  = "biao";
    static final FunctionExecutorRegistry.DatabaseType DB_TYPE = FunctionExecutorRegistry.DatabaseType.Postgres;

    @Test
    public void execute() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Function func = FunctionFetcher.fetch(conn, CATALOG, SCHEMA, "func_has_arg_return_setof_record");
            print(Function.newFunction(func, PostgresFunction.class));

            Result result = FunctionExecutorRegistry.findExecutor(DB_TYPE).execute(conn, func, 1, 2, 3);
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
