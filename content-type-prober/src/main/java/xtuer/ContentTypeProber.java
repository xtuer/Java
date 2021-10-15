package xtuer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;
import java.util.Properties;

/**
 * 文件的 content type 探测工具类，通过 SPI 自动注册到 JVM file type detector，
 * 通过调用 Files.probeContentType(Path) 使用。
 */
public class ContentTypeProber extends FileTypeDetector {
    private static final Logger log = LoggerFactory.getLogger(ContentTypeProber.class);

    /**
     * Content type properties file path.
     */
    private static final String CONTENT_TYPE_PROPS_PATH = "config/content-type.properties";

    /**
     * Content type properties loaded from file CONTENT_TYPE_PROPS_PATH.
     */
    private static final Properties CONTENT_TYPE_PROPS = new Properties();

    static {
        // Loading content type when class loaded.
        loadContentTypes();
    }

    /**
     * 加载 content type
     */
    private static void loadContentTypes() {
        log.info("[开始] 加载 content type file...");

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try (InputStream in = classLoader.getResourceAsStream(CONTENT_TYPE_PROPS_PATH)) {
            CONTENT_TYPE_PROPS.load(in);
        } catch (Exception ex) {
            log.warn(ex.getMessage());
        }

        log.info("[结束] 加载 content type file");
    }

    /**
     * 获取传入的 Path 的文件名后缀
     *
     * @param path 路径
     * @return 返回文件名后缀
     */
    public static String getExtension(Path path) {
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf(".");
        return dot == -1 ? "" : name.substring(dot + 1);
    }

    @Override
    public String probeContentType(Path path) {
        String ext = getExtension(path);
        return CONTENT_TYPE_PROPS.getProperty(ext);
    }
}
