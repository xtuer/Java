import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;

public class TestPdf1 {

    public static void main(String[] args) {
        // Step 1: Create a Document object
        Document document = new Document();

        try {
            // Step 2: Initialize PdfWriter instance and associate it with the Document
            PdfWriter.getInstance(document, new FileOutputStream("/Users/biao/Desktop/example.pdf"));

            // Step 3: Open the document
            document.open();

            // Step 4: Add content to the document
            document.add(new Paragraph("Hello, OpenPDF!"));
            document.add(new Paragraph("This is a simple PDF document created with OpenPDF."));

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } finally {
            // Step 5: Close the document
            document.close();
        }
    }
}
