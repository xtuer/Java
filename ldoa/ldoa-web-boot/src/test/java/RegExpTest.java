import org.junit.jupiter.api.Test;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExpTest {
    @Test
    public void replaceOneByOne() {
        int lastIndex = 0;
        String text = "<111>-TO-<234>";
        StringBuilder output = new StringBuilder();
        Pattern pattern = Pattern.compile("<(\\d+)>");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            int n = Integer.parseInt(matcher.group(1));
            output.append(text, lastIndex, matcher.start()).append(n * 2);
            lastIndex = matcher.end();
        }

        System.out.println(output);
    }

    @Test
    public void testReplace() {
        String text = "<111>-to-<234>-go"; // 输出: 222-to-468-go
        // text = "Box";                   // 输出: Box
        Pattern pattern = Pattern.compile("<(\\d+)>");
        System.out.println(RegExpTest.replace(text, pattern, matcher -> Integer.parseInt(matcher.group(1)) * 2 + ""));
    }

    @Test
    public void testPlaceholder() {
        String text = "用户{}的{}成绩不存在";

        System.out.println(RegExpTest.replaceBracePlaceholder(text, "小明"));
        System.out.println(RegExpTest.replaceBracePlaceholder(text, "小明", "语文"));
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

        return RegExpTest.replace(text, PATTERN_BRACE, matcher -> {
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
