package office;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.alibaba.fastjson.JSON;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ExcelImpexTest {
    public static void main(String[] args) throws Exception {
        exportExcel();
        // importExcel();
    }

    public static void exportExcel() throws Exception {
        List<Student> students = new LinkedList<>();
        students.add(new Student().setId(1).setName("无名").setGender(1).setBirthday(new Date()));
        students.add(new Student().setId(2).setName("杜非").setGender(1).setBirthday(new Date()));
        students.add(new Student().setId(3).setName("小白").setGender(2).setBirthday(new Date()));

        Workbook book = ExcelExportUtil.exportExcel(new ExportParams("计算机一班学生", "学生"), Student.class, students);
        book.write(new FileOutputStream("C:/temp/x.xls"));
    }

    public static void importExcel() throws Exception {
        ImportParams params = new ImportParams();
        params.setTitleRows(1);
        params.setHeadRows(1);
        List<Student> students = ExcelImportUtil.importExcel(new File("/Users/Biao/Desktop/x.xls"),Student.class, params);
        System.out.println(JSON.toJSONString(students));
    }
}
