import java.nio.charset.Charset;
import java.sql.*;
import java.util.*;

public class Test {
    static final String DB_URL = "jdbc:mysql://192.168.12.21:35004/test?useSSL=false";
    static final String USER   = "root";
    static final String PASS   = "mypass";

    public static void main(String[] args) {
        List<String> list = new ArrayList<>(3);
        Collections.fill(list, "?");
        System.out.println(list);
    }
}
