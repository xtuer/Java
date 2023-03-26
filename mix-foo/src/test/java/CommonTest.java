import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommonTest {
    @Test
    public void foo1() {
        List<? super String> list = new LinkedList<Object>();
        list.add("One");
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
}
