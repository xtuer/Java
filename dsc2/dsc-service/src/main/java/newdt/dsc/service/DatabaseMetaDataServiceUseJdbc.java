package newdt.dsc.service;

import newdt.dsc.bean.TableColumn;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * 使用 JDBC 的接口获取数据库的元数据。
 */
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

    public List<String> findTables(Connection conn, String catalog, String schema, String[] tableTypes) throws SQLException {
        List<String> names = new LinkedList<>();
        DatabaseMetaData meta = conn.getMetaData();

        // 使用完关闭 ResultSet。
        try (ResultSet rs = meta.getTables(catalog, schema, null, tableTypes)) {
            while (rs.next()) {
                names.add(rs.getString("TABLE_NAME"));
            }
        }

        return names;
    }

    public List<TableColumn> findTableColumns(Connection conn, String catalog, String schema, String table) throws SQLException {
        List<TableColumn> columns = new LinkedList<>();
        DatabaseMetaData meta = conn.getMetaData();

        try (ResultSet rs = meta.getColumns(catalog, schema, table, null)) {
            while (rs.next()) {
                TableColumn column = new TableColumn();
                column.setName(rs.getString("COLUMN_NAME"));
                column.setTypeName(rs.getString("TYPE_NAME"));

                columns.add(column);
            }
        }

        return columns;
    }
}
