package com.xtuer.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 常用功能的工具类，例如计算 MD5, Base64，UUID 等
 */
@Slf4j
public final class Utils {
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
        try (InputStream in = Files.newInputStream(file.toPath())) {
            return DigestUtils.md5DigestAsHex(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
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
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

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
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

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

    /**
     * 把堆栈信息转为字符串。
     *
     * @param throwable Throwable 对象。
     * @return 返回堆栈的字符串表示。
     */
    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}
