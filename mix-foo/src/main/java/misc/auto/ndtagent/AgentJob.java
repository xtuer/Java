package misc.auto.ndtagent;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * Job 为 Agent 执行 CMD 或 Script 时创建的对象，描述了任务和任务的进程的信息。
 * Job 执行成功概率时 isSuccess() 返回 true，任务的输出结果调用 getStdout() 获取。
 */
@Data
public class AgentJob {
    /**
     * 执行中。
     */
    public static final String STATE_RUNNING = "running";

    /**
     * 执行成功。
     */
    public static final String STATE_SUCCESS  = "success";

    /**
     * 执行失败。
     */
    public static final String STATE_FAILED   = "failed";

    /**
     * 被取消。
     */
    public static final String STATE_CANCELED = "canceled";

    /**
     * 任务的结束状态。
     */
    public static final List<String> FINISH_STATES = Arrays.asList(AgentJob.STATE_SUCCESS, AgentJob.STATE_FAILED, AgentJob.STATE_CANCELED);

    /**
     * 脚本类型: shell。
     */
    public static final String SCRIPT_TYPE_SHELL = "shell";

    /**
     * 脚本类型: python。
     */
    public static final String SCRIPT_TYPE_PYTHON = "python";

    /**
     * 任务的 ID。
     */
    private String id;

    /**
     * 要执行的命令。
     */
    private String cmd;

    /**
     * 执行命令或者脚本的参数，格式为 -k1 "v1" -k2 "v2"。
     */
    private String params;

    /**
     * 要执行的脚本名称。
     */
    private String scriptName;

    /**
     * 脚本内容。
     */
    private String scriptContent;

    /**
     * 脚本类型，值为 shell 或者 python。
     */
    private String scriptType;

    /**
     * 脚本内容保存到系统上的路径。
     */
    private String scriptPath;

    /**
     * 是否异步执行命令或者脚本，为 true 时异步执行，为 false 时同步执行。
     */
    private boolean async;

    /**
     * 任务的进程的 PID。
     */
    private int pid;

    /**
     * 任务的状态，值为 running, finished, canceled。
     */
    private String state;

    /**
     * 任务的进程的标准输出。
     */
    private String stdout;

    /**
     * 任务的进程的错误输出。
     */
    private String stderr;

    /**
     * 任务的进程的返回值。
     */
    private int exitCode;

    /**
     * 执行任务的命令。
     */
    private String execCmd;

    /**
     * Error msg when fork & exec。
     */
    private String error;

    /**
     * 任务创建的时间。
     */
    private String createTime;

    /**
     * 任务开始的时间。
     */
    private String startTime;

    /**
     * 任务结束的时间。
     */
    private String finishTime;

    /**
     * 判断任务是否执行成功。
     *
     * @return 执行成功返回 true，需要注意的是返回 false 不代表任务执行失败，也有可能是仍然中执行中。
     */
    public boolean isSuccess() {
        return STATE_SUCCESS.equals(state);
    }

    public boolean isFinished() {
        return FINISH_STATES.contains(state);
    }
}
