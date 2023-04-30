package dsc.procedure;

import lombok.Data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
public class Procedure {
    /**
     * 存储过程参数类型: 1 (IN), 4 (OUT), 2 (INOUT)。
     */
    public static final int ARG_TYPE_IN    = 1;
    public static final int ARG_TYPE_OUT   = 4;
    public static final int ARG_TYPE_INOUT = 2;
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
    private List<Arg> args = new LinkedList<>();

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
    public void addArg(Arg arg) {
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

        for (Arg arg : this.args) {
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
        for (Arg arg : args) {
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

    /**
     * 存储过程的参数。
     */
    @Data
    public static class Arg {
        /**
         * Oracle 游标的数据类型名。
         */
        public static final String DATA_TYPE_NAME_OF_ORACLE_CURSOR = "REF CURSOR";

        /**
         * Oracle 游标的数据类型值: OracleTypes.CURSOR = -10
         */
        private static final int DATA_TYPE_VALUE_OF_ORACLE_CURSOR = -10;

        /**
         * 参数名称。
         */
        String name;

        /**
         * 参数位置，从 1 开始。
         */
        int index;

        /**
         * 参数类型值: 1 (IN), 4 (OUT), 2 (INOUT)。
         */
        int typeValue;

        /**
         * 参数类型名: IN, OUT, INOUT。
         */
        String typeName;

        /**
         * 参数的数据类型值: SQL type name, for a UDT type the type name is fully qualified。
         */
        int dataTypeValue;

        /**
         * 参数的数据类型名: SQL type from java.sql.Types，例如 INT。
         */
        String dataTypeName;

        /**
         * 长度。
         */
        int length;

        /**
         * 精度: 是指数值数据类型的总位数，包括整数和小数部分的位数，例如整数的显示长度，varchar 的长度。
         * 对于 DECIMAL(10,2) 类型的列，它的 precision 是 10，scale 是 2。这意味着该列可以存储 10 位数，其中小数部分占 2 位。
         */
        int precision;

        /**
         * 标度: 是指数值数据类型中小数部分的位数。
         */
        short scale;

        /**
         * 参数值: 执行存储过程，前端传给后端时保存用户输入的值。
         */
        Object value;

        public Arg() {}

        /**
         * 创建存储过程的参数对象。
         *
         * @param name          参数名称。
         * @param typeValue     参数类型值: 1 (IN), 4 (OUT), 2 (INOUT)。
         * @param dataTypeName  参数的数据类型名: 例如 NCHAR。
         * @param dataTypeValue 参数的数据类型值: 例如 -15。
         * @param length        长度。
         * @param precision     精度。
         * @param scale         标度。
         */
        public Arg(String name, int typeValue, String dataTypeName, int dataTypeValue, int length, int precision, short scale) {
            this.name          = name;
            this.typeValue     = typeValue;
            this.dataTypeName  = dataTypeName;
            this.dataTypeValue = dataTypeValue;
            this.length        = length;
            this.precision     = precision;
            this.scale         = scale;

            // 参数类型名
            switch (typeValue) {
                case ARG_TYPE_IN:
                    this.typeName = "IN";
                    break;
                case ARG_TYPE_OUT:
                    this.typeName = "OUT";
                    break;
                case ARG_TYPE_INOUT:
                    this.typeName = "INOUT";
                    break;
                default:
                    this.typeName = "UNKNOWN";
            }
        }

        /**
         * 判断是否 Oracle 的 cursor 参数。
         *
         * @return 是返回 true，否则返回 false。
         */
        public boolean useOracleCursor() {
            return DATA_TYPE_NAME_OF_ORACLE_CURSOR.equals(this.dataTypeName);
        }

        /**
         * 获取 Oracle 游标的数据类型 (在这里写死而不是引用 Oracle 的类中的值是为了先不引用 Oracle 驱动)。
         *
         * @return 返回游标的类型值。
         */
        public int getDataTypeValueOfOracleCursor() {
            return DATA_TYPE_VALUE_OF_ORACLE_CURSOR;
        }
    }

    /**
     * 存储过程的执行结果。
     */
    @Data
    public static class Result {
        /**
         * 更新的影响行数。
         * The current result as an update count;
         * -1 if the current result is a ResultSet object or there are no more results.
         */
        private int updateCount;

        /**
         * 输出参数的结果。
         */
        private Map<String, Object> outResult = new HashMap<>();

        /**
         * 查询的结果。
         */
        private List<Map<String, Object>> rows = new LinkedList<>();
    }
}
