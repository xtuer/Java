package gbase8a;

import org.junit.jupiter.api.Test;
import xtuer.sp.DatabaseType;
import xtuer.sp.function.FunctionExecutors;
import xtuer.sp.procedure.ProcedureExecutors;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

public class GBase8aProcedureTest {
    static final String DB_URL  = "jdbc:gbase://192.168.1.108:5050/test_fan";
    static final String USER    = "root";
    static final String PASS    = "Newdt@cn";
    static final String CATALOG = "test_fan";
    static final String SCHEMA  = null; // 可以为 null, "", "test", 或者任意值，也就是这个值不生效。
    static final DatabaseType DB_TYPE = DatabaseType.GBASE8A;

    @Test
    public void findProcedures() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            List<String> names = ProcedureExecutors.findProcedureNames(DB_TYPE, conn, CATALOG, SCHEMA);
            System.out.println(names);
        }
    }
}
