package misc.auto.agent;

import lombok.Data;

/**
 * 请求的响应对象。
 */
@Data
public class Response<T> {
    /**
     * 业务逻辑相关的 code，不是 HTTP Status Code。
     */
    private int code;

    /**
     * 业务逻辑处理成功时为 true，错误时为 false。
     */
    private boolean success;

    /**
     * 请求的描述。
     */
    private String msg;

    /**
     * 请求的 payload。
     */
    private T data;
}
