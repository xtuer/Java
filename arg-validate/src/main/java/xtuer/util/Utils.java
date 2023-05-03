package xtuer.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 常用功能的工具类，例如计算 MD5, Base64，UUID 等
 */
@Slf4j
public final class Utils {
    private static final DateFormat DATE_FORMATTER_WITH_WEEKDAY = new SimpleDateFormat("M 月 d 日 E", Locale.SIMPLIFIED_CHINESE);

    /**
     * BindingResult 中的错误信息很多，对用户不够友好，使用 getBindingMessage()
     * 提取对用户阅读友好的定义验证规则 message.
     *
     * @param result 验证的结果对象
     * @return 验证规则 message
     */
    public static String getBindingMessage(BindingResult result) {
        StringBuilder sb = new StringBuilder();

        for (FieldError error : result.getFieldErrors()) {
            // sb.append(error.getField() + " : " + error.getDefaultMessage() + "\n");
            sb.append(error.getDefaultMessage()).append("\n");
        }

        return sb.toString();
    }

    /**
     * 计算字符串的 MD5.
     *
     * @param text 需要计算 MD5 的字符串
     * @return 返回字符串的 MD5
     */
    public static String md5(String text) {
        return DigestUtils.md5DigestAsHex(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算文件的 MD5.
     * MD5 包含 16 进制表示的 10 个字符: 0-9, a-z
     *
     * @param file 需要计算 MD5 的文件
     * @return 返回文件的 MD5，如果出错，例如文件不存在则返回 null
     */
    public static String md5(File file) {
        try (InputStream in = new FileInputStream(file)) {
            return DigestUtils.md5DigestAsHex(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 对字符串 text 进行 Base64 编码.
     * Base64 有 64 个字符: 0-9, a-z, A-Z, +, /
     * 等号 = 用于补齐.
     *
     * @param text 要进行编码的字符串
     * @return 返回使用 Base64 编码后的字符串
     */
    public static String base64(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解码 Base64 编码的字符串 base64Text.
     *
     * @param base64Text Base64 编码的字符串
     * @return 返回源字符串
     */
    public static String unbase64(String base64Text) {
        return new String(Base64.getDecoder().decode(base64Text), StandardCharsets.UTF_8);
    }

    /**
     * 对字符串 text 进行 URL Safe 的 Base64 编码: +, /, =, 被置换为 -, _, *, 只包含 64 个 URL safe 的字符: 0-9, a-z, A-Z, -, _
     * 注意: Base64.getUrlEncoder() 编码后的 Base64 结果还有 =，不能使用
     * <p>
     * 系统中有一些值使用 BASE64 编码后存储在 COOKIE 中, 当编码后的字符串最后有一个或者两个等号(=)时,
     * 使用 Request.getCookies().getValue() 会丢失等号, 再用 BASE64 解码时产生错误.
     *
     * @param text 要进行编码的字符串
     * @return 返回使用 URL Safe Base64 编码后的字符串
     */
    public static String base64UrlSafe(String text) {
        String base64Text = Utils.base64(text);
        base64Text = base64Text.replace('+', '-');
        base64Text = base64Text.replace('/', '_');
        base64Text = base64Text.replace('=', '*');

        return base64Text;
    }

    /**
     * 解码 URL Safe 的 Base64 编码的字符串 urlBase64Text.
     *
     * @param urlBase64Text URL Safe 的 Base64 编码的字符串
     * @return 返回源字符串
     */
    public static String unbase64UrlSafe(String urlBase64Text) {
        urlBase64Text = urlBase64Text.replace('-', '+');
        urlBase64Text = urlBase64Text.replace('_', '/');
        urlBase64Text = urlBase64Text.replace('*', '=');

        return Utils.unbase64(urlBase64Text);
    }

    /**
     * 生成 UUID，格式为 1E87E000-92C0-4660-B00D-FF92B37B0A7B
     *
     * @return 返回 UUID
     */
    public static String uuid() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    /**
     * 获取 date 这一天的开始时间，例如 2020-11-11 00:00:00
     *
     * @param date 日期
     * @return 返回 date 这一天的开始时间
     */
    public static Date startOfDay(Date date) {
        if (date == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    /**
     * 获取 date 这一天的最后时间，例如 2020-11-11 23:59:59
     *
     * @param date 日期
     * @return 返回 date 这一天的最后时间
     */
    public static Date endOfDay(Date date) {
        if (date == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTime();
    }

    /**
     * 输出对象到控制台
     *
     * @param object 要输出的对象
     */
    public static void dump(Object object) {
        System.out.println(Utils.toJson(object));
    }

    /**
     * 把对象转为 Json 字符串
     *
     * @param object 要转为 Json 字符串的对象
     * @return 返回对象的 Json 字符串表示
     */
    public static String toJson(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();

        // Date format
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));

        // Indent
        // objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // 2 个空格
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
        DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
        printer.indentObjectsWith(indenter); // Indent JSON objects
        printer.indentArraysWith(indenter);  // Indent JSON arrays

        try {
            return objectMapper.writer(printer).writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    /**
     * 把 JSON 字符串转为对象
     *
     * @param json  JSON 字符串
     * @param clazz 目标类
     * @return 返回得到的对象，转换失败时返回 null
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 把 JSON 字符串转为对象
     *
     * @param json JSON 字符串
     * @param ref  类型引用，用于集合类型
     * @return 返回得到的对象，转换失败时返回 null
     */
    public static <T> T fromJson(String json, TypeReference<T> ref) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(json, ref);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 花括号的 pattern
     */
    private static final Pattern PATTERN_BRACE = Pattern.compile("\\{}");

    /**
     * 使用数组 args 中的元素按顺序替换 text 中的 {} 占位符，如果 args 的元素个数少于 {} 的个数，则对应位置仍然返回 {}。
     *
     * 示例:
     * text = "用户{}的{}成绩不存在"
     * RegExpTest.replaceBracePlaceholder(text, "小明") => 用户小明的{}成绩不存在
     * RegExpTest.replaceBracePlaceholder(text, "小明", "语文") => 用户小明的语文成绩不存在
     *
     * @param text 要替换的字符串
     * @param args 替换 {} 的数组
     * @return 返回替换后的字符串
     */
    public static String replaceBracePlaceholder(String text, String ...args) {
        if (args.length == 0) {
            return text;
        }

        int[] index = new int[1]; // 为了个 Lambda 里传递可变的 int 数据

        return Utils.replace(text, PATTERN_BRACE, matcher -> {
            int i = index[0]++;

            if (i >= args.length) {
                return "{}";
            } else {
                return args[i];
            }
        });
    }

    /**
     * 替换字符串 text 中匹配 pattern 的子串，每个匹配的内容使用 converter 方法进行转换.
     *
     * @param text      要替换的字符串
     * @param pattern   正则表达式的 pattern
     * @param converter 转换函数
     * @return 返回替换后的字符串
     */
    public static String replace(String text, Pattern pattern, Function<Matcher, String> converter) {
        StringBuilder output = new StringBuilder();
        int indexAfterMatched = 0;
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            // [indexAfterMatched, matcher.start()) 之间的内容为字符串中不匹配的内容，原样复制到结果串中
            output.append(text, indexAfterMatched, matcher.start()).append(converter.apply(matcher));
            indexAfterMatched = matcher.end();
        }

        output.append(text, indexAfterMatched, text.length());

        return output.toString();
    }
}
