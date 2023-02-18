package misc.auto.ndtagent.upload;

import lombok.extern.slf4j.Slf4j;
import misc.auto.ndtagent.AgentRequestUtils;
import misc.auto.ndtagent.AgentUrls;
import misc.auto.ndtagent.Response;
import org.springframework.http.HttpMethod;

import java.util.concurrent.TimeUnit;

import static com.google.common.collect.ImmutableMap.of;
import static org.apache.commons.text.StringSubstitutor.replace;

@Slf4j
public class AgentFileTransfer {
    /**
     * 源 Agent IP。
     */
    private final String srcAgentIp;

    /**
     * 源 Agent 端口。
     */
    private final int srcAgentPort;

    /**
     * 目标 Agent IP。
     */
    private final String dstAgentIp;

    /**
     * 目标 Agent 端口。
     */
    private final int dstAgentPort;

    /**
     * 源 Agent 上需要复制的文件。
     */
    private final String srcPath;

    /**
     * 目标 Agent 保存文件的目录。
     */
    private final String dstDir;

    /**
     * 传输的唯一 ID。
     */
    private String transferUid;

    public AgentFileTransfer(String srcAgentIp, int srcAgentPort, String dstAgentIp, int dstAgentPort, String srcPath, String dstDir) {
        this.srcAgentIp = srcAgentIp;
        this.srcAgentPort = srcAgentPort;
        this.dstAgentIp = dstAgentIp;
        this.dstAgentPort = dstAgentPort;
        this.srcPath = srcPath;
        this.dstDir = dstDir;
    }

    /**
     * 传输文件。
     *
     * @return 传输成功返回 true、失败返回 false。
     */
    public boolean transferFile() {
        /*
         逻辑:
         1. 创建表单数据。
         2. 创建 URL。
         3. 发起传输文件请求。
         4. 轮询传输任务状态直到结束。
         */
        log.info("Agent 间传文件: 源 Agent [{}]，目标 Agent [{}], 源文件路径 [{}], 保存目录 [{}]", this.srcAgentIp, this.dstAgentIp, this.srcPath, this.dstDir);

        // [1] 创建表单数据。
        AgentFileTransferData data = new AgentFileTransferData();
        data.setTargetIp(this.dstAgentIp);
        data.setTargetPort(this.dstAgentPort);
        data.setSrcPath(this.srcPath);
        data.setDstDir(this.dstDir);

        // [2] 创建 URL。
        String url = replace(AgentUrls.API_TRANSFERS_FILE, of(
                "ip", this.srcAgentIp,
                "port", this.srcAgentPort
        ));

        // [3] 发起传输文件请求。
        Response<AgentFileTransferData> rsp = AgentRequestUtils.doRequest(url, HttpMethod.POST, data, AgentFileTransferData.class);

        if (rsp == null || !rsp.isSuccess()) {
            String msg = rsp == null ? "" : ": " + rsp.getMsg();
            throw new RuntimeException("创建上传信息失败" + msg);
        }

        // 获取服务器返回的传输响应。
        data = rsp.getData();
        this.transferUid = data.getUid();

        // [4] 轮询传输任务状态直到结束。
//        boolean result = waitForTransferToFinish();
//
//        if (result) {
//            log.info("[成功] Agent 间传文件成功: 源 Agent [{}]，目标 Agent [{}], 源文件路径 [{}], 保存目录 [{}]", this.srcAgentIp, this.dstAgentIp, this.srcPath, this.dstDir);
//        } else {
//            log.warn("[错误] Agent 间传文件失败: 源 Agent [{}]，目标 Agent [{}], 源文件路径 [{}], 保存目录 [{}]", this.srcAgentIp, this.dstAgentIp, this.srcPath, this.dstDir);
//        }

        log.info("Agent 间传文件, 轮询传输状态，传输 ID [{}]...", this.transferUid);

        url = replace(AgentUrls.API_TRANSFERS_BY_UID, of(
                "ip", this.srcAgentIp,
                "port", this.srcAgentPort,
                "transferUid", this.transferUid
        ));

        AgentFileTransferData resultTransfer = AgentRequestUtils.waitForCondition(url, AgentFileTransferData.class, transfer -> {
            int state = transfer.getState();
            return state == UploadState.SUCCESS || state == UploadState.FAILED;
        });

        if (resultTransfer.getState() == UploadState.SUCCESS) {
            log.info("[成功] Agent 间传文件成功: 源 Agent [{}]，目标 Agent [{}], 源文件路径 [{}], 保存目录 [{}]", this.srcAgentIp, this.dstAgentIp, this.srcPath, this.dstDir);
            return true;
        } else if (resultTransfer.getState() == UploadState.FAILED) {
            log.warn("[错误] Agent 间传文件失败: 源 Agent [{}]，目标 Agent [{}], 源文件路径 [{}], 保存目录 [{}], 错误: [{}]", this.srcAgentIp, this.dstAgentIp, this.srcPath, this.dstDir, resultTransfer.getError());
            return false;
        }

        return false;
    }

    /**
     * 出错的最大尝试次数 (4.3G 的文件在有的环境合并使用了 58S)。
     */
    private static final int MAX_FAULT_TOLERANCE = 20;

    /**
     * 重试的时间间隔 (单位为秒)。
     */
    private static final int RETRY_INTERVAL = 5;

    /**
     * 等待传输执行结束 (轮询脚本状态，直到传输执行结束)。
     *
     * @return 传输成功返回 true、失败返回 false。
     */
    private boolean waitForTransferToFinish() {
        String url = replace(AgentUrls.API_TRANSFERS_BY_UID, of(
                "ip", this.srcAgentIp,
                "port", this.srcAgentPort,
                "transferUid", this.transferUid
        ));

        try {
            // 请求容错次数，使用 faultTolerance 解决暂时性的网络波动问题。
            int faultTolerance = 0;
            Response<AgentFileTransferData> rsp;

            while (faultTolerance < MAX_FAULT_TOLERANCE) {
                // 请求之间等待几秒，避免短时间发起大量请求。
                TimeUnit.SECONDS.sleep(RETRY_INTERVAL);

                try {
                    rsp = AgentRequestUtils.doRequest(url, HttpMethod.GET, null, AgentFileTransferData.class);
                } catch (Exception ex) {
                    faultTolerance++;
                    log.warn("请求任务状态容错: {}", ex.getMessage());
                    continue;
                }

                if (rsp == null || rsp.getData() == null) {
                    faultTolerance++;
                    log.warn("请求任务状态容错: {} null", rsp == null ? "响应为" : "响应的 data 为");
                    continue;
                }

                AgentFileTransferData data = rsp.getData();
                int state = data.getState();

                // 还在执行中。
                if (state == UploadState.HANDLING) {
                    faultTolerance = 0;
                    continue;
                } else if (state == UploadState.SUCCESS) {
                    return true;
                } else if (state == UploadState.FAILED) {
                    return false;
                }
            }
        } catch (InterruptedException ignored) {}

        throw new RuntimeException("获取任务状态异常: 无法访问 Agent，当 Agent 已退出、网络不可用等均可发生此问题");
    }
}
