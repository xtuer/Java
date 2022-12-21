package misc.auto;

import com.google.common.base.Preconditions;
import org.apache.commons.io.FilenameUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

/**
 * 自动化的脚本类，统一管理要执行脚本的各种路径，例如:
 * A. SaltMaster 上保存文件的目录为 /srv/salt/base
 * B. SaltMaster 上临时脚本的路径为 /srv/salt/base/scripts-temp/{yyyy-MM-dd}/{script-name.sh}
 * C. 临时脚本的 Salt 文件系统路径为 salt://scripts-temp/{yyyy-MM-dd}/{script-name.sh}
 *
 * 脚本涉及到 3 个系统:
 * A. Web 服务器所在主机
 * B. 自动化引擎所在主机
 * C. 执行脚本的主机
 *
 * 说明:
 * A. 自动化引擎 engine: 指的是 SaltStack 的 Master、Ansible
 * B. 执行脚本的主机: SaltStack 的 Minion、Ansible 使用 ssh 访问的普通主机
 * C. 脚本中使用到的路径都是绝对路径
 * D. 脚本分 2 种，一种是加密的脚本，另一种是解密后得到的脚本 (都是临时文件)
 */
public class AutoScript {
    /**
     * 脚本的本地临时文件路径 (Web 服务器所在系统)。
     */
    public final String tempScriptPathInLocal;

    /**
     * 加密的脚本在自动化引擎主机上的文件路径
     */
    public final String encryptedScriptPathInEngine;

    /**
     * 临时脚本在自动化引擎主机上的目录路径。
     */
    public final String tempScriptDirInEngine;

    /**
     * 临时脚本在自动化引擎主机上文件路径。
     */
    public final String tempScriptPathInEngine;

    /**
     * 临时脚本在要执行的主机上的目录路径。
     */
    public final String tempScriptDirInTarget;

    /**
     * 临时脚本在要执行的主机上的文件路径。
     */
    public final String tempScriptPathInTarget;

    /**
     * 径创建脚本对象: 基于自动化引擎上存储文件的基础目录 baseDirInEngine 统一计算出脚本的各种路，避免在使用的时候四处拼字符串。
     *
     * @param baseDirInEngine 自动化引擎上存储文件的基础目录
     * @param targetHome 用户在执行脚本主机上的 Home 目录，在其下保存临时文件
     * @param scriptFileName 脚本文件名
     */
    public AutoScript(String baseDirInEngine, String targetHome, String scriptFileName) throws IOException {
        // 参数校验
        Preconditions.checkArgument(StringUtils.hasText(baseDirInEngine), "基础路径不能为空");
        Preconditions.checkArgument(StringUtils.hasText(targetHome), "Home 目录不能为空");
        Preconditions.checkArgument(StringUtils.hasText(scriptFileName), "脚本名不能为空");
        Preconditions.checkArgument(!StringUtils.containsWhitespace(baseDirInEngine), "基础路径不能包含空格");
        Preconditions.checkArgument(!StringUtils.containsWhitespace(targetHome), "Home 目录不能包含空格");
        Preconditions.checkArgument(!StringUtils.containsWhitespace(scriptFileName), "脚本名不能包含空格");

        // 创建脚本的临时文件 (权限为 755)，系统会自动回收。
        Set<PosixFilePermission> permission_644 = PosixFilePermissions.fromString("rwxr-xr-x");
        FileAttribute<?> permissions = PosixFilePermissions.asFileAttribute(permission_644);

        // 有可能执行 shell 和 python 脚本。
        Path tempFile = Files.createTempFile(scriptFileName + "-", "." + FilenameUtils.getExtension(scriptFileName), permissions);
        String tempFileName = tempFile.getFileName().toString();
        String date = today();

        // 本地临时文件路径。
        tempScriptPathInLocal = tempFile.toString();

        // 例如: <base>/scripts-encrypted/x.sh
        encryptedScriptPathInEngine = String.format("%s/%s/%s", baseDirInEngine, AutoConst.DIR_SCRIPTS_ENCRYPTED, scriptFileName);

        // 例如: <base>/scripts-temp/2022-10-01
        tempScriptDirInEngine = String.format("%s/%s/%s", baseDirInEngine, AutoConst.DIR_SCRIPTS_TEMP, date);

        // 例如: <base>/scripts-temp/2022-10-01/x.sh
        tempScriptPathInEngine = String.format("%s/%s", tempScriptDirInEngine, tempFileName);

        // 例如: /root/scripts-temp/2022-10-01
        tempScriptDirInTarget = String.format("%s/%s/%s", targetHome, AutoConst.DIR_SCRIPTS_TEMP, date);

        // 例如: /root/scripts-temp/2022-10-01/x.sh
        tempScriptPathInTarget = String.format("%s/%s", tempScriptDirInTarget, tempFileName);
    }

    /**
     * 今天的日期格式。
     */
    private static final SimpleDateFormat TODAY_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 获取今天的日期，格式为 yyyy-MM-dd。
     *
     * @return 返回日期的字符串
     */
    public static String today() {
        return TODAY_FORMATTER.format(new Date());
    }
}
