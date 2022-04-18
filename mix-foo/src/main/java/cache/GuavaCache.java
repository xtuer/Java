package cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Guava 的 LoadingCache 介绍。
 * 只能设置最大数量和超时时间。
 * 参考: https://segmentfault.com/a/1190000011105644
 */
public class GuavaCache {
    public static void main(String[] args) throws ExecutionException {
        // Values are automatically loaded by the cache。
        // 使用 LoadingCache#get 如果 value 不存在，则调用 CacheLoader#load 方法获取数据。
        LoadingCache<String, String> cache = CacheBuilder.newBuilder()
                .maximumSize(10)
                .expireAfterAccess(30, TimeUnit.SECONDS)
                .build(CacheLoader.from(key -> {
                    System.out.println("Fetch value of key: " + key);

                    // 回查数据库，如果查不到记录则返回 null，这时 LoadingCache 会抛异常 InvalidCacheLoadException
                    return "key-1".equals(key) ? null : key + "-value";
                }));

        System.out.println(cache.get("Hello"));

        try {
            System.out.println(cache.get("key-1"));
        } catch (CacheLoader.InvalidCacheLoadException ex) {
            // 抛出异常时，说明数据库里没有数据，则把数据插入到数据库和缓存中。
            System.err.println(ex.getMessage());
            cache.put("key-1", "key-1-value");
        }

        // 这次获取 "key-1" 的缓存不再抛出异常，因为上面已经把它的值更新到了缓存里。
        System.out.println(cache.get("key-1"));
    }
}
