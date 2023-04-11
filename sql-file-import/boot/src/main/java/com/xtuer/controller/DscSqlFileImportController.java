package com.xtuer.controller;

import com.xtuer.bean.Result;
import com.xtuer.bean.SqlFileImportTask;
import com.xtuer.bean.UFileConst;
import com.xtuer.bean.Urls;
import com.xtuer.mapper.SqlFileImportTaskMapper;
import com.xtuer.util.RequestUtils;
import com.xtuer.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * DSC 导入 SQL 文件的控制器。
 */
@RestController
public class DscSqlFileImportController {
    @Value("${server.port}")
    private int port;

    @Autowired
    private SqlFileImportTaskMapper taskMapper;

    /**
     * 导入 SQL 文件。
     *
     * 网址: http://localhost:8080/api/dsc/sql-file-imports
     * 参数: 无
     * 请求体: {"fileUid": "xxx"}
     * 测试: curl -X POST http://localhost:8080/api/dsc/sql-file-imports -d '{"fileUid": "xxx"}' -H 'Content-Type: application/json'
     *
     * @param paramTask 上传文件得到的唯一 ID。
     * @return payload 为导入任务对象。
     */
    @PostMapping(Urls.API_SQL_FILE_IMPORTS)
    public Result<SqlFileImportTask> importSqlFile(@RequestBody SqlFileImportTask paramTask) {
        /*
         逻辑:
         1. 创建文件导入任务。
         2. 保存导入任务到数据库。
         3. 调用 SQL 执行服务执行具体的导入。
         4. 更新导入任务到数据库: 更新 startTime, totalBytes, state 为导入中。
         */

        String fileUid = StringUtils.trimWhitespace(paramTask.getFileUid());

        if (!StringUtils.hasLength(fileUid)) {
            throw new RuntimeException("导入 SQL 问的 fileUid 不能为空");
        }

        // [1] 创建文件导入任务。
        SqlFileImportTask task = new SqlFileImportTask();
        task.setTaskId(System.currentTimeMillis());
        task.setFileUid(fileUid);
        task.setState(UFileConst.INIT);
        task.setRollbackSql(paramTask.getRollbackSql());

        // [2] 保存导入任务到数据库。
        taskMapper.createImportTask(task);

        // [3] 调用 SQL 执行服务执行具体的导入。
        String url = "http://localhost:" + port + Urls.API_SQL_FILE_IMPORTS_EXEC;
        Result<SqlFileImportTask> rsp = RequestUtils.doRequest(url, HttpMethod.POST, task, SqlFileImportTask.class);
        Utils.dump(rsp);

        // [4] 更新导入任务到数据库: 更新 startTime, totalBytes, state 为导入中。
        task.setStartTime(new Date());
        task.setState(UFileConst.DOING);
        taskMapper.startImportTask(task);

        return Result.ok(task);
    }

    @PostMapping(Urls.API_SQL_FILE_IMPORTS_PROCESS)
    public Result<Boolean> updateImportProcess(@RequestBody SqlFileImportTask task) {
        /*
         逻辑:
         执行结束的情况: error 不为空或者 committedBytes == totalBytes
         */

        if (task.getCommittedBytes() == task.getTotalBytes()) {
            task.setState(UFileConst.SUCCESS);
        }

        if (StringUtils.hasText(task.getError())) {
            task.setState(UFileConst.FAILED);
        }

        // 保存进度到数据库。
        if (task.getState() == UFileConst.SUCCESS || task.getState() == UFileConst.FAILED) {
            taskMapper.endImportTask(task);
        } else {
            taskMapper.updateImportProcess(task);
        }

        return Result.ok();
    }
}
