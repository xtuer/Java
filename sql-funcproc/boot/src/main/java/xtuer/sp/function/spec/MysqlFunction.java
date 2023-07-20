package xtuer.sp.function.spec;

import lombok.Getter;
import xtuer.sp.Arg;
import xtuer.sp.FuncProcUtils;
import xtuer.sp.function.Function;

import java.util.stream.Collectors;

/**
 * MySQL 函数，只有输入参数、一个返回值，不支持输出参数。
 */
@Getter
public class MysqlFunction extends Function {
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
        return String.format("{ ? = call %s(%s) }", super.name, FuncProcUtils.generateCallableSqlParameterQuestionMarks(super.inArgs.size()));
    }
}
