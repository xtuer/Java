package com.xtuer.service;

import com.google.common.base.Preconditions;
import com.xtuer.bean.UFile;
import com.xtuer.bean.UFileChunk;
import com.xtuer.bean.UFileConst;
import com.xtuer.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 大文件上传的服务类。
 */
@Service
@Slf4j
public class UFileService {
    @Autowired
    private UFileRepo ufileRepo;

    /**
     * 保存文件的目录。
     * 文件保格式为: {dstDir}/{fileMd5}，不保存为文件名是为了避免名字冲突，下载文件或者从 FTP 复制到本地的时候再修改本地得到的文件名为文件的真实名字。
     */
    private String dstDir = "/Users/biao/Documents/temp/ufile";

    /**
     * 保存分片的临时目录。
     * 每个文件的分片保存格式为: {chunkBaseDir}/{fileUid}/0.tmp, 1.tmp, 2.tmp, 3.tmp, ...
     */
    private String chunkBaseDir = "/Users/biao/Documents/temp/ufile-tmp";

    /**
     * 是否使用 FTP 保存文件。
     */
    private boolean ftpEnabled = true;

    /**
     * 创建上传文件信息。
     *
     * @param fileName 文件名。
     * @param fileMd5 文件 MD5。
     * @param fileSize 文件大小。
     * @return 返回上传的文件。
     */
    public UFile createUFile(String fileName, String fileMd5, long fileSize) {
        /*
         逻辑:
         1. 数据校验。
         2. 如果文件上传信息已经存在:
            2.1 上传完成，使用 FTP 保存文件，目标文件在 FTP 上存在。
            2.2 上传完成，但目标文件在本地不存在，删除已有上传信息重新创建。
            2.3 其他情况返回已存在的上传文件信息，不重复创建。
         3. 创建文件上传对象。
         4. 如果目标文件已经存在，则不需要重复上传。
         5. 创建分片信息。
         6. 把文件上传信息保存起来。
         */

        // [1] 参数校验
        Preconditions.checkArgument(StringUtils.hasText(fileName), "文件名 fileName 不能为空");
        Preconditions.checkArgument(StringUtils.hasText(fileMd5), "文件的 fileMd5 不能为空");
        Preconditions.checkArgument(fileSize > 0, "文件大小 fileSize 不能小于 0");

        // [2] 如果文件上传信息已经存在:
        String fileUid = fileMd5;
        UFile uf = ufileRepo.findUFile(fileMd5);

        if (uf != null) {
            if (uf.getState() == UFileConst.SUCCESS && ftpEnabled && this.targetFileExistsInFtp(fileUid)) {
                // [2.1] 上传完成，使用 FTP 保存文件，目标文件在 FTP 上存在。
                log.info("上传信息已经存在，目标文件在 FTP 上存在: fileName [{}], fileMd5 [{}]", fileName, fileMd5);
                return uf;
            } else if (uf.getState() == UFileConst.SUCCESS && !this.targetFileExistsAtLocal(fileUid)) {
                // [2.2] 上传完成，但目标文件在本地不存在，删除已有上传信息重新创建。
                ufileRepo.deleteUFile(fileUid);
                log.warn("上传信息已经存在，曾经上传成功，但目标文件已经不存在，删除已有上传信息重新创建: fileName [{}], fileMd5 [{}]", fileName, fileMd5);
            } else {
                // [2.3] 其他情况返回已存在的上传文件信息，不重复创建。
                log.info("上传信息已经存在，不再重复创建，返回当前的上传信息: fileName [{}], fileMd5 [{}]", fileName, fileMd5);
                return uf;
            }
        }

        // [3] 创建文件上传对象。
        log.info("创建上传文件: fileName [{}], fileMd5 [{}], fileSize [{}]", fileName, fileMd5, fileSize);

        uf = new UFile();
        uf.setFileUid(fileUid);
        uf.setFileName(fileName);
        uf.setFileMd5(fileMd5);
        uf.setFileSize(fileSize);
        uf.setState(UFileConst.INIT);
        uf.setChunkSize(UFileConst.CHUNK_SIZE);

        // [4] 如果目标文件已经存在，则不需要重复上传。
        // 此处不考虑: 有的使用环境下可以特殊考虑这种情况，为了把文件信息在系统里管理起来，数据库里没有上传信息则认为没有，需要重传。

        // [5] 创建分片信息。
        int sn = 0;
        boolean last = false;
        while (!last) {
            long startPos = (long) sn * UFileConst.CHUNK_SIZE;
            long endPos = startPos + UFileConst.CHUNK_SIZE;

            // 达到文件大小的时候，说明是最后一个分片，结束循环。
            if (endPos >= fileSize) {
                endPos = fileSize;
                last = true;
            }

            // 分片对象。
            UFileChunk chunk = new UFileChunk();
            chunk.setSn(sn);
            chunk.setStart(startPos);
            chunk.setEnd(endPos);
            chunk.setState(UFileConst.INIT);
            uf.getChunks().add(chunk);

            sn++;
        }

        // [6] 把文件上传信息保存起来。
        ufileRepo.insertUFile(uf);

        return uf;
    }

    /**
     * 上传分片。
     *
     * @param fileUid 文件唯一 ID。
     * @param chunkSn 分片序号。
     * @param chunkMd5 分片 MD5。
     * @param file 分片的文件。
     * @return 返回 UFileChunk 对象。
     * @throws IOException 操作分片相关的文件或者目录出错时抛出 IOException 异常。
     */
    public UFileChunk uploadChunk(String fileUid, int chunkSn, String chunkMd5, MultipartFile file) throws IOException {
        /*
         逻辑:
         1. 查询分片信息:
            1.1 如果分片不存在则返回。
            1.2 上传中、上传成功直接返回。
            1.3 初始化、上传失败则继续上传。
         2. 保存分片到临时目录，分片保存路径为 {chunkBaseDir}/{fileUid}/{sn}.tmp
         3. 验证分片的 MD5。
         4. 如果所有分片都上传完成，则合并成完整文件，并且删除分片。
         */

        // [1] 查询分片信息:
        UFileChunk chunk = ufileRepo.findUFileChunk(fileUid, chunkSn);

        // [1.1] 如果分片不存在则返回。
        if (chunk == null) {
            log.warn("上传的分片不存在, fileUid [{}], chunkSn [{}]", fileUid, chunkSn);
            throw new RuntimeException(String.format("上传的分片不存在, fileUid [%s], chunkSn [%d]", fileUid, chunkSn));
        }

        // [1.2] 上传中、上传成功直接返回。
        if (chunk.getState() == UFileConst.DOING) {
            log.info("分片正在上传中，不需要重复上传: fileUid [{}], chunkSn [{}]", fileUid, chunkSn);
            return chunk;
        }
        if (chunk.getState() == UFileConst.SUCCESS) {
            log.info("分片已上传成功，不需要重复上传: fileUid [{}], chunkSn [{}]", fileUid, chunkSn);
            return chunk;
        }
        // [1.3] 初始化、上传失败则继续上传。

        // [2] 保存分片到临时目录，分片保存路径为 {chunkBaseDir}/{fileUid}/{sn}.tmp
        String chunkPath = this.generateChunkPath(fileUid, chunkSn);
        log.info("保存分片: fileUid [{}], chunkSn [{}], chunkPath [{}]", fileUid, chunkSn, chunkPath);

        File chunkFile = new File(chunkPath);
        FileUtils.forceMkdirParent(chunkFile); // 确保分片文件夹存在。
        FileUtils.copyInputStreamToFile(file.getInputStream(), chunkFile);

        // [4] 验证分片的 MD5。
        ufileRepo.updateUFileChunkMd5(fileUid, chunkSn, chunkMd5);
        String tempMd5 = Utils.md5(chunkFile);
        if (!Objects.equals(chunkMd5, tempMd5)) {
            // 上传失败。
            log.warn("上传分片错误，MD5 不匹配: fileUid [{}], chunkSn [{}], chunkMd5 [{}], currentMd5 [{}]", fileUid, chunkSn, chunkMd5, tempMd5);

            chunk.setState(UFileConst.FAILED);
            ufileRepo.updateUFileChunkState(fileUid, chunkSn, UFileConst.FAILED);
            return chunk;
        } else {
            // 上传成功。
            chunk.setState(UFileConst.SUCCESS);
            ufileRepo.updateUFileChunkState(fileUid, chunkSn, UFileConst.SUCCESS);
        }

        // [4] 如果所有分片都上传完成，则合并成完整文件，并且删除分片。
        if (this.needMerge(fileUid)) {
            this.mergeChunks(fileUid);
        }

        return chunk;
    }

    /**
     * 合并分片。
     *
     * @param fileUid 上传文件唯一 ID。
     */
    public void mergeChunks(String fileUid) {
        // 合并是一个耗时操作，4G 文件合并需要 20S 左右，所以在多线程中进行合并。
        // 由于保证了所有分片都会上传到同一个 DSC 服务，所以使用本地锁机制即可，不需要分布式锁保证合并冲突。
        new Thread(() -> {
            try {
                mergeUploadedFileWhenAllChunksSuccessfullyUploaded(fileUid);
            } catch (IOException e) {
                log.warn("合并文件错误: {}", e.getMessage());
                ufileRepo.updateUFileState(fileUid, UFileConst.FAILED);
            }
        }).start();
    }

    /**
     * 当所有分片都上传成功后把所有分片按序号合并成文件。
     *
     * @param fileUid 上传文件唯一 ID。
     */
    public synchronized void mergeUploadedFileWhenAllChunksSuccessfullyUploaded(String fileUid) throws IOException {
        /*
         逻辑:
         1. 如果不需要合并则返回。
         2. 如果目标文件已存在:
            2.1 目标文件的 MD5 和上传文件的 MD5 相同，则更新文件上传状态为完成，并删除上传的分片。
            2.2 目标文件的 MD5 和上传文件的 MD5 不相同，则删除已存在的目标文件。
         3. 按照分片顺序合并分片到目标文件，合并成功后删除分片文件。
         */

        // [1] 如果不需要合并则返回。
        if (!needMerge(fileUid)) {
            return;
        }

        UFile ufile = ufileRepo.findUFile(fileUid);
        String targetFilePath = generateTargetFilePath(fileUid);
        ufileRepo.updateUFileState(fileUid, UFileConst.DOING);

        // [2] 如果目标文件已存在:
        if (targetFileExistsAtLocal(fileUid)) {
            String tempMd5 = Utils.md5(new File(targetFilePath));

            if (Objects.equals(ufile.getFileMd5(), tempMd5)) {
                // [2.1] 目标文件的 MD5 和上传文件的 MD5 相同，则更新文件上传状态为完成，并删除上传的分片。
                log.info("目标文件已经存在，且 MD5 和上传文件的 MD5 一样，不需要重复合并: fileUid [{}], path [{}]", fileUid, targetFilePath);
                ufileRepo.updateUFileState(fileUid, UFileConst.SUCCESS);
                removeChunkDir(fileUid);
                return;
            } else {
                // [2.2] 目标文件的 MD5 和上传文件的 MD5 不相同，则删除已存在的目标文件。
                log.info("目标文件已经存在，且 MD5 和上传文件的 MD5 不一样，删除已存在的目标文件: fileUid [{}], path [{}]", fileUid, targetFilePath);
                FileUtils.deleteQuietly(new File(targetFilePath));
            }
        }

        // [3] 按照分片顺序合并分片到目标文件，合并成功后删除分片文件 (查询得到的分片已经按分片序号排好序)。
        List<String> chunkPaths = new LinkedList<>();
        for (UFileChunk chunk : ufile.getChunks()) {
            String chunkPath = generateChunkPath(fileUid, chunk.getSn());
            chunkPaths.add(chunkPath);
        }

        log.info("[开始] 合并分片为目标文件: filePath [{}], chunkCount [{}], fileSize [{}]", targetFilePath, chunkPaths.size(), ufile.getFileSize());
        boolean mergeOk = mergeFiles(chunkPaths, targetFilePath, ufile.getFileMd5());

        if (mergeOk) {
            // 合并成功。
            ufileRepo.updateUFileState(fileUid, UFileConst.SUCCESS);
            // 删除分片文件目录。
            removeChunkDir(fileUid);

            log.info("[成功] 合并分片为目标文件: filePath [{}]", targetFilePath);

            if (ftpEnabled) {
                copyFileToFtp(fileUid);
            }
        } else {
            // 合并失败。
            ufileRepo.updateUFileState(fileUid, UFileConst.FAILED);
            log.info("[失败] 合并分片为目标文件错误: filePath [{}]", targetFilePath);
        }
    }

    /**
     * 合并传入的多个文件 srcPaths 为一个文件 dstPath。
     *
     * @param srcPaths 要被合并的文件路径。
     * @param dstPath 合并得到的文件路径。
     * @param dstMd5 合并得到的文件的 MD5.
     * @return 合并成功返回 true，失败返回 false。
     * @throws IOException 操作文件错误时抛出 IOException 异常。
     */
    public boolean mergeFiles(List<String> srcPaths, String dstPath, String dstMd5) throws IOException {
        // 如果目标目录不存在则创建。
        FileUtils.forceMkdirParent(new File(dstPath));

        // 创建要合并的目标文件，逐个合并分片。
        try (BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(Paths.get(dstPath)))) {
            for (String srcPath : srcPaths) {
                try (InputStream input = new BufferedInputStream(Files.newInputStream(Paths.get(srcPath)))) {
                    IOUtils.copy(input, output);
                }
            }
        }

        // 比较 MD5 验证文件的完整性。
        String tempMd5 = Utils.md5(new File(dstPath));
        if (!Objects.equals(dstMd5, tempMd5)) {
            log.warn("合并分片得到的文件 MD5 不匹配: dstMd5 [{}], currMd5 [{}]", dstMd5, tempMd5);
            return false;
        }

        return true;
    }

    /**
     * 判断分片是否需要合并。
     *
     * @param fileUid 上传文件的唯一 ID。
     * @return 分片都上传完成后需要合并分片 返回 true，否则返回 false。
     */
    public boolean needMerge(String fileUid) {
        // [1] 使用文件 Uid 查询上传文件对象。
        UFile ufile = ufileRepo.findUFile(fileUid);
        if (ufile == null) {
            return false;
        }

        // [2] 如果还有分片没有上传完成则返回，所有分片都上传成功了则进行合并。
        if (ufileRepo.countUnsuccessfulUFileChunk(fileUid) > 0) {
            return false;
        }

        // [3] 所有分片都上传成功了则进行合并，如果正在合并中则返回。
        if (ufile.getState() == UFileConst.DOING) {
            return false;
        }

        return true;
    }

    /**
     * 判断上传的目标文件在服务本地是否存在。
     *
     * @param fileUid 上传文件的唯一 ID。
     * @return 目标文件存在返回 true，否则返回 false。
     */
    public boolean targetFileExistsAtLocal(String fileUid) {
        String path = generateTargetFilePath(fileUid);
        return Files.exists(Paths.get(path));
    }

    /**
     * 判断上传的目录文件在 FTP 上是否存在。
     *
     * @param fileUid fileUid 上传文件的唯一 ID。
     * @return 目标文件存在返回 true，否则返回 false。
     */
    public boolean targetFileExistsInFtp(String fileUid) {
        // TODO: 补上逻辑
        return false;
    }

    /**
     * 生成上传文件的保存路径。
     *
     * @param fileUid 上传文件的唯一 ID。
     * @return 返回文件路径。
     */
    public String generateTargetFilePath(String fileUid) {
        return String.format("%s/%s", this.dstDir, fileUid);
    }

    /**
     * 生成分片的保存路径。
     *
     * @param fileUid 上传文件的唯一 ID。
     * @param chunkSn 分片序号。
     * @return 返回文件路径。
     */
    public String generateChunkPath(String fileUid, int chunkSn) {
        return String.format("%s/%s/%d.tmp", this.chunkBaseDir, fileUid, chunkSn);
    }

    /**
     * 删除传入 fileUid 的上传文件的所有分片。
     *
     * @param fileUid 返回文件路径。
     */
    public void removeChunkDir(String fileUid) {
        String chunkDir = String.format("%s/%s", this.chunkBaseDir, fileUid);
        FileUtils.deleteQuietly(new File(chunkDir));
    }

    /**
     * 把 fileUid 对应的文件复制到 FTP。
     *
     * @param fileUid 上传文件的唯一 ID。
     */
    public void copyFileToFtp(String fileUid) {
        // TODO: 把文件复制到 FTP
        log.info("复制文件到 FTP...");
    }
}
