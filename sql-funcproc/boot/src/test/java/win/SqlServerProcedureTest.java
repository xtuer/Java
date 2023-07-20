package win;

import com.google.common.io.Files;
import org.junit.jupiter.api.Test;
import xtuer.funcproc.DatabaseType;
import xtuer.funcproc.Result;
import xtuer.funcproc.procedure.Procedure;
import xtuer.funcproc.procedure.ProcedureExecutors;
import xtuer.util.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;

import static xtuer.util.ProcedurePrinter.print;

public class SqlServerProcedureTest {
    static final String DB_URL  = "jdbc:sqlserver://192.168.1.28:1533;encrypt=true;trustServerCertificate=true;";
    static final String USER    = "sa";
    static final String PASS    = "Newdt@cn";
    static final String CATALOG = "test_fan"; // TEST, test_fan
    static final String SCHEMA  = "test";
    static final DatabaseType DB_TYPE = DatabaseType.SQLSERVER;

    @Test
    public void execute() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Procedure proc = ProcedureExecutors.findProcedure(DB_TYPE, conn, CATALOG, SCHEMA, "deleteAndUpdate1");
            print(proc);

            // Result result = ProcedureExecutors.executeProcedure(DB_TYPE, conn, proc, 2, 10, 2);
            Result result = ProcedureExecutors.executeProcedure(DB_TYPE, conn, proc, 402, "new1", 2);
            Utils.dump(result);
        }
    }

    @Test
    public void executeProcedure() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog(CATALOG);
            conn.setSchema(SCHEMA);

            CallableStatement cstmt = conn.prepareCall("{ call test.deleteAndUpdate1(?, ?) }");
            cstmt.setObject(1, 402);
            cstmt.setObject(2, "new1");
            // cstmt.setObject(3, 5);
            // cstmt.registerOutParameter(1, Types.OTHER);
            cstmt.execute();

            while (cstmt.getMoreResults()) {
                ResultSet rs = cstmt.getResultSet();
                while (rs != null && rs.next()) {
                    Utils.dump(rs);
                }
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

    // 测试创建存储过程。
    @Test
    public void testCreateProcedure() throws SQLException, IOException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog(CATALOG);
            conn.setSchema(SCHEMA);

            String sql = Files.asCharSource(new File("/Users/biao/Desktop/sqlserver.sql"), StandardCharsets.UTF_8).read();
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);

            while (stmt.getMoreResults()) {
                ResultSet rs = stmt.getResultSet();
                while (rs != null && rs.next()) {
                    Utils.dump(rs);
                }
            }
        }
    }
}
