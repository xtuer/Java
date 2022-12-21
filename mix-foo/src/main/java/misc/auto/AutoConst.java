package misc.auto;

/**
 * 自动化常量。
 */
public interface AutoConst {
    /**
     * 临时脚本目录名，位于 ${autoBase}/scripts-temp。
     */
    String DIR_SCRIPTS_TEMP = "scripts-temp";

    /**
     * 加密脚本目录名，位于 ${autoBase}/scripts-encrypted。
     */
    String DIR_SCRIPTS_ENCRYPTED = "scripts-encrypted";

    /**
     * 安装包目录名，位于 ${autoBase}/install-package。
     */
    String DIR_INSTALL_PACKAGE = "install-package";

    /**
     * 更新包目录名，位于 ${autoBase}/patch-package。
     */
    String DIR_PATCH_PACKAGE = "patch-package";

    /**
     * Ansible 的临时认证文件目录名，位于 ${autoBase}/inventory。
     */
    String DIR_INVENTORY = "inventory";
}
