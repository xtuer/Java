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

    // 网盘
    API_DISK_FILES      : '/api/disk/files',          // 网盘中的文件
    API_DISK_FILES_BY_ID: '/api/disk/files/{fileId}', // 指定 ID 的网盘中的文件

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
    API_PRODUCTS_EXPORT    : '/api/products/export',      // 导出产品
    API_PRODUCT_ITEMS      : '/api/productItems',         // 所有产品项
    API_PRODUCT_ITEMS_BY_ID: '/api/productItems/{productItemId}', // 指定 ID 的产品项

    // 订单
    API_ORDERS         : '/api/orders',                    // 订单
    API_ORDERS_BY_ID   : '/api/orders/{orderId}',          // 指定 ID 的订单
    API_ORDERS_COMPLETE: '/api/orders/{orderId}/complete', // 完成订单
    API_ORDERS_PROGRESS: '/api/orders/{orderId}/progress', // 订单的进度
    API_ORDERS_EXPORT  : '/api/orders/export',             // 导出订单

    // 维保订单
    API_MAINTENANCE_ORDERS         : '/api/maintenance-orders',
    API_MAINTENANCE_ORDERS_BY_ID   : '/api/maintenance-orders/{orderId}',
    API_MAINTENANCE_ORDERS_COMPLETE: '/api/maintenance-orders/{orderId}/complete', // 完成订单
    API_MAINTENANCE_ORDERS_PROGRESS: '/api/maintenance-orders/{orderId}/progress', // 订单的进度
    API_MAINTENANCE_ORDER_ITEMS    : '/api/maintenance-orders/{orderId}/items',    // 维保订单项
    API_MAINTENANCE_ORDERS_EXPORT  : '/api/maintenance-orders/export',             // 导出维保订单

    // 审批
    API_AUDITS            : '/api/audits',                      // 审批
    API_AUDITS_BY_ID      : '/api/audits/{auditId}',            // 指定 ID 的审批
    API_AUDITS_BY_TARGET  : '/api/audits/of-target/{targetId}', // 审批目标的审批
    API_AUDIT_CONFIGS     : '/api/audit-configs',               // 审批配置
    API_AUDIT_STEPS       : '/api/audit-steps',                 // 审批项
    API_AUDIT_STEPS_ACCEPT: '/api/audits/{auditId}/steps/{step}/accept', // 通过或拒绝审批阶段
    API_AUDIT_STEPS_RECALL: '/api/audits/{auditId}/steps/{step}/recall', // 撤销审批阶段
    API_AUDITORS          : '/api/auditors',                    // 审批员
    API_WAITING_AUDIT_STEPS_COUNT_BY_USER_ID: '/api/users/{userId}/waiting-audit-steps-count', // 待审批阶段的数量

    // 库存
    API_STOCKS                   : '/api/stocks',              // 库存
    API_STOCKS_EXPORT            : '/api/stocks/export',       // 库存导出
    API_STOCKS_RECORDS           : '/api/stocks/records',      // 库存操作记录
    API_STOCKS_RECORDS_BY_ID     : '/api/stocks/records/{recordId}', // 库存操作记录
    API_STOCKS_IN                : '/api/stocks/in',           // 入库
    API_STOCKS_OUT               : '/api/stocks/out',          // 出库
    API_STOCKS_OUT_REQUESTS      : '/api/stocks/out/requests', // 出库申请
    API_STOCKS_OUT_REQUESTS_BY_ID: '/api/stocks/out/requests/{requestId}', // 指定 ID 的出库申请
    API_STOCKS_REQUESTS          : '/api/stocks/requests',     // 库存操作申请
    API_STOCKS_REQUESTS_BY_ID    : '/api/stocks/requests/{requestId}', // 指定 ID 的库存操作申请

    // 销售
    API_SALES_CUSTOMERS        : '/api/sales/customers', // 客户
    API_SALES_CUSTOMERS_BY_ID  : '/api/sales/customers/{customerId}', // 指定 ID 的客户
    API_SALES_CUSTOMERS_FINANCE: '/api/sales/customers/{customerId}/finance', // 指定 ID 的客户财务信息
    API_SALES_CUSTOMERS_IMPORT : '/api/sales/customers/import', // 导入客户
    API_SALES_CUSTOMERS_EXPORT : '/api/sales/customers/export', // 导出客户
    API_SALES_ORDERS           : '/api/sales/salesOrders', // 销售订单
    API_SALES_ORDERS_BY_ID     : '/api/sales/salesOrders/{salesOrderId}', // 指定 ID 的销售订单
    API_SALES_ORDERS_PAYMENTS  : '/api/sales/salesOrders/{salesOrderId}/payments', // 订单收款
    API_SALES_ORDERS_COMPLETE  : '/api/sales/salesOrders/{salesOrderId}/complete', // 完成订单
    API_SALES_ORDERS_EXPORT    : '/api/sales/salesOrders/export',                  // 导出销售订单
    API_SALES_ORDERS_EXPORT_PAY: '/api/sales/salesOrders/export-payment',          // 导出支付信息的销售订单

    // 表格配置
    API_TABLE_CONFIG_BY_TABLE_NAME_AND_USER: '/api/tables/{tableName}/users/{userId}/config',

    // 消息
    API_MESSAGES_AS_READ                 : '/api/messages/{messageId}/read', // 标记消息为已读
    API_MESSAGES_OF_RECEIVER             : '/api/messages/receivers/{receiverId}/messages', // 指定接收者的消息
    API_MESSAGES_UNREAD_COUNT_OF_RECEIVER: '/api/messages/receivers/{receiverId}/messages/unreadCount', // 指定接收者的未读消息
};

window.Urls = Urls;
