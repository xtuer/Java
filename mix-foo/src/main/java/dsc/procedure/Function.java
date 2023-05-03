package dsc.procedure;

import lombok.Data;

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
        arg.setIndex(this.args.size() + 1);
        this.args.add(arg);
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
}
