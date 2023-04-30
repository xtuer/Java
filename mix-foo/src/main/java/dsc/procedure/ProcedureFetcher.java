package dsc.procedure;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 存储过程获取类。
 */
public class ProcedureFetcher {
    /**
     * 获取存储过程。
     *
     * @param conn 数据库连接。
     * @param catalog 所属 catalog。
     * @param schema 所属 schema。
     * @param procedureName 存储过程的名称。
     * @return 返回获取到的存储过程对象。
     */
    public static Procedure fetch(Connection conn, String catalog, String schema, String procedureName) throws SQLException {
        Procedure procedure = new Procedure(catalog, schema, procedureName);
        DatabaseMetaData meta = conn.getMetaData();

        try (ResultSet rs = meta.getProcedureColumns(procedure.getCatalog(), procedure.getSchema(), procedure.getName(), null)) {
            while (rs.next()) {
                String argName          = rs.getString("COLUMN_NAME"); // 参数名称
                int    argTypeValue     = rs.getInt("COLUMN_TYPE");    // 入参出参:  1 (IN), 4 (OUT), 2 (INOUT)
                int    argDataTypeValue = rs.getInt("DATA_TYPE");      // 参数的数据类型值: SQL type from java.sql.Types
                String argDataTypeName  = rs.getString("TYPE_NAME");   // 参数的数据类型名: SQL type name, for a UDT type the type name is fully qualified
                int    length           = rs.getInt("LENGTH");         // 长度
                int    precision        = rs.getInt("PRECISION");      // 精度
                short  scale            = rs.getShort("SCALE");        // 标度

                procedure.addArg(new ProcedureArg(argName, argTypeValue, argDataTypeName, argDataTypeValue, length, precision, scale));
                // Utils.dump(new BasicRowProcessor().toMap(rs));
            }

            return procedure;
        }
    }
}
