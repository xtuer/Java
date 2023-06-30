package pg;

import org.junit.jupiter.api.Test;
import xtuer.util.Utils;

import java.sql.*;

/**
 * 有返回值是才使用 {? = call xxx}，否则使用 {call xxx}
 */
public class PgTest {
    static final String DB_URL  = "jdbc:postgresql://192.168.12.19:33005/postgres";
    static final String USER    = "postgres";
    static final String PASS    = "123456";
    static final String CATALOG = "postgres";
    static final String SCHEMA  = "biao";

    @Test
    public void callFunctionReturnSimple() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog("postgres");
            conn.setSchema("biao");

            CallableStatement stmt = conn.prepareCall("{? = call foo(?)}");
            stmt.setInt(2, 10);
            // stmt.setInt(3, 2);
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.execute();
            System.out.println(stmt.getInt(1));
        }
    }

    @Test
    public void callFunctionOutOne() throws Exception {
        // 有 return, 有 out
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog("postgres");
            conn.setSchema("biao");

            CallableStatement stmt = conn.prepareCall("{call out_one(?)}");
            stmt.setInt(1, 10);
            // stmt.setInt(3, 2);
            // stmt.registerOutParameter(1, Types.INTEGER);
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                Utils.dump(rs);
            }
        }
    }

    @Test
    public void callFunctionTableSource() throws Exception {
        // SQL Functions as Table Sources
        // TODO: returns 为表名: "COLUMN_TYPE" : 5, "ORDINAL_POSITION" : 0,
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog("postgres");
            conn.setSchema("biao");

            CallableStatement stmt = conn.prepareCall("{call getfoo(?)}"); // 虽然是调用函数，但不要 ?
            stmt.setInt(1, 2);
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                Utils.dump(rs);
            }
        }
    }

    @Test
    public void callFunctionSetofRecord() throws Exception {
        // SQL Functions as Table Sources
        // TODO: returns 为表名: "COLUMN_TYPE" : 3, "ORDINAL_POSITION" : 0,
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog("postgres");
            conn.setSchema("biao");

            CallableStatement stmt = conn.prepareCall("{call out_set(?)}"); // 虽然是调用函数，但不要 ?
            stmt.setInt(1, 2);
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                Utils.dump(rs);
            }
        }
    }

    @Test
    public void dumpFunction() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog("postgres");
            conn.setSchema("biao");

            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getFunctionColumns("postgres", "biao", "out_one", null);
            while (rs.next()) {
                Utils.dump(rs);
            }
        }
    }

    // 列出所有函数。
    @Test
    public void listFunctions() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog(CATALOG);
            conn.setSchema(SCHEMA);

            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getFunctions(CATALOG, SCHEMA, null);
            while (rs.next()) {
                Utils.dump(rs);
            }
        }
    }

    // 列出所有存储过程。
    @Test
    public void listProcedures() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog(CATALOG);
            conn.setSchema(SCHEMA);

            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getProcedures(CATALOG, SCHEMA, null);
            while (rs.next()) {
                Utils.dump(rs);
            }
        }
    }

    // 使用 select 执行 function。
    @Test
    public void selectFunction() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog("postgres");
            conn.setSchema("biao");

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT somefunc()");
            while (rs.next()) {
                Utils.dump(rs);
            }
        }
    }

    @Test
    public void testGetTableTypes() throws Exception {
        try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setCatalog(CATALOG);
            conn.setSchema(SCHEMA);
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getTables(CATALOG, SCHEMA, null, null);
            rs = md.getTableTypes();
            while (rs.next()) {
                Utils.dump(rs);
            }
        }
    }
}
