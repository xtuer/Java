package xtuer.funcproc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
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
}
