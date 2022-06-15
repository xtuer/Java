import misc.OracleBackupInTheSameDb;
import misc.OracleBackupInTheSameDbUtils;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OracleBackupTest {
    @Test
    public void testParseDmlSql() {
        System.out.println(OracleBackupInTheSameDbUtils.generateBackupSql("update test SET name='Hello'"));
        System.out.println(OracleBackupInTheSameDbUtils.generateBackupSql("UPDATE test SET name='Hello' WHERE id=1"));
        System.out.println(OracleBackupInTheSameDbUtils.generateBackupSql("DELETE  FROM  test"));
        System.out.println(OracleBackupInTheSameDbUtils.generateBackupSql("DELETE  FROM  test WHERE id=1 AND count > 2"));
    }

    @Test
    public void testGreedy() {
        String str = "<biao><>c<b>";
        Pattern pattern;
        Matcher matcher;

        // 贪婪: 最长匹配 .* : 输出: <biao><>c<b>
        pattern = Pattern.compile("<.*>");
        matcher = pattern.matcher(str);
        while (matcher.find()) {
            System.out.println(matcher.group());
        }

        // 不知是否非贪婪 .*? : 输出: <biao>, <>, <b>
        pattern = Pattern.compile("<.*?>");
        matcher = pattern.matcher(str);
        while (matcher.find()) {
            System.out.println(matcher.group());
        }

        // 使用组, 输出<>里的内容, 输出: 'biao', ' ', 'b'
        // 0组代表整个表达式, 子组从1开始
        pattern = Pattern.compile("<(.*?)>");
        matcher = pattern.matcher(str);
        while (matcher.find()) {
            System.out.println(matcher.group(1));
        }
    }
}
