package xtuer.procfunc.function;

import xtuer.procfunc.Arg;

public class FunctionArg extends Arg {
    /**
     * 函数参数类型: 1 (IN), 2 (INOUT), 3 (OUT), 4 (RETURN)。
     */
    public static final int ARG_TYPE_VALUE_IN       = 1;
    public static final int ARG_TYPE_VALUE_INOUT    = 2;
    public static final int ARG_TYPE_VALUE_OUT      = 3;
    public static final int ARG_TYPE_VALUE_RETURN   = 4;
    public static final int ARG_TYPE_VALUE_RETURN_2 = 5;

    public FunctionArg() {}

    public FunctionArg(String name, int originalPosition, int argTypeValue, String dataTypeName, int dataTypeValue, int length, int precision, short scale) {
        super(name, originalPosition, argTypeValue, dataTypeName, dataTypeValue, length, precision, scale);
    }

    @Override
    protected void calculateArgTypeName() {
        switch (super.getArgTypeValue()) {
            case ARG_TYPE_VALUE_IN:
                super.setArgTypeName(ARG_TYPE_NAME_IN);
                break;
            case ARG_TYPE_VALUE_INOUT:
                super.setArgTypeName(ARG_TYPE_NAME_INOUT);
                break;
            case ARG_TYPE_VALUE_OUT:
                super.setArgTypeName(ARG_TYPE_NAME_OUT);
                break;
            case ARG_TYPE_VALUE_RETURN:
                super.setArgTypeName(ARG_TYPE_NAME_RETURN);
                break;
            default:
                super.setArgTypeName(ARG_TYPE_NAME_UNKNOWN);
        }
    }
}
