package xtuer.procfunc.function;

import lombok.extern.slf4j.Slf4j;
import xtuer.procfunc.Result;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * PostgreSQL 的函数执行器。
 */
@Slf4j
public class PostgresFunctionExecutor extends FunctionExecutor {
    @Override
    protected void setAndRegisterParameters() throws SQLException {
        PostgresFunction pgFunc = (PostgresFunction) super.func;
        int index = 0;

        // 注册游标参数。
        if (pgFunc.isRefCursorReturned()) {
            log.debug("注册游标 Types.REF_CURSOR");
            index = 1;
            super.cstmt.registerOutParameter(index, Types.REF_CURSOR);
        }

        // 设置输入参数。只设置 IN, INOUT 入参，OUT 出参 OUT 不需要设置。
        for (FunctionArg arg : pgFunc.getInArgs()) {
            index++;
            log.debug("输入参数: 下标 [{}], 参数 [{}]", index, super.funcArguments.get(index - 1));
            super.cstmt.setObject(index, super.funcArguments.get(index - 1));
        }
    }

    @Override
    protected void getOutParameters(Result result) throws SQLException {
        // Postgres 不直接从 OUT 参数里获取结果，OUT 参数的结果作为返回值处理。
    }

    @Override
    protected ResultSet getResultSet() throws SQLException {
        PostgresFunction pgFunc = (PostgresFunction) super.func;

        if (pgFunc.isRefCursorReturned()) {
            // 返回游标的时候需要使用 stmt.getObject(1) 获取结果集。
            return (ResultSet) super.cstmt.getObject(1);
        } else {
            return super.cstmt.getResultSet();
        }
    }

    @Override
    protected void preCheck() {
        FunctionExecutor.checkAssignable(PostgresFunction.class, super.func);
    }
}
