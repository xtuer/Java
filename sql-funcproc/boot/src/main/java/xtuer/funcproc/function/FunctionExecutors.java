package xtuer.funcproc.function;

import lombok.extern.slf4j.Slf4j;
import xtuer.funcproc.DatabaseType;
import xtuer.funcproc.Result;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 函数执行工具类，集中管理数据库和对应的函数执行器。
 * - 查询函数: findFunction()
 * - 执行函数: executeFunction()
 *
 * 提示: 需要判断函数是否存在可调用 FunctionFetcher.checkFunctionExists()
 */
@Slf4j
public final class FunctionExecutors {
    /**
     * 每种数据库对应的函数类型注册表。
     */
    private static final Map<DatabaseType, Class<? extends Function>> DB_FUNCTION_MAP = new HashMap<>();

    /**
     * 每种数据库对应的函数执行器注册表。
     */
    private static final Map<DatabaseType, Class<? extends FunctionExecutor>> DB_EXECUTOR_MAP = new HashMap<>();

    // 注册函数执行器和类型。
    static {
        DB_FUNCTION_MAP.put(DatabaseType.Mysql, MysqlFunction.class);
        DB_EXECUTOR_MAP.put(DatabaseType.Mysql, MysqlFunctionExecutor.class);

        DB_FUNCTION_MAP.put(DatabaseType.Oracle, OracleFunction.class);
        DB_EXECUTOR_MAP.put(DatabaseType.Oracle, OracleFunctionExecutor.class);

        DB_FUNCTION_MAP.put(DatabaseType.Postgres, PostgresFunction.class);
        DB_EXECUTOR_MAP.put(DatabaseType.Postgres, PostgresFunctionExecutor.class);

        DB_FUNCTION_MAP.put(DatabaseType.DB2, DB2Function.class);
        DB_EXECUTOR_MAP.put(DatabaseType.DB2, DB2FunctionExecutor.class);

        DB_FUNCTION_MAP.put(DatabaseType.SqlServer, SqlServerFunction.class);
        DB_EXECUTOR_MAP.put(DatabaseType.SqlServer, SqlServerFunctionExecutor.class);
    }

    /**
     * 获取指定数据库类型的存储函数。
     *
     * @param dbType 数据库类型。
     * @param conn 数据库连接。
     * @param catalog 所属 catalog。
     * @param schema 所属 schema。
     * @param functionName 函数的名称。
     * @return 返回获取到的函数对象。
     */
    public static Function findFunction(DatabaseType dbType,
                                        Connection conn,
                                        String catalog,
                                        String schema,
                                        String functionName) throws SQLException {
        Class<? extends Function> klass = DB_FUNCTION_MAP.get(dbType);
        if (klass == null) {
            log.debug("数据库 [{}] 没有注册函数类型", dbType);
            return null;
        }

        // 把查询得到的函数转为目标数据库使用的函数对象。
        Function func = FunctionFetcher.fetchFunction(conn, catalog, schema, functionName);
        Function specFunc = Function.newFunction(func, klass);

        return specFunc;
    }

    /**
     * 列出 schema 中的所有函数名。
     */
    public static List<String> findFunctionNames(Connection conn,
                                                 String catalog,
                                                 String schema) throws SQLException {
        return FunctionFetcher.fetchFunctionNames(conn, catalog, schema);
    }

    /**
     * 执行存储函数。
     *
     * @param dbType 数据库类型。
     * @param conn 数据库连接。
     * @param func 要执行的函数。
     * @param funcArguments 函数参数。
     * @return 返回执行结果。
     */
    public static Result executeFunction(DatabaseType dbType,
                                         Connection conn,
                                         Function func,
                                         Object ...funcArguments) throws SQLException {
        FunctionExecutor executor = FunctionExecutors.findFunctionExecutor(dbType);
        if (executor == null) {
            throw new RuntimeException(String.format("数据库 [%s] 没有注册函数执行器", dbType));
        }

        return executor.execute(conn, func, funcArguments);
    }

    /**
     * 获取数据库的函数执行器。
     *
     * @param dbType 数据库类型。
     * @return 返回找到的函数执行器。
     */
    public static FunctionExecutor findFunctionExecutor(DatabaseType dbType) {
        Class<? extends FunctionExecutor> klass = DB_EXECUTOR_MAP.get(dbType);
        if (klass == null) {
            log.debug("数据库 [{}] 没有注册函数执行器", dbType);
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
