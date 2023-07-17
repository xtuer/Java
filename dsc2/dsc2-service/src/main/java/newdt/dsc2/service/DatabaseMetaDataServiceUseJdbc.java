package newdt.dsc2.service;

import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

@Service
public class DatabaseMetaDataServiceUseJdbc {
    public List<String> findCatalogs(Connection conn) throws SQLException {
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

    public List<String> findSchemas(Connection conn, String catalog) throws SQLException {
        List<String> names = new LinkedList<>();
        DatabaseMetaData meta = conn.getMetaData();

        // 使用完关闭 ResultSet。
        try (ResultSet rs = meta.getSchemas(catalog, null)) {
            while (rs.next()) {
                names.add(rs.getString("TABLE_SCHEM"));
            }
        }

        return names;
    }

    public List<String> findTables(Connection conn, String catalog, String schema, String[] tableJdbcTypes) throws SQLException {
        List<String> names = new LinkedList<>();
        DatabaseMetaData meta = conn.getMetaData();

        // 使用完关闭 ResultSet。
        try (ResultSet rs = meta.getTables(catalog, schema, null, tableJdbcTypes)) {
            while (rs.next()) {
                names.add(rs.getString("TABLE_NAME"));
            }
        }

        return names;
    }

    public List<String> findViews(Connection conn, String catalog, String schema, String[] viewJdbcTypes) throws SQLException {
        List<String> names = new LinkedList<>();
        DatabaseMetaData meta = conn.getMetaData();

        // 使用完关闭 ResultSet。
        try (ResultSet rs = meta.getTables(catalog, schema, null, viewJdbcTypes)) {
            while (rs.next()) {
                names.add(rs.getString("TABLE_NAME"));
            }
        }

        return names;
    }
}
