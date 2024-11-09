package xtuer.sp.procedure;

import xtuer.sp.DatabaseType;
import xtuer.sp.FuncProcUtils;
import xtuer.sp.Result;
import xtuer.sp.procedure.spec.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 存储过程执行工具类，集中管理数据库和对应的存储过程执行器。
 * - 查询存储过程: findProcedure()
 * - 执行存储过程: executeProcedure()
 *
 * 提示: 需要判断存储过程是否存在可调用 ProcedureFetcher.checkProcedureExists()
 */
public final class ProcedureExecutors {
    /**
     * 每种数据库对应的存储过程类型注册表。
     */
    private static final Map<DatabaseType, Class<? extends Procedure>> DB_PROCEDURE_MAP = new HashMap<>();

    /**
     * 每种数据库对应的存储过程执行器注册表。
     */
    private static final Map<DatabaseType, Class<? extends ProcedureExecutor>> DB_EXECUTOR_MAP = new HashMap<>();

    private static final Map<DatabaseType, Integer> DB_PROCEDURE_TYPE = new HashMap<>();

    // 注册存储过程执行器和类型。
    static {
        register(DatabaseType.DB2, DB2Procedure.class, DB2ProcedureExecutor.class, 1);
        register(DatabaseType.MYSQL, MysqlProcedure.class, MysqlProcedureExecutor.class, 1);
        register(DatabaseType.GBASE8A, MysqlProcedure.class, MysqlProcedureExecutor.class, 1);
        register(DatabaseType.ORACLE, OracleProcedure.class, OracleProcedureExecutor.class, 1);
        register(DatabaseType.POSTGRES, PostgresProcedure.class, PostgresProcedureExecutor.class, 2);
        register(DatabaseType.SQLSERVER, SqlServerProcedure.class, SqlServerProcedureExecutor.class, 2);
    }

    /**
     * 获取指定数据库类型的存储过程。
     *
     * @param dbType 数据库类型。
     * @param conn 数据库连接。
     * @param catalog 所属 catalog。
     * @param schema 所属 schema。
     * @param procedureName 存储过程的名称。
     * @return 返回获取到的存储过程对象。
     */
    public static Procedure findProcedure(DatabaseType dbType,
                                          Connection conn,
                                          String catalog,
                                          String schema,
                                          String procedureName) throws SQLException {
        Class<? extends Procedure> klass = DB_PROCEDURE_MAP.get(dbType);
        if (klass == null) {
            throw new RuntimeException(String.format("数据库 [%s] 没有注册存储过程类型", dbType));
        }

        // 把查询得到的函数转为目标数据库使用的函数对象。
        Procedure proc = ProcedureFetcher.fetchProcedure(conn, catalog, schema, procedureName);
        Procedure specProc = Procedure.newProcedure(proc, klass);

        return specProc;
    }

    /**
     * 列出 schema 中的所有存储过程名。
     */
    public static List<String> findProcedureNames(DatabaseType dbType,
                                                  Connection conn,
                                                  String catalog,
                                                  String schema) throws SQLException {
        int procedureType = DB_PROCEDURE_TYPE.get(dbType);
        List<String> procNames = ProcedureFetcher.fetchProcedureNames(conn, catalog, schema, procedureType);

        if (DatabaseType.SQLSERVER.equals(dbType)) {
            procNames = FuncProcUtils.extractProcedureNamesForSqlServer(procNames);
        }

        return procNames;
    }

    /**
     * 执行存储过程。
     *
     * @param dbType 数据库类型。
     * @param conn 数据库连接。
     * @param proc 要执行的存储过程。
     * @param funcArguments 存储过程参数。
     * @return 返回执行结果。
     */
    public static Result executeProcedure(DatabaseType dbType,
                                          Connection conn,
                                          Procedure proc,
                                          Object ...funcArguments) throws SQLException {
        return ProcedureExecutors.findProcedureExecutor(dbType).execute(conn, proc, funcArguments);
    }

    /**
     * 获取数据库的存储过程执行器。
     *
     * @param dbType 数据库类型。
     * @return 返回找到的存储过程执行器。
     */
    public static ProcedureExecutor findProcedureExecutor(DatabaseType dbType) {
        Class<? extends ProcedureExecutor> klass = DB_EXECUTOR_MAP.get(dbType);
        if (klass == null) {
            throw new RuntimeException(String.format("数据库 [%s] 没有注册存储过程执行器", dbType));
        }

        try {
            return klass.getConstructor().newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 注册数据库的存储过程、存储过程执行器以及使用 DatabaseMetaData 获取存储过程时 PROCEDURE_TYPE 的值。
     */
    public static void register(DatabaseType dbType, Class<? extends Procedure> procedureClass, Class<? extends ProcedureExecutor> executorClass, int procedureType) {
        DB_PROCEDURE_MAP.put(dbType, procedureClass);
        DB_EXECUTOR_MAP.put(dbType, executorClass);
        DB_PROCEDURE_TYPE.put(dbType, procedureType);
    }
}
