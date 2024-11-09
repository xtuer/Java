public class WildcardToRegex {
    /**
     * 在 Java 中，通配符 (wildcard) 通常用于文件名模式匹配，如 * 表示任意数量的字符，? 表示单个字符。
     * 为了使用正则表达式 (regex) 进行更强大的模式匹配，可以将通配符模式转换为正则表达式模式。
     *
     * 通配符到正则表达式的转换规则:
     * - * 转换为 .*
     * - ? 转换为 .
     *
     * 此外，需要对其他正则表达式中的特殊字符 (如 ., ^, $, [, ], (, ), {, }, |, +, \)进行转义，以避免它们在转换后的正则表达式中具有特殊意义。
     *
     * @param wildcard 通配符表达式。
     * @return 返回正则表达式。
     */
    public static String convert(String wildcard) {
        StringBuilder sb = new StringBuilder();
        sb.append("^"); // 匹配开始

        for (int i = 0; i < wildcard.length(); i++) {
            char c = wildcard.charAt(i);
            switch (c) {
                case '*':
                    sb.append(".*"); // 匹配任意字符 (包括空字符)。
                    break;
                case '?':
                    sb.append("."); // 匹配任意单个字符。
                    break;
                case '.':
                case '^':
                case '$':
                case '[':
                case ']':
                case '(':
                case ')':
                case '{':
                case '}':
                case '|':
                case '+':
                case '\\':
                    sb.append("\\").append(c); // 转义其他正则特殊字符。
                    break;
                default:
                    sb.append(c); // 其他字符原样添加。
                    break;
            }
        }

        sb.append("$"); // 匹配结束。
        return sb.toString();
    }

    public static void main(String[] args) {
        String wildcard = "192.168.1.*";
        String regex = convert(wildcard);

        System.out.println("Wildcard: " + wildcard);
        System.out.println("Regex: " + regex);

        // 测试
        String filename = "192.168.1.13";
        boolean matches = filename.matches(regex);
        System.out.println("Does '" + filename + "' match? " + matches);
    }
}
