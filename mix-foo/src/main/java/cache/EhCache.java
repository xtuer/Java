package cache;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.Configuration;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;

import java.net.URL;

public class EhCache {
    public static void main(String[] args) throws InterruptedException {
        // [1] 创建 CacheManager
        URL url = EhCache.class.getClassLoader().getResource("ehcache.xml");
        Configuration xmlConfig = new XmlConfiguration(url);
        CacheManager cacheManager = CacheManagerBuilder.newCacheManager(xmlConfig);
        cacheManager.init();

        // [2] CacheManager 创建 Cache
        Cache<String, String> cache = cacheManager.getCache("bar", String.class, String.class);

        // [3] 使用 Cache
        for (int i = 1; i <= 100; i++) {
            cache.put("key-" + i, "value-" + i);
        }

        System.out.println(cache.get("key-1"));
        System.out.println(cache.get("key-100"));

        Thread.sleep(6000);

        System.out.println(cache.get("key-1"));
        System.out.println(cache.get("key-100"));

        // [4] 程序结束时关闭 CacheManager，会把缓存保存到磁盘
        cacheManager.close();
    }
}
