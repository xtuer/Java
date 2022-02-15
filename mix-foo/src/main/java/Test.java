import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) {
        Pattern pattern = Pattern.compile("<(\\w)>(.+)</\\1>");
        Matcher matcher = pattern.matcher("<p>Hello</p><a>World</a>");

        while (matcher.find()) {
            System.out.println(matcher.group(2));
        }
    }
}
