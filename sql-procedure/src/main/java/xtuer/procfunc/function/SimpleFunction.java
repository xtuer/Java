package xtuer.procfunc.function;

import xtuer.procfunc.Arg;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 简单函数，只有输入参数、一个返回值，不支持输出参数。
 */
public class SimpleFunction extends Function {
    public String getSignature() {
        // func_name(IN id int, IN count int) return int
        List<FunctionArg> inArgs = super.inoutArgs;
        String inArgsString = inArgs.stream().map(Arg::getSignature).collect(Collectors.joining(", "));
        String returnArgsString = super.returnArgs.get(0).getDataTypeName();

        return String.format("%s(%s) return %s", super.name, inArgsString, returnArgsString);
    }

    public String getCallableSql() {
        // { ? = call func_name(?, ?, ?) }

        // 问号 ? 的数量为输入参数的个数。
        List<FunctionArg> inArgs = super.inoutArgs;
        List<String> questionMarks = new LinkedList<>();
        for (int i = 0; i < inArgs.size(); i++) {
            questionMarks.add("?");
        }

        return String.format("{ ? = call %s(%s) }", super.name, String.join(", ", questionMarks));
    }
}
