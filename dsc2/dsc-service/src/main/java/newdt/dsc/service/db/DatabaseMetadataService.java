package newdt.dsc.service.db;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import newdt.dsc.bean.db.DatabaseMetadataConfig;
import newdt.dsc.bean.db.DatabaseType;
import newdt.dsc.bean.db.TableColumn;
import newdt.dsc.config.DatabaseMetadataConfigs;
import newdt.dsc.service.TestConnections;
import newdt.dsc.util.Utils;
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
public class DatabaseMetadataService {
    @Autowired
    private DatabaseMetadataConfigs dbConfigs;

    @Autowired
    private DatabaseMetadataServiceUseJdbc jdbcService;

    @Autowired
    private DatabaseMetadataServiceUseSql sqlService;

    /**
     * 获取数据库的 catalogs。
     *
     * @param type 数据库类型。
     * @param dbid 数据库的 DBID。
     * @return 返回 catalog 数组。
     * @throws SQLException 访问数据库错误时抛出 SQLException 异常。
     * @throws RuntimeException 传入类型的数据库没有 metadata 配置时抛出运行时异常。
     * @throws IllegalArgumentException 参数不满足要求时抛出无效参数异常。
     */
    public List<String> findCatalogs(DatabaseType type, int dbid) throws SQLException {
        /*
         逻辑:
         1. 获取数据库的配置。
         2. 数据库需要支持 catalog。
         3. 根据查询语句类型获取 catalog。
         */
        DatabaseMetadataConfig cfg = dbConfigs.findConfig(type);

        Preconditions.checkArgument(cfg.isUseCatalog(), "数据库不支持 catalog: " + type);

        try (Connection conn = openConnection(type, dbid)) {
            String sql = cfg.getCatalog().getSql();

            if (DatabaseMetadataConfigs.useJdbc(sql)) {
                return jdbcService.findCatalogs(conn);
            } else {
                return sqlService.findCatalogs(conn, type, sql);
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
     */
    public List<String> findSchemas(DatabaseType type, int dbid, String catalog) throws SQLException {
        /*
         逻辑:
         1. 获取数据库的配置。
         2. 数据库需要支持 schema。
         3. 如果需要 catalog 则 catalog 不能为空。
         4. 根据查询语句类型获取 schema。
         */
        DatabaseMetadataConfig cfg = dbConfigs.findConfig(type);

        Preconditions.checkArgument(cfg.isUseSchema(), "数据库不支持 schema: " + type);

        // [3] 如果需要 catalog 则 catalog 不能为空。
        checkCatalogAndSchema(cfg, catalog, null);

        try (Connection conn = openConnection(type, dbid)) {
            if (DatabaseMetadataConfigs.useJdbc(cfg.getSchema().getSql())) {
                return jdbcService.findSchemas(conn, catalog);
            } else {
                throw new UnsupportedOperationException("不支持");
            }
        }
    }

    /**
     * 获取传入的 catalog + schema 下的表。
     *
     * @param type 数据库类型。
     * @param dbid 数据库的 DBID。
     * @param catalog 所属 catalog，如果没有则传入 null。
     * @param schema 所属 schema，如果没有则传入 null。
     * @return 返回 table 数组。
     */
    public List<String> findTables(DatabaseType type, int dbid, String catalog, String schema) throws SQLException {
        /*
         逻辑:
         1. 获取数据库的配置。
         2. 如果需要 catalog 则 catalog 不能为空；如果需要 schema 则 schema 不能为空。
         3. 根据查询语句类型获取 table。
         */
        DatabaseMetadataConfig cfg = dbConfigs.findConfig(type);

        checkCatalogAndSchema(cfg, catalog, schema);

        try (Connection conn = openConnection(type, dbid)) {
            if (DatabaseMetadataConfigs.useJdbc(cfg.getTable().getSql())) {
                return jdbcService.findTables(conn, catalog, schema, cfg.getTable().getTableTypes());
            } else {
                throw new UnsupportedOperationException("不支持");
            }
        }
    }

    /**
     * 获取传入的 catalog + schema 下的视图。
     *
     * @param type 数据库类型。
     * @param dbid 数据库的 DBID。
     * @param catalog 所属 catalog，如果没有则传入 null。
     * @param schema 所属 schema，如果没有则传入 null。
     * @return 返回 view 数组。
     */
    public List<String> findViews(DatabaseType type, int dbid, String catalog, String schema) throws SQLException {
        /*
         逻辑:
         1. 获取数据库的配置。
         2. 如果需要 catalog 则 catalog 不能为空；如果需要 schema 则 schema 不能为空。
         3. 根据查询语句类型获取 view。
         */
        DatabaseMetadataConfig cfg = dbConfigs.findConfig(type);

        checkCatalogAndSchema(cfg, catalog, schema);

        try (Connection conn = openConnection(type, dbid)) {
            if (DatabaseMetadataConfigs.useJdbc(cfg.getView().getSql())) {
                return jdbcService.findTables(conn, catalog, schema, cfg.getView().getTableTypes());
            } else {
                throw new UnsupportedOperationException("不支持");
            }
        }
    }

    /**
     * 获取传入的 catalog + schema 下的表的列。
     *
     * @param type 数据库类型。
     * @param dbid 数据库的 DBID。
     * @param catalog 所属 catalog，如果没有则传入 null。
     * @param schema 所属 schema，如果没有则传入 null。
     * @param table 表名。
     * @return 返回列的数组。
     */
    public List<TableColumn> findTableColumns(DatabaseType type, int dbid, String catalog, String schema, String table) throws SQLException {
        /*
         逻辑:
         1. 获取数据库的配置。
         2. 如果需要 catalog 则 catalog 不能为空；如果需要 schema 则 schema 不能为空。
         3. 检查表名不能为空。
         4. 根据查询语句类型获取表的列。
         */
        DatabaseMetadataConfig cfg = dbConfigs.findConfig(type);

        // [2] 如果需要 catalog 则 catalog 不能为空；如果需要 schema 则 schema 不能为空。
        checkCatalogAndSchema(cfg, catalog, schema);
        Preconditions.checkArgument(StringUtils.hasLength(table), "参数 table 不能为空");

        try (Connection conn = openConnection(type, dbid)) {
            if (DatabaseMetadataConfigs.useJdbc(cfg.getTableColumn().getSql())) {
                return jdbcService.findTableColumns(conn, catalog, schema, table);
            } else {
                throw new UnsupportedOperationException("不支持");
            }
        }
    }

    /**
     * 查询建表语句。
     *
     * @param type 数据库类型。
     * @param dbid 数据库的 DBID。
     * @param catalog 所属 catalog，如果没有则传入 null。
     * @param schema 所属 schema，如果没有则传入 null。
     * @param table 表名。
     * @return 返回建表语句。
     */
    public String findTableDdl(DatabaseType type, int dbid, String catalog, String schema, String table) throws SQLException {
        DatabaseMetadataConfig cfg = dbConfigs.findConfig(type);

        try (Connection conn = openConnection(type, dbid)) {
            String ddlSql = Utils.replace(cfg.getTableDdl().getSql(), "catalog", catalog, "schema", schema,  "table", table);
            int ddlIndex = cfg.getTableDdl().getIndex();

            return sqlService.executeQueryAndMergeSpecifiedColumnToString(conn, ddlSql, ddlIndex);
        }
    }

    /**
     * 获取存储过程创建语句。
     */
    public String findProcedureDdl(DatabaseType type, int dbid, String catalog, String schema, String procedure) throws SQLException {
        DatabaseMetadataConfig cfg = dbConfigs.findConfig(type);

        try (Connection conn = openConnection(type, dbid)) {
            String ddlSql = Utils.replace(cfg.getProcedureDdl().getSql(), "catalog", catalog, "schema", schema,  "procedure", procedure);
            int ddlIndex = cfg.getProcedureDdl().getIndex();

            return sqlService.executeQueryAndMergeSpecifiedColumnToString(conn, ddlSql, ddlIndex);
        }
    }

    /**
     * 获取函数创建语句。
     */
    public String findFunctionDdl(DatabaseType type, int dbid, String catalog, String schema, String function) throws SQLException {
        DatabaseMetadataConfig cfg = dbConfigs.findConfig(type);

        try (Connection conn = openConnection(type, dbid)) {
            String ddlSql = Utils.replace(cfg.getFunctionDdl().getSql(), "catalog", catalog, "schema", schema,  "function", function);
            int ddlIndex = cfg.getFunctionDdl().getIndex();

            return sqlService.executeQueryAndMergeSpecifiedColumnToString(conn, ddlSql, ddlIndex);
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

    /**
     * 根据配置校验 catalog 和 schema。如果配置中指定需要 catalog 而传入的 catalog 为空或 null 则抛出异常，schema 也一样。
     *
     * @param cfg 数据库元数据配置。
     * @param catalog 需要校验的 catalog。
     * @param schema 需要校验的 schema。
     */
    private void checkCatalogAndSchema(DatabaseMetadataConfig cfg, String catalog, String schema) {
        if (cfg.isUseCatalog()) {
            Preconditions.checkArgument(StringUtils.hasLength(catalog), "参数 catalog 不能为空");
        }
        if (cfg.isUseSchema()) {
            Preconditions.checkArgument(StringUtils.hasLength(schema), "参数 schema 不能为空");
        }
    }
}
