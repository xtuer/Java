package xtuer.procfunc.function;

import xtuer.util.Utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 函数获取类。
 */
public class FunctionFetcher {
    /**
     * 获取存储过程。
     *
     * @param conn 数据库连接。
     * @param catalog 所属 catalog。
     * @param schema 所属 schema。
     * @param functionName 函数的名称。
     * @return 返回获取到的函数对象。
     */
    public static Function fetch(Connection conn,
                                 String catalog,
                                 String schema,
                                 String functionName) throws SQLException {
        Function function = new Function(catalog, schema, functionName);
        DatabaseMetaData meta = conn.getMetaData();

        try (ResultSet rs = meta.getFunctionColumns(function.getCatalog(), function.getSchema(), function.getName(), null)) {
            while (rs.next()) {
                String argName         = rs.getString("COLUMN_NAME");   // 参数名称
                int originalPosition   = rs.getInt("ORDINAL_POSITION"); // 参数原始位置
                int argTypeValue       = rs.getInt("COLUMN_TYPE");      // 入参出参:  1 (IN), 4 (OUT), 2 (INOUT)
                int argDataTypeValue   = rs.getInt("DATA_TYPE");        // 参数的数据类型值: SQL type from java.sql.Types
                String argDataTypeName = rs.getString("TYPE_NAME");     // 参数的数据类型名: SQL type name, for a UDT type the type name is fully qualified
                int length             = rs.getInt("LENGTH");           // 长度
                int precision          = rs.getInt("PRECISION");        // 精度
                short scale            = rs.getShort("SCALE");          // 标度
// Utils.dump(rs);
                function.addArg(new FunctionArg(argName, originalPosition, argTypeValue, argDataTypeName, argDataTypeValue, length, precision, scale));
            }

            return function.build();
        }
    }
}
