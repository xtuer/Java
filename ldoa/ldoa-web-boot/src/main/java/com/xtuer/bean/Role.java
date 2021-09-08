package com.xtuer.bean;

/**
 * 用户角色:
 *
 * 管理者：黄经理
 * 销售部销售：薛诚、李征、宋岩、朱娟
 * 销售部技术支持：姜萌
 * 销售部综合保证：王宇
 *
 * 生产部生产维保：王嘉琦、贾琳、高金东、卢鑫
 * 生产部质量保证：李玲琴
 * 生产部计划调度：王嘉琦
 * 生产部检验测试：赵文彬
 *
 * 技术部硬件技术：李宇良
 * 技术部软件技术：刘建忠
 */
public enum Role {
    ROLE_ADMIN_SYSTEM("系统管理员"),
    ROLE_ADMIN_ORG("机构管理员"),
    ROLE_ADMIN("管理者"),
    ROLE_FINANCE("财务"),

    ROLE_SALE_SALESPERSON("销售部销售"),
    ROLE_SALE_SUPPORT("销售部技术支持"),
    ROLE_SALE_GUARANTEE("销售部综合保证"),

    ROLE_PRODUCE_MAINTENANCE("生产部生产维保"),
    ROLE_PRODUCE_QUALITY("生产部质量保证"),
    ROLE_PRODUCE_SCHEDULE("生产部计划调度"),
    ROLE_PRODUCE_TEST("生产部检验测试"),
    ROLE_PRODUCE_REPOSITORY_MANAGER("生产部库房管理"),

    ROLE_TECHNIQUE_HARDWARE("技术部硬件技术"),
    ROLE_TECHNIQUE_SOFTWARE("技术部软件技术");

    // 角色的中文名字，方便记忆 (JSON 序列化的时候不会输出)
    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }
}
