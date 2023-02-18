package misc.auto.ndtagent.upload;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * 上传的文件类。
 */
@Data
public class UploadedFile {
    /**
     * 唯一 ID (<fileMd5>-<dstDirMd5>)。
     */
    private String uid;

    /**
     * 文件名。
     */
    private String fileName;

    /**
     * 文件大小 (Bytes)。
     */
    private long fileSize;

    /**
     * 文件的 MD5。
     */
    private String fileMd5;

    /**
     * 文件保存目录。
     */
    private String dstDir;

    /**
     * 上传状态: 0 (初始化)、1 (合并中)、2 (合并失败)、3 (合并成功)。
     */
    private int state;

    /**
     * 文件上传的分片大小。
     */
    private long chunkSize;

    /**
     * 文件上传的分片信息。
     */
    private List<UploadedFileChunk> chunks = new LinkedList<>();
}
