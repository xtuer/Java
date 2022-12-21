import com.fasterxml.jackson.core.type.TypeReference;
import misc.auto.agent.AgentJob;
import misc.auto.agent.MemStats;
import misc.auto.agent.RequestUtils;
import misc.auto.agent.Response;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

public class RestTest {
    // 测试使用范型获取响应。
    // A. 没有参数。
    // B. 没有 header。
    @Test
    public void testSimpleGenericRequest() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        ParameterizedTypeReference<Response<MemStats>> typeRef = new ParameterizedTypeReference<Response<MemStats>>() {};
        ResponseEntity<Response<MemStats>> rspEntity = restTemplate.exchange("http://localhost:8080/api/memStats", HttpMethod.GET, null, typeRef);
        Response<MemStats> rsp = rspEntity.getBody();

        System.out.println(rsp);
        System.out.println(rsp.getData().getClass()); // 输出: class rest.MemStats
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
        ResponseEntity<Response<Integer>> rspEntity = restTemplate.exchange("http://localhost:8080/api/jobs/count", HttpMethod.GET, new HttpEntity<>(headers), typeRef);
        Response<Integer> rsp = rspEntity.getBody();

        System.out.println(rsp);
    }

    // 测试执行命令。
    @Test
    public void testExecuteCmd() throws Exception {
        AgentJob job = new AgentJob();
        job.setCmd("for ((i=0;i<5;i++));do echo $i; sleep 1; done;");

        Response<AgentJob> rsp = RequestUtils.doRequest("http://localhost:8080/api/jobs/execute/cmd", HttpMethod.POST, job, AgentJob.class);

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

        Response<AgentJob> rsp = RequestUtils.doRequest("http://localhost:8080/api/jobs/execute/script", HttpMethod.POST, job, AgentJob.class);

        if (rsp == null) {
            return;
        }

        System.out.println(rsp);
    }

    // 测试获取 Job 信息。
    @Test
    public void testGetJob() {
        Response<AgentJob> rsp = RequestUtils.doRequest("http://localhost:8080/api/jobs/55d07fb88dda4873b08192077f13e732", HttpMethod.GET, null, new TypeReference<AgentJob>() {});
        System.out.println(rsp.getData());
    }

    // 测试获取 Job 数量。
    @Test
    public void testGetJobCount() {
        Response<Integer> rsp = RequestUtils.doRequest("http://localhost:8080/api/jobs/count", HttpMethod.GET, null, Integer.class);
        int count = Optional.ofNullable(rsp).map(Response::getData).orElse(0);
        System.out.println(count);
    }
}
