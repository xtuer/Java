import com.csvreader.CsvWriter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CsvTest {
    public static void main(String[] args) throws IOException {
        CsvWriter writer = new CsvWriter("/Users/biao/Desktop/test.csv", ',', StandardCharsets.UTF_8);
        writer.setForceQualifier(true);

        // 写入 CSV Headers
        writer.writeRecord(new String[] { "账号", "类型", "角色", "时间", "内容\n回车" });

        // 写入 CSV 内容
        writer.writeRecord(new String[] { "1", "2", "3", "4", "5" });
        writer.writeRecord(new String[] { "1", "2", "3", "4", "5" });
        writer.writeRecord(new String[] { "1", "2", "3", "4", "5" });

        // 结束写入，保存到文件
        writer.close();
    }
}
