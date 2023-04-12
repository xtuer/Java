package com.xtuer.util;

import com.xtuer.bean.SqlFileImportTask;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SQL 文件导入。
 */
@Slf4j
@Data
public class SqlImporter {
    /**
     * JDBC 连接字符串。
     */
    private String jdbcUrl;

    /**
     * 数据库账号。
     */
    private String dbUsername;

    /**
     * 数据库密码。
     */
    private String dbPassword;

    /**
     * 导入任务的 ID。
     */
    private String taskId;

    /**
     * SQL 文件路径。
     */
    private String sqlFilePath;

    /**
     * 出错时执行的回滚 SQL。
     */
    private String rollbackSql;

    /**
     * 把 batchSize 个 SQL 语句作为一个事务进行提交。
     */
    private int batchSize;

    /**
     * 从文件中一次读取 SQL 语句的行数。
     */
    private int lineCount;

    /**
     * 创建 SQL 文件导入类的对象。
     *
     * @param jdbcUrl JDBC 连接字符串。
     * @param dbUsername 数据库账号。
     * @param dbPassword 数据库密码。
     * @param taskId 导入任务的 ID。
     * @param sqlFilePath SQL 文件路径。
     * @param rollbackSql 出错时执行的回滚 SQL。
     * @param batchSize 把 batchSize 个 SQL 语句作为一个事务进行提交。
     * @param lineCount 从文件中一次读取 SQL 语句的行数。
     */
    public SqlImporter(String jdbcUrl, String dbUsername, String dbPassword, String taskId, String sqlFilePath, String rollbackSql, int batchSize, int lineCount) {
        this.jdbcUrl = jdbcUrl;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;

        this.taskId = taskId;
        this.sqlFilePath = sqlFilePath;
        this.rollbackSql = rollbackSql;
        this.batchSize = batchSize;
        this.lineCount = lineCount;
    }

    /**
     * 导入 SQL 文件。
     */
    public void importSqlFile() {
        /*
         业务逻辑:
         1. 接收到 SQL 语句后使用事务执行 (只支持变更语句，不支持查询语句)。
         2. 执行的 SQL 语句达到提交数量后提交事务。
         3. 当 SQL 提取完成后，如果还有没提交的 SQL 则提交事务。
         4. 导入完成时保存完成进度。
         5. 当 SQL 执行出错时，结束执行，如果用户输入了回滚 SQL 语句则执行回滚语句。
         */

        log.info("[开始] 导入 SQL 文件，文件路径 [{}]，回滚语句 [{}]，事务的 SQL 语句数量 [{}]，一次读取 SQL 文件中的行数 [{}]", sqlFilePath, rollbackSql, batchSize, lineCount);

        File file = new File(sqlFilePath);

        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword); BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // 提示: 使用 atomic 的整数是因为 Lambda 里不能直接修改外部的 primitive 整数变量。
            AtomicBoolean stopped = new AtomicBoolean(false); // 执行 SQL 出错时停止继续读取 SQL，终止导入操作。
            ExecuteStatus status = new ExecuteStatus(); // 执行的状态数据。
            status.totalBytes = file.length();
            status.startTime = System.currentTimeMillis();

            // 取消自动提交，使用事务执行变更语句。
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();

            SqlExtractor.extractSqls(reader, lineCount, stopped, (sqls, finished) -> {
                // 用户记录当前执行的 SQL 语句，抛异常的时候方便输出错误的 SQL 语句。
                String currentSql = null;

                try {
                    // [1] 接收到 SQL 语句后使用事务执行 (只支持变更语句，不支持查询语句)。
                    for (String sql : sqls) {
                        currentSql = sql;
                        status.executedSqlCount++;
                        stmt.executeUpdate(sql); // 执行 SQL 语句。

                        status.uncommittedSqlCount++;
                        status.committedBytes += sql.getBytes().length;

                        // [2] 执行的 SQL 语句达到提交数量后提交事务。
                        if (status.uncommittedSqlCount % batchSize == 0) {
                            commit(conn, status);
                        }
                    }

                    // [3] 当 SQL 提取完成后，如果还有没提交的 SQL 则提交事务。
                    if (status.uncommittedSqlCount % batchSize != 0 && finished) {
                        status.committedBytes = status.totalBytes;
                        commit(conn, status);
                    }

                    if (finished) {
                        // [4] 导入完成时保存完成进度。
                        // 需要校正提交的数据量，因为提取 SQL 语句时可能会去掉一些空白字符。
                        status.committedBytes = status.totalBytes;
                        saveProcess(status);

                        long elapsed = (System.currentTimeMillis() - status.startTime) / 1000;
                        log.info("[结束] 导入 SQL 文件完成，耗时 [{}] 秒,文件路径 [{}]", elapsed, sqlFilePath);
                    }
                } catch (SQLException e) {
                    // 报错情况:
                    // A. SQL 语句语法错误。
                    // B. 有 select 语句的时候会报错: Can not issue SELECT via executeUpdate() or executeLargeUpdate()。
                    // C. 网络问题。
                    status.error = String.format("执行第 [%d] 条 SQL 错误, SQL [%s], 异常:\n%s", status.executedSqlCount, currentSql, Utils.getStackTrace(e));
                    log.warn("[错误] {}", status.error);

                    // 保存错误信息。
                    saveProcess(status);

                    // 出错时事务回滚。
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        log.warn(Utils.getStackTrace(ex));
                    }

                    // [5] 当 SQL 执行出错时，结束执行，如果用户输入了回滚 SQL 语句则执行回滚语句。
                    stopped.set(true);
                    if (StringUtils.hasText(rollbackSql)) {
                        log.info("执行回滚语句: {}", rollbackSql);
                        quietlyExecuteUpdateAndCommit(conn, rollbackSql);
                    }
                }

                return null;
            });
        } catch (SQLException | IOException e) {
            log.warn(Utils.getStackTrace(e));

            // 连接数据库错误或者 SQL 文件不存在。
            ExecuteStatus status = new ExecuteStatus();
            status.error = Utils.getStackTrace(e);
            saveProcess(status);
        }
    }

    /**
     * 提交事务。
     */
    private void commit(Connection conn, ExecuteStatus status) throws SQLException {
        conn.commit();
        status.uncommittedSqlCount = 0;
        saveProcess(status);
    }

    /**
     * 处理进度。
     */
    private void saveProcess(ExecuteStatus status) {
        // 避免除 0 错误。
        if (status.totalBytes != 0) {
            int percent = (int) ((double) status.committedBytes / status.totalBytes * 100);
            log.debug("完成 {}%: {} / {} (Bytes)", percent, status.committedBytes, status.totalBytes);
        }

        // TODO: 回调 DSC 保存进度。
        SqlFileImportTask task = new SqlFileImportTask();
        task.setTaskId(taskId);
        task.setCommittedBytes(status.committedBytes);
        task.setTotalBytes(status.totalBytes);
        task.setError(status.error);

        String url = String.format("http://localhost:8080/api/dsc/sql-file-imports/%s/process", taskId);
        RequestUtils.doRequest(url, HttpMethod.POST, task, SqlFileImportTask.class);
    }

    /**
     * 执行并提交 SQL 语句，不抛出异常。
     */
    private static void quietlyExecuteUpdateAndCommit(Connection conn, String sql) {
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            conn.commit();
        } catch (SQLException ex) {
            log.warn(Utils.getStackTrace(ex));

            try {
                conn.rollback();
            } catch (SQLException ex2) {
                log.warn(Utils.getStackTrace(ex2));
            }
        }
    }

    /**
     * 执行的状态数据。
     */
    public static class ExecuteStatus {
        /**
         * 已经提交的字节数，计算进度。
         */
        public long committedBytes = 0;

        /**
         * 总共要执行的字节数，即 文件的大小，计算进度。
         */
        public long totalBytes = 0;

        /**
         * 未提交的 SQL 语句数量。
         */
        public int uncommittedSqlCount = 0;

        /**
         * 执行过的 SQL 语句数量。
         */
        public int executedSqlCount = 0;

        /**
         * 开始导入时间。
         */
        public long startTime = 0;

        /**
         * 发生错误的错误信息。
         */
        public String error;
    }
}
