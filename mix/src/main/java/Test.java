import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class Test {
    public static void main(String[] args) throws Exception {
        // 协变
        InputStream[] ips = new FileInputStream[3];
        ips[0] = new BufferedInputStream(new FileInputStream(""));

        // 泛型解决协变的问题
        List<? extends InputStream> list = new LinkedList<FileInputStream>();
        list.add(new FileInputStream(""));
    }
}
