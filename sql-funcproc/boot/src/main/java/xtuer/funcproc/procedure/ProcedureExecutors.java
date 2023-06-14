package xtuer.funcproc.procedure;

import lombok.extern.slf4j.Slf4j;
import xtuer.funcproc.DatabaseType;
import xtuer.funcproc.Result;

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
@Slf4j
public final class ProcedureExecutors {
    /**
     * 每种数据库对应的存储过程类型注册表。
     */
    private static final Map<DatabaseType, Class<? extends Procedure>> DB_PROCEDURE_MAP = new HashMap<>();

    /**
     * 每种数据库对应的存储过程执行器注册表。
     */
    private static final Map<DatabaseType, Class<? extends ProcedureExecutor>> DB_EXECUTOR_MAP = new HashMap<>();

    // 注册存储过程执行器和类型。
    static {
        DB_PROCEDURE_MAP.put(DatabaseType.Mysql, MysqlProcedure.class);
        DB_EXECUTOR_MAP.put(DatabaseType.Mysql, MysqlProcedureExecutor.class);
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
            log.debug("数据库 [{}] 没有注册存储过程类型", dbType);
            return null;
        }

        // 把查询得到的函数转为目标数据库使用的函数对象。
        Procedure proc = ProcedureFetcher.fetchProcedure(conn, catalog, schema, procedureName);
        Procedure specProc = Procedure.newProcedure(proc, klass);

        return specProc;
    }

    /**
     * 列出 schema 中的所有存储过程名。
     */
    public static List<String> findProcedureNames(Connection conn,
                                                  String catalog,
                                                  String schema) throws SQLException {
        return ProcedureFetcher.fetchProcedureNames(conn, catalog, schema);
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
        ProcedureExecutor executor = ProcedureExecutors.findProcedureExecutor(dbType);
        if (executor == null) {
            throw new RuntimeException(String.format("数据库 [%s] 没有注册存储过程执行器", dbType));
        }

        return executor.execute(conn, proc, funcArguments);
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
            log.debug("数据库 [{}] 没有注册存储过程执行器", dbType);
            return null;
        }

        try {
            return klass.getConstructor().newInstance();
        } catch (Exception ex) {
            log.warn(ex.getMessage());
        }

        return null;
    }
}
