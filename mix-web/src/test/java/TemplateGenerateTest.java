import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 生成 Web 项目需要的 Controller，Service 等模板代码。
 */
public class TemplateGenerateTest {
    private static final String PACKAGE = "xtuer";
    private static final String TYPE = "Bar";
    private static final String OUT_DIR = "/Users/biao/Documents/temp/skeleton";

    @Test
    public void generate() throws Exception {
        URL url = ClassLoader.getSystemClassLoader().getResource("templates/skeleton");
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
        cfg.setDirectoryForTemplateLoading(new File(url.toURI()));

        generate(cfg, "Controller.java");
        generate(cfg, "Service.java");
        generate(cfg, "Mapper.java");
        generate(cfg, "Mapper.xml");
    }

    // SkeletonType 为 Controller.java, Service.java
    private static void generate(Configuration cfg, String SkeletonType) throws Exception {
        Template template = cfg.getTemplate(SkeletonType + ".ftl", "UTF-8");
        String path = String.format("%s/%s%s", OUT_DIR, TYPE, SkeletonType);
        System.out.println("生成文件: " + path);
        Writer out = new BufferedWriter(new FileWriter(path));

        Map<String, Object> model = new HashMap<>();
        model.put("package", PACKAGE);
        model.put("Type", TYPE);

        template.process(model, out);
        out.flush();
        out.close();
    }
}
