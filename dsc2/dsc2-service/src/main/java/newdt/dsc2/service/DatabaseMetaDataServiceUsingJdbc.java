package newdt.dsc2.service;

import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

@Service
public class DatabaseMetaDataServiceUsingJdbc {
    public List<String> findCatalogs(Connection conn) throws SQLException {
        List<String> names = new LinkedList<>();
        DatabaseMetaData md = conn.getMetaData();

        // 使用完关闭 ResultSet。
        try (ResultSet rs = md.getCatalogs()) {
            while (rs.next()) {
                names.add(rs.getString("TABLE_CAT"));
            }
        }

        return names;
    }

    public List<String> findSchemas(Connection conn, String catalog) throws SQLException {
        List<String> names = new LinkedList<>();
        conn.setCatalog(catalog);
        DatabaseMetaData md = conn.getMetaData();

        // 使用完关闭 ResultSet。
        try (ResultSet rs = md.getSchemas(catalog, null)) {
            while (rs.next()) {
                names.add(rs.getString("TABLE_SCHEM"));
            }
        }

        return names;
    }
}
