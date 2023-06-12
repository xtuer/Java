package xtuer.bean;

import lombok.Data;

/**
 * 分片上传的文件分片。
 */
@Data
public class UFileChunk {
    /**
     * 分片的序号，按序号合并分片。
     */
    int sn;

    /**
     * 分片的 MD5。
     */
    String md5;

    /**
     * 分片在文件中的起始位置。
     */
    long start;

    /**
     * 分片装文件中的结束位置 (不包含 EndPos)。
     */
    long end;

    /**
     * 分片上传状态: 0 (初始化)、1 (上传成功)、2 (上传失败)、3 (上传中)
     */
    int state;
}
