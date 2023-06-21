package win;

import org.junit.jupiter.api.Test;
import xtuer.funcproc.DatabaseType;
import xtuer.funcproc.Result;
import xtuer.funcproc.procedure.Procedure;
import xtuer.funcproc.procedure.ProcedureExecutors;
import xtuer.util.Utils;

import java.sql.*;
import java.util.List;

import static xtuer.util.ProcedurePrinter.print;

public class SqlServerProcedureTest {
    static final String DB_URL  = "jdbc:sqlserver://192.168.1.28:1533;encrypt=true;trustServerCertificate=true;";
    static final String USER    = "sa";
    static final String PASS    = "Newdt@cn";
    static final String CATALOG = "TEST";
    static final String SCHEMA  = "test";
    static final DatabaseType DB_TYPE = DatabaseType.SQLSERVER;

    @Test
    public void execute() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Procedure proc = ProcedureExecutors.findProcedure(DB_TYPE, conn, CATALOG, SCHEMA, "PROC_IN_OUT_ARGS");
            print(proc);

            Result result = ProcedureExecutors.executeProcedure(DB_TYPE, conn, proc, 2, 10, 2);
            Utils.dump(result);
        }
    }

    @Test
    public void executeProcedure() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog(CATALOG);
            conn.setSchema(SCHEMA);

            CallableStatement cstmt = conn.prepareCall("{ call test.PROC_IN_OUT_ARGS(?, ?, ?) }");
            cstmt.setObject(1, 2);
            cstmt.setObject(2, 3);
            cstmt.setObject(3, 5);
            cstmt.registerOutParameter(3, Types.INTEGER);
            cstmt.execute();

            System.out.println(cstmt.getObject(3));
            ResultSet rs = cstmt.getResultSet();
            while (rs != null && rs.next()) {
                Utils.dump(rs);
            }
        }
    }

    @Test
    public void testFindProcedures() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            List<String> names = ProcedureExecutors.findProcedureNames(DB_TYPE, conn, CATALOG, SCHEMA);
            System.out.println(names);
        }
    }
}
