package com.xtuer.service;

import com.xtuer.bean.UFile;
import com.xtuer.bean.UFileChunk;
import com.xtuer.mapper.UFileMapper;
import com.xtuer.util.Utils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 大文件上传的 Repo 类，对 Mapper 进行了业务逻辑封装。
 */
@Component
public class UFileRepo {
    @Autowired
    private UFileMapper ufileMapper;

    /**
     * 使用传入的 fileUid 查询上传文件对象 (包含分片信息)。
     *
     * @param fileUid 文件唯一 ID。
     * @return 返回上传的文件，查询不到时返回 null。
     */
    public UFile findUFile(String fileUid) {
        // 查询 UFile 基础信息。
        UFile ufile = findUFileWithoutChunks(fileUid);
        if (ufile == null) {
            return null;
        }

        // 查询 UFile 的 chunks。
        List<UFileChunk> chunks = ufile.getChunks();
        for (String chunkJson : ufileMapper.findUFileChunkJsonsByFileUid(fileUid)) {
            UFileChunk chunk = Utils.fromJson(chunkJson, UFileChunk.class);

            if (chunk != null) {
                chunks.add(chunk);
            }
        }

        return ufile;
    }

    /**
     * 查询传入 fileUid 的上传文件 (不包含分片信息)。
     *
     * @param fileUid 文件唯一 ID。
     * @return 返回上传文件对象，查询不到时返回 null。
     */
    public UFile findUFileWithoutChunks(String fileUid) {
        // 查询 UFile 基础信息。
        String json = ufileMapper.findUFileJsonByFileUid(fileUid);
        if (!StringUtils.hasText(json)) {
            return null;
        }

        UFile ufile = Utils.fromJson(json, UFile.class);
        if (ufile == null) {
            return null;
        }

        if (ufile.getChunks() == null) {
            ufile.setChunks(new LinkedList<>());
        }

        return ufile;
    }

    /**
     * 查询传入 fileUid 的上传文件的第 chunkSn 个分片。
     *
     * @param fileUid 文件唯一 ID。
     * @param chunkSn 分片序号。
     * @return 返回查询到的分片，查询不到返回 null。
     */
    public UFileChunk findUFileChunk(String fileUid, int chunkSn) {
        String json = ufileMapper.fileUFileChunkJsonByFileUidAndSn(fileUid, chunkSn);
        if (!StringUtils.hasText(json)) {
            return null;
        }

        UFileChunk chunk = Utils.fromJson(json, UFileChunk.class);
        return chunk;
    }

    /**
     * 统计不成功的分片数量。
     *
     * @param fileUid 文件唯一 ID。
     * @return 返回不成功的分片数量。
     */
    public int countUnsuccessfulUFileChunk(String fileUid) {
        return ufileMapper.countUnsuccessfulUFileChunk(fileUid);
    }

    /**
     * 插入上传文件信息到数据库。
     *
     * @param ufile 上传的文件。
     */
    @Transactional
    public void insertUFile(UFile ufile) {
        /*
         逻辑:
         1. 把上传文件保存到数据库: 克隆一个新的文件对象并设置其 chunks 为空，目的是序列化的时候不包含 chunks 信息。
         2. 把分片分片保存到数据库。
         */

        // [1] 把上传文件保存到数据库: 克隆一个新的文件对象并设置其 chunks 为空，目的是序列化的时候不包含 chunks 信息。
        ufileMapper.insertUFile(ufile.getFileUid(), ufile.getFileName(), ufileToJsonWithoutChunks(ufile));

        // [2] 把分片分片保存到数据库。
        for (UFileChunk chunk : ufile.getChunks()) {
            ufileMapper.insertUFileChunk(ufile.getFileUid(), chunk.getSn(), Utils.toJson(chunk));
        }
    }

    /**
     * 删除传入 fileUid 的上传文件 (分片信息也会同时删除)。
     *
     * @param fileUid 文件唯一 ID。
     */
    @Transactional
    public void deleteUFile(String fileUid) {
        ufileMapper.deleteUFile(fileUid);
        ufileMapper.deleteUFileChunks(fileUid);
    }

    /**
     * 更新传入 fileUid 的上传文件状态。
     *
     * @param fileUid 文件唯一 ID。
     * @param state 上传文件的状态。
     */
    public void updateUFileState(String fileUid, int state) {
        /*
         逻辑:
         1. 查询上传文件。
         2. 更新上传文件的状态。
         */
        UFile ufile = findUFileWithoutChunks(fileUid);
        ufile.setState(state);
        ufileMapper.updateUFileState(fileUid, state, ufileToJsonWithoutChunks(ufile));
    }

    /**
     * 更新传入 fileUid 的第 chunkSn 个分片的状态。
     *
     * @param fileUid 文件唯一 ID。
     * @param chunkSn 分片序号。
     * @param state 状态。
     */
    public void updateUFileChunkState(String fileUid, int chunkSn, int state) {
        /*
         逻辑:
         1. 查询上传文件分片。
         2. 更新上传文件分片的状态。
         */
        UFileChunk chunk = findUFileChunk(fileUid, chunkSn);
        chunk.setState(state);
        ufileMapper.updateUFileChunkState(fileUid, chunkSn, state, Utils.toJson(chunk));
    }

    /**
     * 更新传入 fileUid 的第 chunkSn 个分片的 MD5。
     *
     * @param fileUid 文件唯一 ID。
     * @param chunkSn 分片序号。
     * @param md5 分片的 MD5。
     */
    public void updateUFileChunkMd5(String fileUid, int chunkSn, String md5) {
        /*
         逻辑:
         1. 查询上传文件分片。
         2. 更新上传文件分片的状态 (因为 MD5 是在 Json 字符串中，所以可以借助更新分片状态接口实现)。
         */
        UFileChunk chunk = findUFileChunk(fileUid, chunkSn);
        chunk.setMd5(md5);
        ufileMapper.updateUFileChunkState(fileUid, chunkSn, chunk.getState(), Utils.toJson(chunk));
    }

    /**
     * 把传入的 ufile 序列化为 Json 字符串 (不包含分片信息)。
     *
     * @param ufile 上传的文件。
     * @return 返回 Json 字符串。
     */
    public String ufileToJsonWithoutChunks(UFile ufile) {
        UFile tempUf = new UFile();
        BeanUtils.copyProperties(ufile, tempUf);
        tempUf.setChunks(Collections.emptyList());

        return Utils.toJson(tempUf);
    }
}
