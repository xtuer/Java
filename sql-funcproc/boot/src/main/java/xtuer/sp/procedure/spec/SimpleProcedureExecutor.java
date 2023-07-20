package xtuer.funcproc.procedure.spec;

import lombok.extern.slf4j.Slf4j;
import xtuer.funcproc.Result;
import xtuer.funcproc.procedure.ProcedureArg;
import xtuer.funcproc.procedure.ProcedureExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 简单的存储过程执行器: MySQL, PostgreSQL, SqlServer 的存储过程执行都一样，DB2 和 Oracle 的有不一样的地方。
 */
@Slf4j
public abstract class SimpleProcedureExecutor extends ProcedureExecutor {
    @Override
    protected void setAndRegisterParameters() throws SQLException {
        /*
         提示:
         A. 参数类型有 IN，INOUT，OUT。
         B. 可能有多个 OUT 参数，且 OUT 参数有可能在 IN 参数的前面。
         C. 同一个参数即可以是 IN 参数，也可以是 OUT 参数，即是 INOUT 参数。
         */

        // 输入参数的值的下标。
        int argValueIdx = 0;

        for (int argIdx = 1; argIdx <= super.proc.getInOutInoutArgs().size(); argIdx++) {
            ProcedureArg arg = super.proc.getInOutInoutArgs().get(argIdx - 1);

            // 注册 IN 参数。
            if (arg.isInArg()) {
                Object argValue = super.procArguments.get(argValueIdx++);
                log.debug("输入参数: 下标 [{}], 参数 [{}]", argIdx, argValue);
                super.cstmt.setObject(argIdx, argValue, arg.getDataTypeValue());
            }

            // 注册 OUT 参数。
            if (arg.isOutArg()) {
                log.debug("输出参数: 下标 [{}]", argIdx);
                super.cstmt.registerOutParameter(argIdx, arg.getDataTypeValue());
            }
        }
    }

    @Override
    protected ResultSet getResultSet() throws SQLException {
        return super.cstmt.getResultSet();
    }

    @Override
    protected void getOutParameters(Result result) throws SQLException {
        for (int argIdx = 1; argIdx <= super.proc.getInOutInoutArgs().size(); argIdx++) {
            ProcedureArg arg = super.proc.getInOutInoutArgs().get(argIdx - 1);

            if (arg.isOutArg()) {
                result.getOutResult().put(arg.getName(), super.cstmt.getObject(argIdx));
            }
        }
    }
}
