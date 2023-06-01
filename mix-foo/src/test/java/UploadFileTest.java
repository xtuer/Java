import org.junit.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import util.Utils;

import java.io.File;

public class UploadFileTest {
    private static final RestTemplate restTemplate = new RestTemplate();

    @Test
    public void uploadFile() throws Exception {
        HttpHeaders headers = new HttpHeaders();

        // [2] 设置上传文件的 header。
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // [3] 读取分片内容。

        // [4] 上传的文件对象: fileEntity
        ContentDisposition contentDisposition = ContentDisposition
                .builder("form-data")
                .name("file")
                // .filename("chunk-中国.txt")
                .build();

        // [3] 请求体设置 fileEntity 和表单参数。
        // 创建文件资源
        File file = new File("/Users/biao/Downloads/面试问题.md");
        FileSystemResource fileResource = new FileSystemResource(file);
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("file", fileResource);
        form.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());

        String url = "http://localhost:8080/form/upload/temp/file";
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(form, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        Utils.dump(response);
    }
}
