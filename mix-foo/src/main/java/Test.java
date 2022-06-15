import com.google.common.base.Stopwatch;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class Test {
    public static void main(String[] args) throws IOException {
        Stopwatch watch = Stopwatch.createStarted();
        for (int i = 0; i < 1_000_000_000; i++) {
            try {
                int k = i+1;
            } catch (Exception ex) {

            }
            // int k = i;
        }
        System.out.println(watch.stop().elapsed(TimeUnit.MILLISECONDS));
    }
}
