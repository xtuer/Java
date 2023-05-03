package xtuer.procedure;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class Procedure {
    /**
     * 存储过程所属 catalog (database)。
     */
    private String catalog;
    /**
     * 存储过程所属 schema。
     */
    private String schema;
    /**
     * 存储过程的名字。
     */
    private String name;
    /**
     * 存储过程的参数列表，参数顺序不能乱。
     */
    private List<ProcedureArg> args = new LinkedList<>();

    /**
     * 存储过程执行时是否使用花括号 {}。
     */
    private boolean useCurlyBrace;

    /**
     * 默认构造函数，反序列化需要。
     */
    public Procedure() {}

    /**
     * 创建存储过程对象。
     *
     * @param catalog       存储过程所属 catalog (database)。
     * @param schema        存储过程所属 schema。
     * @param procedureName 储过程的名称。
     * @param useCurlyBrace 存储过程执行时是否使用花括号 {}。
     */
    public Procedure(String catalog, String schema, String procedureName, boolean useCurlyBrace) {
        this.catalog = catalog;
        this.schema  = schema;
        this.name    = procedureName;
        this.useCurlyBrace = useCurlyBrace;
    }

    /**
     * 添加参数。
     *
     * @param arg 存储过程的参数。
     */
    public void addArg(ProcedureArg arg) {
        arg.setIndex(this.args.size() + 1);
        this.args.add(arg);
    }

    /**
     * 获取存储过程的签名。
     *
     * @return 返回存储过程的签名。
     */
    public String getSignature() {
        StringBuilder signature = new StringBuilder(this.name);
        signature.append("(");

        for (FunctionOrProcedureArg arg : this.args) {
            signature.append(arg.argTypeName)
                    .append(" ")
                    .append(arg.name)
                    .append(" ")
                    .append(arg.dataTypeName)
                    .append(", ");
        }
        signature.append(")");
        replaceLast(signature, ", )", ")"); // 把 ", )" 替换为 ")"

        return signature.toString();
    }

    /**
     * 获取 JDBC 执行的存储过程语句。
     *
     * @return 返回 JDBC call 的 SQL 语句。
     */
    public String getCallableSql() {
        // 参考示例: {call mix_demo(?, ?, ?, ?)} 和 call mix_demo(?, ?, ?, ?)
        StringBuilder sql = new StringBuilder();

        if (useCurlyBrace) {
            sql.append("{call ").append(this.name).append("(");
            for (FunctionOrProcedureArg arg : args) {
                sql.append("?, ");
            }
            sql.append(")}");
            replaceLast(sql, ", )}", ")}"); // 把 ", )}" 替换为 ")}"
        } else {
            // [*] 例如 Postgre 调用存储过程时需要使用使用 call proc() 的格式，不能使用 {call proc()} 的格式。
            sql.append("call ").append(this.name).append("(");
            for (FunctionOrProcedureArg arg : args) {
                sql.append("?, ");
            }
            sql.append(")");
            replaceLast(sql, ", )", ")"); // 把 ", )" 替换为 ")"
        }

        return sql.toString();
    }

    public static void replaceLast(StringBuilder buf, String oldStr, String newStr) {
        int start = buf.lastIndexOf(oldStr);
        int end = start + oldStr.length();

        if (start != -1) {
            buf.replace(start, end, newStr);
        }
    }
}
