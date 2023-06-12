package xtuer.controller;

import xtuer.bean.Result;
import xtuer.bean.UFile;
import xtuer.bean.UFileChunk;
import xtuer.bean.Urls;
import xtuer.service.UFileRepo;
import xtuer.service.UFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 大文件上传的控制器。
 */
@RestController
public class UFileController {
    @Autowired
    private UFileService ufileService;

    @Autowired
    private UFileRepo ufileRepo;

    /**
     * 根据文件唯一 ID 获取上传信息。
     *
     * 链接: http://localhost:8080/api/bigfile/uploads/{fileUid}
     * 参数: 无
     * 测试: curl http://localhost:8080/api/bigfile/uploads/xyz
     *
     * @param fileUid 文件唯一 ID。
     * @return payload 为 UFile 对象。
     */
    @GetMapping(Urls.API_BIGFILE_UPLOADS_BY_FILE_UID)
    public Result<UFile> findUFile(@PathVariable String fileUid) {
        return Result.ok(ufileRepo.findUFile(fileUid));
    }

    /**
     * 创建上传文件对象。
     *
     * 链接: http://localhost:8080/api/bigfile/uploads
     * 参数: 无
     * 请求体: { "fileName", "fileMd5", "fileSize" }
     * 测试: curl -X POST http://localhost:8080/api/bigfile/uploads -d '{"fileName": "xx.zip", "fileMd5": "xyz", "fileSize": 23000000}' -H 'Content-Type: application/json'
     *
     * @param ufile 接收请求体参数用于创建上传文件信息。
     * @return payload 为 UFile 对象。
     */
    @PostMapping(Urls.API_BIGFILE_UPLOADS)
    public Result<UFile> createUFile(@RequestBody UFile ufile) {
        return Result.ok(ufileService.createUFile(ufile.getFileName(), ufile.getFileMd5(), ufile.getFileSize()));
    }

    /**
     * 上传分片。
     *
     * 链接: http://localhost:8080/api/bigfile/uploads/{fileUid}/chunks
     * 参数:
     *      chunkSn  (必要): 分片序号。
     *      chunkMd5 (必要): 分片 MD5。
     *      file     (必要): 分片的文件内容。
     * 测试: curl -X POST http://localhost:8080/api/bigfile/uploads/xyz/chunks -F "file=@/Users/biao/Downloads/gotop" -F "chunkMd5=xxx" -F "chunkSn=0" -H "Content-Type: multipart/form-data"
     *
     * @param fileUid 文件唯一 ID。
     * @param chunkSn 分片序号。
     * @param chunkMd5 分片 MD5。
     * @param file 分片的文件。
     * @return payload 为 UFileChunk 对象。
     */
    @PostMapping(Urls.API_BIGFILE_UPLOADS_CHUNK)
    public Result<UFileChunk> uploadChunk(@PathVariable String fileUid, @RequestParam int chunkSn, @RequestParam String chunkMd5, MultipartFile file) throws IOException {
        return Result.single(ufileService.uploadChunk(fileUid, chunkSn, chunkMd5, file));
    }

    /**
     * 查询上传文件的第 chunkSn 个分片。
     * 链接: http://localhost:8080/api/bigfile/uploads/{fileUid}/chunks/{chunkSn}
     * 参数: 无
     * 测试: curl http://localhost:8080/api/bigfile/uploads/fb9e041c2037a794cd8b1a44949f023e/chunks/2
     *
     * @param fileUid 文件唯一 ID。
     * @param chunkSn 分片序号。
     * @return payload 为分片对象。
     */
    @GetMapping(Urls.API_BIGFILE_UPLOADS_CHUNK_BY_ID)
    public Result<UFileChunk> findChunk(@PathVariable String fileUid, @PathVariable int chunkSn) {
        return Result.single(ufileRepo.findUFileChunk(fileUid, chunkSn));
    }
}
