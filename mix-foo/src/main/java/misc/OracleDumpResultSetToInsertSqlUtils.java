package misc;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 导出 ResultSet 为 INSERT 语句的工具类。
 *
 * 参考 Oracle 和 Java 的数据类型映射关系: https://docs.oracle.com/cd/A97335_02/apps.102/a83724/basic3.htm
 */
public class OracleDumpResultSetToInsertSqlUtils {
    /**
     * 把 ResultSet 导出为 INSERT 语句。
     *
     * @param rs 结果集
     * @param tableName INSERT 语句中的表名
     * @return 返回 INSERT 语句字符串
     */
    public static String dumpResultSetToString(ResultSet rs, String tableName) throws SQLException {
        /*
         逻辑:
         1. 获取列信息
         2. 构建 INSERT 语句的前面部分
         3. 遍历 ResultSet，每行记录生成一个 INSERT 语句
         */

        // [1] 获取列信息
        List<ColumnInfo> columnInfoList = getColumnInfoList(rs);
        final int columnCount = columnInfoList.size();

        // [2] 构建 INSERT 语句的前面部分
        StringBuilder prefixSql = new StringBuilder("INSERT INTO ");
        prefixSql.append(tableName);
        prefixSql.append(" (");

        // INSERT 语句中的列名，逗号分隔
        for (int i = 0; i < columnCount; i++) {
            prefixSql.append("\"")
                    .append(columnInfoList.get(i).name)
                    .append("\"");

            // 最后一列不增加逗号
            if (i < columnCount - 1) {
                prefixSql.append(",");
            }
        }
        prefixSql.append(") VALUES (");

        StringBuilder result = new StringBuilder();

        // [3] 遍历 ResultSet，每行记录生成一个 INSERT 语句
        while (rs.next()) {
            StringBuilder sql = new StringBuilder(prefixSql);

            // INSERT 语句中列的值，逗号分隔
            for (int i = 0; i < columnCount; i++) {
                sql.append(getColumnAsString(rs, columnInfoList.get(i)));

                // 最后一列不增加逗号
                if (i < columnCount - 1) {
                    sql.append(",");
                }
            }
            sql.append(");\n");
            result.append(sql);
        }

        return result.toString();
    }

    /**
     * 读取 ResultSet 中列转为字符串。
     * 字符串列使用单引号括起来。
     *
     * @param rs 结果集
     * @param ci 列信息
     * @return 返回列值，空值列和不支持的类型列都返回 "NULL"。
     */
    public static String getColumnAsString(ResultSet rs, ColumnInfo ci) throws SQLException {
        if (rs.getObject(ci.column) == null) {
            return "NULL";
        }

        // 参考 Oracle 和 Java 的数据类型映射关系: https://docs.oracle.com/cd/A97335_02/apps.102/a83724/basic3.htm
        switch (ci.jdbcType) {
            // 字符串
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARBINARY:
                return singleQuoteString(rs.getString(ci.column));
            case Types.CLOB:
                Clob clob = rs.getClob(ci.column);
                String str = clob.getSubString(1, (int) clob.length());
                return singleQuoteString(str);

            // 日期时间
            case Types.DATE:
                java.sql.Date date = rs.getDate(ci.column);
                return singleQuoteString(date.toString());
            case Types.TIME:
                java.sql.Time time = rs.getTime(ci.column);
                return singleQuoteString(time.toString());
            case Types.TIMESTAMP:
                java.sql.Timestamp timestamp = rs.getTimestamp(ci.column);
                return singleQuoteString(timestamp.toString());

            // 不支持的类型
            case Types.BLOB:
            case Types.STRUCT:
            case Types.REF:
            case Types.ARRAY:
                System.out.printf("不支持的类型: %d, 列名: %s", ci.jdbcType, ci.typeName);
                return "NULL";
            // 默认类型直接返回对应的字符串值
            default:
                return rs.getObject(ci.column).toString();
        }
    }

    /**
     * 从 ResultSet 中获取列的信息
     *
     * @param rs 结果集
     * @return 返回结果集中的列信息
     */
    public static List<ColumnInfo> getColumnInfoList(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        List<ColumnInfo> columnInfoList = new ArrayList<>(rsmd.getColumnCount());

        for (int c = 1; c <= rsmd.getColumnCount(); c++) {
            columnInfoList.add(new ColumnInfo(rsmd.getColumnName(c), c, rsmd.getColumnType(c), rsmd.getColumnTypeName(c)));
        }

        return columnInfoList;
    }

    /**
     * 根据 SQL 规范把字符串中的特殊字符进行转义，例如
     * A. \ 变为 \\
     * B. ' 变为 \'
     * C. 回车变为 \n
     *
     * @param str 字符串
     * @return 返回转义后的字符串
     */
    public static String escapeString(String str) {
        str = str.replace("\\", "\\\\");
        str = str.replace("'", "\\'");
        str = str.replace("\n", "\\n");

        return str;
    }

    /**
     * 把字符串使用单引号括起来
     *
     * @param str 字符串
     * @return 返回引号括起来的字符串
     */
    public static String singleQuoteString(String str) {
        return "'" + escapeString(str) + "'";
    }

    /**
     * 列的信息
     */
    public static class ColumnInfo {
        public String name;     // 列名
        public int    column;   // ResultSet 中的索引
        public int    jdbcType; // JDBC 标准的类型值
        public String typeName; // DB Driver 相关的类型名 (同一个 JDBC 类型，不同数据库返回的 typeName 可能不同)

        public ColumnInfo(String name, int column, int jdbcType, String typeName) {
            this.name     = name;
            this.column   = column;
            this.jdbcType = jdbcType;
            this.typeName = typeName;
        }

        @Override
        public String toString() {
            return String.format("%3d: %10s, %s", jdbcType, typeName, name);
        }
    }
}
