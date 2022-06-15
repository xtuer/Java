package misc;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.languages.Dialect;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Oracle 的数据转储的工具类。
 *
 * 提示:
 * A. 很多操作都是 Oracle 相关的。
 * B. Oracle 建的表名和字段名时，如果没有加引号 (ORACLE 中 "" 的作用是强制区分大小写，以及关键字做字段时用 ")，都会自动给我们转换为大写的。
 * C. SQL 格式工具依赖 com.github.vertical-blank:sql-formatter:2.0.3
 * D. 字符串拆分合并依赖 Google Guava 'com.google.guava:guava:31.0.1-jre'
 */
public final class OracleDataTransferUtils {
    public static final String TABLE_NAME = "tableName";
    public static final String CREATE_TABLE_SQL = "createTableSql";

    /**
     * 从结果集中获取列名:
     * * 去掉了重复列名
     * * 如果特殊情况返回的列名都是大写的，因为 Oracle 默认列名大写
     *
     * @param rs SQL 查询的结果集
     * @return 返回列名的 Set
     */
    public static Set<String> getColumnNames(ResultSet rs) throws SQLException {
        Set<String> columnNames = new TreeSet<>();

        ResultSetMetaData rsmd = rs.getMetaData();
        for (int i = 0; i < rsmd.getColumnCount(); i++) {
            columnNames.add(rsmd.getColumnName(i + 1));
        }

        return columnNames;
    }

    /**
     * 转储表名的时间格式
     */
    private static final SimpleDateFormat TABLE_NAME_DATE_FORMATTER = new SimpleDateFormat("MMdd_HHmmss");

    /**
     * 根据源表的定义生成数据转储表的建表语句，新表的列为包含在 columnNames 中指定的转储列，并且去掉约束语句。
     * 转储表名的格式为 <table-name>_<timestamp(MMdd_HHmmss)>，如 table1_0525_103012。
     *
     * @param tableName 原表名
     * @param columnNames 转储的列名，列名是大小写敏感的 (Oracle 内部默认使用大写)，如无特殊情况必须使用大写的列名
     * @param originalCreateTableSql 原建表语句
     * @return 返回数据转储的建表信息 Map:
     *   tableName: 表名
     *   createTableSql: 建表语句
     */
    public static Map<String, String> generateDataTransferCreateTableSql(String tableName, Set<String> columnNames, String originalCreateTableSql) {
        /*
         逻辑:
         1. 把列名全部大写 (去掉这一步，全部大写有问题)
         2. 格式化建表语句
         3. 使用 Guava Splitter 对建表语句按行分割 (去掉前后的空白字符和最后面的逗号)
         4. 如果列的定义在传入的 columnNames 中则保存到 transferColumnDefinitions
         5. 生成转储表名，格式为 <table-name>_<timestamp(MMdd_hhmmss)>，如 table1_0525_103012
         6. 使用转储表名和转储列定义创建转储表的建表语句
         */

        // [1] 把列名全部大写
        // columnNames = columnNames.stream().map(String::toUpperCase).collect(Collectors.toSet());

        // [2] 格式化建表语句
        originalCreateTableSql = OracleDataTransferUtils.formatSql(originalCreateTableSql);

        // [3] 使用 Guava Splitter 对建表语句按行分割 (去掉前后的空白字符和最后面的逗号)
        Iterable<String> lines = Splitter.on("\n").trimResults(CharMatcher.anyOf(" ,")).split(originalCreateTableSql);

        // [4] 如果列的定义在传入的 columnNames 中则保存到 transferColumnDefinitions
        List<String> transferColumnDefinitions = new LinkedList<>(); // 转储列的定义

        for (String line : lines) {
            if (needColumnDefinition(line, columnNames)) {
                transferColumnDefinitions.add(line);
            }
        }

        // [5] 生成转储表名，格式为 <table-name(指定)>_<timestamp(MMdd_hhmmss)>，如 table1_0525_103012
        // [6] 使用转储表名和转储列定义创建转储表的建表语句
        String newTableName = tableName + "_" + TABLE_NAME_DATE_FORMATTER.format(new Date());
        String transferColumnDefinitionsString = Joiner.on(",").join(transferColumnDefinitions); // 例如 "ID" NUMBER(*, 0),"NAME" VARCHAR2(256)
        String newCreateTableSql = String.format("CREATE TABLE %s (%s)", newTableName, transferColumnDefinitionsString);

        return ImmutableMap.of(
                TABLE_NAME, newTableName,
                CREATE_TABLE_SQL, OracleDataTransferUtils.formatSql(newCreateTableSql)
        );
    }

    /**
     * 判断列的定义是否在指定的列中
     *
     * @param columnDefinition 列定义
     * @param columnNames 列名
     * @return 判断列的定义在指定的列中返回 true，否则返回 false
     */
    private static boolean needColumnDefinition(String columnDefinition, Set<String> columnNames) {
        for (String name : columnNames) {
            if (columnDefinition.startsWith("\"" + name + "\"")) {
                return true;
            }
        }

        return false;
    }

    /**
     * 格式化 SQL 语句，不会改变输入 sql 语句中字符的大小写。
     * 依赖 com.github.vertical-blank:sql-formatter:2.0.3
     *
     * @param sql 要格式化的 SQL 语句
     * @return 返回格式化后的 SQL 语句
     */
    public static String formatSql(String sql) {
        return SqlFormatter.of(Dialect.PlSql).format(sql);
    }

    /**
     * 从数据库中查询建表语句 (字符都是大写的)
     *
     * @param conn   数据库连接
     * @param schema 数据库的 Schema
     * @param tableName 表名
     * @return 返回建表语句，格式如下 (属性名都被双引号括起来):
     *   CREATE TABLE "DBMON"."TEST" (
     *     "ID" NUMBER(*, 0),
     *     "NAME" VARCHAR2(256),
     *     "AGE" NUMBER(*, 0)
     *   ) SEGMENT CREATION DEFERRED PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING TABLESPACE "USERS"
     * @throws SQLException Schema 或者 表表不存在时抛出异常。Schema 不存在的异常信息 user does not exist， 表不存在的异常信息 table or view does not exist。
     */
    public static String findCreateTableSql(Connection conn, String schema, String tableName) throws SQLException {
        // 提示:
        // A. 查询建表语句的 SQL: SELECT DBMS_METADATA.GET_DDL('TABLE', tableName, schemaName) FROM DUAL
        // B. Schema 和表名要大写: Oracle 建的表名和字段名时，如果没有加引号 (ORACLE 中 "" 的作用是强制区分大小写，以及关键字做字段时用 ")，都会自动给我们转换为大写的。

        String sql = String.format("SELECT DBMS_METADATA.GET_DDL('TABLE', '%s', '%s') FROM DUAL", tableName.toUpperCase(), schema.toUpperCase());
        System.out.println("查询建表语句: " + sql);

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                return OracleDataTransferUtils.formatSql(rs.getString(1));
            }
        }

        return null;
    }

    /**
     * 获取查询 SQL 语句的表名 (只支持单表查询，并且不支持单表的自我关联等)
     *
     * SQL 示例:
     * A. select * from user
     * B. select * from user where id = 1
     * C. select name from user
     * D. select name from user where id = 1
     * E. select * from user AS u
     * F. select * from user AS u where id = 1
     *
     * @param selectSql 查询 SQL 语句 (调用者保证 SQL 语句的正确性)
     * @return 返回 SQL 语句使用的表名，如果非单表操作则返回 null
     */
    public static String extractTableName(String selectSql) {
        // 注意:
        // A. ResultSetMetaData#getTableName() 结果集为空时返回空字符串，所以不能使用这种方式
        // B. AS 和 WHERE 子句为可选
        // C. 对 SQL 语句不做完整的校验，如果要做完善的校验就不能简单的使用正则表达式，而要用 Antlr 等专业工具来做

        Pattern pattern = Pattern.compile("FROM\\s+(\\w+)(\\s+AS\\w+)?(\\s+WHERE)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(selectSql);

        if (matcher.find()) {
            String tableName = matcher.group(1);
            return tableName;
        }

        return null;
    }

    /**
     * 从结果集中获表名
     *
     * @param rs SQL 查询的结果集
     * @return 返回第一列的表名
     * @deprecated 结果集为空时有问题，不要使用
     */
    public static String getTableNameOnlySupportOneTable(ResultSet rs) throws SQLException {
        Set<String> names = new HashSet<>();

        // 获取所有列的表名
        ResultSetMetaData rsmd = rs.getMetaData();
        for (int i = 0; i < rsmd.getColumnCount(); i++) {
            names.add(rsmd.getTableName(i + 1));
        }

        // 如果表名个数不为 1 个，则抛出异常
        if (names.size() != 1) {
            throw new RuntimeException("表名数量不为 1: " + names.size());
        }

        // 返回表名
        return names.stream().filter(n -> !"".equals(n)).findAny().get();
    }

    /**
     * 创建插入语句
     *
     * @param tableName 表名
     * @param columnNames 要插入的列，列名是大小写敏感的
     * @return 返回插入语句，格式为 INSERT INTO ${tableName} ("COL1, "COL2", "COL3") VALUES(?, ?, ?)
     */
    public static String generatePreparedInsertSql(String tableName, Set<String> columnNames) {
        // 列名转为大写，并且使用双引号括起来避免关键字导致 SQL 语句无效
        columnNames = columnNames.stream().map(col -> "\"" + col + "\"").collect(Collectors.toSet());

        // 每个列名生成一个对应的占位符号 "?"
        List<String> questionMarks = columnNames.stream().map(n -> "?").collect(Collectors.toList());

        // 生成插入语句并返回
        return String.format("INSERT INTO %s (%s) VALUES (%s)",
                tableName,
                Joiner.on(", ").join(columnNames),
                Joiner.on(", ").join(questionMarks));
    }
}
