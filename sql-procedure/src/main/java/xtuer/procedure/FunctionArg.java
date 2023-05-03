package xtuer.procedure;

public class FunctionArg extends FunctionOrProcedureArg {
    /**
     * 函数参数类型: 1 (IN), 2 (INOUT), 3 (OUT), 4 (RETURN)。
     */
    public static final int TYPE_IN     = 1;
    public static final int TYPE_INOUT  = 2; // TODO: 待确定。
    public static final int TYPE_OUT    = 3;
    public static final int TYPE_RETURN = 4;

    /**
     * Postgre 函数返回类型名:
     * A. typeName (COLUMN_NAME) 为 "returnValue", typeValue (COLUMN_TYPE) 为 4。
     * B. typeName (COLUMN_NAME) 为 "用户定义的，如返回 table 的列 username", typeValue (COLUMN_TYPE) 为 4。
     */
    public static final String TYPE_NAME_RETURN_OF_POSTGRES_FUNCTION = "returnValue";

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
}
