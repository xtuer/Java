import com.google.common.collect.ImmutableMap;
import org.apache.commons.text.StringSubstitutor;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommonTest {
    @Test
    public void generateImageGallery() throws IOException {
        String path = "/Users/biao/Documents/workspace/Books/笔记/img";
        List<String> dirs = new LinkedList<>();

        // 在此列表中的 title 都要转为大写
        List<String> upperCases = Arrays.asList("db", "fe", "ds", "dp", "k8s");

        // 获取 img 下的所有目录
        try (Stream<Path> stream = Files.list(Paths.get(path))) {
            stream.forEach(p -> {
                if (Files.isDirectory(p)) {
                    dirs.add(p.getFileName().toString());
                }
            });
        }

        dirs.sort(String::compareTo);
        dirs.add(0, ".");

        for (String dir : dirs) {
            // title 首字母大写
            String title = dir.substring(0, 1).toUpperCase() + dir.substring(1);

            // title 全大写
            if (upperCases.contains(title.toLowerCase())) {
                title = title.toUpperCase();
            }

            if (".".equals(dir)) {
                dir = "";
            }

            System.out.printf("## %s\n" +
                            "```img-gallery\n" +
                            "path: img/%s\n" +
                            "type: vertical\n" +
                            "radius: 6\n" +
                            "columns: 4\n" +
                            "```\n\n",
                    title, dir);
        }
    }

    @Test
    public void foo2() {
        String[] statements = {
                "hello world",
                "hello java",
                "hello go",
        };

        Map<String, Long> result = Stream.of(statements)
                .map(stmt -> stmt.split(" "))
                // .map(Arrays::asList)
                .flatMap(Stream::of)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        System.out.println(result);
    }

    @Test
    public void testRegex() {
        System.out.println(checkVersion("v1.0.2"));
        System.out.println(checkVersion("v1.0.a2"));
    }

    public static boolean checkVersion(String version) {
        return version.matches("^v\\d+(\\.\\d+)*$");
    }

    @Test
    public void bar() {
        long start = System.currentTimeMillis();

        File file = new File("/Users/biao/Documents/temp/log-error-2022-11-04.46.log1127740749091859.tmp");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int startLine = 200000;
            int lineCount = 10;
            int endLine = startLine + lineCount;
            String line;
            StringBuilder buf = new StringBuilder();
            int i = 0;
            while ((line = reader.readLine()) != null) {
                // i++;
                //
                // if (i >= startLine) {
                //     buf.append(line).append("\n");
                // }
                // if (i >= endLine) {
                //     break;
                // }
                i++;
            }
            System.out.println("line count: " + i);
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        long end = System.currentTimeMillis();
        System.out.println("Time used: " + (end - start));
    }

    @Test
    public void testFile() {
        for (int i = 0; i < 1000; i++) {
            System.out.printf("insert into test_1(id, name) values(%d, 'alice');\n", i);
        }
    }

    @Test
    public void bar2() throws Exception {
        String template = "Hello ${name}, your age is ${age}";

        // 使用常用 Map。
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Alice");
        map.put("age", 12);
        String result = StringSubstitutor.replace(template, map);

        System.out.println(result); // 输出: Hello Alice, your age is 12

        // 如果只有一个值，可以使用 Collections.singletonMap() 创建 Map。
        System.out.println(StringSubstitutor.replace(template, Collections.singletonMap("name", "Alice")));

        // 还可以使用 Guava 的 ImmutableMap.of("k1", "v1", "k2", "v2") 创建 Map。
        System.out.println(StringSubstitutor.replace(template, ImmutableMap.of(
                "name", "Alice",
                "age", 20
        )));
    }
}
