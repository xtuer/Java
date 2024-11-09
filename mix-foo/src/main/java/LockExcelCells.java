import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class LockExcelCells {
    public static void main(String[] args) throws Exception {
        // Load the existing Excel file
        FileInputStream fis = new FileInputStream("/Users/biao/Downloads/user-import.xlsx");
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0); // 获取第一个工作表

        // 锁定工作表中的所有单元格
        for (Row row : sheet) {
            for (Cell cell : row) {
                CellStyle cellStyle = workbook.createCellStyle();
                cellStyle.cloneStyleFrom(cell.getCellStyle());
                cellStyle.setLocked(true); // 锁定单元格
                cell.setCellStyle(cellStyle);
            }
        }

        // 保护工作表并设置密码
        sheet.protectSheet("password123");

        // 保存文件
        try (FileOutputStream fos = new FileOutputStream("/Users/biao/Downloads/user-import_protected.xlsx")) {
            workbook.write(fos);
        }

        workbook.close();
        fis.close();
    }
}
