package misc;

import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;

public class PoiTest {
    private static final String TEMPLATE_DIR = "/Users/Biao/Desktop/";

    public static void main(String[] args) throws Exception {
        // new 空白的文档
        XWPFDocument document = new XWPFDocument();

        createTitle(document, "你好马大哈");
        createParagraph(document, "学校:________  班级:________  姓名:________  考号:________", 14, true, true);
        addEmptyRow(document);
        createParagraph(document, "学校:________  班级:________  姓名:________  考号:________");
        createParagraph(document, "学校:________  班级:________  姓名:________  考号:________");
        createParagraph(document, "其实，人在脆弱<strong>时都想有份</strong>停靠，心在无助时都想要个依靠。若有人能解读忧伤。");

        // 在文件系统中编写文档并给文档命名为：test.docx
        FileOutputStream out = new FileOutputStream(new File(TEMPLATE_DIR + "test.docx"));
        document.write(out);
        out.close();

        System.out.println("test.docx written successfully");
    }

    public static void createTitle(XWPFDocument document, String title) {
        createParagraph(document, title, 20, true, true);
    }

    public static void createHead1(XWPFDocument document, String head, int fontSize) {
        createParagraph(document, head, 20, true, false);
    }

    public static void createHead2(XWPFDocument document, String head, int fontSize) {
        createParagraph(document, head, 18, true, false);
    }

    public static void createParagraph(XWPFDocument document, String text) {
        createParagraph(document, text, 14, false, false);
    }

    public static void createParagraph(XWPFDocument document, String text, int fontSize, boolean bold, boolean center) {
        XWPFParagraph paragraph = document.createParagraph();

        if (center) {
            paragraph.setAlignment(ParagraphAlignment.CENTER);
        }

        XWPFRun run = paragraph.createRun();
        run.setFontSize(fontSize);
        run.setBold(bold);
        run.setText(text);
    }

    public static void addEmptyRow(XWPFDocument document) {
        document.createParagraph();
    }
}
