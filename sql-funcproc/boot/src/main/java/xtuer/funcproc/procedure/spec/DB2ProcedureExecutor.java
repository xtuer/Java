package xtuer.funcproc.procedure.spec;

import lombok.extern.slf4j.Slf4j;
import xtuer.funcproc.procedure.ProcedureExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DB2 存储过程执行器。
 */
@Slf4j
public class DB2ProcedureExecutor extends SimpleProcedureExecutor {
    @Override
    protected ResultSet getResultSet() throws SQLException {
        // Note: DB2 的存储过程理论上可以支持多个结果集，我们只处理了一个，否则需要改 Result 类的结构。
        return super.cstmt.getResultSet();
    }

    @Override
    protected void preCheck() {
        ProcedureExecutor.checkAssignable(DB2Procedure.class, super.proc);
    }
}
