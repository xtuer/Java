package misc;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.layout.font.FontProvider;

import java.io.File;
import java.io.IOException;

public class Html2Pdf {
    public static void main(String[] args) throws IOException {
        File htmlSrc = new File("/Users/Biao/Desktop/x.html");
        File pdfDest = new File("/Users/Biao/Desktop/x.pdf");

        // 使用中文字体解决不显示中文问题: 字体文件可以从系统中找，也可以从网上下载
        FontProvider fontProvider = new FontProvider();
        fontProvider.addStandardPdfFonts();
        fontProvider.addFont("yahei.ttf");
        // fontProvider.addDirectory("..."); // 添加文件夹下的所有字体

        ConverterProperties converterProperties = new ConverterProperties();
        converterProperties.setCharset("UTF-8");
        converterProperties.setFontProvider(fontProvider);

        // HTML 中本地图片要使用 file:// 的格式，如 file:///Users/Biao/Desktop/shot.png
        HtmlConverter.convertToPdf(htmlSrc, pdfDest, converterProperties);
    }
}
