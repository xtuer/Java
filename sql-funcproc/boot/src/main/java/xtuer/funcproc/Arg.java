package xtuer.funcproc;

import lombok.Data;

/**
 * 存储过程或者函数的参数，它们既有相似之处，又有不同之处。
 */
@Data
public abstract class Arg {
    /**
     * 参数类型名称。
     */
    public static final String ARG_TYPE_NAME_IN      = "IN";
    public static final String ARG_TYPE_NAME_INOUT   = "INOUT";
    public static final String ARG_TYPE_NAME_OUT     = "OUT";
    public static final String ARG_TYPE_NAME_RETURN  = "RETURN";
    public static final String ARG_TYPE_NAME_UNKNOWN = "UNKNOWN";
    public static final String ARG_TYPE_NAME_RETURN_POST = "RETURN_POST";

    /**
     * 参数名称。
     */
    String name;

    /**
     * 参数的原始位置，有返回值时从 0 开始。
     */
    int originalPosition;

    /**
     * 参数类型值: 1 (IN), 4 (OUT), 2 (INOUT)。
     */
    int argTypeValue;

    /**
     * 参数类型名: IN, OUT, INOUT, RETURN。
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

    public Arg() {}

    /**
     * 创建参数对象。
     *
     * @param name          参数名称。
     * @param originalPosition 参数的原始位置。
     * @param argTypeValue  参数类型值: 1 (IN), 4 (OUT), 2 (INOUT)。
     * @param dataTypeName  参数的数据类型名: 例如 NCHAR。
     * @param dataTypeValue 参数的数据类型值: 例如 -15。
     * @param length        长度。
     * @param precision     精度。
     * @param scale         标度。
     */
    public Arg(String name, int originalPosition, int argTypeValue, String dataTypeName,
               int dataTypeValue, int length, int precision, short scale) {
        this.name             = name;
        this.originalPosition = originalPosition;
        this.argTypeValue     = argTypeValue;
        this.dataTypeName     = dataTypeName;
        this.dataTypeValue    = dataTypeValue;
        this.length           = length;
        this.precision        = precision;
        this.scale            = scale;

        // 计算参数类型名字。
        this.calculateArgTypeName();
    }

    /**
     * 计算参数的类型名: IN, INOUT, OUT, RETURN 等。
     * 函数或者存储过程的入参出参等的值不一样。
     */
    protected abstract void calculateArgTypeName();

    /**
     * 获取参数的签名，例如 IN name varchar。
     *
     * @return 返回参数的签名字符串。
     */
    public String getSignature() {
        if (ARG_TYPE_NAME_RETURN.equals(argTypeName) || ARG_TYPE_NAME_RETURN_POST.equals(argTypeName) || ARG_TYPE_NAME_UNKNOWN.equals(argTypeName)) {
            // 返回类型: count int
            return String.format("%s %s", name, dataTypeName);
        } else {
            // 其他类型，如 IN, OUT, INOUT: IN first int
            return String.format("%s %s %s", argTypeName, name, dataTypeName);
        }
    }

    /**
     * 判断参数是否输入参数。
     *
     * @return 输入参数返回 true，否则返回 false。
     */
    public boolean isInArg() {
        return ARG_TYPE_NAME_IN.equals(argTypeName) || ARG_TYPE_NAME_INOUT.equals(argTypeName);
    }

    /**
     * 判断参数是否输出参数。
     *
     * @return 输出参数返回 true，否则返回 false。
     */
    public boolean isOutArg() {
        return ARG_TYPE_NAME_OUT.equals(argTypeName) || ARG_TYPE_NAME_INOUT.equals(argTypeName);
    }
}
