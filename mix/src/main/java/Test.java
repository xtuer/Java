import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;

public class Test {
    public static void main(String[] args) throws Exception {
        System.out.println(Files.probeContentType(Paths.get("foo/test.txt")));
        System.out.println(Files.probeContentType(Paths.get("foo/test.xlsx")));
        System.out.println(Files.probeContentType(Paths.get("foo/test.bib")));
        System.out.println(Files.probeContentType(Paths.get("foo/txt")));
    }
}
