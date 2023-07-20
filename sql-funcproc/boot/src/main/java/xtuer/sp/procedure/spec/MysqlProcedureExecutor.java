package xtuer.sp.procedure.spec;

import lombok.extern.slf4j.Slf4j;
import xtuer.sp.procedure.ProcedureExecutor;

/**
 * MySQL 的存储过程执行器。
 */
@Slf4j
public class MysqlProcedureExecutor extends SimpleProcedureExecutor {
    @Override
    protected void preCheck() {
        ProcedureExecutor.checkAssignable(MysqlProcedure.class, super.proc);
    }
}
