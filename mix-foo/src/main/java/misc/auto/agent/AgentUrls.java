package misc.auto.agent;

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
}
