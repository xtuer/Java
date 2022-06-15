package misc;

import com.google.common.collect.ImmutableMap;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 同库备份的工具类。
 * 主要是解析 DML SQL 和构造同库备份的 SQL。
 */
public class OracleBackupInTheSameDbUtils {
    public static final String TABLE_NAME = "tableName";
    public static final String WHERE_CLAUSE = "whereClause";

    /**
     * 转储表名的时间格式
     */
    private static final SimpleDateFormat TABLE_NAME_DATE_FORMATTER = new SimpleDateFormat("MMdd_HHmmss");

    /**
     * 构造同库备份语句: create table test4 as select * from test where id > 1
     *
     * @param dmlSql 更新的 SQL 语句，包含 UPDATE 和 DELETE
     * @return 返回同库备份语句
     */
    public static String generateBackupSql(String dmlSql) {
        /*
         逻辑:
         1. 获取 DML 语句的表名
         2. 获取 DML 语句后面的 where 条件
         3. 构造备份语句: create table test4 as select * from test where id > 1
         */

        Map<String, String> tableNameAndWhereClause = parseDmlSql(dmlSql);
        String tableName = tableNameAndWhereClause.get(TABLE_NAME);
        String whereClause = tableNameAndWhereClause.get(WHERE_CLAUSE);
        String newTableName = tableName + "_" + TABLE_NAME_DATE_FORMATTER.format(new Date());

        return String.format("CREATE TABLE %s AS SELECT * FROM %s %s", newTableName, tableName, whereClause);
    }

    /**
     * 解析 DML SQL 语句，提取表名和条件子句
     *
     * @param dmlSql 更新的 SQL 语句，包含 UPDATE 和 DELETE
     * @return 返回 Map:
     *   tableName: 表名
     *   whereClause: WHERE 子句
     */
    public static Map<String, String> parseDmlSql(String dmlSql) {
        Pattern pattern;
        String tempSql = dmlSql.toUpperCase();

        if (tempSql.startsWith("UPDATE")) {
            // 更新语句: UPDATE <table-name> SET x=x, y=y [WHERE conditions]
            // 提示: 不能使用贪婪匹配直接找到 WHERE 子句，因为 WHERE 子句是可选的导致冲突
            pattern = Pattern.compile("UPDATE\\s+(\\w+)\\s+SET", Pattern.CASE_INSENSITIVE);
        } else if (tempSql.startsWith("DELETE")) {
            // 删除语句: DELETE FROM <table-name> [WHERE conditions]
            pattern = Pattern.compile("DELETE\\s+FROM\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        } else {
            throw new RuntimeException("不是 DML 语句: " + dmlSql);
        }

        Matcher matcher = pattern.matcher(dmlSql);
        if (matcher.find()) {
            String tableName = matcher.group(1);
            String whereClause = "";

            // 查找 where 子句
            int whereIndex = tempSql.indexOf("WHERE");
            if (whereIndex != -1) {
                whereClause = dmlSql.substring(whereIndex);
            }

            return ImmutableMap.of(
                    TABLE_NAME, tableName,
                    WHERE_CLAUSE, whereClause
            );
        }

        throw new RuntimeException("DML 语句不对: " + dmlSql);
    }
}
