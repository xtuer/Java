package pg;

import org.junit.Test;
import xtuer.procfunc.function.Function;
import xtuer.procfunc.function.FunctionFetcher;
import xtuer.procfunc.function.PostgresFunction;
import xtuer.util.TablePrinter;
import xtuer.util.Utils;
import java.nio.file.Paths;

import java.nio.file.Files;
import java.sql.*;

public class FunctionFetchTest {
    static final String DB_URL  = "jdbc:postgresql://192.168.12.19:33005/postgres";
    static final String USER    = "postgres";
    static final String PASS    = "123456";
    static final String CATALOG = "postgres";
    static final String SCHEMA  = "biao";

    @Test
    public void fetch() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            PostgresFunction function = PostgresFunction.fromFunction(FunctionFetcher.fetch(conn, CATALOG, SCHEMA, "reffunc"));
            print(function);
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

            CallableStatement cstmt = conn.prepareCall("{ ? = call func_has_return_cursor(?) }");
            cstmt.setObject(2, 1);
            cstmt.registerOutParameter(1, Types.REF_CURSOR);
            cstmt.execute();

            ResultSet rs = (ResultSet) cstmt.getObject(1);
            while (rs != null && rs.next()) {
                Utils.dump(rs);
            }

            conn.commit();
        }
    }

    public static void print(Function func) {
        TablePrinter.print(func.getOriginalArgs(), "scale", "value", "length", "precision", "dataTypeValue");
    }
}
