package misc.auto.ndtagent.upload;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 目录上传类。
 */
@Slf4j
public class DirUploader {
    /**
     * Agent 的 IP。
     */
    private final String ip;

    /**
     * Agent 的 Port。
     */
    private final int port;

    /**
     * 要上传的目录。
     */
    private final String srcDir;

    /**
     * 文件在 Agent 上保存的基础目录。
     */
    private final String dstDir;

    /**
     * 创建目录上传的对象。
     *
     * @param ip Agent 的 IP。
     * @param port Agent 的 Port。
     * @param srcDir 要上传的目录路径。
     * @param dstDir 文件在 Agent 上保存的基础目录。
     */
    public DirUploader(String ip, int port, String srcDir, String dstDir) {
        this.ip = ip;
        this.port = port;
        this.srcDir = srcDir;
        this.dstDir = dstDir;
    }

    /**
     * 上传目录，支持递归上传目录中的所有文件。
     *
     * 例如:
     *   srcDir 是 /Users/biao/Documents/temp/inter，
     *   dstDir 是 /Users/biao/Downloads，
     * 则会把 inter 整个目录上传到 Downloads 目录里，上传后则 Agent 主机上得到目录 /Users/biao/Downloads/inter，
     * 其中 Downloads/inter 中的文件结构和 srcDir 的 inter 的文件结构一样。
     *
     * @return 目录上传成功返回 true，失败返回 false。
     */
    public boolean uploadDir() {
        log.info("上传目录: 源目录 [{}], 保存目录 [{}]", this.srcDir, this.dstDir);

        // 统计出文件的 srcPath, dstDir，逐个上传文件。
        // 上传目录失败，删除目标目录。
        Map<String, String> paths = DirUploader.generateUploadPaths(this.srcDir, this.dstDir);

        for (Map.Entry<String, String> entry : paths.entrySet()) {
            String srcPath = entry.getKey();
            String saveDir = entry.getValue();
            FileUploader uploader = new FileUploader(this.ip, this.port, srcPath, saveDir);

            // 上传失败则终止上传。
            if (!uploader.uploadFile()) {
                log.warn("[失败] 上传目录中的文件失败: SrcPath [{}], DstDir [{}]", srcPath, saveDir);
                log.info("[失败] 上传目录失败: 源目录 [{}], 保存目录 [{}]", this.srcDir, this.dstDir);
                return false;
            }
        }

        log.info("[成功] 上传目录成功: 源目录 [{}], 保存目录 [{}]", this.srcDir, this.dstDir);

        return true;
    }

    /**
     * 遍历要上传的目录 srcDir，为它里面的每一个文件生成上传路径信息保存到 Map 里。Map 的 key 为要上传的文件路径，value 为此文件在 Agent 上保存的目录。
     *
     * @param srcDir 要上传的目录路径。
     * @param dstDir 文件在 Agent 上保存的基础目录。
     * @return 返回上传路径的 Map。
     */
    public static Map<String, String> generateUploadPaths(String srcDir, String dstDir) {
        /*
         逻辑:
         1. 获取 srcDir 的真实路径 (软连接的真实路径)，并且去掉路径中不规则的部分，例如多个 /，. 等。
         2. 拼接出保存的目录，包含了 srcDir 的目录名。
         3. 遍历 srcDir 的所有文件 (不包含隐藏文件)。
         4. 计算出每个文件相对于 srcDir 的相对路径。
         5. 计算出每个文件保存的目录。
         */
        // key 为要上传的文件路径，value 为此文件在 Agent 上保存的目录。
        final Map<String, String> paths = new HashMap<>();

        // [1] 获取 srcDir 的真实路径 (软连接的真实路径)，并且去掉路径中不规则的部分，例如多个 /，. 等。
        final Path realSrcDir;
        try {
            // 解决软连接和去掉路径中不规则的部分。
            realSrcDir = Paths.get(srcDir).toRealPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final int realSrcDirLength = realSrcDir.toString().length();

        // [2] 拼接出保存的目录，包含了 srcDir 的目录名。
        final Path realDstDir = Paths.get(dstDir).resolve(realSrcDir.getFileName().toString());

        // [3] 遍历 srcDir 的所有文件 (不包含隐藏文件)。
        try (Stream<Path> pathStream = Files.walk(realSrcDir)) {
            pathStream.filter(path -> !path.getFileName().toString().startsWith(".")) // 过滤掉隐藏文件。
                    .filter(path -> path.toFile().isFile()) // 过滤掉目录，只保留文件。
                    .map(Path::toString) // Path 转为字符串，方便后面使用。
                    .forEach(srcPath -> { // 遍历每一个文件。
                        // [4] 计算出每个文件相对于 srcDir 的相对路径。
                        String relativePathToSrcDir = srcPath.substring(realSrcDirLength);

                        // [5] 计算出每个文件保存的目录。
                        String savePath = realDstDir + relativePathToSrcDir;
                        String saveDir = Paths.get(savePath).getParent().toString();

                        paths.put(srcPath, saveDir);
                    });
        } catch (IOException e) {
            // formalSrcDir 不存在时抛出的异常。
            throw new RuntimeException(e);
        }

        return paths;
    }
}
