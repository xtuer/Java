package xtuer.procedure;

/**
 * 存储过程的参数。
 */
public class ProcedureArg extends FunctionOrProcedureArg {
    /**
     * 存储过程参数类型: 1 (IN), 2 (INOUT), 4 (OUT), 5 (RETURN)。
     */
    public static final int TYPE_IN     = 1;
    public static final int TYPE_INOUT  = 2;
    public static final int TYPE_OUT    = 4;
    public static final int TYPE_RETURN = 5;

    /**
     * Oracle 游标的数据类型名。
     */
    public static final String DATA_TYPE_NAME_OF_ORACLE_CURSOR = "REF CURSOR";

    /**
     * Oracle 游标的数据类型值: OracleTypes.CURSOR = -10
     * 提示: 在这里写死而不是引用 Oracle 的类中的值是为了先不依赖 Oracle 驱动。
     */
    public static final int DATA_TYPE_VALUE_OF_ORACLE_CURSOR = -10;

    public ProcedureArg() {}

    public ProcedureArg(String name, int argTypeValue, String dataTypeName, int dataTypeValue, int length, int precision, short scale) {
        super(name, argTypeValue, dataTypeName, dataTypeValue, length, precision, scale);
    }

    @Override
    public int argTypeIn() {
        return TYPE_IN;
    }

    @Override
    public int argTypeOut() {
        return TYPE_OUT;
    }

    @Override
    public int argTypeInOut() {
        return TYPE_INOUT;
    }

    @Override
    public int argTypeReturn() {
        return TYPE_RETURN;
    }

    /**
     * 判断是否 Oracle 的 cursor 参数。
     *
     * @return 是返回 true，否则返回 false。
     */
    public boolean useOracleCursorInProcedure() {
        return TYPE_OUT == this.argTypeValue && DATA_TYPE_NAME_OF_ORACLE_CURSOR.equals(this.dataTypeName);
    }
}
