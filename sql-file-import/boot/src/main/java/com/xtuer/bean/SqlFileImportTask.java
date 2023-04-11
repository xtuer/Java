package com.xtuer.bean;

import lombok.Data;

import java.util.Date;

/**
 * 导入 SQL 文件任务。
 */
@Data
public class SqlFileImportTask {
    /**
     * SQL 文件导入任务 ID。
     */
    private long taskId;

    /**
     * SQL 文件 UID。
     */
    private String fileUid;

    /**
     * 导入状态: 0 (初始化)、1 (成功)、2 (失败)、3 (导入中)。
     */
    private int state;

    /**
     * 导入开始时间。
     */
    private Date startTime;

    /**
     * 导入结束时间。
     */
    private Date endTime;

    /**
     * SQL 文件的大小。
     */
    private long totalBytes;

    /**
     * 已导入大小 (计算进度)。
     */
    private long committedBytes;

    /**
     * 发生错误时回滚的 SQL 语句。
     */
    private String rollbackSql;

    /**
     * 发生错误的错误信息。
     */
    private String error;
}
