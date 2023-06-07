package xtuer.funcproc.function;

import lombok.extern.slf4j.Slf4j;
import xtuer.funcproc.Result;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DB2 的函数执行器。
 * - 参数有 IN, INOUT, OUT
 * - 返回类型: 普通类型，Row，Table
 */
@Slf4j
public class DB2FunctionExecutor extends FunctionExecutor {
    @Override
    public Result execute(Connection conn, Function func, Object ...funcArguments) throws SQLException {
        super.useCallableStatement = false;
        return super.execute(conn, func, funcArguments);
    }

    @Override
    protected void setAndRegisterParameters() throws SQLException {
        // 设置 IN 的入参。
        int index = 0;
        for (FunctionArg arg : super.func.getInArgs()) {
            index++;
            Object value = super.funcArguments.get(index - 1);
            log.debug("输入参数: 下标 [{}], 参数 [{}]", index, value);
            super.pstmt.setObject(index, value);
        }
    }

    @Override
    protected ResultSet getResultSet() throws SQLException {
        return super.pstmt.getResultSet();
    }

    @Override
    protected void getOutParameters(Result result) throws SQLException {

    }

    @Override
    protected void preCheck() {
        FunctionExecutor.checkAssignable(DB2Function.class, super.func);
    }
}
