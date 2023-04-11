import org.apache.commons.io.FileUtils;
import org.junit.Test;
import util.SqlExtractor;
import util.SqlExtractorUsingStateMachine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SqlExtractorTest {
    /**
     * SQL 文件的路径。
     */
    private static final String SQL_FILE_PATH = "/Users/biao/Downloads/big.sql";
    // private static final String SQL_FILE_PATH = "/Users/biao/Documents/temp/sqls/insert-sql-5000000.sql";

    // 案例一: 从文件中读取。
    @Test
    public void testReadSqlFromBigFile() throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(SQL_FILE_PATH))) {
            AtomicBoolean stopped = new AtomicBoolean(false);
            AtomicInteger count = new AtomicInteger(0);
            SqlExtractor.extractSqls(reader, 100, stopped, (sqls, finished) -> {
                // System.out.println("---------------------------------------");
                for (String sql : sqls) {
                    // System.out.println("==> " + sql);
                    count.addAndGet(1);
                }
                // stopped.set(true);
                return null;
            });
            System.out.println(count.get());
        }
    }

    // 案例二: 从字符串中读取。
    @Test
    public void testReadSqlFromString() throws Exception {
        String content = FileUtils.readFileToString(new File(SQL_FILE_PATH), StandardCharsets.UTF_8); // Files.readString(Paths.get(SQL_FILE_PATH));
        try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
            AtomicBoolean stopped = new AtomicBoolean(false);
            SqlExtractor.extractSqls(reader, 4, stopped, (sqls, finished) -> {
                System.out.println("---------------------------------------");
                for (String sql : sqls) {
                    System.out.println("==> " + sql);
                }
                // stopped.set(true);

                return null;
            });
        }
    }

    // 案例一: 从文件中读取。
    @Test
    public void testReadSqlFromBigFileUsingStateMachine() throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(SQL_FILE_PATH))) {
            AtomicBoolean stopped = new AtomicBoolean(false);
            AtomicInteger count = new AtomicInteger(0);
            SqlExtractorUsingStateMachine.extractSqls(reader, 100, stopped, (sqls, finished) -> {
                // System.out.println("---------------------------------------");
                for (String sql : sqls) {
                    // System.out.println("==> " + sql);
                    count.addAndGet(1);
                }
                // stopped.set(true);
                return null;
            });
            System.out.println(count.get());
        }
    }
}
