package xtuer.funcproc.function.spec;

import xtuer.funcproc.Arg;
import xtuer.funcproc.FuncProcUtils;
import xtuer.funcproc.function.Function;
import xtuer.funcproc.function.FunctionArg;

import java.util.stream.Collectors;

/**
 * Postgres 使用的函数类，函数的类型有近 20 种类型，支持 SQL 和 PL/pgSQL 模式。
 *
 * 官方文档: https://www.postgresql.org/docs/current/xfunc-sql.html
 */
public class PostgresFunction extends Function {
    /**
     * Ref cursor 的数据类型名。
     */
    public static final String REF_CURSOR_NAME = "refcursor";

    @Override
    public Function build() {
        super.build();

        // 判断是否返回游标 cursor。
        super.cursorReturned = false;
        for (FunctionArg arg : super.returnArgs) {
            if (REF_CURSOR_NAME.equals(arg.getDataTypeName())) {
                super.cursorReturned = true;
                break;
            }
        }

        // 有输出参数时不能使用 RETURN。
        for (FunctionArg arg : super.inOutInoutArgs) {
            if (arg.getArgTypeValue() == FunctionArg.ARG_TYPE_VALUE_INOUT || arg.getArgTypeValue() == FunctionArg.ARG_TYPE_VALUE_OUT) {
                super.returnArgs.clear();
                break;
            }
        }

        // 有下划线开头的类型参数时不支持: 数组、可变参数。
        for (FunctionArg arg : super.inOutInoutArgs) {
            String typeName = arg.getDataTypeName();

            // 类型以 _ 开头的是数组，以 anyelement 开头的是 polymorphic。
            if (typeName.startsWith("_") || typeName.startsWith("any")) {
                super.supported = false;
                break;
            }
        }
        for (FunctionArg arg : super.returnArgs) {
            String typeName = arg.getDataTypeName();

            if (typeName.startsWith("_") || typeName.startsWith("any") || "trigger".equals(typeName)) {
                super.supported = false;
                break;
            }
        }

        return this;
    }

    @Override
    public String getSignature() {
        // 函数签名需要显示输入参数、输出参数、返回值。
        // func_name() return (varchar name)
        // func_name(IN id int, OUT count int) return (void)
        // func_name(IN id int, IN count int) return (id int, name varchar)

        String inoutArgsString = super.inOutInoutArgs.stream().map(Arg::getSignature).collect(Collectors.joining(", "));
        String returnArgsString = super.returnArgs.stream().map(Arg::getSignature).collect(Collectors.joining(", "));

        if ("".equals(returnArgsString) || "returnValue void".equals(returnArgsString)) {
            returnArgsString = "void";
        }

        return String.format("%s(%s) return (%s)", super.name, inoutArgsString, returnArgsString);
    }

    @Override
    public String getCallableSql() {
        // 函数调用的 SQL 只和输入参数有关，输出参数在结果集中获取。
        // 普通: { call func_name(?, ?, ?) }
        // 游标: { ? = call func_name(?) }

        String questionMarks = FuncProcUtils.generateCallableSqlParameterQuestionMarks(super.inArgs.size());

        if (super.cursorReturned) {
            return String.format("{ ? = call %s(%s) }", super.name, String.join(", ", questionMarks));
        } else {
            return String.format("{ call %s(%s) }", super.name, String.join(", ", questionMarks));
        }
    }
}
