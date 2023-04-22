package dsc.desensitivate;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

import static com.google.common.collect.ImmutableMap.of;
import static org.apache.commons.text.StringSubstitutor.replace;

/**
 * 扫描数据库中的所有表，查找出脱敏匹配度高的列。
 */
@Data
@Slf4j
public class SensitiveColumnsScanner {
    /**
     * 数据库信息。
     */
    private MetaVo meta;

    /**
     * 要扫描的数据库名字。
     */
    private String databaseName;

    /**
     * 查询数据库所有表的 SQL 模版。
     */
    private String selectTableNamesSqlPattern;

    /**
     * 查询表中前 N 条数据的 SQL 模板。
     */
    private String selectRowsSqlPattern;

    /**
     * 扫描表的行数。
     */
    private int rowCount;

    /**
     * 当前扫描任务使用的敏感规则。
     */
    private Map<Integer, SensitiveRule> sensitiveRules;

    /**
     * 匹配度阈值，匹配度大于它的列才作为敏感列考虑。
     */
    private double sensitiveRateThreshold;

    /**
     * 预先定义的敏感规则。
     */
    private static final Map<Integer, SensitiveRule> PREDEFINED_SENSITIVE_RULES = ImmutableMap.of(
            SensitiveRule.TYPE_ID_CARD_NO, new SensitiveRuleOfIdCardNo(),
            SensitiveRule.TYPE_EMAIL, new SensitiveRuleOfEmail()
    );

    /**
     * 创建敏感列扫描对象。
     *
     * @param meta 数据库信息。
     * @param databaseName 要扫描的数据库名字。
     * @param selectTableNamesSqlPattern 查询数据库所有表的 SQL 模版。
     *                                   例如 MySQL: SELECT table_name FROM information_schema.tables WHERE table_schema = '${databaseName}'
     * @param selectRowsSqlPattern 查询表中前 N 条数据的 SQL 模板。
     *                             例如 MySQL: SELECT * FROM ${tableName} LIMIT ${rowCount}
     * @param rowCount 扫描表的行数。
     * @param sensitiveTypes 敏感列规则的类型值 (1. 姓名; 2. 身份证号码; 3. 手机号码; 4. 邮箱; 5. 地址; 6. 金额)。
     * @param sensitiveRateThreshold 匹配度阈值，匹配度大于它的列才作为敏感列考虑
     */
    public SensitiveColumnsScanner(MetaVo meta,
                                   String databaseName,
                                   String selectTableNamesSqlPattern,
                                   String selectRowsSqlPattern,
                                   int rowCount,
                                   List<Integer> sensitiveTypes,
                                   double sensitiveRateThreshold) {
        /*
         逻辑:
         1. 参数校验。
         2. 简单成员属性赋值。
         3. 从预先定义好的敏感列规则中找到 sensitiveTypes 对应的对象放入当前扫描任务使用的敏感规则 sensitiveRules 中。
         */

        // [1] 参数校验。
        Preconditions.checkArgument(meta != null, "MetaVo 不能为空");
        Preconditions.checkArgument(StringUtils.hasText(databaseName), "数据库名不能为空");
        Preconditions.checkArgument(StringUtils.hasText(selectTableNamesSqlPattern), "查询数据库所有表的 SQL 模版不能为空");
        Preconditions.checkArgument(StringUtils.hasText(selectRowsSqlPattern), "查询表中前 N 条数据的 SQL 模板不能为空");
        Preconditions.checkArgument(rowCount >= 10, "每个表扫描的行数不能小于 10 行");
        Preconditions.checkArgument(sensitiveTypes != null, "敏感规则类型不能为空");
        Preconditions.checkArgument(sensitiveRateThreshold > 0.2, "匹配度阈值至少大于 0.2，当前值 [{}]", sensitiveRateThreshold);

        // [2] 简单成员属性赋值。
        this.meta = meta;
        this.databaseName = databaseName;
        this.selectTableNamesSqlPattern = selectTableNamesSqlPattern;
        this.selectRowsSqlPattern = selectRowsSqlPattern;
        this.rowCount = rowCount;
        this.sensitiveRateThreshold = sensitiveRateThreshold;
        this.sensitiveRules = new HashMap<>();

        // [3] 从预先定义好的敏感列规则中找到 sensitiveTypes 对应的对象放入当前扫描任务使用的敏感规则 sensitiveRules 中。
        for (Integer type : sensitiveTypes) {
            if (type == null) {
                continue;
            }

            SensitiveRule rule = PREDEFINED_SENSITIVE_RULES.get(type);
            if (rule != null) {
                this.sensitiveRules.put(type, rule);
            } else {
                log.warn("敏感规则不存在，规则类型值 [{}]", type);
            }
        }
    }

    /**
     * 数据库敏感列扫描。
     *
     * @return 返回数据库中所有敏感列信息。
     * @throws SQLException 扫描出问题时抛出异常。
     */
    public List<SensitiveColumn> scan() throws SQLException, InterruptedException, ExecutionException {
        /*
         逻辑:
         1. 使用多线程并发的方式进行敏感列扫描，创建线程池：使用 CompletionService
         2. 查询得到数据库中的所有表名
         3. 多线程查询每个表中的前 N 行数据，计算每个表的敏感列
         4. 合并多线程的结果: 遍历 Future list，通过 get() 方法获取每个 future 结果
         5. 关闭线程池，避免资源泄漏
         */

        // [1] 使用多线程并发的方式进行敏感列扫描，创建线程池：使用 CompletionService
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        CompletionService<List<SensitiveColumn>> executorCompletionService = new ExecutorCompletionService<>(executorService);
        List<Future<List<SensitiveColumn>>> futures = new ArrayList<>();

        // [2] 查询得到数据库中的所有表名
        List<String> tableNames = selectTableNames();

        // [3] 多线程查询每个表中的前 N 行数据，计算每个表的敏感列
        for (String tableName : tableNames) {
            Future<List<SensitiveColumn>> future = executorCompletionService.submit(() -> scanTableSensitiveColumns(tableName));
            futures.add(future);
        }

        // [4] 合并多线程的结果: 遍历 Future list，通过 get() 方法获取每个 future 结果
        List<SensitiveColumn> sensitiveColumns = new LinkedList<>();
        for (int i = 0; i < futures.size(); i++) {
            List<SensitiveColumn> r = executorCompletionService.take().get();
            sensitiveColumns.addAll(r);
        }

        // [5] 关闭线程池，避免资源泄漏
        executorService.shutdown();

        return sensitiveColumns;
    }

    /**
     * 查询得到数据库中的所有表名
     */
    private List<String> selectTableNames() throws SQLException {
        /*
         逻辑:
         1. 查询指定数据库中所有表名的 SQL 语句: 替换 selectTablesSqlPattern 中数据库名字为 databaseName 得到查询所有表名的 SQL 语句
         2. 调用 SQL 执行服务执行查询表名的 SQL 语句得到结果集
         3. 从结果集中拿到表名放入 tableNames 中
         */

        // [1] 查询指定数据库中所有表名的 SQL 语句: 替换 selectTablesSqlPattern 中数据库名字为 databaseName 得到查询所有表名的 SQL 语句
        String sql = replace(selectTableNamesSqlPattern, of("databaseName", databaseName));

        // [2] 调用 SQL 执行服务执行查询表名的 SQL 语句得到结果集
        SqlExecuteService ses = new SqlExecuteService();
        List<Map<String, Object>> sqlResult = ses.select(new MetaVo(), sql);

        // [3] 从结果集中拿到表名放入 tableNames 中
        List<String> tableNames = new LinkedList<>();
        sqlResult.forEach(row -> {
            tableNames.add(row.get("table_name").toString());
        });

        return tableNames;
    }

    /**
     * 扫描表的敏感列
     *
     * @param tableName 表明
     * @return 返回敏感结果数组
     * @throws SQLException 访问数据库错误时抛出 SQL 异常
     */
    private List<SensitiveColumn> scanTableSensitiveColumns(String tableName) throws SQLException {
        /*
         逻辑:
         1. 查询每个表中的前 N 行数据的 SQL 语句：替换 selectRowsSqlPattern 中表名和 rowCount 部分得到查询前 N 行的 SQL 语句
         2. 调用 SQL 执行服务执行查询表的前 N 行数据
         3. 每行的每个列逐个匹配传入的扫描规则，如果匹配度大于等于 50% 则保存到脱敏列中
         4. 敏感列计算: 匹配数量除以 rowCount 得到匹配度，如果某个列的匹配度大于 sensitiveRateThreshold 则作为敏感列
         */

        // [1] 查询每个表中的前 N 行数据的 SQL 语句：替换 selectRowsSqlPattern 中表名和 rowCount 部分得到查询前 N 行的 SQL 语句
        String sql = replace(selectRowsSqlPattern, of(
                "tableName", tableName,
                "rowCount", rowCount
        ));

        // [2] 调用 SQL 执行服务执行查询表的前 N 行数据
        SqlExecuteService ses = new SqlExecuteService();
        List<Map<String, Object>> sqlResult = ses.select(new MetaVo(), sql);

        if (sqlResult.isEmpty()) {
            return Collections.emptyList();
        }

        // [3] 每行的每个列逐个匹配传入的扫描规则，如果匹配度大于等于 50% 则保存到脱敏列中
        // 使用 Guava Table 存储敏感列信息: <列名, 敏感规则, 匹配数量>
        Table<String, Integer, Integer> sensitiveTable = HashBasedTable.create();
        sqlResult.forEach(row -> { // row 是一个 Map<String, Object>
            row.forEach((columnName, columnValue) -> {
                if (columnValue != null) {
                    calculateColumnSensitiveCount(sensitiveTable, columnName, columnValue.toString());
                }
            });
        });

        // [4] 敏感列计算: 匹配数量除以 rowCount 得到匹配度，如果某个列的匹配度大于 sensitiveRateThreshold 则作为敏感列
        List<SensitiveColumn> sensitiveColumns = new LinkedList<>();
        Set<String> columns = sensitiveTable.rowKeySet();

        for (String column : columns) {
            // 每列对每个敏感规则的匹配度
            sensitiveRules.forEach((ruleType, rule) -> {
                Integer matchCount = sensitiveTable.row(column).get(ruleType);
                if (matchCount != null && matchCount / (double) rowCount > sensitiveRateThreshold) {
                    sensitiveColumns.add(new SensitiveColumn(tableName, column, ruleType, matchCount / (double) rowCount));
                }
            });
        }

        return sensitiveColumns;
    }

    /**
     * 计算列匹配敏感规则的数量。
     *
     * @param sensitiveTable 保存列敏感信息的 Guava Table: <列名, 敏感规则类型, 匹配数量>。
     *                       表格数据格式 (垂直表头为列名、水平表头为敏感规则类型、单元格的值为对应敏感规则匹配的数量):
     *                       +-------+---+---+---+
     *                       |       | 1 | 2 | 3 |
     *                       +-------+---+---+---+
     *                       | name  | 2 | 2 | 0 |
     *                       +-------+---+---+---+
     *                       | idno  | 0 | 1 | 0 |
     *                       +-------+---+---+---+
     *                       | email | 0 | 0 | 5 |
     *                       +-------+---+---+---+
     * @param columnName  列名
     * @param columnValue 列值
     */
    private void calculateColumnSensitiveCount(Table<String, Integer, Integer> sensitiveTable, String columnName, String columnValue) {
        /*
         逻辑:
         1. 列的值逐个敏感规则进行测试查看是否匹配
         2. 如果匹配了某个规则，则把此规则匹配数加 1
         */

        // [1] 列的值逐个敏感规则进行测试查看是否匹配
        sensitiveRules.forEach((ruleType, rule) -> {
            // [2] 如果匹配了某个规则，则把此规则匹配数加 1
            if (rule.test(columnValue)) {
                // 对应的规则记录不存在则创建
                if (!sensitiveTable.containsRow(columnName)) {
                    sensitiveTable.put(columnName, ruleType, 0);
                }

                // 把此规则匹配数加 1
                int matchCount = sensitiveTable.row(columnName).get(ruleType);
                sensitiveTable.put(columnName, ruleType, matchCount + 1);
            }
        });
    }

    /**
     * 敏感列
     */
    @Data
    public static class SensitiveColumn {
        private String tableName;         // 表名
        private String columnName;        // 列名
        private int    sensitiveRuleType; // 敏感规则
        private double sensitiveRate;     // 匹配度

        public SensitiveColumn(String tableName, String columnName, int sensitiveRuleType, double sensitiveRate) {
            this.tableName = tableName;
            this.columnName = columnName;
            this.sensitiveRuleType = sensitiveRuleType;
            this.sensitiveRate = sensitiveRate;
        }
    }
}
