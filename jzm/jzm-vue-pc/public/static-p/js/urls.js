const Urls = {
    // 用户
    API_USERS               : '/api/currentOrg/users',       // 当前机构用户列表
    API_USERS_COUNT         : '/api/currentOrg/users/count', // 当前机构总记录数
    API_USERS_BY_ID         : '/api/users/{userId}',         // 指定 ID 的用户
    API_USERS_CURRENT       : '/api/login/users/current',    // 当前登录的用户
    API_USER_PASSWORDS_RESET: '/api/users/{userId}/passwords/reset', // 重置密码

    FORM_UPLOAD_TEMPORARY_FILE : '/form/upload/temp/file',  // 上传一个临时文件
    FORM_UPLOAD_TEMPORARY_FILES: '/form/upload/temp/files', // 上传多个临时文件
    API_CAN_PREVIEW_FILE_PREFIX: '/api/canPreview',         // 请求是否可预览文件的前缀

    // 消息系统
    MESSAGE_WEBSOCKET_URL: `ws://${window.location.hostname}:3721`,

    // 机构
    API_ORGS        : '/api/orgs',         // 所有机构
    API_ORGS_BY_ID  : '/api/orgs/{orgId}', // 指定 ID 的机构
    API_ORGS_ENABLED: '/api/orgs/{orgId}/enabled', // 指定 ID 的机构的启用禁用状态

    // 字典
    API_DICTS_ID : '/api/dicts/{dictId}',   // 单个查询/修改/删除
    API_DICTS    : '/api/currentOrg/dicts', // 查询当前机构字典列表

    // 订单
    API_ORDERS           : '/api/orders',                   // 订单
    API_ORDERS_BY_ID     : '/api/orders/{orderId}',         // 指定 ID 的订单
    API_ORDERS_ITEM_BY_ID: '/api/orderItems/{orderItemId}', // 指定 ID 的订单项

    // 备件
    API_SPARES      : '/api/spares',           // 备件
    API_SPARES_BY_ID: '/api/spares/{spareId}', // 指定 ID 的备件
    API_SPARES_WAREHOUSING: '/api/spares/{spareId}/warehousing', // 入库 | 出库
    API_WAREHOUSING_LOGS  : '/api/warehousing/logs', // 库存日志
};

window.Urls = Urls;
