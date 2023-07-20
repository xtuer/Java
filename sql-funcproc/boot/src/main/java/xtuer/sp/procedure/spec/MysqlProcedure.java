package xtuer.sp.procedure.spec;

import xtuer.sp.Arg;
import xtuer.sp.FuncProcUtils;
import xtuer.sp.procedure.Procedure;

import java.util.stream.Collectors;

public class MysqlProcedure extends Procedure {
    @Override
    public String getSignature() {
        // 无参数: proc_name()
        // 有参数: proc_name(IN x INT, IN y INT, OUT sum INT)
        String inOutInoutArgsString = super.inOutInoutArgs.stream().map(Arg::getSignature).collect(Collectors.joining(", "));

        return String.format("%s(%s)", super.name, inOutInoutArgsString);
    }

    @Override
    public String getCallableSql() {
        // 无参数: { call proc_name() }
        // 有参数: { call proc_name(?, ?) }
        String questionMarks = FuncProcUtils.generateCallableSqlParameterQuestionMarks(super.inOutInoutArgs.size());

        return String.format("{ call %s(%s) }", super.name, questionMarks);
    }
}
