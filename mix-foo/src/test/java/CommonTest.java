import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommonTest {
    @Test
    public void foo1() {
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
    public void testFile() {
        File file = new File("/Users/biao/Downloads/temp/arthas.zip2");
        System.out.println(file.exists());
    }
}
