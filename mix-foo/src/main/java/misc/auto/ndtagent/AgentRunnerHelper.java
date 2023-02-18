package misc.auto.ndtagent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

import java.util.concurrent.TimeUnit;

import static com.google.common.collect.ImmutableMap.of;
import static org.apache.commons.text.StringSubstitutor.replace;

/**
 * AgentRunnerHelper 服务的工具类。
 */
@Slf4j
public class AgentRunnerHelper {
    /**
     * 最大请求容错次数。
     */
    private static final int MAX_FAULT_TOLERANCE = 5;

    /**
     * 请求任务状态的时间间隔，单位为秒。
     */
    private static final int REQUEST_JOB_STATE_INTERVAL = 10;

    /**
     * 在 Agent 所在目标主机上执行脚本。
     *
     * @param ip Agent IP。
     * @param port Agent 端口。
     * @param scriptName 脚本名称。
     * @param scriptContent 脚本内容。
     * @param params 脚本参数。
     * @param scriptType 脚本类型。
     * @param async 是否异步执行。
     * @return 返回执行得到的任务对象，其中包含了脚本的正常输出，错误输出，任务执行成功失败等信息。
     */
    public static AgentJob executeScript(String ip, int port, String scriptName, String scriptContent, String params, String scriptType, boolean async) {
        /*
         逻辑:
         1. 创建脚本的任务对象。
         2. 异步执行脚本，因为脚本大多执行时间比较长，所以不能使用阻塞执行方式，否则容易导致 Http 请求超时。
         3. 轮询脚本状态，等待脚本执行结束。
         */

        // [1] 创建脚本的任务对象。
        AgentJob willExecuteJob = new AgentJob();
        willExecuteJob.setScriptContent(scriptContent);
        willExecuteJob.setScriptType(scriptType);
        willExecuteJob.setScriptName(scriptName);
        willExecuteJob.setParams(params);
        willExecuteJob.setAsync(async);

        // [2] 异步执行脚本，因为脚本大多执行时间比较长，所以不能使用阻塞执行方式，容易导致 Http 请求超时。
        String url = replace(AgentUrls.API_JOBS_EXECUTE_SCRIPT, of(
                "ip", ip,
                "port", port
        ));
        Response<AgentJob> rsp = AgentRequestUtils.doRequest(url, HttpMethod.POST, willExecuteJob, AgentJob.class);

        // 如果脚本执行失败则抛出异常。
        if (rsp == null || !rsp.isSuccess()) {
            String err = String.format("执行脚本失败，脚本名 [%s]", scriptName);

            // 获取错误原因。
            if (rsp != null) {
                err = String.format("%s，错误 [%s]", err, rsp.getMsg());
            }

            log.warn(err);
            throw new RuntimeException(err);
        }

        // Agent 返回的正在执行中的 Job。
        AgentJob executingJob = rsp.getData();

        // 同步执行的话脚本至此执行结束，直接返回，异步的话需要轮询任务状态。
        if (!async) {
            return executingJob;
        }

        // 获取任务状态的 URL。
        url = replace(AgentUrls.API_JOBS_BY_ID, of(
                "ip", ip,
                "port", port,
                "jobId", executingJob.getId()
        ));

        // [3] 轮询脚本状态，等待脚本执行结束。
        log.info("执行中的任务: {}\n轮询任务状态，任务 ID [{}]...", executingJob, executingJob.getId());

        // return waitForJobToFinish(url);
        return AgentRequestUtils.waitForCondition(url, AgentJob.class, AgentJob::isFinished);
    }

    /**
     * 等待任务执行结束 (轮询脚本状态，直到脚本执行结束)。
     *
     * @param jobStateUrl 获取 Job 状态的 URL。
     * @return 返回执行结束后的 Job 对象。
     */
    private static AgentJob waitForJobToFinish(String jobStateUrl) {
        try {
            // 请求容错次数，使用 faultTolerance 解决暂时性的网络波动问题。
            int faultTolerance = 0;
            Response<AgentJob> rsp;

            while (faultTolerance < MAX_FAULT_TOLERANCE) {
                // 请求之间等待几秒，避免短时间发起大量请求。
                TimeUnit.SECONDS.sleep(REQUEST_JOB_STATE_INTERVAL);

                try {
                    rsp = AgentRequestUtils.doRequest(jobStateUrl, HttpMethod.GET, null, AgentJob.class);
                } catch (Exception ex) {
                    faultTolerance++;
                    log.warn("请求任务状态容错: {}", ex.getMessage());
                    continue;
                }

                if (rsp == null) {
                    faultTolerance++;
                    log.warn("请求任务状态容错: 响应为 null");
                    continue;
                }

                AgentJob tempJob = rsp.getData();
                String state = tempJob.getState();

                // 还在执行中。
                if (AgentJob.STATE_RUNNING.equals(state)) {
                    faultTolerance = 0;
                    continue;
                }

                // 执行结束。
                if (tempJob.isFinished()) {
                    return tempJob;
                }
            }
        } catch (InterruptedException ignored) {}

        throw new RuntimeException("获取任务状态异常: 无法访问 Agent，当 Agent 已退出、网络不可用等均可发生此问题");
    }
}
