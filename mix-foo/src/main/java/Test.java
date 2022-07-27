import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

import static com.google.common.collect.ImmutableMap.of;
import static org.apache.commons.text.StringSubstitutor.replace;

public class Test {
    private static final SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss");
    private static final String SALT_CMD_RUN = "salt '${minionIp}' cmd.run '${cmd}' --out=json";

    public static void main(String[] args) throws Exception {
        Path path = Paths.get("/root/foo/data/");
        System.out.println(path.getFileName());
    }
}
