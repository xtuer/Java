package xtuer.procedure.test;

import xtuer.procedure.Procedure;
import xtuer.procedure.ProcedureFetcher;
import xtuer.util.Utils;

import java.sql.Connection;
import java.sql.DriverManager;

public class ProcedureFetchTest {
    static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false";
    static final String USER = "root";
    static final String PASS = "root";

    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Procedure procedure = ProcedureFetcher.fetch(conn, "test", null, "mix_demo");
            Utils.dump(procedure);
        }
    }
}


