package com.xtuer.bean;

/**
 * 集中管理 URL.
 *
 * 其实此类名叫 Urls 不是很合适，基本都是 URI，但是对于大多数人来说 URL 更熟悉好记忆一些。
 * 还有少量变量不是 URI，例如 JSONP_CONTENT_TYPE，FORWARD 等，但不多，为了减少类，故就放在这里吧，约定好了就行。
 *
 * 变量名和 URI 规则:
 * 1. 页面 URI 的变量名以 PAGE_ 开头，此 URI 以 /page 开头，看到 URL 就知道是什么用途了
 * 2. 页面对应模版文件的变量名以 FILE_ 开头，表明文件的路径，即模版的路径
 * 3. 普通 FORM 表单处理 URI 的变量名以 FORM_ 开头，此 URI 以 /form 开头
 * 4. 操作资源的 api 变量名以 API_ 开头，此 URI 以 /api 开头，使用 RESTful 风格，资源名使用复数
 */
public interface Urls {
    String JSONP_CONTENT_TYPE = "application/javascript;charset=UTF-8"; // JSONP 响应的 header

    // 通用
    String FORWARD    = "forward:";
    String REDIRECT   = "redirect:";
    String PAGE_404   = "/404";
    String FILE_ERROR = "error.html";

    // 案例展示
    String PAGE_DEMO_REST   = "/demo/rest";
    String FILE_DEMO_REST   = "demo/rest.html";
    String FORM_DEMO_UPLOAD = "/form/demo/upload";
    String API_DEMO_MYBATIS = "/api/demo/mybatis/{id}";

    String PAGE_DOWNLOAD  = "/download"; // 下载
    String FILE_DOWNLOAD  = "download.html";  // 下载

    // 登录注销
    String PAGE_LOGIN = "/login"; // 登陆
    String PAGE_DENY  = "/deny";  // 无权访问页面的 URL
    String FILE_LOGIN = "login.html";  // 登陆页面
    String API_LOGIN_TOKENS        = "/api/login/tokens";        // 登陆用户的 token
    String API_LOGIN_USERS_CURRENT = "/api/login/users/current"; // 当前登录的用户
    String PAGE_USER_BACKEND       = "/userBackend";             // 访问当前登录用户的后台页面地址

    // 用户
    String API_USERS                = "/api/users";                          // 用户
    String API_USERS_BY_ID          = "/api/users/{userId}";                 // 指定 ID 的用户
    String API_USER_PASSWORDS_RESET = "/api/users/{userId}/passwords/reset"; // 重置密码

    String API_SERVER_CURRENT_TIME = "/api/serverCurrentTime"; // 服务器当前时间

    // API 使用 RESTful 风格，变量名以 API_ 开头，URI 以 /api 开头, 资源都用复数形式便于统一管理 URL。
    // 下面以操作 subject, qa 资源的 RESTful 风格的 URL 为例:
    // 列出 qa 有 2 个相关的 URL，一是列出所有的 questions 用 API_QUESTIONS，
    // 另一个是列出主题下的所有 questions 用 API_QUESTIONS_IN_SUBJECT。
    String API_SUBJECTS        = "/api/subjects";
    String API_SUBJECTS_BY_ID  = "/api/subjects/{subjectId}";

    // 上传文件、图片到临时目录，仓库中的文件为正式的文件
    String FORM_UPLOAD_TEMP_FILE  = "/form/upload/temp/file";  // 上传一个临时文件
    String FORM_UPLOAD_TEMP_FILES = "/form/upload/temp/files"; // 上传多个临时文件
    String URL_TEMP_FILE_PREFIX   = "/file/temp/";             // 临时文件的 URL 前缀
    String URL_TEMP_FILE          = "/file/temp/{filename}";   // 临时文件的 URL
    String URL_REPO_FILE_PREFIX   = "/file/repo/";             // 仓库文件的 URL 前缀
    String URL_REPO_FILE          = "/file/repo/**";           // 仓库文件的 URL
    String URL_REPO_FILE_DOWNLOAD = "/file/download/**";       // 下载仓库文件的 URL

    // 网盘
    String API_DISK_FILES       = "/api/disk/files";          // 网盘中的文件
    String API_DISK_FILES_BY_ID = "/api/disk/files/{fileId}"; // 指定 ID 的网盘中的文件

    // 机构
    String API_ORGS        = "/api/orgs";                 // 所有机构
    String API_ORGS_BY_ID  = "/api/orgs/{orgId}";         // 指定 ID 的机构
    String API_ORGS_ENABLE = "/api/orgs/{orgId}/enabled"; // 指定 ID 的机构的启用禁用状态

    // 产品
    String API_PRODUCTS            = "/api/products";             // 所有产品
    String API_PRODUCTS_BY_ID      = "/api/products/{productId}"; // 指定 ID 的产品
    String API_PRODUCTS_EXPORT     = "/api/products/export";      // 导出产品
    String API_PRODUCT_ITEMS       = "/api/productItems";         // 所有产品项
    String API_PRODUCT_ITEMS_BY_ID = "/api/productItems/{productItemId}"; // 指定 ID 的产品项

    // 订单
    String API_ORDERS          = "/api/orders";                    // 订单
    String API_ORDERS_BY_ID    = "/api/orders/{orderId}";          // 指定 ID 的订单
    String API_ORDERS_COMPLETE = "/api/orders/{orderId}/complete"; // 完成订单
    String API_ORDERS_PROGRESS = "/api/orders/{orderId}/progress"; // 订单的进度
    String API_ORDERS_EXPORT   = "/api/orders/export";             // 导出订单
    String API_ORDERS_PRODUCTS = "/api/orders/{orderId}/products"; // 订单的产品

    // 维保订单
    String API_MAINTENANCE_ORDERS          = "/api/maintenance-orders";
    String API_MAINTENANCE_ORDERS_BY_ID    = "/api/maintenance-orders/{orderId}";
    String API_MAINTENANCE_ORDERS_COMPLETE = "/api/maintenance-orders/{orderId}/complete"; // 完成订单
    String API_MAINTENANCE_ORDERS_PROGRESS = "/api/maintenance-orders/{orderId}/progress"; // 订单的进度
    String API_MAINTENANCE_ORDER_ITEMS     = "/api/maintenance-orders/{orderId}/items";    // 维保订单项
    String API_MAINTENANCE_ORDERS_EXPORT   = "/api/maintenance-orders/export";             // 导出维保订单

    // 审批
    String API_AUDITS                = "/api/audits";                               // 审批
    String API_AUDITS_BY_ID          = "/api/audits/{auditId}";                     // 指定 ID 的审批
    String API_AUDITS_BY_TARGET      = "/api/audits/of-target/{targetId}";          // 审批目标的审批
    String API_AUDIT_CONFIGS         = "/api/audit-configs";                        // 审批配置
    String API_AUDIT_CONFIGS_BY_TYPE = "/api/audit-configs/of-type/{type}";         // 指定类型的审批
    String API_AUDIT_STEPS           = "/api/audit-steps";                          // 审批项
    String API_AUDIT_STEPS_ACCEPT    = "/api/audits/{auditId}/steps/{step}/accept"; // 通过或拒绝审批阶段
    String API_AUDIT_STEPS_RECALL    = "/api/audits/{auditId}/steps/{step}/recall"; // 撤销审批阶段
    String API_AUDITORS              = "/api/auditors";                             // 审批员
    String API_WAITING_AUDIT_STEPS_COUNT_BY_USER_ID = "/api/users/{userId}/waiting-audit-steps-count"; // 待审批阶段的数量

    // 库存
    String API_STOCKS          = "/api/stocks";          // 库存
    String API_STOCKS_RECORDS  = "/api/stocks/records";  // 库存操作记录
    String API_STOCKS_IN       = "/api/stocks/in";       // 入库
    String API_STOCKS_OUT      = "/api/stocks/out";      // 出库
    String API_STOCKS_EXPORT   = "/api/stocks/export";   // 库存导出
    String API_STOCKS_RECORDS_BY_ID      = "/api/stocks/records/{recordId}"; // 库存操作记录
    String API_STOCKS_OUT_REQUESTS       = "/api/stocks/out/requests"; // 出库申请
    String API_STOCKS_OUT_REQUESTS_BY_ID = "/api/stocks/out/requests/{requestId}"; // 指定 ID 的出库申请
    String API_STOCKS_REQUESTS           = "/api/stocks/requests";     // 库存操作申请
    String API_STOCKS_REQUESTS_COUNT     = "/api/stocks/requests/count";       // 库存操作申请数量
    String API_STOCKS_REQUESTS_BY_ID     = "/api/stocks/requests/{requestId}"; // 指定 ID 的库存操作申请
    String API_STOCKS_PRODUCT_ITEM_OUT_REQUESTS = "/api/stocks/product-items/{productItemId}/out/requests"; // 物料的出库记录

    // 销售
    String API_SALES_CUSTOMERS         = "/api/sales/customers"; // 客户
    String API_SALES_CUSTOMERS_BY_ID   = "/api/sales/customers/{customerId}"; // 指定 ID 的客户
    String API_SALES_CUSTOMERS_FINANCE = "/api/sales/customers/{customerId}/finance"; // 指定 ID 的客户财务信息
    String API_SALES_CUSTOMERS_IMPORT  = "/api/sales/customers/import"; // 导入客户
    String API_SALES_CUSTOMERS_EXPORT  = "/api/sales/customers/export"; // 导出客户
    String API_SALES_ORDERS            = "/api/sales/salesOrders"; // 销售订单
    String API_SALES_ORDERS_BY_ID      = "/api/sales/salesOrders/{salesOrderId}"; // 指定 ID 的销售订单
    String API_SALES_ORDERS_PAYMENTS   = "/api/sales/salesOrders/{salesOrderId}/payments"; // 订单收款
    String API_SALES_ORDERS_COMPLETE   = "/api/sales/salesOrders/{salesOrderId}/complete"; // 完成订单
    String API_SALES_ORDERS_EXPORT     = "/api/sales/salesOrders/export";         // 导出销售订单
    String API_SALES_ORDERS_EXPORT_PAY = "/api/sales/salesOrders/export-payment"; // 导出支付信息的销售订单

    // 表格配置
    String API_TABLE_CONFIG_BY_TABLE_NAME_AND_USER = "/api/tables/{tableName}/users/{userId}/config";

    // 消息
    String API_MESSAGES_AS_READ                  = "/api/messages/{messageId}/read"; // 标记消息为已读
    String API_MESSAGES_OF_RECEIVER              = "/api/messages/receivers/{receiverId}/messages"; // 指定接收者的消息
    String API_MESSAGES_UNREAD_COUNT_OF_RECEIVER = "/api/messages/receivers/{receiverId}/messages/unreadCount"; // 指定接收者的未读消息
}
