package xtuer.procfunc.function;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 函数。
 */
@Data
public class Function {
    /**
     * 函数所属 catalog (database)。
     */
    private String catalog;
    /**
     * 函数所属 schema。
     */
    private String schema;
    /**
     * 函数的名字。
     */
    private String name;

    /**
     * 是否支持。
     */
    private boolean supported = true;

    /**
     * 函数的参数列表，参数顺序不能乱。
     */
    private List<FunctionArg> args = new ArrayList<>();

    /**
     * 原始获取到的参数。
     */
    private List<FunctionArg> originalArgs = new ArrayList<>();

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
