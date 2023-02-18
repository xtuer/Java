package misc.auto.ndtagent.upload;

import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * MD5 计算工具类。
 */
public class Md5Utils {
    /**
     * 计算字符串的 MD5.
     *
     * @param text 需要计算 MD5 的字符串
     * @return 返回字符串的 MD5
     */
    public static String md5(String text) {
        return DigestUtils.md5DigestAsHex(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算二进制数据的 MD5.
     *
     * @param data 二进制数组
     * @return 返回 MD5
     */
    public static String md5(byte[] data) {
        return DigestUtils.md5DigestAsHex(data);
    }

    /**
     * 计算文件的 MD5.
     * MD5 包含 16 进制表示的 10 个字符: 0-9, a-z
     *
     * @param file 需要计算 MD5 的文件
     * @return 返回文件的 MD5，如果出错，例如文件不存在则返回 null
     */
    public static String md5(File file) {
        try (InputStream in = Files.newInputStream(file.toPath())) {
            return DigestUtils.md5DigestAsHex(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
