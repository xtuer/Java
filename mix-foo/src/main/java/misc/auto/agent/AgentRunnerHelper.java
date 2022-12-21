package misc.auto.agent;

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

    public static AgentJob executeScript(String ip, int port, String scriptName, String scriptContent, String params, String scriptType) {
        /*
         逻辑:
         1. 创建脚本对象。
         2. 异步执行脚本，因为脚本大多执行时间比较长，所以不能使用阻塞执行方式，容易导致 Http 请求超时。
         3. 轮询脚本状态，等待脚本执行结束。
         */

        // [1] 创建脚本对象。
        AgentJob willExecuteJob = new AgentJob();
        willExecuteJob.setScriptContent(scriptContent);
        willExecuteJob.setScriptType(scriptType);
        willExecuteJob.setScriptName(scriptName);
        willExecuteJob.setParams(params);
        willExecuteJob.setAsync(true);

        // [2] 异步执行脚本，因为脚本大多执行时间比较长，所以不能使用阻塞执行方式，容易导致 Http 请求超时。
        String url = replace(AgentUrls.API_JOBS_EXECUTE_SCRIPT, of(
                "ip", ip,
                "port", port
        ));
        Response<AgentJob> rsp = RequestUtils.doRequest(url, HttpMethod.POST, willExecuteJob, AgentJob.class);

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
        log.info("执行中的脚本任务: {}\n轮询任务状态，任务 ID [{}]...", executingJob, executingJob.getId());

        // 获取任务状态的 URL。
        url = replace(AgentUrls.API_JOBS_BY_ID, of(
                "ip", ip,
                "port", port,
                "jobId", executingJob.getId()
        ));

        // [3] 轮询脚本状态，等待脚本执行结束。
        try {
            // 请求容错次数，使用 faultTolerance 解决暂时性的网络波动问题。
            int faultTolerance = 0;

            while (faultTolerance < MAX_FAULT_TOLERANCE) {
                TimeUnit.SECONDS.sleep(REQUEST_JOB_STATE_INTERVAL);

                try {
                    rsp = null;
                    rsp = RequestUtils.doRequest(url, HttpMethod.GET, null, AgentJob.class);
                } catch (Exception ignored) {
                    faultTolerance++;
                    continue;
                }

                if (rsp == null) {
                    faultTolerance++;
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

        // 按理正常是不会走到这一步的，除非 sleep 出错，但是没有并发竞争资源，所以可以忽视。
        return null;
    }
}
