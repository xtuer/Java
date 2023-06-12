package xtuer.controller;


import org.springframework.web.bind.annotation.*;
import xtuer.bean.Result;
import xtuer.funcproc.DatabaseType;
import xtuer.funcproc.function.Function;
import xtuer.funcproc.function.FunctionExecutors;
import xtuer.funcproc.function.FunctionFetcher;
import xtuer.util.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 函数的控制器。
 */
@RestController
public class FunctionController {
    // PG 连接。
    static final String DB_URL  = "jdbc:postgresql://192.168.12.19:33005/postgres";
    static final String USER    = "postgres";
    static final String PASS    = "123456";
    static final String CATALOG = "postgres";
    static final String SCHEMA  = "biao";
    static final DatabaseType DB_TYPE = DatabaseType.Postgres;

    /**
     * 列出 schema 中的所有函数名。
     *
     * 网址: http://localhost:8080/api/functionNames
     * 参数: 无
     * 测试: curl http://localhost:8080/api/functionNames
     * @return payload 为函数名。
     */
    @GetMapping("/api/functionNames")
    public Result<List<String>> listFunctionNames() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            List<String> functionNames = FunctionFetcher.listFunctionNames(conn, CATALOG, SCHEMA);
            return Result.ok(functionNames);
        }
    }

    /**
     * 查找传入函数名的函数。
     *
     * 网址: http://localhost:8080/api/functions/{functionName}
     * 参数: 无
     * 测试: curl http://localhost:8080/api/functions/func_has_arg_return_base_type
     *
     * @param functionName 函数名。
     * @return payload 为函数对象的 JSON。
     */
    @GetMapping("/api/functions/{functionName}")
    public Result<Function> findFunctionByName(@PathVariable String functionName) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Function func = FunctionExecutors.findFunction(DB_TYPE, conn, CATALOG, SCHEMA, functionName);
            return Result.ok(func);
        }
    }

    /**
     * 执行函数。
     *
     * 网址: http://localhost:8080/api/functions/{functionName}/execute
     * 参数: 无。
     * 请求体: 函数参数，参数值是数组，如 { "funcArguments": [1, 2, "2023-06-12" ] }
     * 测试: curl -X POST http://localhost:8080/api/functions/func_has_arg_return_base_type/execute  --data '{ "funcArguments": [1, 2, "2023-06-12" ] }' --header 'Content-Type: application/json'
     *
     * @param functionName 函数名。
     * @param jsonMap Json 格式的函数参数。
     * @return payload 为函数执行结果。
     */
    @PostMapping("/api/functions/{functionName}/execute")
    public Result<xtuer.funcproc.Result> executeFunction(@PathVariable String functionName, @RequestBody Map<String, List<Object>> jsonMap) throws SQLException {
        Utils.dump(jsonMap);

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            List<Object> funcArguments = jsonMap.get("funcArguments");
            Function func = FunctionExecutors.findFunction(DB_TYPE, conn, CATALOG, SCHEMA, functionName);
            xtuer.funcproc.Result ret = FunctionExecutors.executeFunction(DB_TYPE, conn, func, funcArguments.toArray());
            return Result.ok(ret);
        }
    }
}
