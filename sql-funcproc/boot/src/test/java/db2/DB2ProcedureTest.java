package db2;

import com.google.common.io.CharSource;
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

public class DB2ProcedureTest {
    static final String DB_URL  = "jdbc:db2://192.168.1.115:30011/sample";
    static final String USER    = "db2inst1";
    static final String PASS    = "db2inst1";
    static final String CATALOG = null;
    static final String SCHEMA  = "TEST_FAN"; // TEST_FAN, DB2INST1
    static final DatabaseType DB_TYPE = DatabaseType.DB2;

    @Test
    public void execute() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Procedure proc = ProcedureExecutors.findProcedure(DB_TYPE, conn, CATALOG, SCHEMA, "UPDATEUSERSANDRETURNSTATS");
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

            CallableStatement cstmt = conn.prepareCall("call TEST_FAN.UPDATEUSERSANDRETURNSTATS(?, ?, ?)");
            cstmt.setObject(1, 1, Types.INTEGER);
            // cstmt.setInt(1, 1);
            cstmt.registerOutParameter(2, Types.INTEGER);
            cstmt.registerOutParameter(3, Types.DECIMAL);
            cstmt.execute();

            System.out.println(cstmt.getObject(2));
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

    // 测试创建存储过程。
    @Test
    public void testCreateProcedure() throws SQLException, IOException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = Files.asCharSource(new File("/Users/biao/Desktop/db2.sql"), StandardCharsets.UTF_8).read();
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        }
    }
}
