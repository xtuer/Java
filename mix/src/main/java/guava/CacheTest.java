package guava;

import bean.User;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CacheTest {
    public static void main(String[] args) throws ExecutionException {
        // 注意: 不支持 null 的 value，如果一定会有 null，要么捕捉异常，要么 value 封装为 Optional<User>
        LoadingCache<String, User> cache = CacheBuilder.newBuilder()
                .maximumSize(10)
                .expireAfterAccess(30, TimeUnit.SECONDS)
                .build(CacheLoader.from(key -> {
                    log.info("Fetch user {}", key);
                    return findUserByUsername(key);
                }));

        System.out.println(cache.get("Alice"));
        System.out.println(cache.get("Alice"));

        cache.put("John", new User().setUsername("John"));
        System.out.println(cache.get("John"));

        System.out.println(cache.getIfPresent("Jack"));
        System.out.println(cache.get("Jack")); // CacheLoader returned null for key Jack.
    }

    public static User findUserByUsername(String username) {
        if ("Alice".equals(username)) {
            return new User().setUsername("Alice");
        } else if ("Bob".equals(username)) {
            return new User().setUsername("Bob");
        } else {
            return null;
        }
    }
}
