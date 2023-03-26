package com.xtuer.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 大文件上传的 Mapper。
 */
@Mapper
public interface UFileMapper {
    /**
     * 使用 fileUid 查询上传文件对象的 Json，用于反序列化出 UFile 对象。
     *
     * @param fileUid 文件的唯一 ID。
     * @return 返回 UFile 序列化得到的 json 字符串。
     */
    String findUFileJsonByFileUid(String fileUid);

    /**
     * 使用 fileUid 查询上传文件的分片 Json (按照分片序号 sn 升序排列)。
     *
     * @param fileUid 文件的唯一 ID。
     * @return 返回 UFileChunk 序列化得到的 json 字符串数组。
     */
    List<String> findUFileChunkJsonsByFileUid(String fileUid);

    /**
     * 查询传入 fileUid 上次文件的第 sn 个分片。
     *
     * @param fileUid 文件唯一 ID。
     * @param sn 分片序号。
     * @return 返回 UFileChunk 序列化得到的 json 字符串。
     */
    String fileUFileChunkJsonByFileUidAndSn(@Param("fileUid") String fileUid, @Param("sn") int sn);

    /**
     * 统计不成功的分片数量。
     *
     * @param fileUid 文件唯一 ID。
     * @return 返回不成功的分片数量。
     */
    int countUnsuccessfulUFileChunk(String fileUid);

    /**
     * 插入上传文件对象。
     *
     * @param fileUid 文件唯一 ID。
     * @param fileName 文件名。
     * @param json 文件序列化得到的 Json (不包含分片数组)
     */
    void insertUFile(@Param("fileUid") String fileUid, @Param("fileName") String fileName, @Param("json") String json);

    /**
     * 插入上传文件的分片。
     *
     * @param fileUid 文件唯一 ID。
     * @param sn 分片序号。
     * @param json 分片序列化得到的 Json。
     */
    void insertUFileChunk(@Param("fileUid") String fileUid, @Param("sn") int sn, @Param("json") String json);

    /**
     * 更新传入 fileUid 的上传文件的状态。
     *
     * @param fileUid 文件唯一 ID。
     * @param state 状态。
     * @param json 上传文件序列化的 Json。
     */
    void updateUFileState(@Param("fileUid") String fileUid, @Param("state") int state, @Param("json") String json);

    /**
     * 更新传入 fileUid 的第 sn 个分片的状态。
     *
     * @param fileUid 文件唯一 ID。
     * @param sn 分片序号。
     * @param state 状态。
     */
    void updateUFileChunkState(@Param("fileUid") String fileUid, @Param("sn") int sn, @Param("state") int state, @Param("json") String json);

    /**
     * 删除传入 fileUid 的上传文件。
     *
     * @param fileUid 文件唯一 ID。
     */
    void deleteUFile(String fileUid);

    /**
     * 删除传入 fileUid 的上传文件的所有分片。
     *
     * @param fileUid 文件唯一 ID。
     */
    void deleteUFileChunks(String fileUid);
}
