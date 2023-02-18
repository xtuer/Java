package misc.auto.ndtagent.upload;

import lombok.extern.slf4j.Slf4j;
import misc.auto.ndtagent.AgentRequestUtils;
import misc.auto.ndtagent.AgentUrls;
import misc.auto.ndtagent.Response;

import java.util.concurrent.Callable;

import static com.google.common.collect.ImmutableMap.of;
import static org.apache.commons.text.StringSubstitutor.replace;

/**
 * 上传文件分片的任务。
 */
@Slf4j
public class UploadChunkTask implements Callable<Void> {
    private final FileUploader uploader;
    private final UploadedFileChunk chunk;

    /**
     * 创建上传文件分片任务对象。
     *
     * @param uploader 文件上传类。
     * @param chunk 要上传的分片。
     */
    public UploadChunkTask(FileUploader uploader, UploadedFileChunk chunk) {
        this.uploader = uploader;
        this.chunk = chunk;
    }

    @Override
    public Void call() throws Exception {
        this.uploadFileChunk();
        return null;
    }

    /**
     * 上传文件分片。
     *
     * @throws Exception 读取文件异常
     * @throws RuntimeException 上传失败时服务器返回的异常信息。
     */
    private void uploadFileChunk() throws Exception {
        String url = replace(AgentUrls.API_UPLOADS_CHUNK, of(
                "ip", this.uploader.getIp(),
                "port", this.uploader.getPort(),
                "fileUid", this.uploader.getUf().getUid()
        ));

        String srcPath = this.uploader.getSrcPath();
        int sn = this.chunk.getSn();
        long size = this.chunk.getEndPos() - this.chunk.getStartPos();

        log.debug("上传文件分片: 文件 [{}]，分片 Sn [{}], 分片大小 [{}]", srcPath, sn, size);
        Response<UploadedFileChunk> rsp = AgentRequestUtils.uploadFileChunk(url, this.uploader.getSrcPath(), this.chunk);

        if (!rsp.isSuccess()) {
            log.warn("上传分片失败: 文件 [{}]，分片 Sn [{}]，失败原因 [{}]", srcPath, sn, rsp.getMsg());
            throw new RuntimeException(rsp.getMsg());
        }

        // 更新本地分片的信息。
        UploadedFileChunk rspChunk = rsp.getData();
        this.chunk.setMd5(rspChunk.getMd5());
        this.chunk.setState(rspChunk.getState());
    }
}
