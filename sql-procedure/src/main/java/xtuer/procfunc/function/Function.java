package xtuer.procfunc.function;

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
     * 返回参数。
     */
    protected List<FunctionArg> returnArgs = new LinkedList<>();

    /**
     * 输入输出参数。
     */
    protected List<FunctionArg> inoutArgs = new LinkedList<>();

    /**
     * 输入参数个数。
     */
    protected int inArgsCount = 0;

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
     * @throws Exception 使用反射创建对象出错时抛出异常。
     */
    public static <T> T fromFunction(Function src, Class<T> dstFunctionClass) throws Exception {
        if (!Function.class.isAssignableFrom(dstFunctionClass.getSuperclass())) {
            throw new RuntimeException("类型 dstFunctionClass 必须是 Function 的子类");
        }

        T dst = dstFunctionClass.getConstructor().newInstance();
        BeanUtils.copyProperties(src, dst);
        ((Function) dst).build();

        return dst;
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
     * 构建，把原始参数处理处理成可用的。
     */
    public Function build() {
        // 把返回参数和输入输出参数分别提取出来。
        returnArgs.clear();
        inoutArgs.clear();
        inArgsCount = 0;

        for (FunctionArg arg : originalArgs) {
            final int argTypeValue = arg.getArgTypeValue();

            // 返回参数。
            if (argTypeValue == FunctionArg.ARG_TYPE_VALUE_RETURN || argTypeValue == FunctionArg.ARG_TYPE_VALUE_RETURN_POST) {
                returnArgs.add(arg);
            }

            // 输入输出参数。
            if (argTypeValue == FunctionArg.ARG_TYPE_VALUE_IN
                    || argTypeValue == FunctionArg.ARG_TYPE_VALUE_INOUT
                    || argTypeValue == FunctionArg.ARG_TYPE_VALUE_OUT) {
                inoutArgs.add(arg);
            }

            // 输入参数个数。
            if (argTypeValue == FunctionArg.ARG_TYPE_VALUE_IN || argTypeValue == FunctionArg.ARG_TYPE_VALUE_INOUT) {
                inArgsCount++;
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
        return null;
    }

    /**
     * 获取 JDBC 执行的存储过程语句，例如 {? = call func_sum(?, ?, ?)}
     *
     * @return 返回 JDBC call 的 SQL 语句。
     */
    public String getCallableSql() {
        return null;
    }
}
