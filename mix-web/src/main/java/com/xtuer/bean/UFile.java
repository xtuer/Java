package com.xtuer.bean;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * 分片上传的文件。
 */
@Data
public class UFile {
    /**
     * 唯一 ID: <fileMd5>
     */
    String fileUid;

    /**
     * 文件名。
     */
    String fileName;

    /**
     * 文件大小 (Bytes)。
     */
    long fileSize;

    /**
     * 文件的 MD5。
     */
    String fileMd5;

    /**
     * 上传状态: 0 (初始化)、1 (合并成功)、2 (合并失败)、3 (合并中)。
     */
    int state;

    /**
     * 文件上传的分片大小。
     */
    int chunkSize;

    /**
     * 文件上传的分片信息。
     */
    List<UFileChunk> chunks = new LinkedList<>();
}
