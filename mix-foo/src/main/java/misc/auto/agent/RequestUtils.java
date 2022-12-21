package misc.auto.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Agent 的请求工具类 (不是通用类，是因为其绑定了 Agent 需要的签名信息)。
 */
@Slf4j
public final class RequestUtils {
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
        log.info("发起请求，URL [{}]", url);

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

        log.info("请求结束，URL [{}]", url);

        return rsp;
    }
}
