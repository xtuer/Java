import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Sign {
    public static void main(String[] args) throws IOException {
        String appId = "015B512C873648578FB2C32BD5677BD4";
        String appKey = "927170905ECA42FC9813DD7EED21A5AF";

        // 参与签名的参数
        Map<String, String> params = new HashMap<>();
        params.put("app_id", appId);
        params.put("app_key", appKey);
        params.put("username", "alice");
        params.put("productId", "1001");
        params.put("signedTime", "1499914521231");

        // 签名
        String signString = sign(params); // 281879C9007C3698D1106F9CF6A097A3

        // URL 中不包含 app_key，但要包含 sign 用以校验
        String queryString = urlQueryString(params, "app_key") + "&sign=" + signString;
        System.out.println(queryString);
    }

    /**
     * 计算签名
     *
     * @param params 参数 key value 的 map
     * @return 返回签名字符串
     */
    public static String sign(Map<String, String> params) {
        String queryString = urlQueryString(params);
        String signValue = DigestUtils.md5DigestAsHex(queryString.getBytes(StandardCharsets.UTF_8)).toUpperCase(); // 使用 MD5 计算签名字符串
        System.out.println(queryString);

        return signValue;
    }

    /**
     * 计算 URL 的请求字符串
     *
     * @param params 参数 key value 的 map
     * @param ignoredKeys 需要忽略的 key
     * @return 返回 URL 的请求字符串
     */
    public static String urlQueryString(Map<String, String> params, String... ignoredKeys) {
        // 按照 key 进行升序排序，然后 key value 拼接成 key=value 的格式
        Map<String, String> keyValueSet = new TreeMap<>(params);
        List<String> keyValueList = new LinkedList<>();

        // 删除被忽略的部分
        for (String s : ignoredKeys) {
            keyValueSet.remove(s);
        }

        for (String key : keyValueSet.keySet()) {
            Optional.ofNullable(keyValueSet.get(key)).ifPresent(value -> {
                keyValueList.add(key + "=" + value);
            });
        }

        String queryString = String.join("&", keyValueList);
        return queryString;
    }
}
