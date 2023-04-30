package dsc.procedure;

import org.apache.commons.dbutils.BasicRowProcessor;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static dsc.procedure.Procedure.*;

/**
 * 存储过程的执行类。
 */
public class ProcedureExecutor {

    /**
     * 执行存储过程。
     *
     * @param conn 数据库链接。
     * @param procedure 存储过程对象。
     * @return 返回存储过程执行结果。
     * @throws SQLException SQL 异常，例如连接异常，存储过程的调用语句有问题。
     */
    public static Procedure.Result execute(Connection conn, Procedure procedure) throws SQLException {
        /*
         逻辑 (使用 Object 类型设置入参和出参，是因为结果显示给前端使用不需要数据类型参与业务计算):
         1. 创建 CallableStatement。
         2. 设置存储过程的参数: 入参、出参、入出参。
         3. 执行存储过程。
         4. 获取存储过程执行后的输出参数，如果有。
         5. 获取更新的影响行数。
         6. 获取存储过程执行后的结果集，如果有。
         7. 关闭释放资源。
         */

        // [1] 创建 CallableStatement。
        CallableStatement stmt = conn.prepareCall(procedure.getCallableSql());

        // [2] 设置存储过程的参数: 入参、出参、入出参。
        for (Procedure.Arg arg : procedure.getArgs()) {
            int index = arg.getIndex();

            // 注意:
            // A. 入参都可以设置为 Object。
            // B. 出参需要设置为对应的类型 (MySQL 可以设置为 OTHER，但 Oracle 需要设置为游标类型)。
            // C. 出参可以使用 getObject() 获取。
            switch (arg.getTypeValue()) {
                case ARG_TYPE_IN:
                    stmt.setObject(index, arg.getValue());
                    break;
                case ARG_TYPE_OUT:
                    if (arg.useOracleCursor()) {
                        // Oracle 查询结果集需要使用游标获取。
                        stmt.registerOutParameter(index, arg.getDataTypeValueOfOracleCursor());
                    } else {
                        stmt.registerOutParameter(index, arg.getDataTypeValue());
                    }
                    break;
                case ARG_TYPE_INOUT:
                    stmt.setObject(index, arg.getValue());
                    stmt.registerOutParameter(index, arg.getDataTypeValue());
                    break;
                default:
                    System.out.println("UNKNOWN Arg Type: " + arg.getTypeValue());
            }
        }

        // [3] 执行存储过程。
        stmt.execute();

        // 保存结果。
        Procedure.Result result = new Procedure.Result();

        // [4] 获取存储过程执行后的输出参数，如果有。
        for (Procedure.Arg arg : procedure.getArgs()) {
            if (arg.useOracleCursor()) {
                // Oracle: 获取游标类型的输出参数。
                ResultSet rs = (ResultSet) stmt.getObject(arg.getIndex());
                handleResultSet(rs, result);
            } else if (arg.getTypeValue() == ARG_TYPE_OUT || arg.getTypeValue() == ARG_TYPE_INOUT) {
                // MySQL: 获取输出参数。
                Object out = stmt.getObject(arg.getIndex());
                result.getOutResult().put(arg.getName(), out);
            }
        }

        // [5] 获取更新的影响行数。
        result.setUpdateCount(stmt.getUpdateCount());

        // [6] 获取存储过程执行后的结果集，如果有。
        ResultSet rs = stmt.getResultSet();
        handleResultSet(rs, result);

        // [7] 关闭释放资源。
        stmt.close();

        return result;
    }

    /**
     * 把结果集保存到输出结果中。
     *
     * @param rs ResultSet 对象。
     * @param result 存储过程的结果对象。
     * @throws SQLException SQL 异常。
     */
    private static void handleResultSet(ResultSet rs, Procedure.Result result) throws SQLException {
        if (rs == null) {
            return;
        }

        BasicRowProcessor rowProcessor = new BasicRowProcessor();
        while (rs.next()) {
            result.getRows().add(rowProcessor.toMap(rs));
        }
        rs.close();
    }
}
