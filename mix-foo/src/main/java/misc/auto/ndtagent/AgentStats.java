package misc.auto.ndtagent;

import lombok.Data;

/**
 * Agent 的状态。
 */
@Data
public class AgentStats {
    /**
     * 版本。
     */
    private String version;

    /**
     * 是否正在执行中。
     */
    private boolean running;

    /**
     * 升级状态。
     */
    private int updateState;

    /**
     * Agent 访问地址。
     */
    private String agentAddr;

    /**
     * Watchdog 访问地址。
     */
    private String watchdogAddr;

    /**
     * 启动时间。
     */
    private String startTime;

    /**
     * 系统时间。
     */
    private String systemTime;

    /**
     * 当前分配的内存。
     */
    private String heapAlloc;

    /**
     * 数据库路径。
     */
    private String dbPath;

    /**
     * 日志目录。
     */
    private String logDir;

    /**
     * 任务数量。
     */
    private int jobsCount;

    /**
     * 执行中任务数量。
     */
    private int runningJobsCount;

    /**
     * 允许同时执行的最大任务数量。
     */
    private int maxRunningJobsCount;

    /**
     * 上传的文件数量。
     */
    private int uploadedFileCount;

    /**
     * 上传中的文件数量。
     */
    private int uploadingFileCount;

    /**
     * 上传文件的临时目录。
     */
    private String uploadTempDir;

    /**
     * 上传文件的文件分片大小。
     */
    private String uploadFileChunkSize;
}
