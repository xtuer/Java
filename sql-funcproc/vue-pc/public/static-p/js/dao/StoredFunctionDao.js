/**
 * 存储函数的 Dao
 */
export default class StoredFunctionDao {
    /**
     * 列出 schema 中的所有函数名。
     *
     * 网址: http://localhost:8080/api/functions/functionNames
     * 参数: catalog: 数据库
     *      schema : 模式
     * 测试: curl http://localhost:8080/api/functions/functionNames?catalog=x&schema=y
     *
     * @param catalog 数据库名。
     * @param schema 模式名。
     * @return {Promise} 返回 Promise 对象，resolve 的参数为函数名数组，reject 的参数为错误信息。
     */
    static listFunctionNames(catalog, schema) {
        return Rest.url('/api/functions/functionNames')
            .data({ catalog, schema })
            .get()
            .then(({ data: functionNames, success, message }) => {
                return Utils.response(functionNames, success, message);
            });
    }

    /**
     * 查找传入函数名的函数。
     *
     * 网址: http://localhost:8080/api/functions/{functionName}
     * 参数: catalog: 数据库
     *      schema : 模式
     * 测试: curl http://localhost:8080/api/functions/func_has_arg_return_base_type?catalog=x&schema=y
     *
     * @param catalog 数据库。
     * @param schema 模式。
     * @param functionName 函数名。
     * @return {Promise} 返回 Promise 对象，resolve 的参数为函数名数组，reject 的参数为错误信息。
     */
    static findFunction(catalog, schema, functionName) {
        return Rest.url('/api/functions/{functionName}')
            .params({ functionName })
            .data({ catalog, schema })
            .get()
            .then(({ data: func, success, message }) => {
                return Utils.response(func, success, message);
            });
    }

    /**
     * 执行函数。
     *
     * 网址: http://localhost:8080/api/functions/execute
     * 参数: 无。
     * 请求体: 函数参数，参数值是数组，如 { "catalog": "x", "schema": "z", "functionName": "z", "funcArguments": [1, 2, "2023-06-12" ] }
     * 测试: curl -X POST http://localhost:8080/api/functions/execute \
     *           --header 'Content-Type: application/json' \
     *           --data '{ "catalog": "", "schema": "", "functionName": "", "funcArguments": [1, 2, "2023-06-12" ] }'
     *
     * @param funcForm 要执行的函数信息: { catalog, schema, functionName, functionArguments }
     * @return {Promise} 返回 Promise 对象，resolve 的参数为函数执行结果，reject 的参数为错误信息。
     */
    static executeFunction(funcForm) {
        return Rest.url('/api/functions/{functionName}/execute')
            .data(funcForm)
            .json(true)
            .create()
            .then(({ data: funcResult, success, message }) => {
                return Utils.response(funcResult, success, message);
            });
    }
}
