package misc;

/**
 * 获取文件的编码，参考: https://stackoverflow.com/questions/499010/java-how-to-determine-the-correct-charset-encoding-of-a-stream
 */
public class CharsetDetect {
    // public static void main(String[] args) throws Exception {
    //     Path path = Paths.get("/Users/biao/Documents/temp/todo-3.txt");
    //
    //     // 输出 GB18030
    //     System.out.println(guessCharset(Files.newInputStream(path)));
    //
    //     // 读取 GB18030 文件内容，可保存为 UTF-8
    //     byte[] content = Files.readAllBytes(path);
    //     System.out.println(new String(content, Charset.forName("GB18030")));
    // }
    //
    // public static Charset guessCharset(InputStream is) throws IOException {
    //     return Charset.forName(new TikaEncodingDetector().guessEncoding(is));
    // }
}
