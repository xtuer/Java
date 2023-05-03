package xtuer.procedure;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
     * 函数的参数列表，参数顺序不能乱。
     */
    private List<FunctionArg> args = new LinkedList<>();

    /**
     * 原始获取到的参数。
     */
    private List<FunctionArg> originalArgs = new LinkedList<>();

    /**
     * 是否包含 Return 语句。
     */
    private boolean withReturn = true;

    public Function() {}

    /**
     * 创建存储过程对象。
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
        boolean hasOutArg    = false;
        boolean prefixReturn = false;
        boolean suffixReturn = false;

        int i = 0;
        int returnCount = 0;
        for (FunctionArg arg : originalArgs) {
            // 返回值个数。
            if (arg.argTypeValue == FunctionArg.TYPE_RETURN) {
                returnCount++;
            }

            // 是否有前置返回值。
            if (i == 0 && arg.argTypeValue == FunctionArg.TYPE_RETURN) {
                prefixReturn = true;
            }

            // 是否有出参。
            if (arg.argTypeValue == FunctionArg.TYPE_OUT || arg.argTypeValue == FunctionArg.TYPE_INOUT) {
                hasOutArg = true;
            }

            i++;
        }

        // 判断是否有出参。
        // if (argTypeValue == FunctionArg.TYPE_OUT || argTypeValue == FunctionArg.TYPE_INOUT) {
        //     hasOutArg = true;
        // }
        //
        // // [特殊] 确定 OUT 和 Return 是否不能同时共存。
        // // Postgres:
        // //   OUT 和 Return 不能同时存在，getFunctionColumns 一定会获取到 Return 参数，
        // //   如果发现有 OUT 参数则删除 Return 参数。
        // if (hasOutArg) {
        //     function.setWithReturn(false);
        //     function.getArgs().remove(0);
        //     for (FunctionArg arg : function.getArgs()) {
        //         arg.setIndex(arg.getIndex() - 1);
        //     }
        // }

        this.args = this.originalArgs;
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
        StringBuilder signature = new StringBuilder(this.name);
        signature.append("(");

        for (FunctionOrProcedureArg arg : this.args) {
            if (arg.getArgTypeValue() == arg.argTypeReturn()) {
                continue;
            }

            signature.append(arg.argTypeName)
                    .append(" ")
                    .append(arg.name)
                    .append(" ")
                    .append(arg.dataTypeName)
                    .append(", ");
        }
        signature.append(")");
        Procedure.replaceLast(signature, ", )", ")"); // 把 ", )" 替换为 ")"

        // 加上返回类型。
        if (args.size() > 0 && withReturn) {
            FunctionArg arg = args.get(0);
            signature.append(" RETURNS ").append(arg.dataTypeName);
        }

        return signature.toString();
    }

    /**
     * 获取 JDBC 执行的存储过程语句，例如 {? = call func_sum(?, ?, ?)}
     *
     * @return 返回 JDBC call 的 SQL 语句。
     */
    public String getCallableSql() {
        // 参考示例: {call mix_demo(?, ?, ?, ?)}
        StringBuilder sql = new StringBuilder();

        sql.append("{? = call ").append(this.name).append("(");
        for (FunctionOrProcedureArg arg : args) {
            if (arg.getArgTypeValue() == arg.argTypeReturn()) {
                continue;
            }

            sql.append("?, ");
        }
        sql.append(")}");
        Procedure.replaceLast(sql, ", )}", ")}"); // 把 ", )}" 替换为 ")}"

        return sql.toString();
    }

    // 获取原始的函数参数列名。
    public void printOriginalArgTypes() {
        System.out.println(originalArgs.stream().map(arg -> {
            return String.format("%s:%d:%s", arg.getArgTypeName(), arg.getArgTypeValue(), arg.getName());
        }).collect(Collectors.toList()));
    }
}
