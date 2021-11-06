import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.TimeUnit;

public class Test {
    // [1] 并发控制，每秒生成 1 个令牌 (令牌桶算法控制流量)，还有个设计为保持多久的令牌 (漏桶中最多存放令牌数)，并不是桶里只有 1 个令牌
    private static final RateLimiter limiter = RateLimiter.create(1, 10, TimeUnit.SECONDS);

    public static void main(String[] args) {
        // 瞬间产生大量并发请求，但是 RateLimiter 控制并行
        Stopwatch watch = Stopwatch.createStarted();
        double at = limiter.acquire(1); 
        System.out.println("go");
        System.out.println(watch.elapsed(TimeUnit.SECONDS));
        System.out.println(at);
        at = limiter.acquire(1);
        System.out.println("go");
        System.out.println(watch.elapsed(TimeUnit.SECONDS));
        System.out.println(at);
    }
}
