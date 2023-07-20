package xtuer.funcproc.procedure.spec;

import xtuer.funcproc.Arg;
import xtuer.funcproc.FuncProcUtils;
import xtuer.funcproc.procedure.Procedure;
import xtuer.funcproc.procedure.ProcedureArg;

import java.util.stream.Collectors;

/**
 * Oracle 的存储过程。
 */
public class OracleProcedure extends Procedure {
    /**
     * Oracle 游标的数据类型名。
     */
    public static final String ARG_DATA_TYPE_NAME_OF_ORACLE_CURSOR = "REF CURSOR";

    /**
     * Oracle 游标的数据类型值: OracleTypes.CURSOR = -10
     * 提示: 在这里写死而不是引用 Oracle 的类中的值是为了先不依赖 Oracle 驱动。
     */
    public static final int ARG_DATA_TYPE_VALUE_OF_ORACLE_CURSOR = -10;

    @Override
    public Procedure build() {
        super.build();

        // 判断是否使用了游标的 OUT 参数。
        for (ProcedureArg arg : super.inOutInoutArgs) {
            if (arg.isOutArg() && ARG_DATA_TYPE_NAME_OF_ORACLE_CURSOR.equals(arg.getDataTypeName())) {
                super.cursorOuted = true;
                break;
            }
        }

        return this;
    }

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
