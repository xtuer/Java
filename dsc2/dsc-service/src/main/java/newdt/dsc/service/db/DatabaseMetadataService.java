package newdt.dsc.service.db;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import newdt.dsc.bean.db.DatabaseMetadataConfig;
import newdt.dsc.bean.db.DatabaseType;
import newdt.dsc.config.DatabaseMetadataConfigs;
import newdt.dsc.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static newdt.dsc.bean.db.DatabaseMetadataConfig.QueryConfig;

/**
 * 获取数据库元数据的服务，例如获取 catalog，schema，表名等。
 */
@Service
@Slf4j
public class DatabaseMetadataService {
    @Autowired
    private DatabaseMetadataConfigs metaConfigs;

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
    public List<String> findCatalogNames(DatabaseType type, int dbid) throws SQLException {
        /*
         逻辑:
         1. 获取数据库的配置。
         2. 数据库需要支持 catalog。
         3. 根据查询语句类型获取 catalog。
         */
        DatabaseMetadataConfig mc = metaConfigs.findMetadataConfig(type);

        Preconditions.checkArgument(mc.isUseCatalog(), "数据库不支持 catalog: " + type);

        try (Connection conn = openConnection(type, dbid)) {
            QueryConfig qc = DatabaseMetadataConfigs.findMatchedQueryConfig(conn, mc.getCatalogNames());

            if (DatabaseMetadataConfigs.useJdbc(qc.getSql())) {
                return jdbcService.findCatalogNames(conn);
            } else {
                return sqlService.findCatalogNames(conn, type, qc.getSql());
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
    public List<String> findSchemaNames(DatabaseType type, int dbid, String catalog) throws SQLException {
        /*
         逻辑:
         1. 获取数据库的配置。
         2. 数据库需要支持 schema。
         3. 如果需要 catalog 则 catalog 不能为空。
         4. 根据查询语句类型获取 schema。
         */
        DatabaseMetadataConfig mc = metaConfigs.findMetadataConfig(type);

        checkCatalogAndSchema(mc, catalog, null);
        Preconditions.checkArgument(mc.isUseSchema(), "数据库不支持 schema: " + type);

        try (Connection conn = openConnection(type, dbid)) {
            QueryConfig qc = DatabaseMetadataConfigs.findMatchedQueryConfig(conn, mc.getSchemaNames());

            if (DatabaseMetadataConfigs.useJdbc(qc.getSql())) {
                return jdbcService.findSchemaNames(conn, catalog);
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
    public List<String> findTableNames(DatabaseType type, int dbid, String catalog, String schema) throws SQLException {
        /*
         逻辑:
         1. 获取数据库的配置。
         2. 如果需要 catalog 则 catalog 不能为空；如果需要 schema 则 schema 不能为空。
         3. 根据查询语句类型获取 table。
         */
        DatabaseMetadataConfig mc = metaConfigs.findMetadataConfig(type);

        checkCatalogAndSchema(mc, catalog, schema);

        try (Connection conn = openConnection(type, dbid)) {
            QueryConfig qc = DatabaseMetadataConfigs.findMatchedQueryConfig(conn, mc.getTableNames());

            if (DatabaseMetadataConfigs.useJdbc(qc.getSql())) {
                return jdbcService.findTableNames(conn, catalog, schema, qc.getTableTypes());
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
    public List<String> findViewNames(DatabaseType type, int dbid, String catalog, String schema) throws SQLException {
        /*
         逻辑:
         1. 获取数据库的配置。
         2. 如果需要 catalog 则 catalog 不能为空；如果需要 schema 则 schema 不能为空。
         3. 根据查询语句类型获取 view。
         */
        DatabaseMetadataConfig mc = metaConfigs.findMetadataConfig(type);

        checkCatalogAndSchema(mc, catalog, schema);

        try (Connection conn = openConnection(type, dbid)) {
            QueryConfig qc = DatabaseMetadataConfigs.findMatchedQueryConfig(conn, mc.getViewNames());

            if (DatabaseMetadataConfigs.useJdbc(qc.getSql())) {
                return jdbcService.findTableNames(conn, catalog, schema, qc.getTableTypes());
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
    public List<Map<String, Object>> findTableColumns(DatabaseType type, int dbid, String catalog, String schema, String table) throws SQLException {
        /*
         逻辑:
         1. 获取数据库的配置。
         2. 如果需要 catalog 则 catalog 不能为空；如果需要 schema 则 schema 不能为空。
         3. 检查表名不能为空。
         4. 根据查询语句类型获取表的列。
         */
        DatabaseMetadataConfig mc = metaConfigs.findMetadataConfig(type);

        // [2] 如果需要 catalog 则 catalog 不能为空；如果需要 schema 则 schema 不能为空。
        checkCatalogAndSchema(mc, catalog, schema);
        Preconditions.checkArgument(StringUtils.hasText(table), "参数 table 不能为空");

        try (Connection conn = openConnection(type, dbid)) {
            QueryConfig qc = DatabaseMetadataConfigs.findMatchedQueryConfig(conn, mc.getTableColumns());

            if (DatabaseMetadataConfigs.useJdbc(qc.getSql())) {
                return jdbcService.findTableColumns(conn, catalog, schema, table);
            } else {
                throw new UnsupportedOperationException("不支持");
            }
        }
    }

    /**
     * 获取传入的 catalog + schema 下的表的列名。
     *
     * @param type 数据库类型。
     * @param dbid 数据库的 DBID。
     * @param catalog 所属 catalog，如果没有则传入 null。
     * @param schema 所属 schema，如果没有则传入 null。
     * @param table 表名。
     * @return 返回列的数组。
     */
    public List<String> findTableColumnNames(DatabaseType type, int dbid, String catalog, String schema, String table) throws SQLException {
        List<Map<String, Object>> columns = findTableColumns(type, dbid, catalog, schema, table);

        return columns.stream()
                .map(col -> col.get("COLUMN_NAME"))
                .map(Object::toString)
                .collect(Collectors.toList());
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
        DatabaseMetadataConfig mc = metaConfigs.findMetadataConfig(type);

        checkCatalogAndSchema(mc, catalog, schema);
        Preconditions.checkArgument(StringUtils.hasText(table), "参数 table 不能为空");

        try (Connection conn = openConnection(type, dbid)) {
            QueryConfig qc = DatabaseMetadataConfigs.findMatchedQueryConfig(conn, mc.getTableDdls());
            String ddlSql = Utils.replace(qc.getSql(), "catalog", catalog, "schema", schema,  "table", table);
            int ddlIndex = qc.getIndex();

            return sqlService.executeQueryAndMergeSpecifiedColumnToString(conn, ddlSql, ddlIndex);
        }
    }

    /**
     * 查询视图创建语句。
     *
     * @param type 数据库类型。
     * @param dbid 数据库的 DBID。
     * @param catalog 所属 catalog，如果没有则传入 null。
     * @param schema 所属 schema，如果没有则传入 null。
     * @param view 视图名。
     * @return 返回视图创建语句。
     */
    public String findViewDdl(DatabaseType type, int dbid, String catalog, String schema, String view) throws SQLException {
        DatabaseMetadataConfig mc = metaConfigs.findMetadataConfig(type);

        checkCatalogAndSchema(mc, catalog, schema);
        Preconditions.checkArgument(StringUtils.hasText(view), "参数 view 不能为空");

        try (Connection conn = openConnection(type, dbid)) {
            QueryConfig qc = DatabaseMetadataConfigs.findMatchedQueryConfig(conn, mc.getViewDdls());
            String ddlSql = Utils.replace(qc.getSql(), "catalog", catalog, "schema", schema,  "view", view);
            int ddlIndex = qc.getIndex();

            return sqlService.executeQueryAndMergeSpecifiedColumnToString(conn, ddlSql, ddlIndex);
        }
    }

    /**
     * 获取存储过程创建语句。
     */
    public String findProcedureDdl(DatabaseType type, int dbid, String catalog, String schema, String procedure) throws SQLException {
        DatabaseMetadataConfig mc = metaConfigs.findMetadataConfig(type);

        checkCatalogAndSchema(mc, catalog, schema);
        Preconditions.checkArgument(StringUtils.hasText(procedure), "参数 procedure 不能为空");

        try (Connection conn = openConnection(type, dbid)) {
            QueryConfig qc = DatabaseMetadataConfigs.findMatchedQueryConfig(conn, mc.getProcedureDdls());
            String ddlSql = Utils.replace(qc.getSql(), "catalog", catalog, "schema", schema,  "procedure", procedure);
            int ddlIndex = qc.getIndex();

            return sqlService.executeQueryAndMergeSpecifiedColumnToString(conn, ddlSql, ddlIndex);
        }
    }

    /**
     * 获取函数创建语句。
     */
    public String findFunctionDdl(DatabaseType type, int dbid, String catalog, String schema, String function) throws SQLException {
        DatabaseMetadataConfig mc = metaConfigs.findMetadataConfig(type);

        checkCatalogAndSchema(mc, catalog, schema);
        Preconditions.checkArgument(StringUtils.hasText(function), "参数 function 不能为空");

        try (Connection conn = openConnection(type, dbid)) {
            QueryConfig qc = DatabaseMetadataConfigs.findMatchedQueryConfig(conn, mc.getFunctionDdls());
            String ddlSql = Utils.replace(qc.getSql(), "catalog", catalog, "schema", schema,  "function", function);
            int ddlIndex = qc.getIndex();

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
     * @param mc 数据库元数据配置。
     * @param catalog 需要校验的 catalog。
     * @param schema 需要校验的 schema。
     */
    private void checkCatalogAndSchema(DatabaseMetadataConfig mc, String catalog, String schema) {
        if (mc.isUseCatalog()) {
            Preconditions.checkArgument(StringUtils.hasText(catalog), "参数 catalog 不能为空");
        }
        if (mc.isUseSchema()) {
            Preconditions.checkArgument(StringUtils.hasText(schema), "参数 schema 不能为空");
        }
    }
}
