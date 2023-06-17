package pg;

import com.google.common.io.Files;
import org.junit.jupiter.api.Test;
import xtuer.funcproc.DatabaseType;
import xtuer.funcproc.Result;
import xtuer.funcproc.procedure.Procedure;
import xtuer.funcproc.procedure.ProcedureExecutors;
import xtuer.util.Utils;

import java.io.File;
import java.nio.charset.StandardCharsets;
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

    // 列出所有存储过程名称。
    @Test
    public void testFindProcedures() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            List<String> names = ProcedureExecutors.findProcedureNames(DB_TYPE, conn, CATALOG, SCHEMA);
            names.forEach(System.out::println);
        }
    }

    // 使用 BEGIN END 的语法执行存储过程。
    @Test
    public void testExecutePlpgsqlProcedure() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog(CATALOG);
            conn.setSchema(SCHEMA);

            String sql = Files.asCharSource(new File("/Users/biao/Documents/temp/sqls/pg_proc_exec_out1.sql"), StandardCharsets.UTF_8).read();
            // sql = "call proc_no_arg_insert_error()"; // 也可以执行。
            Statement stmt = conn.createStatement();
            stmt.execute(sql);

            // BEGIN AND 中使用 `RAISE NOTICE 'sum = %', sum;` 进行输出。
            SQLWarning warning = stmt.getWarnings();
            while (warning != null) {
                System.out.println(warning.toString());
                System.out.println(warning.getSQLState());
                warning = warning.getNextWarning();
            }
        } catch (SQLException ex) {
            System.out.println(ex.getErrorCode());
            System.out.println(ex.getMessage());
            System.out.println(ex.getSQLState()); // 错误码竟然是用这个函数获取，而不是 getErrorCode()。
        }
    }
}
