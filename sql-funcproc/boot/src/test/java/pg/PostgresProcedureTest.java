package pg;

import org.junit.jupiter.api.Test;
import xtuer.funcproc.DatabaseType;
import xtuer.funcproc.Result;
import xtuer.funcproc.procedure.Procedure;
import xtuer.funcproc.procedure.ProcedureExecutors;
import xtuer.util.Utils;

import java.sql.*;
import java.util.List;

import static xtuer.util.ProcedurePrinter.print;

public class PostgresProcedureTest {
    static final String DB_URL  = "jdbc:postgresql://192.168.12.19:33005/postgres";
    static final String USER    = "postgres";
    static final String PASS    = "123456";
    static final String CATALOG = "postgres";
    static final String SCHEMA  = "biao";
    static final DatabaseType DB_TYPE = DatabaseType.POSTGRES;

    @Test
    public void execute() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Procedure proc = ProcedureExecutors.findProcedure(DB_TYPE, conn, CATALOG, SCHEMA, "proc_in_out_args");
            print(proc);

            Result result = ProcedureExecutors.executeProcedure(DB_TYPE, conn, proc, 1, 2, 3, 4);
            Utils.dump(result);
        }
    }

    @Test
    public void executeProcedure() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog(CATALOG);
            conn.setSchema(SCHEMA);

            CallableStatement cstmt = conn.prepareCall("call proc_no_arg()");
            // cstmt.setObject(1, 5);
            // cstmt.setObject(2, 10);
            // cstmt.setObject(3, 15);
            // cstmt.registerOutParameter(3, Types.INTEGER);
            cstmt.execute();

            // System.out.println(cstmt.getUpdateCount());
            // System.out.println(cstmt.getObject(3));
            // //
            // ResultSet rs = cstmt.getResultSet();
            // while (rs != null && rs.next()) {
            //     Utils.dump(rs);
            // }
        }
    }

    @Test
    public void testFindProcedures() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog(CATALOG);
            conn.setSchema(SCHEMA);

            List<String> names = ProcedureExecutors.findProcedureNames(DB_TYPE, conn, CATALOG, SCHEMA);
            System.out.println(names);
        }
    }
}
