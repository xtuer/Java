package newdt.dsc.bean.db;

import lombok.Data;

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
     * 查询 catalogs 的配置。
     */
    private QueryConfig catalog;

    /**
     * 查询 schemas 的配置。
     */
    private QueryConfig schema;

    /**
     * 查询 tables 的配置。
     */
    private QueryConfig table;

    /**
     * 查询 views 的配置。
     */
    private QueryConfig view;

    private QueryConfig procedure;

    private QueryConfig function;

    private QueryConfig tableColumn;

    private QueryConfig tableDdl;

    private QueryConfig viewDdl;

    private QueryConfig procedureDdl;

    private QueryConfig functionDdl;

    /**
     * 查询语句的配置。
     */
    @Data
    public static class QueryConfig {
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
