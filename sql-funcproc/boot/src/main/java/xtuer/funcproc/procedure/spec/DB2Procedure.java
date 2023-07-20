package xtuer.funcproc.procedure.spec;

import xtuer.funcproc.Arg;
import xtuer.funcproc.FuncProcUtils;
import xtuer.funcproc.procedure.Procedure;

import java.util.stream.Collectors;

/**
 * DB2 的存储过程。
 */
public class DB2Procedure extends Procedure {
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

        return String.format("call %s.%s(%s)", super.schema, super.name, questionMarks);
    }
}
