import org.junit.Test;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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
    public void detectFileEncoding() throws Exception {
        String path = "/Users/biao/Documents/公司文档/项目管理/DSC/客户/渤海人寿/CAL_WAGE_GX_NEW.pck";
        try (InputStream fis = Files.newInputStream(Paths.get(path))) {
            // (1)
            byte[] buf = new byte[4096]; // 4*1024 = 4K
            UniversalDetector detector = new UniversalDetector();

            // (2)
            int readCount;
            int totalCount = 0;
            while ((readCount = fis.read(buf)) > 0 && !detector.isDone() && totalCount < 40960) {
                detector.handleData(buf, 0, readCount);
                totalCount += readCount;
            }
            System.out.println(totalCount);
            // (3)
            detector.dataEnd();

            // (4)
            String encoding = detector.getDetectedCharset();
            if (encoding != null) {
                System.out.println("Detected encoding = " + encoding);
            } else {
                System.out.println("No encoding detected.");
            }

            // (5)
            detector.reset();
        }
    }
}
