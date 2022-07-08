import com.google.common.base.Stopwatch;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) throws IOException {
        Pattern pattern = Pattern.compile("<(\\w+)>(.+?)</\\1>"); // (?:exp) 非捕获分组
        Matcher matcher = pattern.matcher("<a>Biao</a><a>Alice</a>");
        while (matcher.find()) {
            System.out.println(matcher.group(2));
        }
    }
}
