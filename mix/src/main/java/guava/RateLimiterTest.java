package guava;

import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RateLimiterTest {
    // [1] 并发控制，1 次/秒
    private static final RateLimiter limiter = RateLimiter.create(1);

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // 瞬间产生大量并发请求，但是 RateLimiter 控制并行
        for (int i = 0; i < 20; ++i) {
            executor.submit(RateLimiterTest::doWork);
        }

        executor.shutdown();
    }

    public static void doWork() {
        double at = limiter.acquire(); // [2] 阻塞直到得到令牌
        System.out.println(Thread.currentThread() + " at " + at);
    }
}
