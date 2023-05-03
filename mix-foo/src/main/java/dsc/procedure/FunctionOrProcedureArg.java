package dsc.procedure;

import lombok.Data;

/**
 * 函数和存储过程的参数基类。
 */
@Data
public abstract class FunctionOrProcedureArg {
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
    int argTypeValue;

    /**
     * 参数类型名: IN, OUT, INOUT。
     */
    String argTypeName;

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

    public FunctionOrProcedureArg() {}

    /**
     * 创建存储过程的参数对象。
     *
     * @param name          参数名称。
     * @param argTypeValue     参数类型值: 1 (IN), 4 (OUT), 2 (INOUT)。
     * @param dataTypeName  参数的数据类型名: 例如 NCHAR。
     * @param dataTypeValue 参数的数据类型值: 例如 -15。
     * @param length        长度。
     * @param precision     精度。
     * @param scale         标度。
     */
    public FunctionOrProcedureArg(String name, int argTypeValue, String dataTypeName, int dataTypeValue, int length, int precision, short scale) {
        this.name          = name;
        this.argTypeValue = argTypeValue;
        this.dataTypeName  = dataTypeName;
        this.dataTypeValue = dataTypeValue;
        this.length        = length;
        this.precision     = precision;
        this.scale         = scale;

        // 参数类型名
        if (argTypeValue == argTypeIn()) {
            this.argTypeName = "IN";
        } else if (argTypeValue == argTypeOut()) {
            this.argTypeName = "OUT";
        } else if (argTypeValue == argTypeInOut()) {
            this.argTypeName = "INOUT";
        } else if (argTypeValue == argTypeReturn()) {
            this.argTypeName = "RETURN";
        } else {
            this.argTypeName = "UNKNOWN";
        }
    }

    /**
     * 参数类型。
     */
    public abstract int argTypeIn();
    public abstract int argTypeOut();
    public abstract int argTypeInOut();
    public abstract int argTypeReturn();

    /**
     * 判断是否 Oracle 的 cursor 参数。
     *
     * @return 是返回 true，否则返回 false。
     */
    public abstract boolean useOracleCursor();
}
