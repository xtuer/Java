package xtuer.funcproc.procedure.spec;

import lombok.extern.slf4j.Slf4j;
import xtuer.funcproc.Result;
import xtuer.funcproc.procedure.ProcedureArg;
import xtuer.funcproc.procedure.ProcedureExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * SqlServer 存储过程执行器。
 */
@Slf4j
public class SqlServerProcedureExecutor extends ProcedureExecutor {
    @Override
    protected void setAndRegisterParameters() throws SQLException {
        // 可能有多个 OUT 参数，且 OUT 参数有可能在 IN 参数的前面。
        int argValueIdx = 0;

        for (int i = 0; i < super.proc.getInOutInoutArgs().size(); i++) {
            int argIdx = i + 1;
            ProcedureArg arg = super.proc.getInOutInoutArgs().get(i);

            // 注册 IN 参数。
            if (arg.isInArg()) {
                Object argValue = super.procArguments.get(argValueIdx++);
                super.cstmt.setObject(argIdx, argValue, arg.getDataTypeValue());
                log.debug("输入参数: 下标 [{}], 参数 [{}]", argIdx, argValue);
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
        // 可能有多个 OUT 参数，且 OUT 参数有可能在 IN 参数的前面。
        for (int i = 0; i < super.proc.getInOutInoutArgs().size(); i++) {
            ProcedureArg arg = super.proc.getInOutInoutArgs().get(i);

            if (arg.isOutArg()) {
                result.getOutResult().put(arg.getName(), super.cstmt.getObject(i + 1));
            }
        }
    }

    @Override
    protected void preCheck() {
        ProcedureExecutor.checkAssignable(SqlServerProcedure.class, super.proc);
    }
}
