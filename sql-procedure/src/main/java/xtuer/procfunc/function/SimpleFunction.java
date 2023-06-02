package xtuer.procfunc.function;

import xtuer.procfunc.Arg;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 简单函数，只有输入参数、一个返回值，不支持输出参数。
 */
public class SimpleFunction extends Function {
    /**
     * 输入参数。
     */
    private final List<FunctionArg> inArgs = new LinkedList<>();

    @Override
    public Function build() {
        super.build();

        // 输入参数，简单函数的 inoutArgs 只会有输入参数。
        this.inArgs.clear();
        this.inArgs.addAll(super.inoutArgs);

        return this;
    }

    @Override
    public String getSignature() {
        // func_name(IN id int, IN count int) return int

        String inArgsString = this.inArgs.stream().map(Arg::getSignature).collect(Collectors.joining(", "));
        String returnArgsString = super.returnArgs.get(0).getDataTypeName();

        return String.format("%s(%s) return %s", super.name, inArgsString, returnArgsString);
    }

    @Override
    public String getCallableSql() {
        // { ? = call func_name(?, ?, ?) }
        return String.format("{ ? = call %s(%s) }", super.name, Function.generateCallableSqlParameterQuestionMarks(this.inArgs.size()));
    }
}
