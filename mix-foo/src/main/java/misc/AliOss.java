package misc;

import com.alibaba.fastjson.JSON;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * 上传文档: https://help.aliyun.com/document_detail/32013.html?spm=a2c4g.11186623.3.4.3ac339a5K0c1T7
 * 简单上传: https://help.aliyun.com/document_detail/84781.html?spm=a2c4g.11186623.2.7.218d59aa5mAief#concept-84781-zh
 */
public class AliOss {
    public static void main(String[] args) throws FileNotFoundException {
        long start = System.currentTimeMillis();
        System.out.println("Start: " + start);

        // [1] Endpoint 以北京为例，其它 Region 请按实际情况填写
        // [2] 阿里云主账号 AccessKey 拥有所有 API 的访问权限，风险很高，可以使用子账号的
        String endpoint        = "oss-cn-beijing.aliyuncs.com";
        String accessKeyId     = "LTAI4FoHxoqGpSvNfAW4T8Zg";
        String accessKeySecret = "Y4DUYZ920XAlC07P6m5I8MZGLDixWp";

        // [3] 创建 OSSClient 实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // [4] 创建 PutObjectRequest 对象
        String bucketName = "biaomac";         // Bucket 的名字
        String filePath   = "avatar/tear.jpg"; // 文件在 Bucket 中的位置，可以带上目录
        File   localFile  = new File("/Users/Biao/Pictures/tear.jpg");
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, filePath, localFile);

        // [6] 上传文件
        PutObjectResult result = ossClient.putObject(putObjectRequest); // 上传失败抛异常 (RuntimeException)
        System.out.println(JSON.toJSONString(result, true));

        // [7] 关闭 OSSClient
        ossClient.shutdown();

        // [8] 输出访问文件的 URL: Bucket 域名 + 文件在 Bucket 中的路径
        //     如 https://biaomac.oss-cn-beijing.aliyuncs.com/avatar/tear.jpg
        String url = "https://biaomac.oss-cn-beijing.aliyuncs.com/" + filePath;
        System.out.println(url);
        System.out.println("End: " + (System.currentTimeMillis() - start));
    }
}
