package oracle;
import org.junit.*;
import xtuer.funcproc.DatabaseType;
import xtuer.funcproc.function.Function;
import xtuer.funcproc.function.FunctionExecutors;
import xtuer.funcproc.function.FunctionFetcher;

import static xtuer.util.FunctionPrinter.print;

import java.sql.Connection;
import java.sql.DriverManager;

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
            Function func = FunctionExecutors.findFunction(DB_TYPE, conn, CATALOG, SCHEMA, "func_dateToStr");
            print(func);

            // Result result = FunctionExecutors.executeFunction(DB_TYPE, conn, func, "2023-06-02");
            // Utils.dump(result);
        }
    }

    // 检查函数是否存在。
    @Test
    public void checkFunctionExists() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            boolean exists = FunctionFetcher.checkFunctionExists(conn, CATALOG, SCHEMA, "func_dateToStr");
            System.out.println(exists);
        }
    }
}
