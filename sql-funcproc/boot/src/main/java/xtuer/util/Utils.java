package xtuer.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Utils {
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
     * 计算二进制数据的 MD5.
     *
     * @param data 二进制数组
     * @return 返回 MD5
     */
    public static String md5(byte[] data) {
        return DigestUtils.md5DigestAsHex(data);
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
     * 输出对象到控制台
     *
     * @param object 要输出的对象
     */
    public static void dump(Object object) {
        System.out.println(Utils.toJson(object));
    }

    public static void dump(ResultSet rs) throws SQLException {
        System.out.println(Utils.toJson(new BasicRowProcessor().toMap(rs)));
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
            e.printStackTrace();
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
            e.printStackTrace();
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
     * CompareVersion 比较版本，版本的格式为 ^v\d+(\.\d+)*$，以 v 开头，数字间以英文句号分隔。
     * 逐个按照数字部分大小进行比较，直到第一个不相等。
     * v1 > v2 返回正数。
     * v1 = v2 返回 0。
     * v1 < v2 返回负数。
     * v1, v2 不符合版本格式抛出异常。
     *
     * 示例:
     * compareVersion("v0.8.21", "v0.8.10") 返回 11
     * compareVersion("v0.9.21", "v0.8") 返回 1
     * compareVersion("v0.9", "v0.8.21") 返回 1
     *
     * @param v1 版本 1
     * @param v2 版本 2
     * @return v1 < v2 返回负数，v1 = v2 返回 0，v1 > v2 返回正数。
     */
    public static int compareVersion(String v1, String v2) {
        /*
         逻辑:
         1. 版本格式校验。
         2. 如果 v1 == v2 则返回 0。
         3. 去掉版本前面的 v，然后按照 . 进行分隔，得到数值的数组。
         4. 逐个比较数组中的元素，返回第一个不相等的元素差。
         */
        Objects.requireNonNull(v1);
        Objects.requireNonNull(v2);

        // [1] 版本格式校验。
        String pattern = "^v\\d+(\\.\\d+)*$";
        if (!v1.matches(pattern)) {
            throw new IllegalArgumentException("v1 格式不对");
        }
        if (!v2.matches(pattern)) {
            throw new IllegalArgumentException("v2 格式不对");
        }

        // [2] 如果 v1 == v2 则返回 0。
        if (v1.equals(v2)) {
            return 0;
        }

        // [3] 去掉版本前面的 v，然后按照 . 进行分隔，得到数值的数组。
        int[] ns1 = splitToIntegerArray(v1.substring(1));
        int[] ns2 = splitToIntegerArray(v2.substring(1));

        // [4] 逐个比较数组中的元素，返回第一个不相等的元素差。
        int comm = Math.min(ns1.length, ns2.length);
        for (int i = 0; i < comm; i++) {
            if (ns1[i] == (ns2[i])) {
                continue;
            }
            return ns1[i] - ns2[i];
        }

        return ns1.length - ns2.length;
    }

    private static int[] splitToIntegerArray(String s) {
        return Stream.of(s.split("\\.")).mapToInt(Integer::parseInt).toArray();
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
