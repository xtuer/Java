package misc.auto.ndtagent.upload;

import org.apache.commons.cli.*;

/**
 * 上传文件示例。
 * 参数使用 java -jar sitemap.jar -m /Blog/_posts，参数格式 -k1 v1 -k2 v2
 */
public class UploadFileDemo {
    public static void main(String[] args) throws Exception {
        // 1. 定义参数
        Options options = new Options();
        options.addOption("h", "help", false, "查看帮助");
        options.addOption("i", "ip", true, "Agent 的 IP");
        options.addOption("p", "port", true, "Agent 的端口");
        options.addOption("s", "srcPath", true, "要上传的文件路径");
        options.addOption("d", "dstDir", true, "Agent 上保存文件的目录");

        // 2. 解析参数
        CommandLine cmd = new DefaultParser().parse(options, args);

        // 3. 获取参数
        if (cmd.getOptions().length == 0 || cmd.hasOption("h")) {
            // 输出帮助文档
            new HelpFormatter().printHelp("Options", options);
            // new HelpFormatter().printHelp("MyApp", header, options, footer, true);
            System.exit(0);
        }

        String ip = cmd.getOptionValue("i");
        int port = Integer.parseInt(cmd.getOptionValue("p"));
        String srcPath = cmd.getOptionValue("s");
        String dstDir = cmd.getOptionValue("d");

        // 4. 上传文件
        FileUploader uploader = new FileUploader(ip, port, srcPath, dstDir);
        boolean result = uploader.uploadFile();
        System.out.println(result);
    }
}
