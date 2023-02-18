package misc.auto.ndtagent;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * 签名工具。
 */
public final class SignUtils {

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
     * 生成签名。
     *
     * @param secret 签名密钥。
     * @param signAt 签名时间，单位秒。
     * @return 返回签名字符串。
     */
    public static String sign(String secret, long signAt) {
        String salt = "newdt";
        return md5(signAt + md5(secret + salt));
    }

    public static void main(String[] args) throws Exception {
        long signAt = System.currentTimeMillis() / 1000;
        signAt = 1671169517;
        String secret = "shindata";
        String sign = SignUtils.sign(secret, signAt);
        System.out.printf("sign=%s&signAt=%d\n", sign, signAt);
        System.out.printf("-H 'sign: %s' -H 'signAt: %d'\n", sign, signAt);
    }
}
