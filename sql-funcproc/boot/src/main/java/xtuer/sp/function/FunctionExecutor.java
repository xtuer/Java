package xtuer.sp.function;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.BasicRowProcessor;
import xtuer.sp.Result;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

/**
 * 函数执行器，定义了函数执行的框架。
 */
@Slf4j
public abstract class FunctionExecutor {
    protected Connection conn;
    protected Function func;
    protected List<Object> funcArguments;
    protected CallableStatement cstmt;
    protected PreparedStatement pstmt;

    /**
     * 为 true 使用 CallableStatement，为 false 使用 PreparedStatement。
     */
    protected boolean useCallableStatement = true;

    /**
     * 执行存储函数。
     *
     * 1. 获取函数: Function func = FunctionFetcher.fetch(conn, CATALOG, SCHEMA, "func_no_arg_return_base_type");
     * 2. 执行函数:
     *    Function pgFunc = Function.fromFunction(func, PostgresFunction.class); // 把普通的函数转为 Postgres 等特殊数据库的函数对象。
     *    Result result = new PostgresFunctionExecutor().execute(conn, pgFunc, 1, 2, 3);
     *
     * @param conn 数据库连接。
     * @param func 函数对象。需要注意的是对于 Postgres 等有特殊函数实现的类此 func 必须是其对应的对象如 PostgresFunction 的。
     * @param funcArguments 函数参数。
     * @return 返回执行结果。
     * @throws SQLException 执行或获取函数结果出错时抛出异常。
     */
    public Result execute(Connection conn, Function func, Object ...funcArguments) throws SQLException {
        /*
         逻辑 (使用 Object 类型设置入参和出参，是因为结果显示给前端使用不需要数据类型参与业务计算):
         1. 创建 CallableStatement。
         2. 设置函数的参数: 入参、出参、入出参。
         3. 执行函数。
         4. 获取函数执行的结果。
         5. 关闭释放资源。
         */

        this.conn = conn;
        this.func = func;
        this.funcArguments = Arrays.asList(funcArguments);

        // 设置 Catalog, schema, transaction (Postgres 返回游标需要关闭自动提交)。
        this.conn.setCatalog(this.func.getCatalog());
        this.conn.setSchema(this.func.getSchema());
        this.conn.setAutoCommit(false);

        // 执行前的条件检查，例如数据库和函数是否匹配。z
        this.preCheck();

        // [1] 创建 CallableStatement。
        log.info("执行函数: Signature [{}], SQL [{}]，参数 {}", this.func.getSignature(), this.func.getCallableSql(), this.funcArguments);
        if (this.useCallableStatement) {
            return this.executeUseCallableStatement();
        } else {
            return this.executeUsePreparedStatement();
        }
    }

    /**
     * 使用 CallableStatement 的方式执行存储函数。
     */
    private Result executeUseCallableStatement() throws SQLException {
        try (CallableStatement cstmt = this.conn.prepareCall(this.func.getCallableSql())) {
            this.cstmt = cstmt;

            // [2] 设置函数的参数: 入参、出参、入出参。
            setAndRegisterParameters();

            // [3] 执行函数。
            cstmt.execute();

            // [4] 获取函数执行的结果。
            Result result = handleResult();
            conn.commit();

            return result;
        }
    }

    /**
     * 使用 PreparedStatement 的方式执行存储函数。
     */
    private Result executeUsePreparedStatement() throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(this.func.getCallableSql())) {
            this.pstmt = pstmt;

            // [2] 设置函数的参数: 入参、出参、入出参。
            setAndRegisterParameters();

            // [3] 执行函数。
            pstmt.execute();

            // [4] 获取函数执行的结果。
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
     * 获取函数执行的结果集。
     *
     * 注意: 不同的数据库可能不一样。
     */
    protected abstract ResultSet getResultSet() throws SQLException;

    /**
     * 获取输出参数。
     */
    protected abstract void getOutParameters(Result result) throws SQLException;

    /**
     * 执行前的条件检查，例如数据库和函数是否匹配。
     */
    protected abstract void preCheck();

    /**
     * 获取函数执行的结果。
     */
    protected Result handleResult() throws SQLException {
        /*
         逻辑:
         1. 获取更新的影响行数 (即使是更新语句，也有可能返回 -1)。
         2. 获取函数执行的输出参数。
         3. 获取函数执行的结果集。
         */

        Result result = new Result();

        // [1] 获取更新的影响行数 (即使是更新语句，也有可能返回 -1)。
        if (this.cstmt != null) {
            result.setUpdateCount(cstmt.getUpdateCount());
        }

        // [2] 获取函数执行的输出参数。
        getOutParameters(result);

        // [3] 获取函数执行的结果集。
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
     * 检查 klass 是否可以赋值为 func，如果不可以则抛出异常。
     *
     * @param klass 函数类型。
     * @param func 函数对象。
     */
    public static void checkAssignable(Class<? extends Function> klass, Function func) {
        if (!klass.isAssignableFrom(func.getClass())) {
            throw new RuntimeException("要执行的函数类型必须为 " + klass.getName());
        }
    }
}
