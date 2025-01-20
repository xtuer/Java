package oss.minio;

import io.minio.MinioClient;
import io.minio.UploadObjectArgs;

public class MinioUploader {
    public static void main(String[] args) throws Exception {
        try (MinioClient client = MinioClient.builder()
                .endpoint("http://192.168.12.164:9000")
                .credentials("ROOTNAME", "CHANGEME123")
                .build()) {

            // 提示:
            // - 上传的文件 "Java-题目.md" 在 Minio 中保存在目录 "2025-01-17" 下且名字为 "Java-Question.md"。
            // - 上传同名文件会被替换掉，不会抛出异常。
            client.uploadObject(UploadObjectArgs.builder()
                    .bucket("dsc")
                    .object("2025-01-17/Java-Question.md")
                    .filename("/Users/biao/Downloads/Java-题目.md")
                    .build()
            );
        }

        System.out.println("上传完成");
    }
}
