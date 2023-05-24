import misc.auto.ndtagent.upload.AgentFileTransfer;
import misc.auto.ndtagent.upload.DirUploader;
import misc.auto.ndtagent.upload.FileUploader;
import misc.auto.ndtagent.upload.Md5Utils;
import util.Utils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public class UploadTest {
    // 测试文件的 MD5 和读取文件内容到二进制数组里计算 MD5。
    @Test
    public void testMd5() throws IOException {
        File file = new File("/Users/biao/Downloads/gotop");

        // 文件的 MD5
        System.out.println(Md5Utils.md5(file));

        // 文件二进制内容的 MD5
        System.out.println(Md5Utils.md5(Files.readAllBytes(file.toPath())));
    }

    @Test
    public void testBigFileMd5() throws IOException {
        File file = new File("/Users/biao/Documents/workspace/win11-arm.iso");

        // 文件的 MD5
        System.out.println(Md5Utils.md5(file));
    }

    // 测试在 Agent 上创建上传的文件信息。
    /*@Test
    public void testCreateUploadedFile() {
        UploadedFile uf = FileUploader.requestCreateUploadedFile("127.0.0.1", 12301, "/Users/biao/Downloads/arthas.zip", "/root/foo");
        System.out.println(uf);
    }

    @Test
    public void testRequestUploadedFile() {
        UploadedFile uf = FileUploader.requestUploadedFile("127.0.0.1", 12301, "fb9e041c2037a794cd8b1a44949f023e-ed642c96d27c32ac98c4cd41e4916e33");
        System.out.println(uf);
    }

    // 测试上传分片。。
    @Test
    public void testUploadFileChunk() throws Exception {
        String srcPath = "/Users/biao/Documents/电子书籍/算法小抄官方完整版.pdf";
        UploadedFile uf = FileUploader.requestCreateUploadedFile("127.0.0.1", 12301, srcPath, "/Users/biao/Downloads/temp");
        for (UploadedFileChunk chunk : uf.getChunks()) {
            chunk = FileUploader.uploadFileChunk("127.0.0.1", 12301, uf.getUid(), srcPath, chunk);
            System.out.println(chunk);
        }
    }*/

    @Test
    public void testUploadBigFile() {
        FileUploader uploader = new FileUploader("127.0.0.1", 12301, "/Users/biao/Documents/workspace/win11-arm.iso", "/Users/biao/Downloads/temp");
        // FileUploader uploader = new FileUploader("127.0.0.1", 12301, "/Users/biao/Documents/workspace/win11-arm.iso", "/Users/biao/Downloads/temp");
        boolean result = uploader.uploadFile();
        System.out.println(result);
    }

    @Test
    public void testUploadFile() {
        // FileUploader uploader = new FileUploader("192.168.12.101", 12301, "/Users/biao/Documents/电子书籍/算法小抄官方完整版.pdf", "/root/");
        FileUploader uploader = new FileUploader("127.0.0.1", 12301, "/Users/biao/Documents/电子书籍/算法小抄官方完整版.pdf", "/Users/biao/Downloads/temp");
        // FileUploader uploader = new FileUploader("127.0.0.1", 12301, "/Users/biao/Documents/workspace/win11-arm.iso", "/Users/biao/Downloads/temp");
        boolean result = uploader.uploadFile();
        System.out.println(result);
    }

    // 递归遍历目录。
    @Test
    public void testWalkDir() {
        String srcDir = "/Users/biao/Downloads";
        String dstDir = "/root/";
        Map<String, String> paths = DirUploader.generateUploadPaths(srcDir, dstDir);
        Utils.dump(paths);
    }

    // 测试上传目录
    @Test
    public void testUploadDir() {
    // DirUploader uploader = new DirUploader("192.168.12.101", 12301, "/Users/biao/Documents/temp/inter", "/root/x/y");
        DirUploader uploader = new DirUploader("127.0.0.1", 12301, "/Users/biao/Documents/temp/inter", "/Users/biao/Downloads/temp");
        boolean result = uploader.uploadDir();
        System.out.println(result);
    }

    // 测试 Agent 间传文件。
    @Test
    public void testAgentTransferFile() {
        AgentFileTransfer transfer = new AgentFileTransfer("127.0.0.1", 12301, "127.0.0.1", 12301, "/Users/biao/Downloads/算法小抄官方完整版.pdf", "/Users/biao/Downloads/temp");
        // AgentFileTransfer transfer = new AgentFileTransfer("127.0.0.1", 12301, "192.168.12.101", 12301, "/Users/biao/Downloads/算法小抄官方完整版2.pdf", "/root/foo2");
        boolean result = transfer.transferFile();
        System.out.println(result);
    }
}
