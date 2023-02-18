package misc.auto.ndtagent.upload;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import misc.auto.ndtagent.AgentRequestUtils;
import misc.auto.ndtagent.AgentUrls;
import misc.auto.ndtagent.Response;
import org.springframework.http.HttpMethod;

import java.io.File;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.ImmutableMap.of;
import static org.apache.commons.text.StringSubstitutor.replace;

/**
 * 文件上传类。
 */
@Getter
@Slf4j
public class FileUploader {
    private static final CompletionService<Void> completionService;

    static {
        // 控制上传分片并发量。
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        completionService = new ExecutorCompletionService<>(executorService);
    }

    /**
     * 出错的最大尝试次数 (4.3G 的文件在有的环境合并使用了 58S)。
     */
    private static final int MAX_RETRY_COUNT = 20;

    /**
     * Agent 的 IP。
     */
    private final String ip;

    /**
     * Agent 的 Port。
     */
    private final int port;

    /**
     * 要上传的文件路径。
     */
    private final String srcPath;

    /**
     * 文件在 Agent 上保存的目录。
     */
    private final String dstDir;

    /**
     * 上传的文件信息。
     */
    private UploadedFile uf;

    /**
     * 创建文件上传的对象。
     *
     * @param ip Agent 的 IP。
     * @param port Agent 的 Port。
     * @param srcPath 要上传的文件路径。
     * @param dstDir 文件在 Agent 上保存的目录。
     */
    public FileUploader(String ip, int port, String srcPath, String dstDir) {
        this.ip = ip;
        this.port = port;
        this.srcPath = srcPath;
        this.dstDir = dstDir;
        this.uf = newUploadedFile();
    }

    /**
     * 上传文件。
     *
     * @return 上传成功返回 true，上传失败返回 false。
     */
    public boolean uploadFile() {
        /*
         逻辑:
         1. 请求文件上传信息。
         2. 根据 uf 的状态分类处理:
            2.1 上传成功
            2.2 分片合并中，稍后继续请求状态
            2.3 合并分片失败，例如 MD5 不匹配，创建保存目录失败
            2.4 初始化，上传分片
         3. 获取最新上传文件状态，重复步骤 1 直到文件上传成功或者合并分片出错。
         */

        log.info("上传文件: 源文件路径 [{}], 保存目录 [{}], 文件大小 [{}]", this.srcPath, this.dstDir, this.uf.getFileSize());

        // [1] 请求文件上传信息。
        this.uf = this.requestCreateUploadedFile();
        log.info("上传文件: 源文件路径 [{}], 文件大小 [{}], 分片大小 [{}], 分片数量 [{}], 文件 FileUid [{}]",
                this.srcPath, this.uf.getFileSize(),
                this.uf.getChunkSize(), this.uf.getChunks().size(),
                this.uf.getUid()
        );

        try {
            int count = 0;
            while (count++ < MAX_RETRY_COUNT) {
                final int state = this.uf.getState();

                if (state == UploadState.SUCCESS) {
                    // [2.1] 上传成功
                    log.info("上传文件成功: 源文件路径 [{}], 保存目录 [{}], 文件大小 [{}]", this.srcPath, this.dstDir, this.uf.getFileSize());
                    return true;
                } else if (state == UploadState.HANDLING) {
                    // [2.2] 分片合并中，稍后继续请求状态
                    log.info("文件正在合并中: 源文件路径 [{}]", this.srcPath);
                } else if (state == UploadState.FAILED) {
                    // [2.3] 合并分片失败，例如 MD5 不匹配，创建保存目录失败
                    log.info("上传文件失败: 源文件路径 [{}], 保存目录 [{}]，", this.srcPath, this.dstDir);
                    return false;
                } else {
                    // [2.4] 初始化，上传分片
                    this.uploadFileChunks();
                }

                // [3] 获取最新上传文件状态，重复步骤 1 直到文件上传成功或者合并分片出错。
                TimeUnit.SECONDS.sleep(5); // 分片合并也需要一些时间，所以等一下。
                this.uf = this.requestUploadedFile();
            }
        } catch (InterruptedException ignored) {}

        return false;
    }

    /**
     * 创建上传的文件对象。
     *
     * @return 返回上传的文件对象。
     */
    private UploadedFile newUploadedFile() {
        File src = new File(srcPath);

        UploadedFile uf = new UploadedFile();
        uf.setFileName(src.getName());
        uf.setDstDir(dstDir);
        uf.setFileSize(src.length());
        uf.setFileMd5(Md5Utils.md5(src));

        return uf;
    }

    /**
     * 在 Agent 上创建上传文件。
     *
     * @return 返回 Agent 上要上传的文件对象。
     */
    private UploadedFile requestCreateUploadedFile() {
        String url = replace(AgentUrls.API_UPLOADS, of(
                "ip", ip,
                "port", port
        ));

        Response<UploadedFile> rsp = AgentRequestUtils.doRequest(url, HttpMethod.POST, uf, UploadedFile.class);

        if (!rsp.isSuccess()) {
            throw new RuntimeException("创建上传信息失败: " + rsp.getMsg());
        }

        return rsp.getData();
    }

    /**
     * 请求上传文件的状态。
     *
     * @return 返回上传的文件对象。
     */
    private UploadedFile requestUploadedFile() {
        String url = replace(AgentUrls.API_UPLOADS_BY_FILE_UID, of(
                "ip", ip,
                "port", port,
                "fileUid", uf.getUid()
        ));

        Response<UploadedFile> rsp = AgentRequestUtils.doRequest(url, HttpMethod.GET, null, UploadedFile.class);
        return rsp.getData();
    }

    /**
     * 上传需要上传的文件分片。
     */
    private void uploadFileChunks() {
        // 使用 AtomicInteger 是因为 Lambda 表达式中不能直接修改 int 的值。
        AtomicInteger count = new AtomicInteger(0);

        // [1] 使用 CompletionService 并发量为 5 上传分片。
        // [2] 创建并执行上传分片任务。
        // 上传未上传、或者重传失败的分片 (分片上传失败例如不能创建分片的目录)。
        uf.getChunks()
                .stream()
                .filter(chunk -> chunk.getState() == UploadState.INIT || chunk.getState() == UploadState.FAILED)
                .forEach(chunk -> {
                    completionService.submit(new UploadChunkTask(this, chunk));
                    count.addAndGet(1);
                });

        // [3] 等待所有分片上传完成。
        try {
            int len = count.get();
            for (int i = 0; i < len; i++) {
                completionService.take();
            }
        } catch (InterruptedException ignored) {}
    }
}
