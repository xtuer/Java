package misc;

import lombok.Data;

/**
 * SaltStack 配置，有 SaltMaster 的 ssh 连接信息，SaltMaster 的 base 环境文件系统路径。
 *
 * 提示: Salt 文件系统使用 base 环境即可，不必使用 dev 和 prod 等环境。
 */
@Data
public class SaltStackConfig {
    /**
     * SaltMaster 默认的文件系统根路径。
     */
    public static final String DEFAULT_SALT_FILE_SYSTEM_BASE = "/srv/salt/base";

    /**
     * Minion FileSystem 的文件根路径。
     * Minion 复制到 Master 文件的路径如 /var/cache/salt/master/192.168.12.102/root/test.txt。
     * 例如 xbk 备份恢复的自动化需要 Minion 之间复制文件。
     */
    public static final String MINION_FILE_SYSTEM_BASE = "/var/cache/salt/master";

    /**
     * SaltStack 文件系统的根路径，脚本文件和介质文件都放在这个路径下，建议使用默认值。
     */
    private String saltFileSystemBase = DEFAULT_SALT_FILE_SYSTEM_BASE;

    /**
     * SaltMaster IP
     */
    private String saltMasterIp;

    /**
     * SaltMaster 的 ssh 用户名
     */
    private String saltMasterUsername;

    /**
     * SaltMaster 的 ssh 密码
     */
    private String saltMasterPassword;

    /**
     * SaltMaster 的 ssh 端口
     */
    private int saltMasterPort;
}
