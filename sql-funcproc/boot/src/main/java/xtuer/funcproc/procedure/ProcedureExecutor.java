package xtuer.funcproc.procedure;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.BasicRowProcessor;
import xtuer.funcproc.Result;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

/**
 * 存储执行器，定义了存储过程执行的框架。
 */
@Slf4j
public abstract class ProcedureExecutor {
    protected Connection conn;
    protected Procedure proc;
    protected List<Object> procArguments;
    protected CallableStatement cstmt;
    protected PreparedStatement pstmt;

    /**
     * 执行存储过程。
     *
     * 1. 获取存储过程: Procedure proc = ProcedureFetcher.fetch(conn, CATALOG, SCHEMA, "proc_name");
     * 2. 执行存储过程:
     *    Function pgFunc = Function.fromFunction(func, PostgresFunction.class); // 把普通的存储过程转为 Postgres 等特殊数据库的存储过程对象。
     *    Result result = new PostgresFunctionExecutor().execute(conn, pgFunc, 1, 2, 3);
     *
     * @param conn 数据库连接。
     * @param proc 存储过程对象。需要注意的是对于 Postgres 等有特殊存储过程实现的类此 func 必须是其对应的对象如 PostgresFunction 的。
     * @param procArguments 存储过程参数。
     * @return 返回执行结果。
     * @throws SQLException 执行或获取存储过程结果出错时抛出异常。
     */
    public Result execute(Connection conn, Procedure proc, Object ...procArguments) throws SQLException {
        /*
         逻辑 (使用 Object 类型设置入参和出参，是因为结果显示给前端使用不需要数据类型参与业务计算):
         1. 创建 CallableStatement。
         2. 设置存储过程的参数: 入参、出参、入出参。
         3. 执行存储过程。
         4. 获取存储过程执行的结果。
         5. 关闭释放资源。
         */

        this.conn = conn;
        this.proc = proc;
        this.procArguments = Arrays.asList(procArguments);

        // 设置 Catalog, schema, transaction (Postgres 返回游标需要关闭自动提交)。
        this.conn.setCatalog(this.proc.getCatalog());
        this.conn.setSchema(this.proc.getSchema());
        this.conn.setAutoCommit(false);

        // 执行前的条件检查，例如数据库和存储过程是否匹配。
        this.preCheck();

        // [1] 创建 CallableStatement。
        log.info("执行存储过程: SQL [{}]，参数 {}", this.proc.getCallableSql(), this.procArguments);
        return this.executeUseCallableStatement();
    }

    /**
     * 使用 CallableStatement 的方式执行存储存储过程。
     */
    private Result executeUseCallableStatement() throws SQLException {
        try (CallableStatement cstmt = this.conn.prepareCall(this.proc.getCallableSql())) {
            this.cstmt = cstmt;

            // [2] 设置存储过程的参数: 入参、出参、入出参。
            setAndRegisterParameters();

            // [3] 执行存储过程。
            cstmt.execute();

            // [4] 获取存储过程执行的结果。
            Result result = handleResult();
            conn.commit();

            return result;
        }
    }

    /**
     * 设置输入参数，注册输出参数。
     */
    protected abstract void setAndRegisterParameters() throws SQLException;

    /**
     * 获取存储过程执行的结果集。
     *
     * 注意: 不同的数据库可能不一样。
     */
    protected abstract ResultSet getResultSet() throws SQLException;

    /**
     * 获取输出参数。
     */
    protected abstract void getOutParameters(Result result) throws SQLException;

    /**
     * 执行前的条件检查，例如数据库和存储过程是否匹配。
     */
    protected abstract void preCheck();

    /**
     * 获取存储过程执行的结果。
     */
    protected Result handleResult() throws SQLException {
        /*
         逻辑:
         1. 获取更新的影响行数 (即使是更新语句，也有可能返回 -1)。
         2. 获取存储过程执行的输出参数。
         3. 获取存储过程执行的结果集。
         */

        Result result = new Result();

        // [1] 获取更新的影响行数 (即使是更新语句，也有可能返回 -1)。
        if (this.cstmt != null) {
            result.setUpdateCount(cstmt.getUpdateCount());
        }

        // [2] 获取存储过程执行的输出参数。
        getOutParameters(result);

        // [3] 获取存储过程执行的结果集。
        ResultSet rs = getResultSet();

        if (rs != null) {
            // 每行数据转为一个 Map<String, Object>
            BasicRowProcessor rowProcessor = new BasicRowProcessor();
            while (rs.next()) {
                result.getRows().add(rowProcessor.toMap(rs));
            }
            rs.close();
        }

        return result;
    }

    /**
     * 检查 klass 是否可以赋值为 proc，如果不可以则抛出异常。
     *
     * @param klass 存储过程类型。
     * @param proc 存储过程对象。
     */
    public static void checkAssignable(Class<? extends Procedure> klass, Procedure proc) {
        if (!klass.isAssignableFrom(proc.getClass())) {
            throw new RuntimeException("要执行的存储过程类型必须为 " + klass.getName());
        }
    }
}
