const API_SQL_FILE_IMPORTS         = '/api/dsc/sql-file-imports';  // DSC 文件导入
const API_SQL_FILE_IMPORTS_BY_ID   = '/api/dsc/sql-file-imports/{importTaskId}?withRollbackSql={withRollbackSql}'; // DSC 文件导入查询

export default class SqlFileImportApi {
    /**
     * 导入 SQL 文件。
     *
     * 网址: http://localhost:8080/api/dsc/sql-file-imports
     * 参数: 无
     * 请求体: {"fileUid": "xxx"}
     * 测试: curl -X POST http://localhost:8080/api/dsc/sql-file-imports -d '{"fileUid": "xxx"}' -H 'Content-Type: application/json'
     *
     * @param fileUid 上传文件得到的唯一 ID。
     * @param rollbackSql 发生错误时回滚的 SQL 语句。
     * @returns 返回 Promise, resolve 的参数为导入任务对象, reject 的参数为错误信息。
     */
    static importSqlFile(fileUid, rollbackSql) {
        return new Promise((resolve, reject) => {
            const json = JSON.stringify({ fileUid, rollbackSql });

            axios.post(API_SQL_FILE_IMPORTS, json, { headers: { 'Content-Type': 'application/json' } }).then(({ data: rsp }) => {
                if (rsp.success) {
                    let task = rsp.data;
                    resolve(task);
                } else {
                    reject(rsp.message);
                }
            }).catch(err => {
                reject(err);
            });
        });
    }

    /**
     * 查询传入的导入任务 ID 的导入任务。
     *
     * 网址: http://localhost:8080/api/dsc/sql-file-imports/{importTaskId}
     * 参数: withRollbackSql [可选]: 是否需要回滚 SQL。
     * 测试: curl 'http://localhost:8080/api/dsc/sql-file-imports/d00dfaa7-6c44-4b26-a057-1f6e7f8015c5?withRollbackSql=false'
     *
     * @param importTaskId 导入任务 ID。
     * @param withRollbackSql 为 true 时响应中有回滚 SQL，为 false 没有。
     * @returns 返回 Promise, resolve 的参数为导入任务对象, reject 的参数为错误信息。
     */
    static findImportTask(importTaskId, withRollbackSql) {
        return new Promise((resolve, reject) => {
            const url = API_SQL_FILE_IMPORTS_BY_ID.replace('{importTaskId}', importTaskId).replace('{withRollbackSql}', withRollbackSql);
            axios.get(url).then(({ data: rsp }) => {
                if (rsp.success) {
                    let task = rsp.data;
                    resolve(task);
                } else {
                    reject(rsp.message);
                }
            }).catch(err => {
                reject(err);
            });
        });
    }
}
