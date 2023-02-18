package misc.auto.ndtagent;

public interface AgentUrls {
    /**
     * 执行命令。
     */
    String API_JOBS_EXECUTE_CMD = "http://${ip}:${port}/api/jobs/execute/cmd";

    /**
     * 执行脚本。
     */
    String API_JOBS_EXECUTE_SCRIPT = "http://${ip}:${port}/api/jobs/execute/script";

    /**
     * 指定 ID 的任务。
     */
    String API_JOBS_BY_ID = "http://${ip}:${port}/api/jobs/${jobId}";

    /**
     * 内存分配状态。
     */
    String API_MEM_STATS = "http://${ip}:${port}/api/memStats";

    /**
     * 上传信息。
     */
    String API_UPLOADS = "http://${ip}:${port}/api/uploads";

    /**
     * 根据文件 MD5 对应的上传信息。
     */
    String API_UPLOADS_BY_FILE_UID = "http://${ip}:${port}/api/uploads/${fileUid}";

    /**
     * 文件上传的分片。
     */
    String API_UPLOADS_CHUNK = "http://${ip}:${port}/api/uploads/${fileUid}/chunks";

    /**
     * Agent 间传输文件。
     */
    String API_TRANSFERS_FILE = "http://${ip}:${port}/api/transfers/file";

    /**
     * Agent 间传输文件查询状态。
     */
    String API_TRANSFERS_BY_UID = "http://${ip}:${port}/api/transfers/${transferUid}";
}
