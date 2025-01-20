package oss.minio;

import io.minio.DownloadObjectArgs;
import io.minio.MinioClient;

public class MinioDownloader {
    public static void main(String[] args) throws Exception {
        try (MinioClient client = MinioClient
                .builder()
                .endpoint("http://192.168.12.164:9000")
                .credentials("ROOTNAME", "CHANGEME123")
                .build()) {

            // 注意 (抛出异常情况):
            // - 本地目录必须存在，Minio 不会为我们创建，不存在则会抛异常。
            // - 已经存在同名文件，抛出异常。
            client.downloadObject(DownloadObjectArgs
                    .builder()
                    .bucket("dsc")
                    .object("2025-01-17/Java-Question.md")
                    .filename("/Users/biao/Desktop/a/x.md")
                    .build());
        }

        System.out.println("下载完成");
    }
}
