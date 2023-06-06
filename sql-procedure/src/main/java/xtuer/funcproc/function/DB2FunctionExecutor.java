package xtuer.funcproc.function;

import lombok.extern.slf4j.Slf4j;
import xtuer.funcproc.Result;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * DB2 的函数执行器。
 * - 参数有 IN, INOUT, OUT
 * - 返回值有普通类型，Row，Table
 */
@Slf4j
public class DB2FunctionExecutor extends FunctionExecutor {
    private PreparedStatement pstmt;

    @Override
    public Result execute(Connection conn, Function func, Object ...funcArguments) throws SQLException {
        // 提示: 由于 DB2 的没有按照 JDBC 标准实现，所以怎么针对其进行了特殊处理。
        super.conn = conn;
        super.func = func;
        super.funcArguments = Arrays.asList(funcArguments);

        // 设置 Catalog, schema, transaction (Postgres 返回游标需要关闭自动提交)。
        super.conn.setCatalog(this.func.getCatalog());
        super.conn.setSchema(this.func.getSchema());
        super.conn.setAutoCommit(false);

        // 执行前的条件检查，例如数据库和函数是否匹配。
        this.preCheck();

        log.info("执行函数: SQL [{}]，参数 {}", super.func.getCallableSql(), super.funcArguments);
        try (PreparedStatement pstmt = conn.prepareStatement(super.func.getCallableSql())) {
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

    @Override
    protected void setAndRegisterParameters() throws SQLException {
        // 设置 IN 的入参。
        int index = 0;
        for (FunctionArg arg : super.func.getInArgs()) {
            index++;
            Object value = super.funcArguments.get(index - 1);
            log.debug("输入参数: 下标 [{}], 参数 [{}]", index, value);
            this.pstmt.setObject(index, value);
        }
    }

    @Override
    protected ResultSet getResultSet() throws SQLException {
        return this.pstmt.getResultSet();
    }

    @Override
    protected void getOutParameters(Result result) throws SQLException {

    }

    @Override
    protected void preCheck() {
        FunctionExecutor.checkAssignable(DB2Function.class, super.func);
    }
}
