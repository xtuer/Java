const Urls = {
    // JS，CSS URLs
    TINY_MCE: '/static-p/lib/tinymce/tinymce.min.js', // TinyMCE

    // 用户
    API_USERS               : '/api/users',                  // 用户
    API_USERS_BY_ID         : '/api/users/{userId}',         // 指定 ID 的用户
    API_USERS_CURRENT       : '/api/login/users/current',    // 当前登录的用户
    API_USER_PASSWORDS_RESET: '/api/users/{userId}/passwords/reset', // 重置密码

    FORM_UPLOAD_TEMPORARY_FILE : '/form/upload/temp/file',  // 上传一个临时文件
    FORM_UPLOAD_TEMPORARY_FILES: '/form/upload/temp/files', // 上传多个临时文件
    API_CAN_PREVIEW_FILE_PREFIX: '/api/canPreview',         // 请求是否可预览文件的前缀

    // 消息系统
    MESSAGE_WEBSOCKET_URL: `ws://${window.location.hostname}:3721`,

    // 机构
    API_ORGS       : '/api/orgs',         // 所有机构
    API_ORGS_BY_ID : '/api/orgs/{orgId}', // 指定 ID 的机构
    API_ORGS_ENABLE: '/api/orgs/{orgId}/enabled', // 指定 ID 的机构的启用禁用状态

    // 字典
    API_DICTS_ID : '/api/dicts/{dictId}',   // 单个查询/修改/删除
    API_DICTS    : '/api/currentOrg/dicts', // 查询当前机构字典列表

    // 产品
    API_PRODUCTS           : '/api/products',             // 所有产品
    API_PRODUCTS_BY_ID     : '/api/products/{productId}', // 指定 ID 的产品
    API_PRODUCT_ITEMS      : '/api/productItems',         // 所有产品项
    API_PRODUCT_ITEMS_BY_ID: '/api/productItems/{productItemId}', // 指定 ID 的产品项

    // 订单
    API_ORDERS: '/api/orders', // 订单
    API_ORDERS_BY_ID: '/api/orders/{orderId}', // 指定 ID 的订单

    // 审批
    API_AUDITS_BY_ID    : '/api/audits/{auditId}',            // 指定 ID 的审批
    API_AUDITS_BY_TARGET: '/api/audits/of-target/{targetId}', // 审批目标的审批
    API_AUDIT_CONFIGS   : '/api/audit-configs',               // 审批配置
    API_AUDIT_ITEMS     : '/api/audit-items',                 // 审批项
};

window.Urls = Urls;