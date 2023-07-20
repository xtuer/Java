package xtuer.sp.function.spec;

import lombok.extern.slf4j.Slf4j;
import xtuer.sp.Result;
import xtuer.sp.function.FunctionArg;
import xtuer.sp.function.FunctionExecutor;

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
        int index = 0;
        int delta = 1;

        // 注册游标参数。
        if (super.func.isCursorReturned()) {
            log.debug("输出参数: 下标 [1], 类型为游标 Types.REF_CURSOR，类型值 [{}]", Types.REF_CURSOR);
            index = 1;
            delta = 2;
            super.cstmt.registerOutParameter(index, Types.REF_CURSOR);
        }

        // 设置输入参数。只设置 IN, INOUT 入参，OUT 出参不需要设置。
        for (FunctionArg arg : super.func.getInArgs()) {
            index++;
            log.debug("输入参数: 下标 [{}], 参数 [{}]", index, super.funcArguments.get(index - delta));
            super.cstmt.setObject(index, super.funcArguments.get(index - delta), arg.getDataTypeValue());
        }
    }

    @Override
    protected void getOutParameters(Result result) throws SQLException {
        // Postgres 不直接从 OUT 参数里获取结果，OUT 参数的结果作为返回值处理。
    }

    @Override
    protected ResultSet getResultSet() throws SQLException {
        if (super.func.isCursorReturned()) {
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
