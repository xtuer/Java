package misc.auto.ndtagent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import misc.auto.ndtagent.upload.Md5Utils;
import misc.auto.ndtagent.upload.UploadedFileChunk;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Agent 的请求工具类 (不是通用类，是因为其绑定了 Agent 需要的签名信息)。
 */
@Slf4j
public final class AgentRequestUtils {
    private static final RestTemplate restTemplate = new RestTemplate();

    /**
     * Jackson 的 ObjectMapper 对象，用于转换类型。
     */
    private static final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * 执行请求。
     *
     * @param url 请求的 URL。
     * @param method 请求的 HTTP 方法，例如 HttpMethod.GET。
     * @param requestBody 请求体，如果没有则为 null。
     * @param dataClass Response 的 Data 的 Class。
     * @return 返回请求的响应，例如 <code>Response&lt;MemStats&gt;</code>。
     * @param <T> 请求响应的 payload 类型。
     */
    public static <T> Response<T> doRequest(String url, HttpMethod method, Object requestBody, Class<T> dataClass) {
        Response<T> rsp = doRequest(url, method, requestBody);

        if (rsp == null || rsp.getData() == null) {
            return rsp;
        }

        // 此时 rsp.getData() 其实是 LinkedHashMap，并不是最终需要的范型 T 的对象，需要转换一下。
        T data = objectMapper.convertValue(rsp.getData(), dataClass);
        rsp.setData(data);

        return rsp;
    }

    /**
     * 执行请求。
     *
     * @param url 请求的 URL。
     * @param method 请求的 HTTP 方法，例如 HttpMethod.GET。
     * @param requestBody 请求体，如果没有则为 null。
     * @param dataTypeRef Response 的 Data 的 TypeReference，解决 Data 本身又是范型或者集合的情况。
     * @return 返回请求的响应，例如 <code>Response&lt;MemStats&gt;</code>。
     * @param <T> 请求响应的 payload 类型。
     */
    public static <T> Response<T> doRequest(String url, HttpMethod method, Object requestBody, TypeReference<T> dataTypeRef) {
        Response<T> rsp = doRequest(url, method, requestBody);

        if (rsp == null || rsp.getData() == null) {
            return rsp;
        }

        // 此时 rsp.getData() 其实是 LinkedHashMap，并不是最终需要的范型 T 的对象，需要转换一下。
        T data = objectMapper.convertValue(rsp.getData(), dataTypeRef);
        rsp.setData(data);

        return rsp;
    }

    /**
     * 执行请求。
     *
     * @param url 请求的 URL。
     * @param method 请求的 HTTP 方法，例如 HttpMethod.GET。
     * @param requestBody 请求体，如果没有则为 null。
     * @return 返回请求的响应，例如 <code>Response&lt;MemStats&gt;</code>。
     *         需要注意的是这里具有欺骗性是因为 Response.data 其实底层类型是 LinkedHashMap，
     *         但这里仍然使用 Response&lt;T&gt; 而不是 Response&lt;?&gt? 是为了后面处理时方便一点。所以此方法是 private 的，避免被误用。
     * @param <T> 请求响应的 payload 类型。
     */
    private static <T> Response<T> doRequest(String url, HttpMethod method, Object requestBody) {
        // log.debug("发起请求，URL [{}]", url); // 不打印日志了，因为轮询状态的时候会打印大量日志

        // 每次请求都生成签名，因为是简单的计算不消耗多少计算资源，且不需要处理缓存问题。
        long signAt = System.currentTimeMillis() / 1000;
        String sign = SignUtils.sign("shindata", signAt);

        // 签名信息放到请求 Header 中。
        HttpHeaders headers = new HttpHeaders();
        headers.add("sign", sign);
        headers.add("signAt", signAt + "");
        headers.add("Content-Type", "application/json; charset=utf-8");

        // 执行请求。
        ParameterizedTypeReference<Response<T>> typeRef = new ParameterizedTypeReference<Response<T>>() {};
        ResponseEntity<Response<T>> rspEntity = restTemplate.exchange(url, method, new HttpEntity<>(requestBody, headers), typeRef);
        Response<T> rsp = rspEntity.getBody();

        // log.debug("请求结束，URL [{}]", url);

        return rsp;
    }

    /**
     * 上传文件分片。
     *
     * @param url 请求的 URL，例如 http://localhost:8080/api/uploads/7b4fde6eaa0fdbe34e194fca0868c683-ed642c96d27c32ac98c4cd41e4916e33/chunks
     * @param srcPath 要上传的文件路径。
     * @param chunk 文件分片对象。
     * @return payload 为服务器返回的分片信息。
     * @throws Exception 读取文件的异常。
     */
    public static Response<UploadedFileChunk> uploadFileChunk(String url, String srcPath, UploadedFileChunk chunk) throws Exception {
        HttpHeaders headers = new HttpHeaders();

        // [1] 每次请求都生成签名，因为是简单的计算不消耗多少计算资源，且不需要处理缓存问题。
        long signAt = System.currentTimeMillis() / 1000;
        String sign = SignUtils.sign("shindata", signAt);

        // 签名信息放到请求 Header 中。
        headers.add("sign", sign);
        headers.add("signAt", signAt + "");

        // [2] 设置上传文件的 header。
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // [3] 读取分片内容。

        // [4] 上传的文件对象: fileEntity
        MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
        ContentDisposition contentDisposition = ContentDisposition
                .builder("form-data")
                .name("file")
                .filename("chunk-" + chunk.getSn())
                .build();

        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        byte[] data = AgentRequestUtils.readFileChunkData(srcPath, chunk);
        HttpEntity<byte[]> fileEntity = new HttpEntity<>(data, fileMap);

        // [3] 请求体设置 fileEntity 和表单参数。
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("file", fileEntity);
        form.add("md5", Md5Utils.md5(data));
        form.add("sn", chunk.getSn());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(form, headers);
        ParameterizedTypeReference<Response<UploadedFileChunk>> typeRef = new ParameterizedTypeReference<Response<UploadedFileChunk>>() {};
        ResponseEntity<Response<UploadedFileChunk>> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, typeRef);

        return response.getBody();
    }

    /**
     * 读取文件分片的内容到二进制数组。
     *
     * @param srcPath 源文件路径。
     * @param chunk 文件分片对象。
     * @return 返回分片的内容到二进制数组。
     * @throws Exception 访问文件的异常。
     */
    public static byte[] readFileChunkData(String srcPath, UploadedFileChunk chunk) throws Exception {
        try (FileChannel fileChannel = FileChannel.open(Paths.get(srcPath), StandardOpenOption.READ)) {
            long size = chunk.getEndPos() - chunk.getStartPos();
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, chunk.getStartPos(), size);

            byte[] data = new byte[(int) size];
            buffer.get(data);
            return data;
        }
    }

    /**
     * 使用 GET 请求等待任务到达某一个条件。
     *
     * @param url 获取任务状态的 URL。
     * @param payloadClass 响应的 data 属性的类型。
     * @param conditionChecker 任务达到条件的 predicate。
     * @return 返回满足条件时的任务对象。
     */
    public static <T> T waitForCondition(String url, Class<T> payloadClass, Predicate<T> conditionChecker) {
        return waitForCondition(url, payloadClass, conditionChecker, 20, 5);
    }

    /**
     * 使用 GET 请求等待任务到达某一个条件。
     *
     * @param url 获取任务状态的 URL。
     * @param payloadClass 响应的 data 属性的类型。
     * @param conditionChecker 任务达到条件的 predicate。
     * @param maxFaultTolerance 执行请求如响应异常的容差次数。
     * @param retryIntervalInSeconds 执行请求的时间间隔 (单位为秒)。
     * @return 返回满足条件时的任务对象。
     * @exception RuntimeException 请求超过容错次数抛出运行时异常。
     */
    public static <T> T waitForCondition(String url, Class<T> payloadClass, Predicate<T> conditionChecker, int maxFaultTolerance, int retryIntervalInSeconds) {
        log.info("等待请求达到自定条件, URL [{}]", url);

        try {
            // 请求容错次数，使用 faultTolerance 解决暂时性的网络波动问题。
            int faultTolerance = 0;
            Response<T> rsp;

            while (faultTolerance < maxFaultTolerance) {
                // 请求之间等待几秒，避免短时间发起大量请求。
                TimeUnit.SECONDS.sleep(retryIntervalInSeconds);

                try {
                    rsp = AgentRequestUtils.doRequest(url, HttpMethod.GET, null, payloadClass);
                } catch (Exception ex) {
                    faultTolerance++;
                    log.warn("请求任务容错: {}", ex.getMessage());
                    continue;
                }

                if (rsp == null) {
                    faultTolerance++;
                    log.warn("请求任务容错: 响应为 null");
                    continue;
                }
                if (!rsp.isSuccess()) {
                    faultTolerance = 0;
                    continue;
                }
                if (rsp.getData() == null) {
                    faultTolerance++;
                    log.warn("请求任务容错: 响应的 Data 为 null");
                    continue;
                }

                // 达到状态则返回，否则继续请求。
                if (conditionChecker.test(rsp.getData())) {
                    return rsp.getData();
                }

                faultTolerance = 0;
            }
        } catch (InterruptedException ignored) {}

        throw new RuntimeException("获取任务异常: 无法访问 Agent，当 Agent 已退出、网络不可用等均可发生此问题");
    }
}
