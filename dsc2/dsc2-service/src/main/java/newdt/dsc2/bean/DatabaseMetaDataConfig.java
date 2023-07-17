package newdt.dsc2.bean;

import lombok.Data;

/**
 * 数据库元数据配置。
 */
@Data
public class DatabaseMetaDataConfig {
    /**
     * 数据库类型。
     */
    private DatabaseType type;

    /**
     * 显示在对象树上的数据库类型名字。
     */
    private String label;

    /**
     * 是否使用 catalog。
     */
    private boolean useCatalog;

    /**
     * 是否使用 schema。
     */
    private boolean useSchema;

    /**
     * 查询 catalogs 的语句：jdbc 或者 SQL 语句。
     */
    private String catalogQuery;

    /**
     * 查询 schemas 的语句：jdbc 或者 SQL 语句。
     */
    private String schemaQuery;

    /**
     * 查询 tables 的语句：jdbc 或者 SQL 语句。
     * 当为 jdbc 时，需要和 tableJdbcKeys 一起使用。
     */
    private String tableQuery;

    /**
     * 使用 JDBC 查询表时的过滤条件。
     */
    private String tableJdbcKeys;

    private String viewQuery;

    private String viewJdbcKeys;

    private String procedureQuery;

    private String functionQuery;

    private String tableColumnQuery;

    private String tableCreateSql;

    private String procedureCreateSql;

    private String functionCreateSql;
}
