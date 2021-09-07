import org.apache.commons.io.FilenameUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Test {
    public static void main(String[] args) throws Exception {
        Path dir = Paths.get("D:/电子书/k8s-张磊/");

        // [1] 递归列出文件夹下的所有文件及子文件夹
        Files.list(dir).forEach(path -> {
            String fileName = path.getFileName().toString();
            String baseName = FilenameUtils.getBaseName(fileName);
            baseName = baseName.replaceAll("^(\\d+)\\s+", "");
            String a = String.format("<li><a href=\"%s\">%s</a></li>", fileName, baseName);
            System.out.println(a);
        });
    }
}
