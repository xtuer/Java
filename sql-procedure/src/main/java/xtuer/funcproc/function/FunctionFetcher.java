package xtuer.funcproc.function;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 函数获取类。
 */
public class FunctionFetcher {
    /**
     * 获取存储函数。需要注意的是这里返回的函数对象是一个通用的结构，在使用的时候有可能需要转为数据库对应的函数类型。
     *
     * @param conn 数据库连接。
     * @param catalog 所属 catalog。
     * @param schema 所属 schema。
     * @param functionName 函数的名称。
     * @return 返回获取到的函数对象。
     */
    public static Function fetchFunction(Connection conn,
                                         String catalog,
                                         String schema,
                                         String functionName) throws SQLException {
        Function function = new Function(catalog, schema, functionName);
        DatabaseMetaData meta = conn.getMetaData();

        try (ResultSet rs = meta.getFunctionColumns(function.getCatalog(), function.getSchema(), function.getName(), null)) {
            while (rs.next()) {
                String argName         = rs.getString("COLUMN_NAME");   // 参数名称
                int originalPosition   = rs.getInt("ORDINAL_POSITION"); // 参数原始位置
                int argTypeValue       = rs.getInt("COLUMN_TYPE");      // 入参出参:  1 (IN), 2 (INOUT), 3 (OUT), 4 (RETURN)
                int argDataTypeValue   = rs.getInt("DATA_TYPE");        // 参数的数据类型值: SQL type from java.sql.Types
                String argDataTypeName = rs.getString("TYPE_NAME");     // 参数的数据类型名: SQL type name, for a UDT type the type name is fully qualified
                int length             = rs.getInt("LENGTH");           // 长度
                int precision          = rs.getInt("PRECISION");        // 精度
                short scale            = rs.getShort("SCALE");          // 标度

                function.addArg(new FunctionArg(argName, originalPosition, argTypeValue, argDataTypeName, argDataTypeValue, length, precision, scale));
            }

            return function.build();
        }
    }

    /**
     * 检查函数是否存在。
     */
    public static boolean checkFunctionExists(Connection conn,
                                              String catalog,
                                              String schema,
                                              String functionName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();

        // Provide the necessary arguments to getFunctions method based on your specific database schema and settings
        ResultSet functions = metaData.getFunctions(catalog, schema, functionName);

        // Check if the ResultSet has any rows
        boolean exists = functions.next();

        // Close the ResultSet
        functions.close();

        return exists;
    }
}
