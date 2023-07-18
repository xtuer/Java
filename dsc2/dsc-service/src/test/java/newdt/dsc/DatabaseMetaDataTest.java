package newdt.dsc;

import newdt.dsc.bean.DatabaseType;
import newdt.dsc.service.TestConnections;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

public class DatabaseMetaDataTest {
    private static final DatabaseType DB_TYPE = DatabaseType.MYSQL;
    private static final int DBID = 1;
    private static final String CATALOG = "test";
    private static final String SCHEMA = "test";
    private static final String TABLE_NAME = "sp_test";

    // 测试获取 catalog。
    @Test
    public void testFindCatalogs() throws Exception {
        try (Connection conn = TestConnections.openConnection(DB_TYPE, DBID)) {
            DatabaseMetaData meta = conn.getMetaData();

            try (ResultSet rs = meta.getCatalogs()) {
                while (rs.next()) {
                    System.out.println(rs.getString("TABLE_CAT"));
                }
            }
        }
    }

    // 测试获取 schema。
    @Test
    public void testFindSchemas() throws Exception {
        try (Connection conn = TestConnections.openConnection(DB_TYPE, DBID)) {
            DatabaseMetaData meta = conn.getMetaData();

            try (ResultSet rs = meta.getSchemas(CATALOG, null)) {
                while (rs.next()) {
                    System.out.println(rs.getString("TABLE_SCHEM"));
                }
            }
        }
    }

    // 测试获取 table types。
    @Test
    public void testFindTableTypes() throws Exception {
        try (Connection conn = TestConnections.openConnection(DB_TYPE, DBID)) {
            DatabaseMetaData meta = conn.getMetaData();

            try (ResultSet rs = meta.getTableTypes()) {
                while (rs.next()) {
                    System.out.println(rs.getString("TABLE_TYPE"));
                }
            }
        }
    }

    // 测试获取 table。
    @Test
    public void testFindTables() throws Exception {
        try (Connection conn = TestConnections.openConnection(DB_TYPE, DBID)) {
            DatabaseMetaData meta = conn.getMetaData();

            // MySQL: "TABLE", "SYSTEM TABLE"
            // Oracle:
            // Postgres:
            try (ResultSet rs = meta.getTables(CATALOG, SCHEMA, null, new String[]{"TABLE", "SYSTEM TABLE"})) {
                while (rs.next()) {
                    System.out.println(rs.getString("TABLE_NAME"));
                }
            }
        }
    }

    // 测试获取 view。
    @Test
    public void testFindViews() throws Exception {
        try (Connection conn = TestConnections.openConnection(DB_TYPE, DBID)) {
            DatabaseMetaData meta = conn.getMetaData();

            // MySQL: "VIEW", "SYSTEM VIEW"
            // Oracle:
            // Postgres:
            try (ResultSet rs = meta.getTables(CATALOG, SCHEMA, null, new String[]{"VIEW", "SYSTEM VIEW"})) {
                while (rs.next()) {
                    System.out.println(rs.getString("TABLE_NAME"));
                }
            }
        }
    }

    // 测试获取 table columns。
    @Test
    public void testFindTableColumns() throws Exception {
        try (Connection conn = TestConnections.openConnection(DB_TYPE, DBID)) {
            DatabaseMetaData meta = conn.getMetaData();

            try (ResultSet rs = meta.getColumns(CATALOG, SCHEMA, TABLE_NAME, null)) {
                while (rs.next()) {
                    System.out.println(rs.getString("COLUMN_NAME") + ": " + rs.getString("TYPE_NAME"));
                }
            }
        }
    }
}
