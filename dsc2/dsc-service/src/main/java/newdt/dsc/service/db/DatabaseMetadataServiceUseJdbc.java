package newdt.dsc.service.db;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 使用 JDBC 的接口获取数据库的元数据。
 */
@Service
public class DatabaseMetadataServiceUseJdbc {
    public List<String> findCatalogNames(Connection conn) throws SQLException {
        List<String> names = new LinkedList<>();
        DatabaseMetaData meta = conn.getMetaData();

        // 使用完关闭 ResultSet。
        try (ResultSet rs = meta.getCatalogs()) {
            while (rs.next()) {
                names.add(rs.getString("TABLE_CAT"));
            }
        }

        return names;
    }

    public List<String> findSchemaNames(Connection conn, String catalog) throws SQLException {
        List<String> names = new LinkedList<>();
        DatabaseMetaData meta = conn.getMetaData();

        try (ResultSet rs = meta.getSchemas(catalog, null)) {
            while (rs.next()) {
                names.add(rs.getString("TABLE_SCHEM"));
                System.out.println(rs.getString("TABLE_CATALOG"));
            }
        }

        return names;
    }

    public List<String> findTableNames(Connection conn, String catalog, String schema, String[] tableTypes) throws SQLException {
        List<String> names = new LinkedList<>();
        DatabaseMetaData meta = conn.getMetaData();

        // 提示: getTables() 的第 3 个参数 tableNamePattern 目前为 null，测试如果不满足的话可以使用 % 代替。
        try (ResultSet rs = meta.getTables(catalog, schema, null, tableTypes)) {
            while (rs.next()) {
                names.add(rs.getString("TABLE_NAME"));
            }
        }

        return names;
    }

    public List<Map<String, Object>> findTableColumns(Connection conn, String catalog, String schema, String table) throws SQLException {
        List<Map<String, Object>> columns = new LinkedList<>();
        DatabaseMetaData meta = conn.getMetaData();

        try (ResultSet rs = meta.getColumns(catalog, schema, table, null)) {
            while (rs.next()) {
                columns.add(new BasicRowProcessor().toMap(rs));
            }
        }

        return columns;
    }
}
