package xtuer.funcproc.procedure.spec;

import lombok.extern.slf4j.Slf4j;
import xtuer.funcproc.procedure.ProcedureExecutor;

/**
 * SqlServer 存储过程执行器。
 */
@Slf4j
public class SqlServerProcedureExecutor extends SimpleProcedureExecutor {
    @Override
    protected void preCheck() {
        ProcedureExecutor.checkAssignable(SqlServerProcedure.class, super.proc);
    }
}