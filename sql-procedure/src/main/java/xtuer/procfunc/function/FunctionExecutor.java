package xtuer.procfunc.function;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.BasicRowProcessor;
import xtuer.procfunc.Result;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 函数执行器。
 *
 * 具体的数据库有可能需要根据自己的情况实现方法 preCheck, setParameters, getResultSet，参考 PostgresFunctionExecutor 的实现。
 */
@Slf4j
public class FunctionExecutor {
    protected Connection conn;
    protected Function func;
    protected List<?> funcArguments;
    protected CallableStatement cstmt;

    /**
     * 执行存储函数。
     *
     * 1. 获取函数: Function func = FunctionFetcher.fetch(conn, CATALOG, SCHEMA, "func_no_arg_return_base_type");
     * 2. 执行函数:
     *    Function pgFunc = Function.fromFunction(func, PostgresFunction.class); // 把普通的函数转为 Postgres 等特殊数据库的函数对象。
     *    Result result = new PostgresFunctionExecutor().execute(conn, pgFunc, Arrays.asList(1, 2, 3));
     *
     * @param conn 数据库连接。
     * @param func 函数对象。需要注意的是对于 Postgres 等有特殊函数实现的类此 func 必须是其对应的对象如 PostgresFunction 的。
     * @param funcArguments 函数参数。
     * @return 返回执行结果。
     * @throws SQLException 执行或获取函数结果出错时抛出异常。
     */
    public final Result execute(Connection conn, Function func, List<?> funcArguments) throws SQLException {
        /*
         逻辑 (使用 Object 类型设置入参和出参，是因为结果显示给前端使用不需要数据类型参与业务计算):
         1. 创建 CallableStatement。
         2. 设置函数的参数: 入参、出参、入出参。
         3. 执行函数。
         4. 获取函数执行的结果。
         5. 关闭释放资源。
         */

        this.conn = conn;
        this.func = func;
        this.funcArguments = funcArguments;

        // 执行前进行条件校验，例如校验函数对象是否满足要求。
        this.preCheck();

        // 设置 Catalog, schema, transaction。
        conn.setCatalog(func.getCatalog());
        conn.setSchema(func.getSchema());
        conn.setAutoCommit(false);

        // [1] 创建 CallableStatement。
        log.info("执行函数: {}", func.getCallableSql());
        try (CallableStatement cstmt = conn.prepareCall(func.getCallableSql())) {
            this.cstmt = cstmt;

            // [2] 设置函数的参数: 入参、出参、入出参。
            setParameters();

            // [3] 执行函数。
            cstmt.execute();

            // [4] 获取函数执行的结果。
            Result result = getResult();
            conn.commit();

            return result;
        }
    }

    protected void setParameters() throws SQLException {
    }

    /**
     * 获取函数执行的结果。
     */
    private Result getResult() throws SQLException {
        /*
         逻辑:
         1. 获取更新的影响行数 (即使是更新语句，也有可能返回 -1)。
         2. 获取函数执行的输出参数。
         3. 获取函数执行的结果集。
         */

        Result result = new Result();

        // [1] 获取更新的影响行数 (即使是更新语句，也有可能返回 -1)。
        result.setUpdateCount(cstmt.getUpdateCount());

        // [2] 获取函数执行的输出参数。
        getOutParameters(result);

        // [3] 获取函数执行的结果集。
        ResultSet rs = getResultSet();
        handleResultSet(rs, result);

        return result;
    }

    /**
     * 获取函数执行的结果集。
     *
     * 注意: 不同的数据库可能不一样。
     */
    protected ResultSet getResultSet() throws SQLException {
        return cstmt.getResultSet();
    }

    /**
     * 获取输出参数。
     */
    protected void getOutParameters(Result result) throws SQLException {

    }

    /**
     * 处理结果集。
     */
    private void handleResultSet(ResultSet rs, Result result) throws SQLException {
        if (rs == null) {
            return;
        }

        BasicRowProcessor rowProcessor = new BasicRowProcessor();
        while (rs.next()) {
            result.getRows().add(rowProcessor.toMap(rs));
        }
        rs.close();
    }

    /**
     * 执行前进行条件校验，例如校验函数对象是否满足要求，校验不通过抛出运行时异常。
     */
    protected void preCheck() {

    }
}
