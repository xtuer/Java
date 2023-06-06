package xtuer.funcproc.function;

import lombok.Getter;
import xtuer.funcproc.Arg;

import java.util.stream.Collectors;

/**
 * 简单函数，只有输入参数、一个返回值，不支持输出参数。
 */
@Getter
public class SimpleFunction extends Function {
    @Override
    public String getSignature() {
        // func_name(IN id int, IN count int) return int

        // 正确的函数一定会有一个且唯一一个返回参数。
        String inArgsString = super.inArgs.stream().map(Arg::getSignature).collect(Collectors.joining(", "));
        String returnArgsString = super.returnArgs.get(0).getDataTypeName();

        return String.format("%s(%s) return %s", super.name, inArgsString, returnArgsString);
    }

    @Override
    public String getCallableSql() {
        // { ? = call func_name(?, ?, ?) }
        return String.format("{ ? = call %s(%s) }", super.name, Function.generateCallableSqlParameterQuestionMarks(super.inArgs.size()));
    }
}
