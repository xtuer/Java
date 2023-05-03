package dsc.procedure.test;

import dsc.procedure.*;
import org.apache.commons.dbutils.BasicRowProcessor;
import util.Utils;

import java.sql.*;

public class ProcedureExecuteTestPostgres {
    static final String DB_URL = "jdbc:postgresql://192.168.12.19:33005/postgres";
    static final String USER   = "postgres";
    static final String PASS   = "123456";

    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog("postgres");
            conn.setSchema("public");
            // testGet(conn);
            // testUpdate(conn);
            // testSelect(conn);

            // testRaw(conn);
            // getFunctionsOrProcedures(conn);

            testGetFunc(conn);
        }
    }

    public static void testGetFunc(Connection conn) throws SQLException {
        // func_sum, func_with_return
        Function function = FunctionFetcher.fetch(conn, "postgres", "public", "func_sum");
        Utils.dump(function);
    }

    public static void testGet(Connection conn) throws SQLException {
        // 存储过程名字需要小写。
        Procedure procedure = ProcedureFetcher.fetch(conn, "postgres", "public", "func_sum", true);
        Utils.dump(procedure);

        procedure.getArgs().get(0).setValue(3);
        procedure.getArgs().get(1).setValue(5);
        ProcedureResult result = ProcedureExecutor.execute(conn, procedure);
        Utils.dump(result);
    }

    public static void testUpdate(Connection conn) throws SQLException {
        Procedure procedure = ProcedureFetcher.fetch(conn, "postgres", "public", "proc_update", false);
        Utils.dump(procedure);

        procedure.getArgs().get(0).setValue(29);
        ProcedureResult result = ProcedureExecutor.execute(conn, procedure);
        Utils.dump(result);
    }

    public static void testSelect(Connection conn) throws SQLException {
        conn.setAutoCommit(false);
        Procedure procedure = ProcedureFetcher.fetch(conn, "postgres", "public", "func_refcursor", true);
        Utils.dump(procedure);

        ProcedureResult result = ProcedureExecutor.execute(conn, procedure);
        Utils.dump(result);
    }

    public static void testRaw(Connection conn) throws SQLException {
        // 获取 cursor 需要关闭事务，否则会报 cursor "<unnamed portal 1>" does not exist 错误。
        // conn.setAutoCommit(false);
        CallableStatement stmt = conn.prepareCall("{?=call func_sum2(?, ?)}");

        stmt.setInt(2, 3);
        stmt.setInt(3, 5);
        stmt.registerOutParameter(1, Types.INTEGER);
        // stmt.registerOutParameter(4, Types.INTEGER);
        stmt.execute();

        System.out.println(stmt.getObject(1));
        // System.out.println(stmt.getObject(4));

        // conn.commit();
    }

    public static void getFunctionsOrProcedures(Connection conn) throws SQLException {
        ResultSet rs = conn.getMetaData().getProcedures("postgres", "public", null);
        while (rs.next()) {
            Utils.dump(new BasicRowProcessor().toMap(rs));
        }
    }
}
