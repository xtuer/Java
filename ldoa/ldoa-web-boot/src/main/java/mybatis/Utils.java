package mybatis;

/**
 * MyBatis Mapper 里使用的辅助工具类。
 */
public final class Utils {
    /**
     * 判断输入字符串 s 非空
     * @param s 要判断的字符串
     * @return 非空返回 true，否则返回 false
     */
    public static boolean notEmpty(String s) {
        return s != null && !"".equals(s);
    }
}
