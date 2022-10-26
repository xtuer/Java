package dsc;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 敏感数据规则: 身份证号码。
 * 身份证号码正则参考文章: https://segmentfault.com/a/1190000016696368
 */
public class SensitiveRuleOfIdCardNo implements SensitiveRule {
    /**
     * 身份证号码正则表达式
     */
    private static final Pattern pattern = Pattern.compile("(^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$)|(^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}$)");

    @Override
    public boolean test(String text) {
        /*
         逻辑:
         1. 去掉 text 前后的空格
         2. text 包含字符
         3. text 有 15 或者 18 个字符
         4. text 符合身份证号码正则表达式
         */

        // [1] 去掉 text 前后的空格
        text = StringUtils.trimWhitespace(text);

        // [2] text 包含字符
        if (!StringUtils.hasText(text)) {
            return false;
        }

        // [3] text 有 15 或者 18 个字符
        if (text.length() != 15 && text.length() != 18) {
            return false;
        }

        // [4] text 符合身份证号码正则表达式
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }
}
