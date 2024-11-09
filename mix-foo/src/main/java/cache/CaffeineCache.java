package cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;

import java.time.Duration;

public class CaffeineCache {
    public static void main(String[] args) throws Exception {
        Cache<String, String> cache = Caffeine.newBuilder()
                .removalListener((key, value, cause) -> {
                    if (cause == RemovalCause.EXPIRED) {
                        // 元素即使超时了也不会主动调用，而是调用 cache.getIfPresent() 时发现元素超时了才会调用。
                        System.out.println("expire: key: " + key);
                    }

                    System.out.println("cause: " + cause + ", key: " + key);
                })
                .expireAfterWrite(Duration.ofSeconds(1))
                .build();

        cache.put("1", "One");
        cache.put("2", "Two");

        cache.invalidate("2");
        System.out.println(cache.getIfPresent("1"));

        Thread.sleep(2_000);
        // System.out.println(cache.getIfPresent("1"));

        Thread.sleep(10_000);

    }
}
