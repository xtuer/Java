package db2;

import org.junit.jupiter.api.Test;
import xtuer.funcproc.DatabaseType;
import xtuer.funcproc.Result;
import xtuer.funcproc.function.Function;
import xtuer.funcproc.function.FunctionExecutors;
import xtuer.funcproc.function.FunctionFetcher;
import xtuer.util.FunctionPrinter;
import xtuer.util.Utils;

import java.sql.*;
import java.util.List;

public class Db2FunctionTest {
    static final String DB_URL  = "jdbc:db2://192.168.1.115:30011/sample";
    static final String USER    = "db2inst1";
    static final String PASS    = "db2inst1";
    static final String CATALOG = null;
    static final String SCHEMA  = "DB2INST1";
    static final DatabaseType DB_TYPE = DatabaseType.DB2;

    @Test
    public void execute() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Function func = FunctionExecutors.findFunction(DB_TYPE, conn, CATALOG, SCHEMA, "FUNC_RETURN_TABLE"); // TAN, FUNC_RETURN_TABLE
            FunctionPrinter.print(func);

            Result result = FunctionExecutors.executeFunction(DB_TYPE, conn, func, 1);
            Utils.dump(result);
        }
    }

    @Test
    public void executeSql() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog(CATALOG);
            conn.setSchema(SCHEMA);

            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM FUNC_RETURN_TABLE(?)");
            stmt.setObject(1, 1);
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                Utils.dump(rs);
            }
        }
    }

    @Test
    public void executeSql2() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog(CATALOG);
            conn.setSchema(SCHEMA);

            PreparedStatement stmt = conn.prepareStatement("VALUES TAN(?)");
            stmt.setObject(1, 10);
            stmt.execute();

            ResultSet rs = stmt.getResultSet();
            rs.next();
            Utils.dump(rs);
        }
    }

    // 列出所有函数名。
    @Test
    public void testFindFunctions() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            System.out.println(FunctionExecutors.findFunctionNames(DB_TYPE, conn, CATALOG, SCHEMA));
        }
    }
}
