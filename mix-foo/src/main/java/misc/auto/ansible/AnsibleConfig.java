package misc.auto.ansible;

import lombok.Data;

/**
 * Ansible 配置。
 */
@Data
public class AnsibleConfig {
    /**
     * Ansible IP。
     */
    private String ansibleIp;

    /**
     * 访问 Ansible 所在主机的 ssh 用户名。
     */
    private String ansibleSshUsername;

    /**
     * 访问 Ansible 所在主机的 ssh 用户密码。
     */
    private String ansibleSshPassword;

    /**
     * 访问 Ansible 所在主机的 ssh 端口。
     */
    private int ansibleSshPort = 22;

    /**
     * 自动化放置脚本、安装文件、升级文件等的根目录。
     * 加密脚本: ${autoBase}/scripts-encrypted
     * 安装文件: ${autoBase}/installPackage
     * 升级文件: ${autoBase}/patchPackage
     * 临时认证: ${autoBase}/inventory
     * 临时脚本: ${autoBase}/scripts-temp
     */
    private String ansibleAutoBase;
}
