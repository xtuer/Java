package xtuer.procedure.test;

import xtuer.procedure.Procedure;
import xtuer.procedure.ProcedureExecutor;
import xtuer.procedure.ProcedureFetcher;
import xtuer.procedure.ProcedureResult;
import xtuer.util.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ProcedureExecuteTestOracle {
    static final String DB_URL = "jdbc:oracle:thin:@//192.168.12.16:31001/orcl";
    static final String USER   = "system";
    static final String PASS   = "system";

    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setSchema("TEST");

            // testGet(conn);
            // testUpdate(conn);
            testMix(conn);
        }
    }

    public static void testGet(Connection conn) throws SQLException {
        Procedure procedure = ProcedureFetcher.fetch(conn, null, "TEST", "PROC_GET");
        Utils.dump(procedure);

        procedure.getArgs().get(0).setValue(3);
        // procedure.getArgs().get(1).setValue(10);
        ProcedureResult result = ProcedureExecutor.execute(conn, procedure);
        Utils.dump(result);
    }

    public static void testMix(Connection conn) throws SQLException {
        Procedure procedure = ProcedureFetcher.fetch(conn, null, "TEST", "PROC_MIX");
        Utils.dump(procedure);

        procedure.getArgs().get(0).setValue(5);
        procedure.getArgs().get(1).setValue(10);
        procedure.getArgs().get(3).setValue(3);

        ProcedureResult result = ProcedureExecutor.execute(conn, procedure);
        Utils.dump(result);
    }

    public static void testUpdate(Connection conn) throws SQLException {
        Procedure procedure = ProcedureFetcher.fetch(conn, null, "TEST", "PROC_UPDATE");
        Utils.dump(procedure);

        procedure.getArgs().get(0).setValue(88);

        ProcedureResult result = ProcedureExecutor.execute(conn, procedure);
        Utils.dump(result);
    }
}
