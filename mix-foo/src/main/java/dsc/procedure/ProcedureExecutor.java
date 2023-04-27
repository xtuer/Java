package dsc.procedure;

import org.apache.commons.dbutils.BasicRowProcessor;

import java.sql.*;

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
         */

        // [1] 创建 CallableStatement。
        CallableStatement stmt = conn.prepareCall(procedure.getCallableSql());

        // [2] 设置存储过程的参数: 入参、出参、入出参。
        int index = 0;
        for (Procedure.Arg arg : procedure.getArgs()) {
            index++;

            switch (arg.getTypeValue()) {
                case ARG_TYPE_IN:
                    stmt.setObject(index, arg.getValue());
                    break;
                case ARG_TYPE_OUT:
                    stmt.registerOutParameter(index, Types.OTHER);
                    break;
                case ARG_TYPE_INOUT:
                    stmt.setObject(index, arg.getValue());
                    stmt.registerOutParameter(index, Types.OTHER);
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
        index = 0;
        for (Procedure.Arg arg : procedure.getArgs()) {
            index++;

            if (arg.getTypeValue() == ARG_TYPE_OUT || arg.getTypeValue() == ARG_TYPE_INOUT) {
                Object out = stmt.getObject(index);
                result.getOutResult().put(arg.getName(), out.toString());
            }
        }

        // [5] 获取更新的影响行数。
        result.setUpdateCount(stmt.getUpdateCount());

        // [6] 获取存储过程执行后的结果集，如果有。
        ResultSet rs = stmt.getResultSet();
        if (rs != null) {
            BasicRowProcessor rowProcessor = new BasicRowProcessor();
            while (rs.next()) {
                result.getRows().add(rowProcessor.toMap(rs));
            }
        }

        return result;
    }
}
