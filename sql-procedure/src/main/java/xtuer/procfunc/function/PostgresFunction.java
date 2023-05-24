package xtuer.procfunc.function;

import org.springframework.beans.BeanUtils;

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

    /**
     * 传入 Function 构造一个 Postgres 特有的 Function。
     *
     * @param func 普通的 Function 对象。
     * @return 返回 Postgres 的 Function 对象。
     */
    public static PostgresFunction fromFunction(Function func) {
        PostgresFunction pgFunc = new PostgresFunction();
        BeanUtils.copyProperties(func, pgFunc);
        pgFunc.build();

        return pgFunc;
    }

    @Override
    public Function build() {
        return this;
    }

    @Override
    public String getSignature() {
        return null;
    }

    @Override
    public String getCallableSql() {
        return null;
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
