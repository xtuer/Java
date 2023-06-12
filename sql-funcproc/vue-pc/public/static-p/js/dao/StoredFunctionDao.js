/**
 * 存储函数的 Dao
 */
export default class StoredFunctionDao {
    /**
     * 列出 schema 中的所有函数名。
     *
     * 网址: http://localhost:8080/api/functionNames
     * 参数: 无
     * 测试: curl http://localhost:8080/api/functionNames
     * @return {Promise} 返回 Promise 对象，resolve 的参数为函数名数组，reject 的参数为错误信息。
     */
    static listFunctionNames() {
        return Rest.url('/api/functionNames')
            .get()
            .then(({ data: functionNames, success, message }) => {
                return Utils.response(functionNames, success, message);
            });
    }

    /**
     * 查找传入函数名的函数。
     *
     * 网址: http://localhost:8080/api/functions/{functionName}
     * 参数: 无
     * 测试: curl http://localhost:8080/api/functions/func_has_arg_return_base_type
     *
     * @param functionName 函数名。
     * @return {Promise} 返回 Promise 对象，resolve 的参数为函数名数组，reject 的参数为错误信息。
     */
    static findFunction(functionName) {
        return Rest.url('/api/functions/{functionName}')
            .params({ functionName })
            .get()
            .then(({ data: func, success, message }) => {
                return Utils.response(func, success, message);
            });
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
     * @param funcArguments 函数参数。
     * @return {Promise} 返回 Promise 对象，resolve 的参数为函数执行结果，reject 的参数为错误信息。
     */
    static executeFunction(functionName, funcArguments) {
        return Rest.url('/api/functions/{functionName}/execute')
            .params({ functionName })
            .data({ funcArguments })
            .json(true)
            .create()
            .then(({ data: funcResult, success, message }) => {
                return Utils.response(funcResult, success, message);
            });
    }
}
