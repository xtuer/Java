package misc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * 清洗泰康日志
 */
public class TKLogClean {
    // 包含了下列字符串的日志过滤掉
    public static final String[] EXCLUDE_LOGS = {
            "LEVEL",
            "RestoreUtilSubUtil",
            "调用接口",
            "eureka",
            "environment",
            "ConfigServicePropertySourceLocator",
            "ConfigClusterResolver",
            "Fetching",
            "WebLogAspect",

    };

    public static boolean usefulLog(String str) {
        if (str.isEmpty()) {
            return false;
        }

        for (String l : EXCLUDE_LOGS) {
            if (str.contains(l)) {
                return false;
            }
        }

        return true;
    }
    public static void main(String[] args) throws Exception {
        StringBuffer logs = new StringBuffer();

        // 读取所有日志，并且过滤掉不需要的日志
        try (Stream<Path> stream = Files.list(Paths.get("/Users/biao/Desktop/2022-07-27/24"))) {
            stream.filter(path -> path.toString().endsWith(".log")).forEach(path -> {
                try {
                    Files.readAllLines(path, StandardCharsets.UTF_8).stream().filter(TKLogClean::usefulLog).forEach(line -> {
                        logs.append(line).append("\n");
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            System.out.println(logs);
        }

    }
}
