package dsc.desensitivate;

import com.google.common.collect.ImmutableList;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 敏感列扫描测试
 */
public class ScanTest {
    public static void main(String[] args) throws SQLException, ExecutionException, InterruptedException {
        SensitiveColumnsScanner scanTask = new SensitiveColumnsScanner(new MetaVo(),
                "gateway",
                "SELECT table_name FROM information_schema.tables WHERE table_schema = '${databaseName}'",
                "SELECT * FROM ${tableName} LIMIT ${rowCount}",
                10,
                ImmutableList.of(1, 2, 3, 4),
                0.5);

        List<SensitiveColumnsScanner.SensitiveColumn> sensitiveColumns = scanTask.scan();
        System.out.println(sensitiveColumns);
    }
}
