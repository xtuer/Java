package oracle;

import org.junit.jupiter.api.Test;
import xtuer.funcproc.DatabaseType;
import xtuer.funcproc.Result;
import xtuer.funcproc.procedure.Procedure;
import xtuer.funcproc.procedure.ProcedureExecutors;
import xtuer.util.ProcedurePrinter;
import xtuer.util.Utils;

import java.sql.*;

public class OracleProcedureTest {
    static final String DB_URL  = "jdbc:oracle:thin:@//192.168.12.16:31001/orcl";
    static final String USER    = "system";
    static final String PASS    = "system";
    static final String CATALOG = "";
    static final String SCHEMA  = "BIAO";
    static final DatabaseType DB_TYPE = DatabaseType.Oracle;

    @Test
    public void execute() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog(CATALOG);
            conn.setSchema(SCHEMA);

            Procedure proc = ProcedureExecutors.findProcedure(DB_TYPE, conn, CATALOG, SCHEMA, "PROC_UPDATE");
            ProcedurePrinter.print(proc);

            Result result = ProcedureExecutors.executeProcedure(DB_TYPE, conn, proc, 1, 10, 15);
            Utils.dump(result);
        }
    }

    @Test
    public void executeProcedure() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog(CATALOG);
            conn.setSchema(SCHEMA);

            CallableStatement cstmt = conn.prepareCall("{ call PROC_NO_ARG() }");
            // cstmt.setObject(1, 1);
            // cstmt.setObject(2, 10);
            // cstmt.setObject(3, 15);
            // cstmt.registerOutParameter(2, -10);
            cstmt.execute();

            // System.out.println(cstmt.getUpdateCount());
            // System.out.println(cstmt.getObject(3));
            //
            ResultSet rs = cstmt.getResultSet();
            while (rs != null && rs.next()) {
                Utils.dump(rs);
            }
        }
    }
}
