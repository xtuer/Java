const Urls = {
    // 用户
    API_USERS               : '/api/currentOrg/users',       // 当前机构用户列表
    API_USERS_COUNT         : '/api/currentOrg/users/count', // 当前机构总记录数
    API_USERS_BY_ID         : '/api/users/{userId}',         // 指定 ID 的用户
    API_USERS_CURRENT       : '/api/login/users/current',    // 当前登录的用户
    API_USER_PASSWORDS_RESET: '/api/users/{userId}/passwords/reset', // 重置密码
    API_LOGIN_TOKENS        : '/api/login/tokens',           // 登陆用户的 token

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

    // 考试
    API_EXAMS_OF_CURRENT_ORG: '/api/exam/exams/ofCurrentOrg',  // 当前机构的考试
    API_EXAMS_BY_ID         : '/api/exam/exams/{examId}',      // 指定 ID 的考试
    API_USER_EXAMS          : '/api/exam/users/{userId}/exams/{examId}', // 用户的某次考试 (同时得到此次考试的所有考试记录)
};

window.Urls = Urls;
