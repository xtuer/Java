package xtuer.funcproc.function;

import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 函数。
 */
@Data
public class Function {
    /**
     * 函数所属 catalog (database)。
     */
    protected String catalog;

    /**
     * 函数所属 schema。
     */
    protected String schema;

    /**
     * 函数的名字。
     */
    protected String name;

    /**
     * 是否支持。
     */
    protected boolean supported = true;

    /**
     * 原始获取到的参数。
     */
    protected List<FunctionArg> originalArgs = new ArrayList<>();

    /**
     * 输入参数，包括 IN, INOUT 参数。
     */
    protected List<FunctionArg> inArgs = new LinkedList<>();

    /**
     * 输入输出参数，包括 IN, OUT, INOUT 参数。
     */
    protected List<FunctionArg> inOutInoutArgs = new LinkedList<>();

    /**
     * 返回参数。
     */
    protected List<FunctionArg> returnArgs = new LinkedList<>();

    public Function() {}

    /**
     * 创建存储函数对象。
     *
     * @param catalog      函数所属 catalog (database)。
     * @param schema       函数所属 schema。
     * @param functionName 函数的名称。
     */
    public Function(String catalog, String schema, String functionName) {
        this.catalog = catalog;
        this.schema  = schema;
        this.name    = functionName;
    }

    /**
     * 使用传入的函数对象创建一个指定目标类型的函数对象。
     *
     * @param src 源函数对象。
     * @param dstFunctionClass 目标函数的类。
     * @return 返回目标函数的对象。
     * @param <T> 目标函数类型，例如 PostgresFunction
     * @throws RuntimeException 使用反射创建对象出错时抛出异常。
     */
    public static <T> T newFunction(Function src, Class<T> dstFunctionClass) {
        if (!Function.class.isAssignableFrom(dstFunctionClass.getSuperclass())) {
            throw new RuntimeException("类型 dstFunctionClass 必须是 Function 的子类");
        }

        try {
            T dst = dstFunctionClass.getConstructor().newInstance();
            BeanUtils.copyProperties(src, dst);

            // 构建函数。
            ((Function) dst).build();

            return dst;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 添加参数。
     *
     * @param arg 函数的参数。
     */
    public void addArg(FunctionArg arg) {
        this.originalArgs.add(arg);
    }

    /**
     * 构建，把原始参数处理成可用的。
     */
    public Function build() {
        // 把返回参数和输入输出参数分别提取出来。
        returnArgs.clear();
        inOutInoutArgs.clear();
        inArgs.clear();

        for (FunctionArg arg : originalArgs) {
            final int argTypeValue = arg.getArgTypeValue();

            // 输入参数。
            if (argTypeValue == FunctionArg.ARG_TYPE_VALUE_IN || argTypeValue == FunctionArg.ARG_TYPE_VALUE_INOUT) {
                inArgs.add(arg);
            }

            // 输入输出参数。
            if (argTypeValue == FunctionArg.ARG_TYPE_VALUE_IN
                    || argTypeValue == FunctionArg.ARG_TYPE_VALUE_INOUT
                    || argTypeValue == FunctionArg.ARG_TYPE_VALUE_OUT) {
                inOutInoutArgs.add(arg);
            }

            // 返回参数。
            if (argTypeValue == FunctionArg.ARG_TYPE_VALUE_RETURN || argTypeValue == FunctionArg.ARG_TYPE_VALUE_RETURN_POST) {
                returnArgs.add(arg);
            }
        }

        return this;
    }

    /**
     * 获取函数的签名，例如
     * 有返回值: func_sum(IN num1 int4, IN num2 int4, sum int4) RETURNS int4
     * 无返回值: func_sum(IN num1 int4, IN num2 int4, OUT sum int4)
     *
     * @return 返回函数的签名。
     */
    public String getSignature() {
        return null; // 推迟到子类实现。
    }

    /**
     * 获取 JDBC 执行函数的语句，例如 {? = call func_sum(?, ?, ?)}
     *
     * @return 返回 JDBC call 的 SQL 语句。
     */
    public String getCallableSql() {
        return null; // 推迟到子类实现。
    }

    /**
     * 生成 Callable SQL 语句 {? = call func_sum(?, ?, ?)} 的参数部分的问号占位符。
     * 例如 paramCount 为 3 则生成 "?, ?, ?"。
     *
     * @param paramCount 参数个数。
     * @return 返回参数占位符字符串。
     */
    public static String generateCallableSqlParameterQuestionMarks(int paramCount) {
        List<String> qms = new ArrayList<>(paramCount);
        for (int i = 0; i < paramCount; i++) {
            qms.add("?");
        }

        return String.join(", ", qms);
    }
}
