import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.lang.StringUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class TestPdf2 {

    public static void main(String[] args) throws Exception {
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, new FileOutputStream("/Users/biao/Desktop/table_example.pdf"));
            document.open();

            String[] headers = {"One", "Two", "This is only for Test"};
            int maxHeaderLength = 0;
            for (String header : headers) {
                maxHeaderLength = Math.max(maxHeaderLength, header.length());
            }
            for (int i = 0; i < headers.length; i++) {
                headers[i] = StringUtils.leftPad(headers[i], maxHeaderLength);
            }

            Font font = FontFactory.getFont(FontFactory.COURIER, 12, Font.NORMAL, BaseColor.BLACK);
            for (int row = 1; row < 10000; row++) {
                Paragraph rowFlag = new Paragraph(StringUtils.repeat("*", 40) + "Row: " + row + StringUtils.repeat("*", 40));
                document.add(rowFlag);

                for (String header : headers) {
                    Paragraph p = new Paragraph(header + " : " + "Hello", font);
                    document.add(p);
                }
            }

            document.close();

            System.out.println("Table created successfully!");

        } catch (FileNotFoundException | DocumentException e) {
            e.printStackTrace();
        }
    }
}
