package newdt.dsc2.exception;

import lombok.extern.slf4j.Slf4j;
import newdt.dsc2.bean.Response;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Set;

/**
 * 全局异常处理器。
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Response<String> handleException(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        String msg = "";

        if (ex instanceof MissingServletRequestParameterException) {
            msg = MessageFormat.format("缺少参数 {0}", ((MissingServletRequestParameterException) ex).getParameterName());
        } else if (ex instanceof MethodArgumentTypeMismatchException) {
            msg = MessageFormat.format("参数 {0} 类型转换异常", ((MethodArgumentTypeMismatchException) ex).getName());
        } else if (ex instanceof HttpMessageNotReadableException) {
            msg = "反序列化 JSON 错误";
        } else if (ex instanceof ConstraintViolationException) {
            msg = getConstraintViolationMsg((ConstraintViolationException) ex);
        } else if (ex instanceof MethodArgumentNotValidException) {
            msg = getBindingExceptionMsg(((MethodArgumentNotValidException) ex).getBindingResult());
        } else if (ex instanceof BindException){
            msg = getBindingExceptionMsg(((BindException) ex).getBindingResult());
        } else {
            msg = ex.getMessage();
        }

        String stack = getStackTrace(ex);
        Response<String> rsp =new Response<>(false, msg, "", 500);
        rsp.setStack(stack);

        log.warn(stack);

        return rsp;
    }

    private String getConstraintViolationMsg(ConstraintViolationException ex) {
        Set<ConstraintViolation<?>> sets = ex.getConstraintViolations();
        if (sets == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sets.forEach(error -> {
            // if (error instanceof FieldError) {
            //     sb.append(((FieldError) error).getField()).append(": ");
            // }
            sb.append(error.getMessage()).append("\n");
        });

        return sb.toString();
    }

    private String getBindingExceptionMsg(BindingResult result) {
        StringBuilder sb = new StringBuilder();

        for (FieldError error : result.getFieldErrors()) {
            // sb.append(error.getField() + " : " + error.getDefaultMessage() + "\n");
            sb.append(error.getDefaultMessage()).append("\n");
        }

        return sb.toString();
    }

    /**
     * 把异常转为字符串方便输出。
     *
     * @param throwable 异常对象。
     * @return 返回异常的字符串表示。
     */
    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}
