package misc.auto.ansible;

import java.util.regex.Pattern;

/**
 * Ansible 使用的常量。
 */
public interface AnsibleRunnerConst {
    /**
     * 使用 Playbook 的方式从 Ansible 复制文件或目录到 Node。
     * 说明: 其中的 ${inventory} 为 Ansible 的临时认证文件，可为空，非空时为 -i xxx.ini。
     * 例如: ansible-playbook /etc/ansible/playbooks/copy_from_ansible_to_node.yml --extra-vars "nodeIp=192.168.12.101 src=/root/test.txt destDir=/root/a/b/c/"
     *      ansible-playbook /etc/ansible/playbooks/copy_from_ansible_to_node.yml --extra-vars "nodeIp=192.168.12.101 src=/root/test-dir destDir=/root/a/b/c/"
     *
     * copy_from_ansible_to_node.yml 的内容:
     * - name: 从 Ansible 复制文件或目录到 Node
     *   hosts: '{{ nodeIp }}'
     *   tasks:
     *     - name: copy file or directory located in Ansible Host into remote dir
     *       copy:
     *         src: '{{ src }}'
     *         dest: '{{ destDir }}'
     */
    String PLAYBOOK_COPY_FILE_OR_DIR_FROM_ANSIBLE_TO_NODE = "ansible-playbook /etc/ansible/playbooks/copy_from_ansible_to_node.yml --extra-vars \"nodeIp=${nodeIp} src=${src} destDir=${destDir}\" ${inventory}";

    /**
     * 使用 Playbook 的方式从 Node 复制文件到 Ansible。
     * 说明: 其中的 ${inventory} 为 Ansible 的临时认证文件，可为空，非空时为 -i xxx.ini。
     * 例如: ansible-playbook /etc/ansible/playbooks/copy_file_from_node_to_ansible.yml --extra-vars "nodeIp=192.168.12.101 src=/root/test.txt destDir=/root/a/b/c/"
     *
     * copy_file_from_node_to_ansible.yml 的内容:
     * - name: 从 Node 复制文件到 Ansible
     *   hosts: '{{ nodeIp }}'
     *   tasks:
     *     - name: copy file or directory located at remote into Ansible Host
     *       fetch:
     *         src: '{{ src }}'
     *         dest: '{{ destDir }}'
     *         flat: yes
     */
    String PLAYBOOK_COPY_FILE_FROM_NODE_TO_ANSIBLE = "ansible-playbook /etc/ansible/playbooks/copy_file_from_node_to_ansible.yml --extra-vars \"nodeIp=${nodeIp} src=${src} destDir=${destDir}\" ${inventory}";

    /**
     * 使用 Playbook 的方式从 Node 复制目录到 Ansible。
     * 说明: 其中的 ${inventory} 为 Ansible 的临时认证文件，可为空，非空时为 -i xxx.ini。
     * 例如: ansible-playbook /etc/ansible/playbooks/copy_dir_from_node_to_ansible.yml --extra-vars "nodeIp=192.168.12.101 src=/root/test-dir destDir=/root/a/b/c/"
     *
     * 需要注意的是，源目录不存在不报错。
     *
     * copy_dir_from_node_to_ansible.yml 的内容:
     * - name: 从 Node 复制目录到 Ansible
     *   hosts: '{{ nodeIp }}'
     *   tasks:
     *   - name: find files to copy
     *     find:
     *       paths: '{{ src }}'
     *       recurse: yes
     *       patterns: '*'
     *     register: files_to_copy
     *
     *   - name: Copy files
     *     fetch:
     *       src: '{{ item.path }}'
     *       dest: '{{ destDir }}'
     *       flat: no
     *     with_items: '{{ files_to_copy.files }}'
     */
    String PLAYBOOK_COPY_DIR_FROM_NODE_TO_ANSIBLE = "ansible-playbook /etc/ansible/playbooks/copy_dir_from_node_to_ansible.yml --extra-vars \"nodeIp=${nodeIp} src=${src} destDir=${destDir}\" ${inventory}";

    /**
     * Ansible Playbook 复制操作输出中表示结果的行:
     * 192.168.12.101 : ok=2    changed=1    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0
     */
    Pattern PLAYBOOK_COPY_RESULT_PATTERN = Pattern.compile("ok=(\\d+)\\s+changed=\\d+\\s+unreachable=\\d+\\s+failed=\\d+\\s+skipped=\\d+");

    /**
     * Playbook 复制文件或目录成功的 code。
     */
    String PLAYBOOK_COPY_SUCCESS_CODE_2 = "2";
    String PLAYBOOK_COPY_SUCCESS_CODE_3 = "3";

    /**
     * 复制文件命令执行成功，但实际复制操作错误。
     */
    int ERROR_COPY_ACTION = 20001;

    /**
     * 文件不存在。
     */
    int ERROR_FILE_NOT_FOUND = 20404;

    /**
     * 自动化使用的缓存目录名，下面的变量 TEMP_DIR_OF_ANSIBLE 中也会使用此目录名。
     */
    String AUTO_TEMP_DIR_NAME = "shindata-temp-auto";

    /**
     * Ansible 的临时目录 pattern。
     */
    String TEMP_DIR_OF_ANSIBLE = "${home}/shindata-temp-auto/${timestamp}";

    /**
     * 从 Node 复制目录到 Ansible 的临时目录时，被复制目录在临时目录下的路径。
     */
    String TEMP_PATH_OF_COPIED_DIR_FROM_NODE_TO_ANSIBLE = "${ansibleTempDir}/${nodeIp}${srcPath}";

    /**
     * 复制产生的临时目录根路径的正则匹配。
     */
    Pattern TEMP_ROOT_DIR_PATTERN_OF_COPY = Pattern.compile(AnsibleRunnerConst.AUTO_TEMP_DIR_NAME + "/\\w+");

    /**
     * 使用 Ansible 检查远程主机上文件或目录是否存在的命令。
     */
    String FILE_OR_DIR_EXISTS_VIA_ANSIBLE = "ansible ${nodeIp} -m shell -a '[ -e ${path} ] && echo exists || echo non-exists' ${inventory}";

    /**
     * 本地命令行检查文件是否存在的命令。
     */
    String FILE_OR_DIR_EXISTS_IN_CMD = "[ -e ${path} ] && echo file-exists || echo file-non-exists";

    /**
     * 使用 Ansible 获取当前 ssh 用户远程主机的 Home 目录。
     */
    String HOME_VIA_ANSIBLE = "ansible ${nodeIp} -m command -a 'echo $HOME' ${inventory}";

    /**
     * 获取本地主机的 Home 目录。
     */
    String HOME_IN_CMD = "echo $HOME";

    /**
     * 使用 Ansible 给 NodeIp 上的文件增加可执行权限。
     */
    String FILE_EXECUTABLE_VIA_ANSIBLE = "ansible ${nodeIp} -m file -a 'path=${path} mode=a+x' ${inventory}";

    /**
     * 使用 Ansible 在 NodeIp 上执行脚本，使用耗时任务的方式，超时时间为 3 个小时。
     */
    String EXECUTE_SCRIPT_VIA_ANSIBLE = "ansible ${nodeIp} -B 10800 -P 5 -m command -a '${script} ${args}' ${inventory}";

    /**
     * Ansible Inventory 的项。
     * 例如: 192.168.12.28 ansible_port=22 ansible_ssh_user=root ansible_ssh_pass='Newdt@cn'
     */
    String INVENTORY_ITEM = "${ip} ansible_port=${port} ansible_ssh_user=${user} ansible_ssh_pass='${password}'";

    /**
     * Ansible 使用 sudo 的 Inventory 的项。
     * 例如: 192.168.12.20 ansible_port=22 ansible_ssh_user=root ansible_ssh_pass='Newdt@cn' ansible_become=true ansible_become_method=sudo ansible_become_user=null ansible_become_pass='Newdt@cn'
     */
    String INVENTORY_ITEM_SUDO = "${ip} ansible_port=${port} ansible_ssh_user=${user} ansible_ssh_pass='${password}' ansible_become=true ansible_become_method=sudo ansible_become_user=${becomeUser} ansible_become_pass='${becomePassword}'";
}
