package xtuer.funcproc.function;

import lombok.Getter;
import xtuer.funcproc.Arg;

import java.sql.Types;
import java.util.stream.Collectors;

/**
 * Oracle 的存储函数。
 *
 * 注意:
 * - 函数名大小写敏感。
 * - 支持返回简单类型、游标。
 * - 不支持返回复合类型 STRUCT。
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
     * 是否返回 STRUCT 的复合类型。
     */
    @Getter
    private boolean structReturned = false;

    @Override
    public Function build() {
        super.build();

        // 正确的函数一定会有一个且唯一一个返回参数。
        FunctionArg returnArg = super.getReturnArgs().get(0);

        // 判断是否返回游标 cursor。
        super.cursorReturned = REF_CURSOR_NAME.equals(returnArg.getDataTypeName());

        // 返回复合类型: Types.STRUCT，不支持。
        this.structReturned = Types.STRUCT == returnArg.getDataTypeValue();
        super.supported = false;

        return this;
    }

    @Override
    public String getSignature() {
        // func_name(IN id int, IN count int) return int

        String inArgsString = super.inArgs.stream().map(Arg::getSignature).collect(Collectors.joining(", "));
        String returnArgsString = super.returnArgs.get(0).getDataTypeName();

        if (super.cursorReturned) {
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
