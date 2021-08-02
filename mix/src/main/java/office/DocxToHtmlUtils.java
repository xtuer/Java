package office;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import java.io.FileOutputStream;
import java.io.OutputStream;

public class DocxToHtmlUtils {
    /**
     * 把docx转成html
     *
     * @param docxPath 需要被转换的 docx 文件的路径
     * @param htmlPath 转换后保存的 html 文件的路径
     * @throws Exception
     */
    public static void convertDocxToHtml(String docxPath, String htmlPath) throws Exception {
        WordprocessingMLPackage wordMLPackage = Docx4J.load(new java.io.File(docxPath));

        HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
        String imageFilePath = htmlPath.substring(0, htmlPath.lastIndexOf("/") + 1) + "/images"; // 存放图片的文件夹的路径
        htmlSettings.setImageDirPath(imageFilePath);
        htmlSettings.setImageTargetUri("images"); // img 中 src 路径的上一级路径
        htmlSettings.setWmlPackage(wordMLPackage);
        String userCSS = "html, body, div, span, h1, h2, h3, h4, h5, h6, p, a, img,  ol, ul, li, table, caption, tbody, tfoot, thead, tr, th, td " +
                "{ margin: 0; padding: 0; border: 0;}" +
                "body {line-height: 1; padding: 30px;} ";
        userCSS = "body {padding: 30px;}";
        // htmlSettings.setUserCSS(userCSS); // 用户自己定义的 CSS

        OutputStream os = new FileOutputStream(htmlPath);
        Docx4jProperties.setProperty("docx4j.Convert.Out.HTML.OutputMethodXML", true);
        Docx4J.toHTML(htmlSettings, os, Docx4J.FLAG_EXPORT_PREFER_XSL);
    }

    public static void main(String[] args) throws Exception {
        convertDocxToHtml("/Users/Biao/Desktop/y.docx", "/Users/Biao/Desktop/doc/y.html");
    }
}
