package xtuer.controller;


import lombok.Data;
import org.springframework.web.bind.annotation.*;
import xtuer.bean.Result;
import xtuer.funcproc.DatabaseType;
import xtuer.funcproc.function.Function;
import xtuer.funcproc.function.FunctionExecutors;
import xtuer.util.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

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
     * 网址: http://localhost:8080/api/functions/functionNames
     * 参数: catalog: 数据库
     *      schema : 模式
     * 测试: curl http://localhost:8080/api/functions/functionNames?catalog=x&schema=y
     *
     * @param catalog 数据库
     * @param schema 模式
     * @return payload 为函数名数组。
     */
    @GetMapping("/api/functions/functionNames")
    public Result<List<String>> findFunctionNames(@RequestParam String catalog, @RequestParam String schema) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            List<String> functionNames = FunctionExecutors.findFunctionNames(conn, catalog, schema);
            return Result.ok(functionNames);
        }
    }

    /**
     * 查找传入函数名的函数。
     *
     * 网址: http://localhost:8080/api/functions/{functionName}
     * 参数: catalog: 数据库
     *      schema : 模式
     * 测试: curl http://localhost:8080/api/functions/func_has_arg_return_base_type?catalog=x&schema=y
     *
     * @param catalog 数据库
     * @param schema 模式
     * @param functionName 函数名。
     * @return payload 为函数对象。
     */
    @GetMapping("/api/functions/{functionName}")
    public Result<Function> findFunction(@RequestParam String catalog, @RequestParam String schema, @PathVariable String functionName) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Function func = FunctionExecutors.findFunction(DB_TYPE, conn, catalog, schema, functionName);
            return Result.ok(func);
        }
    }

    /**
     * 执行函数。
     *
     * 网址: http://localhost:8080/api/functions/execute
     * 参数: 无。
     * 请求体: 函数参数，参数值是数组，如 { "catalog": "x", "schema": "z", "functionName": "z", "functionArguments": [1, 2, "2023-06-12" ] }
     * 测试: curl -X POST http://localhost:8080/api/functions/execute \
     *           --header 'Content-Type: application/json' \
     *           --data '{ "catalog": "", "schema": "", "functionName": "", "functionArguments": [1, 2, "2023-06-12" ] }'
     *
     * @param funcForm 要执行的函数信息。
     * @return payload 为函数执行结果。
     */
    @PostMapping("/api/functions/{functionName}/execute")
    public Result<xtuer.funcproc.Result> executeFunction(@RequestBody FunctionForm funcForm) throws SQLException {
        Utils.dump(funcForm);

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            // 查找函数然后执行。
            Function func = FunctionExecutors.findFunction(DB_TYPE, conn, funcForm.catalog, funcForm.schema, funcForm.functionName);
            xtuer.funcproc.Result ret = FunctionExecutors.executeFunction(DB_TYPE, conn, func, funcForm.functionArguments.toArray());
            return Result.ok(ret);
        }
    }

    // 获取要执行的函数信息。
    @Data
    public static class FunctionForm {
        String catalog;
        String schema;
        String functionName;
        List<Object> functionArguments;
    }
}
