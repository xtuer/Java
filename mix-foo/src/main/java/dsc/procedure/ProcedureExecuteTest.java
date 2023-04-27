package dsc.procedure;

import util.Utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;

public class ProcedureExecuteTest {
    static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false";
    static final String USER = "root";
    static final String PASS = "root";

    public static void main(String[] args) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get("/Users/biao/Documents/公司文档/项目管理/DSC/重构/sp.json")));
        Procedure procedure = Utils.fromJson(json, Procedure.class);

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Procedure.Result result = ProcedureExecutor.execute(conn, procedure);
            Utils.dump(result);
        }
    }
}
