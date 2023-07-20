package db2;

import com.google.common.io.Files;
import org.junit.jupiter.api.Test;
import xtuer.sp.DatabaseType;
import xtuer.sp.Result;
import xtuer.sp.function.Function;
import xtuer.sp.function.FunctionExecutors;
import xtuer.util.FunctionPrinter;
import xtuer.util.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;

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

            String sql = "CALL SYSPROC.ADMIN_CMD(?)";
            PreparedStatement stmt = conn.prepareCall(sql);
            String param = "'db2look -d sample -e -t SP_TEST -z DB2INST1'";

            // setting the imput parameter
            stmt.setString(1, param);

            System.out.println("\nCALL ADMIN_CMD('" + param + "')");
            // executing export by calling ADMIN_CMD
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

    // 测试创建存储过程。
    @Test
    public void testCreateFunction() throws SQLException, IOException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = Files.asCharSource(new File("/Users/biao/Desktop/DB2.txt"), StandardCharsets.UTF_8).read();
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        }
    }
}
