package dsc.procedure;

import org.apache.commons.dbutils.BasicRowProcessor;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

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
    public static ProcedureResult execute(Connection conn, Procedure procedure) throws SQLException {
        /*
         逻辑 (使用 Object 类型设置入参和出参，是因为结果显示给前端使用不需要数据类型参与业务计算):
         1. 创建 CallableStatement。
         2. 设置存储过程的参数: 入参、出参、入出参。
         3. 执行存储过程。
         4. 获取存储过程执行的结果。
         5. 关闭释放资源。
         */

        // [1] 创建 CallableStatement。
        CallableStatement stmt = conn.prepareCall(procedure.getCallableSql());

        // [2] 设置存储过程的参数: 入参、出参、入出参。
        setupParameters(stmt, procedure);

        // [3] 执行存储过程。
        stmt.execute();

        // [4] 获取存储过程执行的结果。
        ProcedureResult result = getProcedureResult(stmt, procedure);

        // [5] 关闭释放资源。
        stmt.close();

        return result;
    }

    /**
     * 设置存储过程的参数: 入参、出参、入出参。
     */
    private static void setupParameters(CallableStatement stmt, Procedure procedure) throws SQLException {
        for (ProcedureArg arg : procedure.getArgs()) {
            int index = arg.getIndex();

            // 注意:
            // A. 入参都可以设置为 Object。
            // B. 出参需要设置为对应的类型 (MySQL 可以设置为 OTHER，但 Oracle 需要设置为游标类型)。
            // C. 出参可以使用 getObject() 获取。
            switch (arg.getTypeValue()) {
                case ProcedureArg.TYPE_IN:
                    stmt.setObject(index, arg.getValue());
                    break;
                case ProcedureArg.TYPE_OUT:
                    if (arg.useOracleCursor()) {
                        // Oracle 查询结果集需要使用游标获取。
                        stmt.registerOutParameter(index, ProcedureArg.DATA_TYPE_VALUE_OF_ORACLE_CURSOR);
                    } else {
                        stmt.registerOutParameter(index, arg.getDataTypeValue());
                    }
                    break;
                case ProcedureArg.TYPE_INOUT:
                    stmt.setObject(index, arg.getValue());
                    stmt.registerOutParameter(index, arg.getDataTypeValue());
                    break;
                default:
                    System.out.println("UNKNOWN Arg Type: " + arg.getTypeValue());
            }
        }
    }

    /**
     * 获取存储过程执行后的结果。
     */
    private static ProcedureResult getProcedureResult(CallableStatement stmt, Procedure procedure) throws SQLException {
        /*
         逻辑:
         1. 获取更新的影响行数 (即使是更新语句，也有可能返回 -1)。
         2. 获取存储过程执行的输出参数。
         3. 获取存储过程执行的结果集。
         */
        ProcedureResult result = new ProcedureResult();

        // [1] 获取更新的影响行数。
        result.setUpdateCount(stmt.getUpdateCount());

        // [2] 获取存储过程执行的输出参数。
        for (ProcedureArg arg : procedure.getArgs()) {
            if (arg.useOracleCursor()) {
                // Oracle: 获取游标类型的输出参数。
                ResultSet rs = (ResultSet) stmt.getObject(arg.getIndex());
                handleResultSet(rs, result);
            } else if (arg.getTypeValue() == ProcedureArg.TYPE_OUT || arg.getTypeValue() == ProcedureArg.TYPE_INOUT) {
                // MySQL: 获取输出参数。
                Object out = stmt.getObject(arg.getIndex());
                result.getOutResult().put(arg.getName(), out);
            }
        }

        // [3] 获取存储过程执行的结果集。
        handleResultSet(stmt.getResultSet(), result);

        return result;
    }

    /**
     * 把结果集保存到输出结果中。
     *
     * @param rs ResultSet 对象。
     * @param result 存储过程的结果对象。
     * @throws SQLException SQL 异常。
     */
    private static void handleResultSet(ResultSet rs, ProcedureResult result) throws SQLException {
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
