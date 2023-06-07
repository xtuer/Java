package xtuer.funcproc.function;

import lombok.extern.slf4j.Slf4j;
import xtuer.funcproc.Result;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * SQL Server 函数执行器。
 * - 参数只有 IN。
 * - 返回类型: 简单类型、TABLE。
 */
@Slf4j
public class SqlServerFunctionExecutor extends FunctionExecutor {
    private SqlServerFunction ssFunc;

    @Override
    public Result execute(Connection conn, Function func, Object ...funcArguments) throws SQLException {
        this.ssFunc = (SqlServerFunction) func;
        super.useCallableStatement = !ssFunc.isUsePreparedStatement();

        return super.execute(conn, func, funcArguments);
    }

    @Override
    protected void setAndRegisterParameters() throws SQLException {
        int index = 0;
        int delta = 1;

        if (!this.ssFunc.isUsePreparedStatement()) {
            // [1] 注册 OUT 参数获取结果。
            index = 1;
            delta = 2;
            FunctionArg returnArg =  super.func.getReturnArg();
            log.debug("输出参数: 下标 [1], 类型名 [{}], 类型值 [{}]", returnArg.getDataTypeName(), returnArg.getDataTypeValue());
            super.cstmt.registerOutParameter(1, returnArg.getDataTypeValue()); // 返回类型不可以使用 Types.OTHER。
        }

        // [2] 设置 IN 的入参。
        PreparedStatement pstmt = this.ssFunc.isUsePreparedStatement() ? super.pstmt : super.cstmt;
        for (FunctionArg arg : super.func.getInArgs()) {
            index++;
            Object value = super.funcArguments.get(index - delta);
            log.debug("输入参数: 下标 [{}], 参数 [{}]", index, value);
            pstmt.setObject(index, value);
        }
    }

    @Override
    protected ResultSet getResultSet() throws SQLException {
        if (this.ssFunc.isUsePreparedStatement()) {
            return super.pstmt.getResultSet();
        } else {
            return super.cstmt.getResultSet();
        }
    }

    @Override
    protected void getOutParameters(Result result) throws SQLException {
        if (!this.ssFunc.isUsePreparedStatement()) {
            Object ret = super.cstmt.getObject(1); // 获取函数的返回值。
            result.getOutResult().put("returnValue", ret);
        }
    }

    @Override
    protected void preCheck() {
        FunctionExecutor.checkAssignable(SqlServerFunction.class, super.func);
    }
}
