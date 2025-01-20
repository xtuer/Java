package oss.minio;

import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;

public class MinioObjectCheck {
    public static void main(String[] args) throws Exception {
        try (MinioClient client = MinioClient
                .builder()
                .endpoint("http://192.168.12.164:9000")
                .credentials("ROOTNAME", "CHANGEME123")
                .build()) {

            // 查看对象的信息，如果对象不存在则抛出异常。
            StatObjectResponse rsp = client.statObject(StatObjectArgs
                    .builder()
                    .bucket("dsc")
                    .object("2025-01-16/Java-Question.md")
                    .build());

            System.out.println(rsp);
        } catch (Exception ex) {
            System.out.println("文件不存在");
            ex.printStackTrace();
        }
    }
}
