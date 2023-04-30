package dsc.procedure;

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
     * 默认构造函数，反序列化需要。
     */
    public Procedure() {}

    /**
     * 使用存储过程的名称创建存储过程对象。
     *
     * @param catalog       存储过程所属 catalog (database)。
     * @param schema        存储过程所属 schema。
     * @param procedureName 储过程的名称。
     */
    public Procedure(String catalog, String schema, String procedureName) {
        this.catalog = catalog;
        this.schema  = schema;
        this.name    = procedureName;
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

        for (ProcedureArg arg : this.args) {
            signature.append(arg.typeName)
                    .append(" ")
                    .append(arg.name)
                    .append(" ")
                    .append(arg.dataTypeName)
                    .append(", ");
        }
        signature.append(")");

        // 把 ", )" 替换为 ")"
        int index = signature.lastIndexOf(", )");
        if (index != -1) {
            signature.replace(index, signature.length(), ")");
        }

        return signature.toString();
    }

    /**
     * 获取 JDBC 执行的存储过程语句。
     *
     * @return 返回 JDBC call 的 SQL 语句。
     */
    public String getCallableSql() {
        // 参考示例: {call mix_demo(?, ?, ?, ?)}
        StringBuilder sql = new StringBuilder();

        sql.append("{call ")
                .append(this.name)
                .append("(");
        for (ProcedureArg arg : args) {
            sql.append("?, ");
        }
        sql.append(")}");

        // 把 ", )}" 替换为 ")}"
        int index = sql.lastIndexOf(", )}");
        if (index != -1) {
            sql.replace(index, sql.length(), ")}");
        }

        return sql.toString();
    }
}
