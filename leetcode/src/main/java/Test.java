import java.nio.charset.Charset;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

public class Test {
    static final String DB_URL = "jdbc:mysql://192.168.12.21:35004/test?useSSL=false";
    static final String USER   = "root";
    static final String PASS   = "mypass";

    public static void main(String[] args) {
        foo(1, 2, 3);
        foo();
    }

    public static void foo(Integer ...ns) {
        List<Integer> list = Arrays.asList(ns);
        System.out.println(list);
    }
}
