package xtuer.funcproc.function;

import lombok.Getter;
import xtuer.funcproc.Arg;

import java.util.stream.Collectors;

@Getter
public class SqlServerFunction extends Function {
    /**
     * 是否使用 PreparedStatement 执行函数。
     * 提示: 返回 inline table 的时候使用 PreparedStatement 访问。
     */
    private boolean usePreparedStatement;

    @Override
    public Function build() {
        super.build();

        for (FunctionArg arg : super.getInOutInoutArgs()) {
            // 类型为 OUT 且返回值的类型名 "table"。
            if (arg.getArgTypeValue() == FunctionArg.ARG_TYPE_VALUE_OUT && "table".equals(arg.getDataTypeName())) {
                this.usePreparedStatement = true;
                break;
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
        return "";
    }

    @Override
    public String getCallableSql() {
        // 函数名需要有 schema
        // 返回简单类型: { ? = call test.AddNumbers(?) }
        // 返回 inline table: select * from test.func_return_inline_table(?)
        String questionMarks = Function.generateCallableSqlParameterQuestionMarks(super.inArgs.size());

        if (this.usePreparedStatement) {
            return String.format("select * from %s.%s(%s)", super.schema, super.name, questionMarks);
        } else {
            return String.format("{ ? = call %s.%s(%s) }", super.schema, super.name, questionMarks);
        }
    }
}
