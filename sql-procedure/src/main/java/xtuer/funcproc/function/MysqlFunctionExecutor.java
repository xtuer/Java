package xtuer.funcproc.function;

import lombok.extern.slf4j.Slf4j;
import xtuer.funcproc.Result;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MySQL 函数执行器: 只有 IN 入参，单个返回值，没有 INOUT, OUT 参数。
 *
 * MySQL 官方文档: https://dev.mysql.com/doc/refman/8.0/en/create-procedure.html
 */
@Slf4j
public class MysqlFunctionExecutor extends FunctionExecutor {
    @Override
    protected void setAndRegisterParameters() throws SQLException {
        /*
         逻辑: 只有 IN 输入参数和单个返回值，没有 OUT 参数，调用语句为 "{ ? = call fun_dateToStr(?) }":
         1. 注册 OUT 参数获取结果。
         2. 设置 IN 的入参。
         */

        // [1] 注册 OUT 参数获取结果。
        FunctionArg returnArg = super.func.onlyOneReturnArg();
        log.debug("输出参数: 下标 [1], 类型名 [{}], 类型值 [{}]", returnArg.getDataTypeName(), returnArg.getDataTypeValue());
        super.cstmt.registerOutParameter(1, returnArg.getDataTypeValue()); // 返回类型可以使用 Types.OTHER。

        // [2] 设置 IN 的入参。
        int index = 1;
        for (FunctionArg arg : super.func.getInArgs()) {
            index++;
            Object value = super.funcArguments.get(index - 2);
            log.debug("输入参数: 下标 [{}], 参数 [{}]", index, value);
            super.cstmt.setObject(index, value);
        }
    }

    @Override
    protected void getOutParameters(Result result) throws SQLException {
        Object ret = super.cstmt.getObject(1); // 获取函数的返回值。
        result.getOutResult().put("returnValue", ret);
    }

    @Override
    protected ResultSet getResultSet() throws SQLException {
        return super.cstmt.getResultSet();
    }

    @Override
    protected void preCheck() {
        FunctionExecutor.checkAssignable(MysqlFunction.class, super.func);
    }
}
