package misc.auto.ndtagent.upload;

import lombok.Data;

/**
 * 上传文件的分片。
 */
@Data
public class UploadedFileChunk {
    /**
     * 分片的序号，按序号合并分片。
     */
    private int sn;

    /**
     * 分片的 MD5。
     */
    private String md5;

    /**
     * 分片在文件中的起始位置。
     */
    private long startPos;

    /**
     * 分片装文件中的结束位置 (不包含 EndPos)。
     */
    private long endPos;

    /**
     * 分片上传状态: 0 (初始化)、1 (上传中)、2 (上传失败)、3 (上传成功)。
     */
    private int state;
}
