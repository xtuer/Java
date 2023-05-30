package xtuer.procfunc.function;

import xtuer.procfunc.Arg;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Postgres 使用的函数类。
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

    @Override
    public Function build() {
        super.build();

        // 判断是否返回游标 cursor。
        for (FunctionArg arg : returnArgs) {
            if (REF_CURSOR_NAME.equals(arg.getDataTypeName())) {
                this.refCursorReturned = true;
                break;
            }
        }

        // 同时有 OUT 和 return，把 return 删掉。
        for (FunctionArg arg : inoutArgs) {
            if (arg.getArgTypeValue() == FunctionArg.ARG_TYPE_VALUE_INOUT || arg.getArgTypeValue() == FunctionArg.ARG_TYPE_VALUE_OUT) {
                returnArgs.clear();
                break;
            }
        }

        return this;
    }

    @Override
    public String getSignature() {
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
        // 普通: { call func_name(?, ?, ?) }
        // 游标: { ? = call func_name(?) }

        // 问号 ? 的数量为输入参数的个数。
        List<String> questionMarks = new LinkedList<>();
        for (int i = 0; i < super.inArgsCount; i++) {
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
