package misc;

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
}
