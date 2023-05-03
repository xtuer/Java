package dsc.procedure;

public class FunctionArg extends FunctionOrProcedureArg {
    /**
     * 存储过程参数类型: 1 (IN), 2 (INOUT), 3 (OUT), 4 (RETURN)。
     */
    public static final int TYPE_IN     = 1;
    public static final int TYPE_INOUT  = 2; // TODO: 待确定。
    public static final int TYPE_OUT    = 3;
    public static final int TYPE_RETURN = 4;

    public FunctionArg() {}

    public FunctionArg(String name, int argTypeValue, String dataTypeName, int dataTypeValue, int length, int precision, short scale) {
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

    @Override
    public boolean useOracleCursor() {
        return false;
    }
}
