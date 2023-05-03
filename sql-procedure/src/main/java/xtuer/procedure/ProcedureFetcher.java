package xtuer.procedure;

import xtuer.util.Utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 存储过程获取类。
 */
public class ProcedureFetcher {
    private static final boolean DEBUG = true;

    /**
     * 获取存储过程，存储过程执行时使用花括号 {}。
     *
     * @return 返回获取到的存储过程对象。
     */
    public static Procedure fetch(Connection conn,
                                  String catalog,
                                  String schema,
                                  String procedureName) throws SQLException {
        return fetch(conn, catalog, schema, procedureName, true);
    }

    /**
     * 获取存储过程。
     *
     * @param conn 数据库连接。
     * @param catalog 所属 catalog。
     * @param schema 所属 schema。
     * @param procedureName 存储过程的名称。
     * @param useCurlyBrace 存储过程执行时是否使用花括号 {}。
     * @return 返回获取到的存储过程对象。
     */
    public static Procedure fetch(Connection conn,
                                  String catalog,
                                  String schema,
                                  String procedureName,
                                  boolean useCurlyBrace) throws SQLException {
        Procedure procedure = new Procedure(catalog, schema, procedureName, useCurlyBrace);
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

                if (DEBUG) {
                    Utils.dump(rs);
                }

                // [*] Postgres 的返回类型需要过滤 (存储过程没有返回值，因为 Postgres 本质没有存储过程，而是当函数使用，所以获取的参数类型里多了个返回值)。
                if (argTypeValue == ProcedureArg.TYPE_RETURN) {
                    continue;
                }

                procedure.addArg(new ProcedureArg(argName, argTypeValue, argDataTypeName, argDataTypeValue, length, precision, scale));
            }

            return procedure;
        }
    }
}
