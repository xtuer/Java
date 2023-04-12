package com.xtuer.controller;

import com.xtuer.bean.Result;
import com.xtuer.bean.SqlFileImportTask;
import com.xtuer.bean.UFileConst;
import com.xtuer.bean.Urls;
import com.xtuer.mapper.SqlFileImportTaskMapper;
import com.xtuer.util.RequestUtils;
import com.xtuer.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * DSC 导入 SQL 文件的控制器。
 */
@RestController
@Slf4j
public class DscSqlFileImportController {
    @Value("${server.port}")
    private int port;

    @Autowired
    private SqlFileImportTaskMapper taskMapper;

    /**
     * 查询传入的导入任务 ID 的导入任务。
     *
     * 网址: http://localhost:8080/api/dsc/sql-file-imports/{importTaskId}
     * 参数: withRollbackSql [可选]: 是否需要回滚 SQL。
     * 测试: curl 'http://localhost:8080/api/dsc/sql-file-imports/d00dfaa7-6c44-4b26-a057-1f6e7f8015c5?withRollbackSql=false'
     *
     * @param importTaskId 导入任务 ID。
     * @param withRollbackSql 为 true 时响应中有回滚 SQL，为 false 没有。
     * @return 查询到时 payload 为导入任务对象。
     */
    @GetMapping(Urls.API_SQL_FILE_IMPORTS_BY_ID)
    public Result<SqlFileImportTask> findImportTask(@PathVariable String importTaskId,
                                                    @RequestParam(required = false, defaultValue = "true") boolean withRollbackSql) {
        SqlFileImportTask task = taskMapper.findImportTaskById(importTaskId);

        if (task != null && !withRollbackSql) {
            task.setRollbackSql("");
        }

        return Result.single(task, "SQL 文件导入任务不存在");
    }

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
        task.setTaskId(Utils.uuid());
        task.setFileUid(fileUid);
        task.setState(UFileConst.INIT);
        task.setRollbackSql(paramTask.getRollbackSql());

        // [2] 保存导入任务到数据库。
        taskMapper.createImportTask(task);

        // [3] 调用 SQL 执行服务执行具体的导入。
        try {
            // TODO: 调用 SQL 执行。
            String url = "http://localhost:" + port + Urls.API_SQL_FILE_IMPORTS_EXEC;
            RequestUtils.doRequest(url, HttpMethod.POST, task, SqlFileImportTask.class);
        } catch (Exception e) {
            String error = Utils.getStackTrace(e);
            log.warn(error);

            // 调用 SQL 执行出错，结束导入任务。
            task.setState(UFileConst.FAILED);
            task.setError(error);
            taskMapper.endImportTask(task);

            return Result.fail("调用 SQL 执行错误: " + e.getMessage());
        }


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

        if (task.getCommittedBytes() == task.getTotalBytes() && task.getCommittedBytes() != 0) {
            task.setState(UFileConst.SUCCESS);
        }

        if (StringUtils.hasText(task.getError())) {
            task.setState(UFileConst.FAILED);
        }

        // 保存进度到数据库。
        if (task.getState() == UFileConst.SUCCESS || task.getState() == UFileConst.FAILED) {
            task.setEndTime(new Date()); // 完成时间。
            taskMapper.endImportTask(task);
        } else {
            taskMapper.updateImportProcess(task);
        }

        return Result.ok();
    }
}
