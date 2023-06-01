package xtuer.procfunc.function;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 函数执行器注册表，集中管理数据库和对应的函数执行器。
 */
@Slf4j
public class FunctionExecutorRegistry {
    public enum DatabaseType {
        MySQL,
        Postgres,
    }

    // 函数执行器注册表，保存每种数据库使用的执行器。
    private static final Map<DatabaseType, Class<? extends FunctionExecutor>> executors = new HashMap<>();

    static {
        // 注册函数执行器。
        executors.put(DatabaseType.MySQL, SimpleFunctionExecutor.class);
        executors.put(DatabaseType.Postgres, PostgresFunctionExecutor.class);
    }

    /**
     * 查询数据库的函数执行器。
     *
     * @param type 数据库类型。
     * @return 返回找到的函数执行器。
     */
    public static FunctionExecutor findExecutor(DatabaseType type) {
        Class<? extends FunctionExecutor> klass = executors.get(type);

        if (klass != null) {
            try {
                return klass.getConstructor().newInstance();
            } catch (Exception ex) {
                log.warn(ex.getMessage());
            }
        } else {
            log.warn("数据库 [{}] 没有注册函数执行器", type);
        }

        return null;
    }
}
