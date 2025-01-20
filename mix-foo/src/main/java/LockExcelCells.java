import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LockExcelCells {
    public static void main(String[] args) throws Exception {
        String templatePath = "/Users/biao/Desktop/tpl.xlsx";
        String targetPath   = "/Users/biao/Desktop/tpl-1.xlsx";

        FileUtils.deleteQuietly(new File(targetPath));

        // 模拟数据。
        List<Map<String, Object>> rows = new LinkedList<>();
        Map<String, Object> row1 = ImmutableMap.of(
                "name", "Alice", "age", 10
        );
        Map<String, Object> row2 = ImmutableMap.of(
                "name", "Bob", "age", 10
        );
        rows.add(row1);
        rows.add(row2);

        ExcelWriter excelWriter = null;
        try {
            excelWriter = EasyExcel.write(targetPath).withTemplate(templatePath).build();
            WriteSheet writeSheet = EasyExcel.writerSheet().build();

            // 分批写入数据。
            excelWriter.fill(rows, writeSheet);
            excelWriter.fill(rows, writeSheet);
        } finally {
            // 千万别忘记关闭流。
            if (excelWriter != null) {
                excelWriter.close();
            }
        }
    }
}
