import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;

public class CommonDownloadUtil {
    public static void downloadFile(String url, String localPath) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet(url);

            System.out.println("Executing request " + httpget.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());

                // Get hold of the response entity
                HttpEntity entity = response.getEntity();

                // If the response does not enclose an entity, there is no need
                // to bother about connection release
                if (entity != null) {
                    InputStream in = entity.getContent();
                    try {
                        // do something useful with the response
                        byte[] buffer = new byte[1024];
                        BufferedInputStream bufferedIn = new BufferedInputStream(in);
                        int len = 0;

                        FileOutputStream fileOutStream = new FileOutputStream(new File(localPath));
                        BufferedOutputStream bufferedOut = new BufferedOutputStream(fileOutStream);

                        while ((len = bufferedIn.read(buffer, 0, 1024)) != -1) {
                            bufferedOut.write(buffer, 0, len);
                        }
                        bufferedOut.flush();
                        bufferedOut.close();
                    } catch (IOException ex) {
                        // In case of an IOException the connection will be released
                        // back to the connection manager automatically
                        throw ex;
                    } finally {
                        // Closing the input stream will trigger connection release
                        in.close();
                    }
                }
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }

    public final static void main(String[] args) throws Exception {
        downloadFile("http://xtuer.github.io/img/dog.png", "/Users/Biao/Desktop/a.png"); // 下载图片
    }
}
