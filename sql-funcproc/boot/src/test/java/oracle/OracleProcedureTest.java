package oracle;

import com.google.common.io.Files;
import org.junit.jupiter.api.Test;
import xtuer.funcproc.DatabaseType;
import xtuer.funcproc.Result;
import xtuer.funcproc.procedure.Procedure;
import xtuer.funcproc.procedure.ProcedureExecutors;
import xtuer.funcproc.procedure.ProcedureFetcher;
import xtuer.util.ProcedurePrinter;
import xtuer.util.Utils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;

public class OracleProcedureTest {
    static final String DB_URL  = "jdbc:oracle:thin:@//192.168.12.16:31001/orcl";
    static final String USER    = "system";
    static final String PASS    = "system";
    static final String CATALOG = "";
    static final String SCHEMA  = "BIAO";
    static final DatabaseType DB_TYPE = DatabaseType.ORACLE;

    @Test
    public void execute() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog(CATALOG);
            conn.setSchema(SCHEMA);

            Procedure proc = ProcedureExecutors.findProcedure(DB_TYPE, conn, CATALOG, SCHEMA, "PROC_OUT_CURSOR");
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

    @Test
    public void testFindProcedures() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog(CATALOG);
            conn.setSchema(SCHEMA);

            List<String> names = ProcedureExecutors.findProcedureNames(DB_TYPE, conn, CATALOG, SCHEMA);
            System.out.println(names);
        }
    }

    @Test
    public void testExecutePlsqlProcedure1() throws Exception {
        String sql = Files.asCharSource(new File("/Users/biao/Documents/temp/sqls/oracle_proc_exec_out1.sql"), StandardCharsets.UTF_8).read();
        System.out.println(executePlsqlProcedure(sql));
    }

    @Test
    public void testExecutePlsqlProcedure2() throws Exception {
        String sql = Files.asCharSource(new File("/Users/biao/Documents/temp/sqls/oracle_proc_exec_out2.sql"), StandardCharsets.UTF_8).read();
        System.out.println(executePlsqlProcedure(sql));
    }

    /**
     * 执行 PL/SQL 语法的存储过程。
     *
     * @param procedureSql 存储过程语句。
     */
    public static String executePlsqlProcedure(String procedureSql) throws SQLException {
        StringBuilder out = new StringBuilder();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog(CATALOG);
            conn.setSchema(SCHEMA);

            Statement envStmt = null;
            Statement spStmt = null;
            CallableStatement resultCstmt = null;

            try {
                // [1] 开启 DBMSOUTPUT 输出，默认是关闭的。
                envStmt = conn.createStatement();
                envStmt.executeUpdate("begin dbms_output.enable(); end;");

                // [2] 执行存储过程。
                spStmt = conn.createStatement();
                spStmt.execute(procedureSql);

                // [3] 获取存储过程执行结果。
                String resultSql = "begin dbms_output.get_lines(?, ?); end;";
                resultCstmt = conn.prepareCall(resultSql);
                resultCstmt.registerOutParameter(1, Types.ARRAY, "DBMSOUTPUT_LINESARRAY");
                resultCstmt.setInt(2, 1000);
                resultCstmt.execute();

                Array array = null;
                try {
                    // [4] 获取 DBMSOUTPUT 输出的行。
                    array = resultCstmt.getArray(1);
                    String[] lines = (String[]) array.getArray();

                    // 把所有行拼在一起。
                    for (Object line : lines) {
                        out.append(line == null ? "" : line).append("\n");
                    }
                } finally {
                    if (array != null) {
                        array.free();
                    }
                }
            } finally {
                closeStatement(envStmt);
                closeStatement(spStmt);
                closeStatement(resultCstmt);
            }
        }

        return out.toString();
    }

    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
