package xtuer.sp.procedure.spec;

import xtuer.sp.Arg;
import xtuer.sp.FuncProcUtils;
import xtuer.sp.procedure.Procedure;

import java.util.stream.Collectors;

/**
 * SqlServer 的存储过程。
 */
public class SqlServerProcedure extends Procedure {
    @Override
    public String getSignature() {
        // 无参数: proc_name()
        // 有参数: proc_name(IN x INT, IN y INT, OUT sum INT)
        String inOutInoutArgsString = super.inOutInoutArgs.stream().map(Arg::getSignature).collect(Collectors.joining(", "));

        return String.format("%s(%s)", super.name, inOutInoutArgsString);
    }

    @Override
    public String getCallableSql() {
        // 无参数: { call schema.proc_name() }
        // 有参数: { call schema.proc_name(?, ?) }
        String questionMarks = FuncProcUtils.generateCallableSqlParameterQuestionMarks(super.inOutInoutArgs.size());

        return String.format("{ call %s.%s(%s) }", super.schema, super.name, questionMarks);
    }
}
