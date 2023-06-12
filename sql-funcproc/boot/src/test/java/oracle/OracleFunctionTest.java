package oracle;

import org.junit.jupiter.api.Test;
import xtuer.funcproc.DatabaseType;
import xtuer.funcproc.Result;
import xtuer.funcproc.function.Function;
import xtuer.funcproc.function.FunctionExecutors;
import xtuer.funcproc.function.FunctionFetcher;
import xtuer.util.Utils;

import java.sql.Connection;
import java.sql.DriverManager;

import static xtuer.util.FunctionPrinter.print;

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
            Function func = FunctionExecutors.findFunction(DB_TYPE, conn, CATALOG, SCHEMA, "GET_TEST_CURSOR"); // 函数名默认需要大写: CIRCLE_AREA, GET_TEST_CURSOR
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


            conn.commit();
        }
    }

    // 检查函数是否存在。
    @Test
    public void checkFunctionExists() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            boolean exists = FunctionFetcher.checkFunctionExists(conn, CATALOG, SCHEMA, "FUNC_RETURN_VOID");
            System.out.println(exists);
        }
    }
}
