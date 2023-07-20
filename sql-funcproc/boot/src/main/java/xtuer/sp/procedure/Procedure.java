package xtuer.funcproc.procedure;

import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 存储过程。
 */
@Data
public class Procedure {
    /**
     * 函数所属 catalog (database)。
     */
    protected String catalog;

    /**
     * 函数所属 schema。
     */
    protected String schema;

    /**
     * 函数的名字。
     */
    protected String name;

    /**
     * 是否支持。
     */
    protected boolean supported = true;

    /**
     * 原始获取到的参数。
     */
    protected List<ProcedureArg> originalArgs = new ArrayList<>();

    /**
     * 输入参数，包括 IN, INOUT 参数。
     */
    protected List<ProcedureArg> inArgs = new LinkedList<>();

    /**
     * 输入输出参数，包括 IN, OUT, INOUT 参数。
     */
    protected List<ProcedureArg> inOutInoutArgs = new LinkedList<>();

    /**
     * 使用游标 Cursor 的 OUT 参数。
     */
    protected boolean cursorOuted;

    public Procedure() {}

    /**
     * 创建存储过程对象。
     *
     * @param catalog 存储过程所属 catalog (database)。
     * @param schema 存储过程所属 schema。
     * @param procedureName 存储过程的名称。
     */
    public Procedure(String catalog, String schema, String procedureName) {
        this.catalog = catalog;
        this.schema  = schema;
        this.name    = procedureName;
    }

    /**
     * 添加参数。
     *
     * @param arg 函数的参数。
     */
    public void addArg(ProcedureArg arg) {
        this.originalArgs.add(arg);
    }

    /**
     * 获取函数的签名，例如
     * 有返回值: func_sum(IN num1 int4, IN num2 int4, sum int4) RETURNS int4
     * 无返回值: func_sum(IN num1 int4, IN num2 int4, OUT sum int4)
     *
     * @return 返回函数的签名。
     */
    public String getSignature() {
        return null; // 推迟到子类实现。
    }

    /**
     * 获取 JDBC 执行函数的语句，例如 {? = call func_sum(?, ?, ?)}
     *
     * @return 返回 JDBC call 的 SQL 语句。
     */
    public String getCallableSql() {
        return null; // 推迟到子类实现。
    }

    /**
     * 构建，把原始参数处理成可用的。
     */
    public Procedure build() {
        // 把返回参数和输入输出参数分别提取出来。
        inArgs.clear();
        inOutInoutArgs.clear();

        for (ProcedureArg arg : originalArgs) {
            // 输入参数。
            if (arg.isInArg()) {
                inArgs.add(arg);
            }

            // 输入输出参数。
            if (arg.isInArg() || arg.isOutArg()) {
                inOutInoutArgs.add(arg);
            }
        }

        return this;
    }

    /**
     * 使用传入的存储过程对象创建一个指定目标类型的存储过程对象。
     *
     * @param src 源存储过程对象。
     * @param dstProcedureClass 目标存储过程的类。
     * @return 返回目标存储过程的对象。
     * @param <T> 目标存储过程类型，例如 PostgresProcedure
     * @throws RuntimeException 使用反射创建对象出错时抛出异常。
     */
    public static <T> T newProcedure(Procedure src, Class<T> dstProcedureClass) {
        if (!Procedure.class.isAssignableFrom(dstProcedureClass.getSuperclass())) {
            throw new RuntimeException("类型 dstProcedureClass 必须是 Procedure 的子类");
        }

        try {
            T dst = dstProcedureClass.getConstructor().newInstance();
            BeanUtils.copyProperties(src, dst);

            // 构建函数。
            ((Procedure) dst).build();

            return dst;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
