package newdt.dsc2.service;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import newdt.dsc2.bean.DatabaseMetaDataConfig;
import newdt.dsc2.bean.DatabaseType;
import newdt.dsc2.config.DatabaseMetaDataConfigs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * 获取数据库元数据的服务，例如获取 catalog，schema，表名等。
 */
@Service
@Slf4j
public class DatabaseMetaDataService {
    @Autowired
    private DatabaseMetaDataConfigs dbConfigs;

    @Autowired
    private DatabaseMetaDataServiceUsingJdbc jdbcService;

    /**
     * 获取数据库的 catalogs。
     *
     * @param type 数据库类型。
     * @param dbid 数据库的 DBID。
     * @return 返回 catalog 数组。
     * @throws SQLException 访问数据库错误时抛出 SQLException 异常。
     */
    public List<String> findCatalogs(DatabaseType type, int dbid) throws SQLException {
        /*
         逻辑:
         1. 获取数据库的配置。
         2. 数据库需要支持 catalog。
         3. 判断是使用 JDBC 还是使用 SQL 获取 catalog。
         4. 获取 catalog。
         */
        DatabaseMetaDataConfig cfg = dbConfigs.findConfig(type);

        Preconditions.checkArgument(cfg.isUseCatalog(), "数据库不支持 catalog: " + type);

        try (Connection conn = openConnection(type, dbid)) {
            if (DatabaseMetaDataConfigs.useJdbc(cfg.getCatalogQuery())) {
                return jdbcService.findCatalogs(conn);
            } else {
                throw new UnsupportedOperationException("不支持");
            }
        }
    }

    /**
     * 获取数据库的 schemas。
     *
     * @param type 数据库类型。
     * @param dbid 数据库的 DBID。
     * @param catalog Schema 所属 catalog，如果没有则传入 null。
     * @return 返回 schema 数组。
     * @throws SQLException 访问数据库错误时抛出 SQLException 异常。
     * @throws RuntimeException 传入类型的数据库没有 metadata 配置时抛出运行时异常。
     */
    public List<String> findSchemas(DatabaseType type, int dbid, String catalog) throws SQLException {
        /*
         逻辑:
         1. 获取数据库的配置。
         2. 数据库需要支持 schema。
         3. 如果需要 catalog 则 catalog 不能为空。
         4. 判断是使用 JDBC 还是使用 SQL 获取 schema。
         5. 获取 schema。
         */
        DatabaseMetaDataConfig cfg = dbConfigs.findConfig(type);

        Preconditions.checkArgument(cfg.isUseSchema(), "数据库不支持 schema: " + type);

        // [3] 如果需要 catalog 则 catalog 不能为空。
        if (cfg.isUseCatalog()) {
            Preconditions.checkArgument(StringUtils.hasLength(catalog), "Catalog 参数不能为空");
        }

        try (Connection conn = openConnection(type, dbid)) {
            if (DatabaseMetaDataConfigs.useJdbc(cfg.getSchemaQuery())) {
                return jdbcService.findSchemas(conn, catalog);
            } else {
                throw new UnsupportedOperationException("不支持");
            }
        }
    }

    /**
     * 创建获取数据库元数据的连接 (使用 SQL 管控用户)。
     *
     * @param type 数据库类型。
     * @param dbid 数据库的 DBID。
     * @return 返回数据库连接。
     */
    public Connection openConnection(DatabaseType type, int dbid) throws SQLException {
        return TestConnections.openConnection(type, dbid);
    }
}
