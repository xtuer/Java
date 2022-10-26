package dsc;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 敏感数据规则: 邮箱地址。
 * 邮箱地址正则参考文章: https://www.jianshu.com/p/5966a2d9df75
 */
public class SensitiveRuleOfEmail implements SensitiveRule {
    /**
     * 邮箱地址正则表达式
     */
    private static final Pattern pattern = Pattern.compile("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");

    @Override
    public boolean test(String text) {
        /*
         逻辑:
         1. 去掉 text 前后的空格
         2. text 包含字符
         3. text 符合邮箱地址正则表达式
         */

        // [1] 去掉 text 前后的空格
        text = StringUtils.trimWhitespace(text);

        // [2] text 包含字符
        if (!StringUtils.hasText(text)) {
            return false;
        }

        // [3] text 符合邮箱地址正则表达式
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }
}
