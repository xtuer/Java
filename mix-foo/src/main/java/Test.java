import com.google.common.base.Stopwatch;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class Test {
    public static void main(String[] args) throws IOException {
        // Stopwatch watch = Stopwatch.createStarted();
        // for (int i = 0; i < 1_000_000_000; i++) {
        //     try {
        //         int k = i+1;
        //     } catch (Exception ex) {
        //
        //     }
        //     // int k = i;
        // }
        // System.out.println(watch.stop().elapsed(TimeUnit.MILLISECONDS));

        String str = "/srv/salt/base/scripts-temp/2022-06-23/x.sh-13217088053930812980.sh";
        String base = "/srv/salt/base";
        System.out.println(str.substring(base.length()));
    }
}
