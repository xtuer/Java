package pg;

import org.junit.Test;
import xtuer.procfunc.function.Function;
import xtuer.procfunc.function.FunctionArg;
import xtuer.procfunc.function.FunctionFetcher;
import xtuer.util.TablePrinter;

import java.sql.Connection;
import java.sql.DriverManager;

public class FunctionFetchTest {
    static final String DB_URL  = "jdbc:postgresql://192.168.12.19:33005/postgres";
    static final String USER    = "postgres";
    static final String PASS    = "123456";
    static final String CATALOG = "postgres";
    static final String SCHEMA  = "biao";

    @Test
    public void fetch() throws Exception{
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Function function = FunctionFetcher.fetch(conn, CATALOG, SCHEMA, "out_one");
            print(function);
        }
    }

    public static void print(Function func) {
        TablePrinter.print(func.getOriginalArgs(), FunctionArg.class, "scale", "value");
    }
}
