import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    /**
     * 自动化使用的缓存目录名，下面的变量 TEMP_DIR_OF_ANSIBLE 中使用。
     */
    private static final String AUTO_TEMP_DIR_NAME = "shindata-temp-auto";

    /**
     * 复制任务的跟时路径的根路径。
     */
    private static final Pattern COPY_TASK_TEMP_ROOT_DIR_PATTERN = Pattern.compile(AUTO_TEMP_DIR_NAME + "/\\w+");

    public static void deleteAnsibleCopyTaskTempDirectory(String path) {
        /*
         逻辑:
         1. 从路径 path 中查找到 AUTO_TEMP_DIR_NAME 的直接子目录
         2. 删除此子目录
         */

        // [1] 从路径 path 中查找到 AUTO_TEMP_DIR_NAME 的直接子目录
        Matcher matcher = COPY_TASK_TEMP_ROOT_DIR_PATTERN.matcher(path);

        if (!matcher.find()) {
            return;
        }

        String rootTempDir = path.substring(0, matcher.end());
        System.out.println(rootTempDir);
    }

    public static void main(String[] args) throws IOException {
        // 要找到 shindata-temp-auto/(\\w+)
        deleteAnsibleCopyTaskTempDirectory("/root/shindata-temp-auto");
        deleteAnsibleCopyTaskTempDirectory("/root/shindata-temp-auto/1923");
        deleteAnsibleCopyTaskTempDirectory("/root/shindata-temp-auto/1923/biao");
        deleteAnsibleCopyTaskTempDirectory("/home/hello/shindata-temp-auto");
        deleteAnsibleCopyTaskTempDirectory("/home/hello/shindata-temp-auto/1923");
        deleteAnsibleCopyTaskTempDirectory("/home/hello/shindata-temp-auto/1923/biao");
        deleteAnsibleCopyTaskTempDirectory("/root/shindata-temp-autox");
        deleteAnsibleCopyTaskTempDirectory("/root/shindata-temp-autox/1923");
        deleteAnsibleCopyTaskTempDirectory("/root/shindata-temp-autox/1923/biao");
    }
}
