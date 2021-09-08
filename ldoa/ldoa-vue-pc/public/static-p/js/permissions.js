/*
维保订单:
    生产部生产维保人员有编辑、删除，修改的权限
    处理进度那个地方，只能发起订单的人员编辑修改
    销售人员只有查看和导出的权限


审批配置和用户管理：
    只能是超级管理员有修改、编辑、删除权限，其他人只能查看
    每个人若要修改自己的手机号和密码的话，从个人中心修改就可以

物料入库:
    人员只能是生产部质量保证和生产部调度
    其他人只能查看物料入库和出库的情况

物料管理和产品管理: 每个人都可以添加，但是每个人只能编辑和删除自己添加的东西

共享文件部分也是每个人都可以上传，下载，增加编辑和删除功能，但是每个人只能编辑和删除自己上传的文件

销售系统模块只有销售人员、财务人员、以及管理者可以看到
销售订单的录入只有销售人员可以录入
收款金额部分只有财务人员可以填写
管理者可以查看销售系统的信息
*/

/**
 * 判断当前用户是否有权限
 *
 * @param {Array} allowedPermissions 允许的权限数组
 * @param {Array} deniedPermissions 禁止的权限数组
 */
const hasPermission = function(allowedPermissions, deniedPermissions) {
    const roles = this.$store.getters.roles;

    for (let role of roles) {
        // 如果用户的任意一个角色在 allowedPermissions 数组中，则有权限，返回 true
        if (allowedPermissions && allowedPermissions.includes(role)) {
            return true;
        }

        // 如果用户的任意一个角色在 deniedPermissions 数组中，则没有权限，返回 false
        if (deniedPermissions && deniedPermissions.includes(role)) {
            return false;
        }
    }

    // 如果 allowedPermissions 为空数组，则返回 true，表示有权限
    if (allowedPermissions && allowedPermissions.length === 0) {
        return true;
    }

    return false;
};

/**
 * 是否有生产订单的权限
 *
 * @return 有权限返回 true，否则返回 false
 */
const hasPermissionForOrder = function() {
    return this.hasPermission([], PERMISSIONS_DENY.order);
};

/**
 * 是否有完成生产订单的权限
 */
const hasPermissionForOrderComplete = function() {
    return this.hasPermission(PERMISSIONS.orderComplete);
};

/**
 * 是否有维保订单的权限
 *
 * @return 有权限返回 true，否则返回 false
 */
const hasPermissionForMaintenance = function() {
    return this.hasPermission(PERMISSIONS.maintenance);
};

/**
 * 是否有超级管理员的权限
 *
 * @return 有权限返回 true，否则返回 false
 */
const hasPermissionOfSuperAdmin = function() {
    return this.hasPermission(PERMISSIONS.superAdmin);
};

/**
 * 是否有物料入库的权限
 *
 * @return 有权限返回 true，否则返回 false
 */
const hasPermissionForStockIn = function() {
    return this.hasPermission(PERMISSIONS.stockIn);
};

/**
 * 是否有操作销售订单的权限
 */
const hasPermissionForSalesOrder = function() {
    return this.hasPermission(PERMISSIONS.salesOrder);
};

/**
 * 是否有财务的权限
 */
const hasPermissionForFinance = function() {
    return this.hasPermission(PERMISSIONS.finance);
};

export default {
    hasPermission,
    hasPermissionForOrder,
    hasPermissionForOrderComplete,
    hasPermissionForMaintenance,
    hasPermissionOfSuperAdmin,
    hasPermissionForStockIn,
    hasPermissionForSalesOrder,
    hasPermissionForFinance,
};
