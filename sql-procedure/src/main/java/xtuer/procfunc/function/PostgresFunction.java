package xtuer.procfunc.function;

import xtuer.procfunc.Arg;

import java.util.LinkedList;
import java.util.List;
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

    /**
     * 是否返回游标。
     */
    private boolean refCursorReturned;

    /**
     * 输入参数个数。
     */
    protected int inArgsCount = 0;

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

        // 有输出参数时不能使用 RETURN。
        for (FunctionArg arg : super.inoutArgs) {
            if (arg.getArgTypeValue() == FunctionArg.ARG_TYPE_VALUE_INOUT || arg.getArgTypeValue() == FunctionArg.ARG_TYPE_VALUE_OUT) {
                super.returnArgs.clear();
                break;
            }
        }

        // 计算输入参数的个数。
        this.inArgsCount = 0;
        for (FunctionArg arg : super.inoutArgs) {
            if (arg.getArgTypeValue() == FunctionArg.ARG_TYPE_VALUE_IN || arg.getArgTypeValue() == FunctionArg.ARG_TYPE_VALUE_INOUT) {
                this.inArgsCount++;
            }
        }

        return this;
    }

    @Override
    public String getSignature() {
        // 函数签名需要显示输入参数、输出参数、返回值。
        // func_name() return (void)
        // func_name(IN id int, OUT count int) return (id int)
        String inoutArgsString = super.inoutArgs.stream().map(Arg::getSignature).collect(Collectors.joining(", "));
        String returnArgsString = super.returnArgs.stream().map(Arg::getSignature).collect(Collectors.joining(", "));

        if ("".equals(returnArgsString)) {
            returnArgsString = "void";
        }

        return String.format("%s(%s) return (%s)", super.name, inoutArgsString, returnArgsString);
    }

    @Override
    public String getCallableSql() {
        // 函数调用的 SQL 只和输入参数有关，输出参数在结果集中获取。
        // 普通: { call func_name(?, ?, ?) }
        // 游标: { ? = call func_name(?) }

        // 问号 ? 的数量为输入参数的个数。
        List<String> questionMarks = new LinkedList<>();
        for (int i = 0; i < inArgsCount; i++) {
            questionMarks.add("?");
        }

        if (this.refCursorReturned) {
            return String.format("{ ? = call %s(%s) }", super.name, String.join(", ", questionMarks));
        } else {
            return String.format("{ call %s(%s) }", super.name, String.join(", ", questionMarks));
        }
    }

    /**
     * 获取是否返回游标。
     *
     * @return 返回游标返回 true，否则返回 false。
     */
    public boolean isRefCursorReturned() {
        return refCursorReturned;
    }
}
