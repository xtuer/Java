package xtuer.sp.function;

import org.apache.commons.dbutils.BasicRowProcessor;
import xtuer.sp.FuncProcUtils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 函数获取类。
 */
public class FunctionFetcher {
    /**
     * DB2 的 Metadata 里没有 COLUMN_NAME 和 COLUMN_TYPE，对应的是 PARAMETER_NAME 和 PARAMETER_TYPE。
     */
    private static final String PARAMETER_NAME_FOR_DB2 = "PARAMETER_NAME";

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
        conn.setCatalog(catalog);
        conn.setSchema(schema);

        Function function = new Function(catalog, schema, functionName);
        DatabaseMetaData meta = conn.getMetaData();

        try (ResultSet rs = meta.getFunctionColumns(function.getCatalog(), function.getSchema(), function.getName(), null)) {
            // DB2 比较特殊，没有按照 JDBC 规范实现。
            boolean forDb2 = FuncProcUtils.hasColumn(rs, PARAMETER_NAME_FOR_DB2);
            String argTypeNameLabel  = forDb2 ? "PARAMETER_NAME" : "COLUMN_NAME";
            String argTypeValueLabel = forDb2 ? "PARAMETER_TYPE" : "COLUMN_TYPE";

            while (rs.next()) {
                String argName         = rs.getString(argTypeNameLabel); // 参数名称
                int argTypeValue       = rs.getInt(argTypeValueLabel);  // 入参出参:  1 (IN), 2 (INOUT), 3 (OUT), 4 (RETURN)
                int argDataTypeValue   = rs.getInt("DATA_TYPE");        // 参数的数据类型值: SQL type from java.sql.Types
                String argDataTypeName = rs.getString("TYPE_NAME");     // 参数的数据类型名: SQL type name, for a UDT type the type name is fully qualified
                int length             = rs.getInt("LENGTH");           // 长度
                int precision          = rs.getInt("PRECISION");        // 精度
                short scale            = rs.getShort("SCALE");          // 标度
                int originalPosition   = rs.getInt("ORDINAL_POSITION"); // 参数原始位置

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
        conn.setCatalog(catalog);
        conn.setSchema(schema);

        DatabaseMetaData metaData = conn.getMetaData();

        // Provide the necessary arguments to getFunctions method based on your specific database schema and settings
        ResultSet functions = metaData.getFunctions(catalog, schema, functionName);

        // Check if the ResultSet has any rows
        boolean exists = functions.next();

        // Close the ResultSet
        functions.close();

        return exists;
    }

    /**
     * 列出 schema 中的所有函数名。
     */
    public static List<String> fetchFunctionNames(Connection conn,
                                                  String catalog,
                                                  String schema) throws SQLException {
        if (catalog != null) {
            conn.setCatalog(catalog);
        }
        if (schema != null) {
            conn.setSchema(schema);
        }

        List<String> functionNames = new LinkedList<>();
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet functions = metaData.getFunctions(catalog, schema, null);
        System.out.println(metaData.getDatabaseMajorVersion() + "." + metaData.getDatabaseMinorVersion());
        while (functions != null && functions.next()) {
            functionNames.add(functions.getString("FUNCTION_NAME"));

            Map<String, Object> rowMap = new BasicRowProcessor().toMap(functions);
            System.out.println(rowMap);
        }

        return functionNames;
    }
}
