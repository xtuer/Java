import org.junit.Test;
import util.SqlImporter;

/**
 * 测试导入大 SQL 文件 (不能有查询语句)。
 */
public class SqlImporterTest {
    /**
     * SQL 文件的路径。
     */
    private static final String SQL_FILE_PATH = "/Users/biao/Documents/temp/sqls/insert-sql-20000.sql";

    /**
     * 导入出错时回滚 SQL 语句。
     * 提示: 用户输入。
     */
    static final String ROLLBACK_SQL = "truncate table test_performance";

    /**
     * 提示: 下面 2 个属性可以放到配置文件中。
     */
    static int lineCount = 100; // 每次从 SQL 文件中读取的行数。
    static int batchSize = 150; // 批量执行的 SQL 语句数量。


    // 测试从大文件中读取 SQL 并执行。
    @Test
    public void testImportSqlFromBigFile() {
        SqlImporter importer = new SqlImporter();
        importer.importSqlFile(SQL_FILE_PATH, ROLLBACK_SQL, batchSize, lineCount);
    }
}
