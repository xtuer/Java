import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.io.File;

public class TestZip {
    public static void main(String[] args) throws Exception {
        // 要压缩的文件
        File fileToZip = new File("/Users/biao/Downloads/mongo-test.js");

        // 设置 Zip 参数
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(CompressionMethod.DEFLATE);
        zipParameters.setCompressionLevel(CompressionLevel.NORMAL);
        zipParameters.setEncryptFiles(true); // 启用加密
        zipParameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD); // 使用标准加密

        // 创建 Zip 文件并添加文件
        ZipFile zipFile = new ZipFile("/Users/biao/Downloads/x.zip", "123".toCharArray());

        // 压缩单个文件
        zipFile.addFile(fileToZip, zipParameters);

        // 压缩目录
        zipFile.addFolder(new File("/Users/biao/Downloads/a"), zipParameters);

        System.out.println("Zip 文件生成成功！");
    }
}
