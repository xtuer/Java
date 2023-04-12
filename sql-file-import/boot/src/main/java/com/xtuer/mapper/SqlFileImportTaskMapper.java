package com.xtuer.mapper;

import com.xtuer.bean.SqlFileImportTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 导入 SQL 文件任务的 Mapper。
 */
@Mapper
public interface SqlFileImportTaskMapper {
    /**
     * 查询传入的导入任务 ID 的导入任务。
     *
     * @param importTaskId 导入任务的 ID。
     * @return 返回查询到的导入任务。
     */
    SqlFileImportTask findImportTaskById(String importTaskId);

    /**
     * 创建 SQL 文件导入任务。
     *
     * @param task 导入任务。
     */
    void createImportTask(SqlFileImportTask task);

    /**
     * 开始 SQL 文件导入任务。
     *
     * @param task 导入任务。
     */
    void startImportTask(SqlFileImportTask task);

    /**
     * 更新 SQL 文件导入进度。
     *
     * @param task 导入任务。
     */
    void updateImportProcess(SqlFileImportTask task);

    /**
     * 结束 SQL 文件导入任务。
     *
     * @param task 导入任务。
     */
    void endImportTask(SqlFileImportTask task);
}
