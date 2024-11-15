package xtuer.sp.function.spec;

import lombok.Getter;
import xtuer.sp.Arg;
import xtuer.sp.FuncProcUtils;
import xtuer.sp.function.Function;
import xtuer.sp.function.FunctionArg;

import java.util.stream.Collectors;

@Getter
public class SqlServerFunction extends Function {
    /**
     * 是否使用 CallableStatement 执行函数。
     * 提示: 返回 table 的时候使用 PreparedStatement 访问。
     */
    private boolean useCallableStatement = true;
    private boolean tableReturned = false;

    @Override
    public Function build() {
        super.build();

        for (FunctionArg arg : super.getInOutInoutArgs()) {
            // 类型为 OUT 且返回值的类型名为 "table"。
            if (arg.getArgTypeValue() == FunctionArg.ARG_TYPE_VALUE_OUT && "table".equals(arg.getDataTypeName())) {
                this.useCallableStatement = false;
                this.tableReturned = true;
                break;
            }
        }

        return this;
    }

    @Override
    public String getSignature() {
        // 简单类型: FUNC_NAME(IN id int)
        // 返回 TABLE: FUNC_NAME(IN id int) return (inline table)
        String inArgsString = super.inArgs.stream().map(Arg::getSignature).collect(Collectors.joining(", "));
        String returnArgsString = super.returnArgs.stream().map(Arg::getSignature).collect(Collectors.joining(", "));

        if (this.tableReturned) {
            returnArgsString = "@TABLE_RETURN_VALUE";
        }

        return String.format("%s(%s) return (%s)", super.name, inArgsString, returnArgsString);
    }

    @Override
    public String getCallableSql() {
        // 函数名需要有 schema
        // 返回简单类型: { ? = call schema.AddNumbers(?) }
        // 返回 inline table: select * from schema.func_return_inline_table(?)
        String questionMarks = FuncProcUtils.generateCallableSqlParameterQuestionMarks(super.inArgs.size());

        if (this.useCallableStatement) {
            return String.format("{ ? = call %s.%s(%s) }", super.schema, super.name, questionMarks);
        } else {
            return String.format("select * from %s.%s(%s)", super.schema, super.name, questionMarks);
        }
    }
}
