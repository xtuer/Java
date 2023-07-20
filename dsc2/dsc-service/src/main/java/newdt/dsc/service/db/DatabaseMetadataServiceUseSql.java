package newdt.dsc.service.db;

import lombok.extern.slf4j.Slf4j;
import newdt.dsc.bean.db.DatabaseType;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

/**
 * 执行 SQL 语句获取数据库的元数据。
 */
@Service
@Slf4j
public class DatabaseMetadataServiceUseSql {
    /**
     * 执行传入的 catalogSql 查询 type 对应的数据库的 catalogs。
     *
     * @param conn 数据库连接。
     * @param type 数据库类型。
     * @param catalogSql 查询 catalog 的 SQL 语句，结果集的第一列为 catalog 名称，例如 MySQL 使用 "show databases"。
     * @return 返回 catalog 数组。
     * @throws SQLException 访问数据库出错时抛出 SQL 异常。
     */
    public List<String> findCatalogNames(Connection conn, DatabaseType type, String catalogSql) throws SQLException {
        // 提示: 需要对某些数据库进行特殊处理时，通过 type 进行判断。
        List<String> catalogs = new LinkedList<>();

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(catalogSql);

            while (rs.next()) {
                catalogs.add(rs.getString(1));
            }
        }

        return catalogs;
    }

    /**
     * 执行查询语句，把结果集中第 columnIndex 列的值拼成一个字符串。
     *
     * @param conn 数据库连接。
     * @param sql 要执行的查询语句。
     * @param columnIndex 获取结果的列。
     * @param maxOneDelimiter 列之间的分隔符，不传时换行 \n，传了多个只取第一个。
     * @return 返回字符串的查询结果。
     * @throws SQLException 访问数据库出错时抛出 SQL 异常。
     */
    public String executeQueryAndMergeSpecifiedColumnToString(Connection conn, String sql, int columnIndex, String... maxOneDelimiter) throws SQLException {
        log.debug("元数据查询: 获取列 [{}], SQL [{}]", columnIndex, sql);

        StringBuilder ddl = new StringBuilder();
        String delimiter = maxOneDelimiter.length > 0 ? maxOneDelimiter[0] : "\n";

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                ddl.append(rs.getString(columnIndex)).append(delimiter);
            }
        }

        return ddl.toString();
    }
}
