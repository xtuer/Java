package com.xtuer.bean;

/**
 * 大文件上传的常量。
 */
public interface UFileConst {
    /**
     * 初始化。
     */
    int INIT = 0;

    /**
     * 上传成功、合并成功。
     */
    int SUCCESS = 1;

    /**
     * 上传失败、合并失败。
     */
    int FAILED = 2;

    /**
     * 上传中、合并中。
     */
    int DOING = 3;

    /**
     * 分片大小。
     */
    int CHUNK_SIZE = 10000000; // 10M
}
