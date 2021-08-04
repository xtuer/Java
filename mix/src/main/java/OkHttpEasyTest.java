import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class OkHttpEasyTest {
    public static void main(String[] args) throws Exception {
        // 调用 https() 则信任所有证书
        // String responseData = HttpClient.get("https://www.baidu.com").execute().asString(); // 自动处理 CA 签发的证书
        // System.out.println(responseData);

        testK8S();
    }

    // http://hc.apache.org/httpcomponents-client-5.1.x/index.html
    public static void testK8S() throws IOException {
        // 创建HttpClient实例
        String url = "https://www.baidu.com";
        // HttpClient client =  HttpClientBuilder.create().build();
        CloseableHttpClient client = HttpClients
                .custom()
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();

        // 根据URL创建HttpGet实例
        HttpGet get = new HttpGet(url);
        // 执行get请求，得到返回体
        HttpResponse response = client.execute(get);

        // 判断是否正常返回
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            // 解析数据
            String data = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            System.out.println(data);
        }
    }
}
