package mysql;

import org.junit.jupiter.api.Test;
import xtuer.sp.DatabaseType;
import xtuer.sp.Result;
import xtuer.sp.procedure.Procedure;
import xtuer.sp.procedure.ProcedureExecutors;
import xtuer.util.Utils;

import java.sql.*;
import java.util.List;

import static xtuer.util.ProcedurePrinter.print;

public class MysqlProcedureTest {
    // static final String DB_URL  = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false";
    // static final String USER    = "root";
    // static final String PASS    = "root";
    // static final String CATALOG = "test";

    static final String DB_URL  = "jdbc:mysql://192.168.12.21:35001/test_fan?useSSL=false";
    static final String USER    = "root";
    static final String PASS    = "mypass";
    static final String CATALOG = "test_fan";

    static final String SCHEMA  = null; // 可以为 null, "", "test", 或者任意值，也就是这个值不生效。
    static final DatabaseType DB_TYPE = DatabaseType.MYSQL;

    @Test
    public void execute() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Procedure proc = ProcedureExecutors.findProcedure(DB_TYPE, conn, CATALOG, SCHEMA, "test_timeout");
            print(proc);

            Result result = ProcedureExecutors.executeProcedure(DB_TYPE, conn, proc, 5, 10, 2);
            Utils.dump(result);
        }
    }

    @Test
    public void executeProcedure() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog(CATALOG);
            conn.setSchema(SCHEMA);

            CallableStatement cstmt = conn.prepareCall("{ call GetUsersCount(?) }");
            // cstmt.setObject(1, 5);
            // cstmt.setObject(2, 10);
            // cstmt.setObject(3, 15);
            cstmt.registerOutParameter(1, Types.OTHER);
            // cstmt.registerOutParameter(2, Types.OTHER);
            cstmt.execute();

            // System.out.println(cstmt.getUpdateCount());
            System.out.println(cstmt.getObject(1));
            //
            // ResultSet rs = cstmt.getResultSet();
            // while (rs != null && rs.next()) {
            //     Utils.dump(rs);
            // }
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
