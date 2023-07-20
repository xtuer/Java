package xtuer.sp;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class FuncProcUtils {
    /**
     * 判断结果集中是否有传入的列。
     *
     * @param rs 结果集。
     * @param columnLabel 列名。
     * @return 有则返回 true，否则返回 false。
     */
    public static boolean hasColumn(ResultSet rs, String columnLabel) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int count = md.getColumnCount();

        for (int idx = 1; idx <= count; idx++) {
            if (md.getColumnLabel(idx).equals(columnLabel)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 生成 Callable SQL 语句 {? = call func_sum(?, ?, ?)} 的参数部分的问号占位符。
     * 例如 paramCount 为 3 则生成 "?, ?, ?"。
     *
     * @param paramCount 参数个数。
     * @return 返回参数占位符字符串。
     */
    public static String generateCallableSqlParameterQuestionMarks(int paramCount) {
        List<String> qms = new ArrayList<>(paramCount);
        for (int i = 0; i < paramCount; i++) {
            qms.add("?");
        }

        return String.join(", ", qms);
    }

    /**
     * 从 传入的 funcProcNames 中提取出函数名。
     * 因为 SqlServer 的 DatabaseMetaData.getProcedures() 和 DatabaseMetaData.getFunctions() 返回的结果一样，
     * 会把所有的函数名和存储过程名一起返回: [func_return_multi_table;0, PROC_MULTI_ROWS;1]
     * - 存储函数名以 ";0" 结尾
     * - 存储过程名以 ";1" 结尾
     *
     * @param funcProcNames 包含了存储函数名和存储过程名的 list。
     * @return 返回存储函数名的 list。
     */
    public static List<String> extractFunctionNamesForSqlServer(List<String> funcProcNames) {
        List<String> funcNames = new LinkedList<>();

        for (String name : funcProcNames) {
            if (name != null && name.endsWith(";0")) {
                funcNames.add(name.substring(0, name.length() - 2));
            }
        }

        return funcNames;
    }

    /**
     * 参考 extractFunctionNamesForSqlServer 的介绍。
     */
    public static List<String> extractProcedureNamesForSqlServer(List<String> funcProcNames) {
        List<String> procNames = new LinkedList<>();

        for (String name : funcProcNames) {
            if (name != null && name.endsWith(";1")) {
                procNames.add(name.substring(0, name.length() - 2));
            }
        }

        return procNames;
    }
}
