import misc.auto.ndtagent.upload.DirUploader;
import misc.util.Utils;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
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
        File file = new File("/Users/biao/Downloads/temp/arthas.zip2");
        System.out.println(file.exists());
    }
}
