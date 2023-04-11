package com.xtuer.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtuer.bean.Result;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * 简单的请求工具类。
 */
public class RequestUtils {
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
    public static <T> Result<T> doRequest(String url, HttpMethod method, Object requestBody, Class<T> dataClass) {
        Result<T> rsp = doRequest(url, method, requestBody);

        if (rsp == null || rsp.getData() == null) {
            return rsp;
        }

        // 此时 rsp.getData() 其实是 LinkedHashMap，并不是最终需要的范型 T 的对象，需要转换一下。
        T data = objectMapper.convertValue(rsp.getData(), dataClass);
        rsp.setData(data);

        return rsp;
    }

    private static <T> Result<T> doRequest(String url, HttpMethod method, Object requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");

        // 执行请求。
        ParameterizedTypeReference<Result<T>> typeRef = new ParameterizedTypeReference<Result<T>>() {};
        ResponseEntity<Result<T>> rspEntity = restTemplate.exchange(url, method, new HttpEntity<>(requestBody, headers), typeRef);
        Result<T> rsp = rspEntity.getBody();

        return rsp;
    }
}
