package xtuer.funcproc.procedure;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * 存储过程获取的类。
 */
public class ProcedureFetcher {
    /**
     * 获取存储过程。需要注意的是这里返回的存储过程对象是一个通用的结构，在使用的时候有可能需要转为数据库对应的存储过程型。
     *
     * @param conn 数据库连接。
     * @param catalog 所属 catalog。
     * @param schema 所属 schema。
     * @param procedureName 存储过程的名称。
     * @return 返回获取到的存储过程对象。
     */
    public static Procedure fetchProcedure(Connection conn,
                                           String catalog,
                                           String schema,
                                           String procedureName) throws SQLException {
        Procedure procedure = new Procedure(catalog, schema, procedureName);
        DatabaseMetaData meta = conn.getMetaData();

        try (ResultSet rs = meta.getProcedureColumns(procedure.getCatalog(), procedure.getSchema(), procedure.getName(), null)) {
            while (rs.next()) {
                String argName         = rs.getString("COLUMN_NAME");   // 参数名称
                int argTypeValue       = rs.getInt("COLUMN_TYPE");      // 入参出参:  1 (IN), 2 (INOUT), 3 (OUT), 4 (RETURN)
                int argDataTypeValue   = rs.getInt("DATA_TYPE");        // 参数的数据类型值: SQL type from java.sql.Types
                String argDataTypeName = rs.getString("TYPE_NAME");     // 参数的数据类型名: SQL type name, for a UDT type the type name is fully qualified
                int length             = rs.getInt("LENGTH");           // 长度
                int precision          = rs.getInt("PRECISION");        // 精度
                short scale            = rs.getShort("SCALE");          // 标度
                int originalPosition   = rs.getInt("ORDINAL_POSITION"); // 参数原始位置

                procedure.addArg(new ProcedureArg(argName, originalPosition, argTypeValue, argDataTypeName, argDataTypeValue, length, precision, scale));
            }
        }

        return procedure.build();
    }

    /**
     * 检查存储过程是否存在。
     */
    public static boolean checkProcedureExists(Connection conn,
                                               String catalog,
                                               String schema,
                                               String procedureName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();

        // Provide the necessary arguments to getProcedures method based on your specific database schema and settings
        ResultSet procedures = metaData.getProcedures(catalog, schema, procedureName);

        // Check if the ResultSet has any rows
        boolean exists = procedures.next();

        // Close the ResultSet
        procedures.close();

        return exists;
    }

    /**
     * 例如 MySQL 的 metaData.getProcedures 会同时返回存储过程和存储函数，其中存储函数的 PROCEDURE_TYPE 值为 2。
     */
    private static final int TYPE_FOR_FUNCTION = 2;

    /**
     * 列出 schema 中的所有存储过程名称。
     */
    public static List<String> fetchProcedureNames(Connection conn,
                                                   String catalog,
                                                   String schema) throws SQLException {
        List<String> procedureNames = new LinkedList<>();
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet procedures = metaData.getProcedures(catalog, schema, null);

        while (procedures != null && procedures.next()) {
            int procedureType = procedures.getInt("PROCEDURE_TYPE");
            if (procedureType == TYPE_FOR_FUNCTION) {
                continue;
            }

            procedureNames.add(procedures.getString("PROCEDURE_NAME"));
        }

        return procedureNames;
    }
}