import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import xtuer.util.Utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.*;

public class CommonTest {

    @Test
    public void testSpi() throws Exception {
        Enumeration<URL> driverUrls = ClassLoader.getSystemClassLoader().getResources("META-INF/services/java.sql.Driver");

        while (driverUrls.hasMoreElements()) {
            System.out.println(driverUrls.nextElement());
            System.out.println();
        }
    }

    @Test
    public void testCompare() {
        List<String> tokens = new ArrayList<>(Arrays.asList("One", "Two", "Three", "Four"));
        tokens.sort(String::compareTo);
        System.out.println(tokens);
    }

    @Test
    public void testCanonicalPath() throws IOException {
        String path = "/Users/foo//foo/./hi/goo/../nor";
        String canonicalPath = new File(path).getCanonicalPath();
        System.out.println(canonicalPath);
    }

    static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false";
    static final String USER   = "root";
    static final String PASS   = "root";

    @Test
    public void testJdbc() throws Exception {
        try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            // conn.setCatalog("mysql");
            DatabaseMetaData md = conn.getMetaData();
            // System.out.println(md.getTableTypes());
            // System.out.println(md.getStringFunctions());
            ResultSet rs = md.getTables("mysql", "", null, null);
            rs = md.getTableTypes();
            while (rs.next()) {
                Utils.dump(rs);
            }
        }
    }

    @Test
    public void testx() {
        System.out.println(new File("/root", "foo.txt").getAbsolutePath());
        System.out.println(new File("/root/", "foo.txt").getAbsolutePath());

    }
}
