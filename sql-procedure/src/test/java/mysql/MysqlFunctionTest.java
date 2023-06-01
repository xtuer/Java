package mysql;

import org.junit.Test;
import xtuer.procfunc.Result;
import xtuer.procfunc.function.Function;
import xtuer.procfunc.function.FunctionExecutorRegistry;
import xtuer.procfunc.function.FunctionFetcher;
import xtuer.util.TablePrinter;
import xtuer.util.Utils;

import java.sql.*;

public class MysqlFunctionTest {
    static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false";
    static final String USER   = "root";
    static final String PASS   = "root";
    static final String CATALOG = "test";
    static final String SCHEMA  = "test";
    static final FunctionExecutorRegistry.DatabaseType DB_TYPE = FunctionExecutorRegistry.DatabaseType.MySQL;

    @Test
    public void execute() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Function func = FunctionFetcher.fetch(conn, CATALOG, SCHEMA, "fun_dateToStr");
            print(func);

            Result result = FunctionExecutorRegistry.findExecutor(DB_TYPE).execute(conn, func, "2023-06-02");
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

            CallableStatement cstmt = conn.prepareCall("{ ? = call fun_dateToStr(?) }");
            cstmt.setObject(2, "2023-06-02");
            cstmt.registerOutParameter(1, Types.OTHER);
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
