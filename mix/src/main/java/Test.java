import java.nio.file.Files;
import java.nio.file.Paths;

public class Test {
    public static void main(String[] args) throws Exception {
        System.out.println(Files.probeContentType(Paths.get("a.txt")));
    }
}
