import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import misc.auto.ndtagent.*;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.util.Optional;

@Slf4j
public class RestTest {

    // 测试使用范型获取响应。
    // A. 没有参数。
    // B. 没有 header。
    @Test
    public void testSimpleGenericRequest() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        ParameterizedTypeReference<Response<MemStats>> typeRef = new ParameterizedTypeReference<Response<MemStats>>() {};
        ResponseEntity<Response<MemStats>> rspEntity = restTemplate.exchange("http://localhost:12301/api/memStats", HttpMethod.GET, null, typeRef);
        Response<MemStats> rsp = rspEntity.getBody();

        System.out.println(rsp);
        System.out.println(rsp.getData().getClass()); // 输出: class rest.MemStats
    }

    @Test
    public void testSimpleGenericRequest2() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        ParameterizedTypeReference<String> typeRef = new ParameterizedTypeReference<String>() {};
        ResponseEntity<String> rspEntity = restTemplate.exchange("http://localhost:12301/api/memStats", HttpMethod.GET, null, typeRef);
        String rsp = rspEntity.getBody();

        System.out.println(rsp);
        System.out.println(rsp.getClass()); // 输出: class java.lang.String
    }

    // 测试使用范型获取响应。
    // A. 有 header。
    @Test
    public void testSimpleHeaderRequest() throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("sign", "cea9973b520413dc4ead63e8439e92ed");
        headers.add("signAt", "1671540873");

        ParameterizedTypeReference<Response<Integer>> typeRef = new ParameterizedTypeReference<Response<Integer>>() {};
        ResponseEntity<Response<Integer>> rspEntity = restTemplate.exchange("http://localhost:12301/api/jobs/count", HttpMethod.GET, new HttpEntity<>(headers), typeRef);
        Response<Integer> rsp = rspEntity.getBody();

        System.out.println(rsp);
    }

    // 测试执行命令。
    @Test
    public void testExecuteCmd() throws Exception {
        AgentJob job = new AgentJob();
        job.setCmd("for ((i=0;i<5;i++));do echo $i; sleep 1; done;");

        Response<AgentJob> rsp = AgentRequestUtils.doRequest("http://localhost:12301/api/jobs/execute/cmd", HttpMethod.POST, job, AgentJob.class);

        if (rsp == null) {
            return;
        }

        System.out.println(rsp);
        System.out.println(rsp.getData().getClass());
        System.out.println(rsp.getData());
    }

    // 测试执行脚本。
    @Test
    public void testExecuteScript() throws Exception {
        AgentJob job = new AgentJob();
        job.setScriptContent("for ((i=0;i<10;i++));do echo $i; sleep 1; done;");
        job.setScriptType(AgentJob.SCRIPT_TYPE_SHELL);
        job.setScriptName("aloha.sh");
        job.setAsync(true);

        Response<AgentJob> rsp = AgentRequestUtils.doRequest("http://localhost:12301/api/jobs/execute/script", HttpMethod.POST, job, AgentJob.class);

        if (rsp == null) {
            return;
        }

        System.out.println(rsp);
    }

    // 测试获取 Job 信息。
    @Test
    public void testGetJob() {
        Response<AgentJob> rsp = AgentRequestUtils.doRequest("http://localhost:12301/api/jobs/55d07fb88dda4873b08192077f13e732", HttpMethod.GET, null, new TypeReference<AgentJob>() {});
        System.out.println(rsp.getData());
    }

    // 测试获取 Job 数量。
    @Test
    public void testGetJobCount() {
        Response<Integer> rsp = AgentRequestUtils.doRequest("http://localhost:12301/api/jobs/count", HttpMethod.GET, null, Integer.class);
        int count = Optional.ofNullable(rsp).map(Response::getData).orElse(0);
        System.out.println(count);
    }

    // 测试没有 data 的请求。
    @Test
    public void testNoData() {
        Response<Void> rsp = AgentRequestUtils.doRequest("http://192.168.12.101:12302/api/agents/stop", HttpMethod.POST, null, Void.class);
        System.out.println(rsp);
    }

    // 测试获取 Object 数量。
    @Test
    public void testWaitForState() {
        AgentRequestUtils.waitForCondition("http://localhost:12301/api/jobs/10a1d57bb6bd4e3a82ad2e6739661147", AgentJob.class, AgentJob::isFinished);
    }

    @Test
    public void testUploadFile() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:12301/api/uploads/chunks";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA); // [1] 设置 header
        // 每次请求都生成签名，因为是简单的计算不消耗多少计算资源，且不需要处理缓存问题。
        long signAt = System.currentTimeMillis() / 1000;
        String sign = SignUtils.sign("shindata", signAt);

        // 签名信息放到请求 Header 中。
        headers.add("sign", sign);
        headers.add("signAt", signAt + "");

        // [2] 文件对象: fileEntity
        File file = new File("/Users/biao/Documents/temp/x.sh");
        MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
        ContentDisposition contentDisposition = ContentDisposition
                .builder("form-data")
                .name("file")
                .filename(file.getName())
                .build();
        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        HttpEntity<byte[]> fileEntity = new HttpEntity<>(Files.readAllBytes(file.toPath()), fileMap);

        // [3] 请求体设置 fileEntity 和表单参数。
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("file", fileEntity);
        form.add("md5", "123");
        form.add("sn", "1");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(form, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        System.out.println(response.getBody());
    }

    private static final int MAX_FAULT_TOLERANCE = 20;
    private static final int REQUEST_JOB_STATE_INTERVAL = 5;


}
