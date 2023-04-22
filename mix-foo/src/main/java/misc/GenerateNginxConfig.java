package misc;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GenerateNginxConfig {
    public static void main(String[] args) throws IOException, TemplateException {
        // [1] 配置 SQL 执行服务的 WebTerminal IP 和端口范围。
        List<WebTerminalConfig> webTerminalConfigs = new LinkedList<>();
        webTerminalConfigs.add(new WebTerminalConfig("192.168.12.101", 20001, 20050));
        webTerminalConfigs.add(new WebTerminalConfig("192.168.12.102", 20051, 20100));

        // 生成模板需要的数据。
        List<String> backends = new LinkedList<>();
        List<Integer> ports = new LinkedList<>();
        for (WebTerminalConfig cfg : webTerminalConfigs) {
            for (int p = cfg.minPort; p <= cfg.maxPort; p++) {
                // 20001    192.168.12.101:20001
                backends.add(String.format("%d    %s:%d", p, cfg.ip, p));
                ports.add(p);
            }
        }

        // [2] 设置数据到 model 中给 Freemarker 使用。
        Map<String, Object> model = new HashMap<>();
        model.put("backends", backends);
        model.put("ports", ports);

        // [3] Freemarker 根据模板 + 内容生成配置。
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
        cfg.setDirectoryForTemplateLoading(new File("/Users/biao/Documents/temp/nginx"));
        Template template = cfg.getTemplate("nginx-web-terminal.ftl", "UTF-8");
        Writer out = new BufferedWriter(new FileWriter("/Users/biao/Documents/temp/nginx/nginx-web-terminal.conf"));

        template.process(model, out);
        out.flush();
        out.close();
    }

    public static class WebTerminalConfig {
        /**
         * SQL 执行服务 IP。
         */
        String ip;

        /**
         * 监听的最小 IP。
         */
        int minPort;

        /**
         * 监听的最大 IP。
         */
        int maxPort;

        public WebTerminalConfig(String ip, int minPort, int maxPort) {
            this.ip = ip;
            this.minPort = minPort;
            this.maxPort = maxPort;
        }
    }
}
