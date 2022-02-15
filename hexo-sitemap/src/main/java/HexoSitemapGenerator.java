import lombok.Data;
import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
抽取文章的 tag 和 title，按照 tag 分组生成 sitemap

1. 每篇文章的开头都有下面几行 meta 信息
---
title: 数据库常用基础
date: 2017-05-21 11:45:06
tags: DB
---
---
title: 数据库常用基础
date: 2017-05-21 11:45:06
tags: [DB, Java]
---

2. 抽取 tag 和 title，按照 tag 分组，生成 sitemap.md 格式如下:
---
title: All Documents
date: 2015-06-06 06:06:06
tags: Index
---

## Ajax
* [SpringMVC Ajax 拖拽上传文件](/spring-mvc-upload-file-ajax)
* [SpringMVC 处理 Ajax 映射](/spring-mvc-ajax)

## Cas
* [Spring Security 入门](/java-cas-2-security-intro)
* [Tomcat 启用 https](/java-tomcat-https)
...
*/
public class HexoSitemapGenerator {
    // Hexo Blog 的默认目录
    private static final String DEFAULT_BLOG_DIR = "/Users/biao/Documents/workspace/Blog/source/_posts" ; // "/Users/Biao/Documents/workspace/Blog/source/_posts";

    // Sitemap 的头部
    private static final String SITEMAP_HEADER = "---\n" +
            "title: All Documents\n" +
            "date: 2015-06-06 06:06:06\n" +
            "tags: Index\n" +
            "---\n\n";

    public static void main(String[] args) throws IOException, ParseException {
        // A. 定义参数
        Options options = new Options();
        options.addOption("h", "help", false, "Lists short help");
        options.addOption("m", true, "博客的 Markdown 文件所在目录");

        // B. 解析参数
        CommandLine cmd = new DefaultParser().parse(options, args);

        // C. 获取参数
        // 输出帮助文档
        if (cmd.hasOption("h")) {
            String header = "\n" +
                    "运行: java -jar sitemap.jar -m /Blog/_posts\n" +
                    "默认目录为 /Users/Biao/Documents/workspace/Blog/source/_posts\n" +
                    "\nwhere options include:";
            String footer = "";
            new HelpFormatter().printHelp("sitemap", header, options, footer, true);
            System.exit(0);
        }

        // 1. 遍历 md 文件
        // 2. 抽取 md 文件的 title 和 tags，创建 meta 对象
        // 3. 生成 sitemap.md
        //    3.1 meta 按 tag 分组
        //    3.2 生成 sitemap.md 的内容
        //    3.3 保存 sitemap.md 到文件

        String blogDir = Optional.ofNullable(cmd.getOptionValue("m")).orElse(DEFAULT_BLOG_DIR);
        Map<String, List<Meta>> tagMetas = new TreeMap<>(); // key 为 tag, value 为具有相同 tag 的 meta

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(blogDir), "*.md")) {
            stream.forEach(md -> {
                if (md.endsWith("all.md")) {
                    return;
                }

                // [1] 遍历 md 文件
                // [2] 抽取 md 文件的 title 和 tags，创建 meta 对象
                Meta meta = createMeta(md);
                // System.out.println(md);

                // [3.1] meta 按 tag 分组
                for (String tag : meta.tags) {
                    tagMetas.putIfAbsent(tag, new LinkedList<>());
                    tagMetas.get(tag).add(meta);
                }
            });
        }

        // [3.2] 生成 sitemap.md 的内容
        StringBuilder out = new StringBuilder(SITEMAP_HEADER);
        tagMetas.forEach((tag, metas) -> {
            metas.sort(Comparator.comparing(Meta::getTitle)); // 按照 title 对 metas 进行排序

            out.append(String.format("## %s\n", tag)); // tag 作为二级标题, 把 Blog 分类排列
            for (Meta meta : metas) {                  // 每个 Blog 作为一个 bullet
                out.append(String.format("* [%s](%s)\n", meta.title, meta.url));
            }
            out.append("\n");
        });
        System.out.println(out);

        // [3.3] 保存 sitemap.md 到文件
        Files.write(Paths.get(blogDir + "/all.md"), out.toString().getBytes());
    }

    private static Meta createMeta(Path md) {
        Meta meta = new Meta(md.toFile());

        try {
            // 访问前 10 行，提取 title 和 tag
            Files.lines(md).limit(10).forEach(line -> {
                int start = line.indexOf(':');

                if (line.startsWith("title:")) {
                    meta.title = line.substring(start + 1).trim();
                } else if (line.startsWith("tags:")) {
                    // a. 找到 tags: 后面的字符串
                    // b. 去掉 [ 和 ]
                    // c. 使用 , 拆分得到数组
                    // d. 去掉前后的空格
                    // e. 转为 set
                    meta.tags = Stream.of(line.substring(start + 1).replaceAll("[\\[\\]]", "").split(","))
                            .map(String::trim)
                            .collect(Collectors.toSet());
                }
            });

            // 没有设置 tag，归类到 tag zz-NoTag
            if (meta.tags == null) {
                meta.tags = new HashSet<>();
                meta.tags.add("zz-NoTag");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return meta;
    }

    // 博客的 md 文件的元数据
    @Data
    static class Meta {
        public String url;       // 博客的 url，linux-crontab.md 的 url 为 /linux-crontab
        public String title;     // 博客的标题
        public Set<String> tags; // 博客的 tag 集合

        Meta(File md) {
            url = "/" + FilenameUtils.getBaseName(md.getName()) + "/"; // 根据文件名生成 url
        }
    }
}
