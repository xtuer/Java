package com.xtuer.controller;

import com.xtuer.bean.Result;
import com.xtuer.bean.SqlFileImportTask;
import com.xtuer.bean.Urls;
import com.xtuer.util.SqlImporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * SQL 执行服务 SQL 文件导入控制器。
 */
@RestController
public class ExecSqlFileImportController {
    @Value("${sqlImport.lineCount:100}")
    private int lineCount;

    @Value("${sqlImport.batchSize:200}")
    private int batchSize;

    /**
     * 保存文件的目录。
     * 文件保格式为: {dstDir}/{fileUid}
     */
    @Value("${ufile.dstDir}")
    private String dstDir;

    static final String JDBC_URL = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false";
    static final String DB_USER   = "root";
    static final String DB_PASS   = "root";

    /**
     * 导入 SQL 文件。
     *
     * 网址: http://localhost:8080/api/exec/sql-file-imports
     * 参数: 无
     * 请求体: {"id": 123, "fileUid": "xxx"}
     * 测试: curl -X POST http://localhost:8080/api/exec/sql-file-imports -d '{"id": 123, "fileUid": "xxx"}' -H 'Content-Type: application/json'
     *
     * @param task 上传文件得到的唯一 ID。
     * @return payload 为导入任务对象。
     */
    @PostMapping(Urls.API_SQL_FILE_IMPORTS_EXEC)
    public Result<SqlFileImportTask> importSqlFile(@RequestBody SqlFileImportTask task) {
        // 开启线程导入。
        new Thread(() -> {
            String sqlFilePath = dstDir + "/" + task.getFileUid();
            SqlImporter importer = new SqlImporter(JDBC_URL, DB_USER, DB_PASS, task.getTaskId(), sqlFilePath, task.getRollbackSql(), batchSize, lineCount);
            importer.importSqlFile();
        }).start();

        return Result.ok(task);
    }
}
