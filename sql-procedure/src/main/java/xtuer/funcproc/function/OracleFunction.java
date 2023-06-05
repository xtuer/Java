package xtuer.funcproc.function;

import lombok.Getter;
import xtuer.funcproc.Arg;

import java.util.stream.Collectors;

/**
 * Oracle 的存储函数。
 *
 * 注意: 函数名大小写敏感。
 */
public class OracleFunction extends Function {
    /**
     * Ref cursor 的数据类型名。
     */
    public static final String REF_CURSOR_NAME = "REF CURSOR";

    /**
     * 注册游标的输出类型，等于 OracleTypes.CURSOR。
     */
    public static final int ORACLE_TYPES_CURSOR = -10;

    /**
     * 是否返回游标。
     */
    @Getter
    private boolean refCursorReturned;

    @Override
    public Function build() {
        super.build();

        // 判断是否返回游标 cursor。
        this.refCursorReturned = false;
        for (FunctionArg arg : super.returnArgs) {
            if (REF_CURSOR_NAME.equals(arg.getDataTypeName())) {
                this.refCursorReturned = true;
                break;
            }
        }

        return this;
    }

    @Override
    public String getSignature() {
        // func_name(IN id int, IN count int) return int

        String inArgsString = super.inArgs.stream().map(Arg::getSignature).collect(Collectors.joining(", "));
        String returnArgsString = super.returnArgs.get(0).getDataTypeName();

        if (this.refCursorReturned) {
            returnArgsString = "SYS_REFCURSOR";
        }

        return String.format("%s(%s) return %s", super.name, inArgsString, returnArgsString);
    }

    @Override
    public String getCallableSql() {
        // { ? = call func_name(?, ?, ?) }
        return String.format("{ ? = call %s(%s) }", super.name, Function.generateCallableSqlParameterQuestionMarks(super.inArgs.size()));
    }
}
