package xtuer.funcproc.function;

import lombok.extern.slf4j.Slf4j;
import xtuer.funcproc.Result;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Oracle 的函数执行器。
 *
 * 官方文档: https://docs.oracle.com/en/database/oracle/oracle-database/12.2/lnpls/CREATE-FUNCTION-statement.html
 */
@Slf4j
public class OracleFunctionExecutor extends FunctionExecutor {
    @Override
    protected void setAndRegisterParameters() throws SQLException {
        /*
         逻辑: 只有 IN 输入参数和单个返回值，没有 OUT 参数，调用语句为 "{ ? = call fun_dateToStr(?) }":
         1. 注册 OUT 参数或者游标获取结果。
         2. 设置 IN 的入参。
         */

        int index = 1;

        // [1] 注册 OUT 参数或者游标获取结果。
        if (super.func.isCursorReturned()) {
            log.debug("输出参数: 下标 [1], 类型为游标 OracleTypes.CURSOR，类型值 [{}]", OracleFunction.ORACLE_TYPES_CURSOR);
            super.cstmt.registerOutParameter(1, OracleFunction.ORACLE_TYPES_CURSOR);
        } else {
            FunctionArg returnArg = super.func.onlyOneReturnArg();
            log.debug("输出参数: 下标 [1], 类型名 [{}], 类型值 [{}]", returnArg.getDataTypeName(), returnArg.getDataTypeValue());
            super.cstmt.registerOutParameter(1, returnArg.getDataTypeValue()); // 返回类型不能使用 Types.OTHER。
        }

        // [2] 设置 IN 的入参。
        for (FunctionArg arg : super.func.getInArgs()) {
            index++;
            Object value = super.funcArguments.get(index - 2);
            log.debug("输入参数: 下标 [{}], 参数 [{}]", index, value);
            super.cstmt.setObject(index, value);
        }
    }

    @Override
    protected void getOutParameters(Result result) throws SQLException {
        if (!super.func.isCursorReturned()) {
            Object ret = super.cstmt.getObject(1); // 获取函数的返回值。
            result.getOutResult().put("returnValue", ret);
        }
        // 游标使用结果集获取, 在 getResultSet() 中处理。
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
        FunctionExecutor.checkAssignable(OracleFunction.class, super.func);
    }
}
