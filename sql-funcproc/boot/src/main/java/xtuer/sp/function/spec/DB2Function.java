package xtuer.funcproc.function.spec;

import lombok.Getter;
import xtuer.funcproc.Arg;
import xtuer.funcproc.FuncProcUtils;
import xtuer.funcproc.function.Function;
import xtuer.funcproc.function.FunctionArg;

import java.util.stream.Collectors;

/**
 * DB2 的函数。
 * - 支持返回简单类型、TABLE。
 * - 不支持返回 ROW (还没法从特征里判断，得到的 Meta 信息 TABLE 的一样)。
 * - 不支持 OUT 参数。
 *
 * 官方文档: https://www.ibm.com/docs/en/db2/9.7?topic=statements-create-function-sql-scalar-table-row
 * DatabaseMetadata 获取到的函数信息不符合 JDBC 标准。
 */
public class DB2Function extends Function {
    /**
     * 是否返回 TABLE。
     */
    @Getter
    private boolean tableReturned = false;

    /**
     * 返回 TABLE 的参数。
     */
    private static final int ARG_TYPE_VALUE_RETURN_TABLE = 0;

    @Override
    public Function build() {
        super.build();

        super.returnArgs.clear();

        for (FunctionArg arg : super.getOriginalArgs()) {
            int argTypeValue = arg.getArgTypeValue();

            // 返回 TABLE 的参数。
            if (argTypeValue == ARG_TYPE_VALUE_RETURN_TABLE) {
                super.returnArgs.add(arg);
                this.tableReturned = true;
            }

            // 不支持输出参数 INOUT、OUT
            if (argTypeValue == FunctionArg.ARG_TYPE_VALUE_INOUT || argTypeValue == FunctionArg.ARG_TYPE_VALUE_OUT) {
                super.supported = false;
            }
        }

        return this;
    }

    @Override
    public String getSignature() {
        // 简单类型: FUNC_NAME(IN id int)
        // 返回 TABLE: FUNC_NAME(IN id int) return (id int, name varchar, age int)
        String inArgsString = super.inArgs.stream().map(Arg::getSignature).collect(Collectors.joining(", "));
        String returnArgsString = super.returnArgs.stream().map(Arg::getSignature).collect(Collectors.joining(", "));

        if (this.tableReturned) {
            return String.format("%s(%s) return (%s)", super.name, inArgsString, returnArgsString);
        } else {
            return String.format("%s(%s)", super.name, inArgsString);
        }
    }

    @Override
    public String getCallableSql() {
        // 简单类型: VALUES FUNC_NAME(?)
        // 返回 TABLE: SELECT * FROM TABLE(FUNC_NAME(?))
        String questionMarks = FuncProcUtils.generateCallableSqlParameterQuestionMarks(super.inArgs.size());

        if (this.tableReturned) {
            return String.format("SELECT * FROM TABLE(%s(%s))", super.name, questionMarks);
        } else {
            return String.format("VALUES %s.%s(%s)", super.schema, super.name, questionMarks);
        }
    }
}
