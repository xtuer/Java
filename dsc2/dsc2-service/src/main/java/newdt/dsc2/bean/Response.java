package newdt.dsc2.bean;

import lombok.Getter;
import lombok.Setter;
import newdt.dsc2.util.Utils;

/**
 * <pre>
 * 规定应用中 Api 接口都返回统一格式的 Json 数据，方便前端调用，通过类 Response + Json HttpMessageConverter 来实现自动转换。
 * 类 Response 提供了多个变种的方法 ok() 和 fail() 简化创建 Response 对象。
 *
 * 成功关注的是数据，失败关注的是错误信息，所以
 *     A. 方法 ok() 的核心是 data (code 无特殊情况都为 0)
 *     B. 方法 fail() 的核心是 message
 */
@Getter
@Setter
public final class Response<T> {
    private int     code;    // 状态码，一般是当 success 为 true 或者 false 不足够表达时才使用，平时忽略即可
    private boolean success; // 成功时为 true，失败时为 false
    private String  message; // 成功或则失败时的描述信息
    private String  stack;   // 抛出异常时的堆栈信息
    private T       data;    // 成功或则失败时的更多详细数据，一般失败时不需要

    public Response(boolean success, String message) {
        this(success, message, null);
    }

    public Response(boolean success, String message, T data) {
        this(success, message, data, 0);
    }

    public Response(boolean success, String message, T data, int code) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.code = code;
    }

    public static <T> Response<T> ok() {
        return Response.ok(null, "success");
    }

    public static <T> Response<T> ok(T data) {
        return Response.ok(data, "success");
    }

    public static <T> Response<T> ok(T data, String message, String ...messageArgs) {
        if (messageArgs.length > 0) {
            message = Utils.replaceBracePlaceholder(message, messageArgs);
        }

        return new Response<>(true, message, data, 0);
    }

    public static <T> Response<T> fail() {
        return Response.fail("fail", 0);
    }

    public static <T> Response<T> fail(String message, String ...messageArgs) {
        if (messageArgs.length > 0) {
            message = Utils.replaceBracePlaceholder(message, messageArgs);
        }

        return Response.fail(message, 0);
    }

    public static <T> Response<T> fail(String message, int code) {
        return new Response<>(false, message, null, code);
    }

    /**
     * 返回单个对象时根据对象是否为空返回成功或者失败的不同结果:
     *     A. data 不为 null 时执行 Result.ok(data)
     *     B. data 等于 null 时执行 Result.failMessage(error)
     */
    public static <T> Response<T> single(T data) {
        return Response.single(data, "");
    }

    public static <T> Response<T> single(T data, String error) {
        return (data != null) ? Response.ok(data) : Response.fail(error);
    }
}
