package xtuer.procfunc.function;

import xtuer.procfunc.Result;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OracleFunctionExecutor extends FunctionExecutor {
    @Override
    protected void setAndRegisterParameters() throws SQLException {

    }

    @Override
    protected ResultSet getResultSet() throws SQLException {
        // 需要处理游标。
        return null;
    }

    @Override
    protected void getOutParameters(Result result) throws SQLException {
        // 需要处理游标。
    }

    @Override
    protected void preCheck() {
        FunctionExecutor.checkAssignable(OracleFunction.class, super.func);
    }
}
