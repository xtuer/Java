import org.junit.Test;
import util.SqlExtractor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

public class SqlExtractorTest {
    /**
     * SQL 文件的路径。
     */
    private static final String SQL_FILE_PATH = "/Users/biao/Downloads/big.sql";

    // 案例一: 从文件中读取。
    @Test
    public void testReadSqlFromBigFile() throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(SQL_FILE_PATH))) {
            AtomicBoolean stopped = new AtomicBoolean(false);
            SqlExtractor.extractSqls(reader, 4, stopped, sqls -> {
                System.out.println("---------------------------------------");
                for (String sql : sqls) {
                    System.out.println("==> " + sql);
                }
                // stopped.set(true);
            });
        }
    }

    // 案例二: 从字符串中读取。
    @Test
    public void testReadSqlFromString() throws Exception {
        String content = Files.readString(Paths.get(SQL_FILE_PATH));
        try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
            AtomicBoolean stopped = new AtomicBoolean(false);
            SqlExtractor.extractSqls(reader, 4, stopped, sqls -> {
                System.out.println("---------------------------------------");
                for (String sql : sqls) {
                    System.out.println("==> " + sql);
                }
                // stopped.set(true);
            });
        }
    }
}
