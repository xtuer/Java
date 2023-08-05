package newdt.dsc.bean.db;

import lombok.Data;

import java.util.List;

/**
 * 数据库元数据配置。
 */
@Data
public class DatabaseMetadataConfig {
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
     * 是否使用 WebTerminal。
     */
    private boolean useWebTerminal;

    /**
     * 是否使用 procedure。
     */
    private boolean useProcedure;

    /**
     * 是否使用 function。
     */
    private boolean useFunction;

    /**
     * 查询 catalogs 的配置。
     * 支持 JDBC 和 SQL。
     */
    private List<QueryConfig> catalogNames;

    /**
     * 查询 schemas 的配置。
     * 支持 JDBC 和 SQL。
     */
    private List<QueryConfig> schemaNames;

    /**
     * 查询 tables 的配置。
     * 支持 JDBC 和 SQL。
     */
    private List<QueryConfig> tableNames;

    /**
     * 查询表的列的配置。
     * 支持 JDBC 和 SQL。
     */
    private List<QueryConfig> tableColumns;

    /**
     * 查询 views 的配置。
     * 支持 JDBC 和 SQL。
     */
    private List<QueryConfig> viewNames;

    /**
     * 查询存储过程的配置。
     * 支持 JDBC 和 SQL。
     */
    private List<QueryConfig> procedureNames;

    /**
     * 查询函数的配置。
     * 支持 JDBC 和 SQL。
     */
    private List<QueryConfig> functionNames;

    /**
     * 查询触发器的配置。
     * 只支持 SQL。
     */
    private List<QueryConfig> triggerNames;

    /**
     * 查询建表语句的配置。
     * 只支持 SQL。
     */
    private List<QueryConfig> tableDdls;

    /**
     * 查询视图的创建语句的配置。
     * 只支持 SQL。
     */
    private List<QueryConfig> viewDdls;

    /**
     * 查询存储过程的创建语句的配置。
     * 只支持 SQL。
     */
    private List<QueryConfig> procedureDdls;

    /**
     * 查询函数的创建语句的配置。
     * 只支持 SQL。
     */
    private List<QueryConfig> functionDdls;

    /**
     * 查询触发器的创建语句的配置。
     * 只支持 SQL。
     */
    private List<QueryConfig> triggerDdls;

    /**
     * 查询语句的配置。
     */
    @Data
    public static class QueryConfig {
        /**
         * 数据库的版本 (使用 major version)。
         */
        private String version;

        /**
         * 查询的 SQL 语句:
         * - 值为 jdbc 则表示使用 JDBC 接口的方式获取，查询 table，view 等数据使用需要配合 tableTypes 进行过滤。
         * - 值为常规 SQL 语句时执行 SQL 语句获取，这时需要配合 index 一起使用，获取结果集中的第 index 列作为结果。
         */
        private String sql;

        /**
         * 从结果集中第 index 列获取查询结果，从 1 开始。
         */
        private int index = 1;

        /**
         * 使用 JDBC 的接口获取元数据时的过滤条件，例如查询 table，view 时会使用到。
         */
        private String[] tableTypes;
    }
}
