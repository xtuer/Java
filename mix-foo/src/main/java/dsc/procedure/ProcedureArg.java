package dsc.procedure;

import lombok.Data;

/**
 * 存储过程的参数。
 */
@Data
public class ProcedureArg {
    /**
     * 存储过程参数类型: 1 (IN),  2 (INOUT), 4 (OUT)。
     */
    public static final int TYPE_IN    = 1;
    public static final int TYPE_INOUT = 2;
    public static final int TYPE_OUT   = 4;

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

    public ProcedureArg() {}

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
    public ProcedureArg(String name, int typeValue, String dataTypeName, int dataTypeValue, int length, int precision, short scale) {
        this.name          = name;
        this.typeValue     = typeValue;
        this.dataTypeName  = dataTypeName;
        this.dataTypeValue = dataTypeValue;
        this.length        = length;
        this.precision     = precision;
        this.scale         = scale;

        // 参数类型名
        switch (typeValue) {
            case TYPE_IN:
                this.typeName = "IN";
                break;
            case TYPE_OUT:
                this.typeName = "OUT";
                break;
            case TYPE_INOUT:
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
        return TYPE_OUT == this.typeValue && DATA_TYPE_NAME_OF_ORACLE_CURSOR.equals(this.dataTypeName);
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
