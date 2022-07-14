import com.google.common.base.Stopwatch;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    private static final SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss");

    public static void main(String[] args) throws IOException {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
        // scheduler.scheduleWithFixedDelay(() -> {
        //     System.out.println(formatter.format(new Date()));
        //
        //     try {
        //         TimeUnit.SECONDS.sleep(2);
        //     } catch (InterruptedException e) {
        //         throw new RuntimeException(e);
        //     }
        // }, 0, 3, TimeUnit.SECONDS);

        // 不会并发执行
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println(formatter.format(new Date()));

            try {
                TimeUnit.SECONDS.sleep(5);
                System.out.println(Thread.currentThread());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, 0, 2, TimeUnit.SECONDS);
    }
}
