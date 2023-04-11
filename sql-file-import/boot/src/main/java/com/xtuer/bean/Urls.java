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
    // 大文件上传
    String API_BIGFILE_UPLOADS             = "/api/bigfile/uploads";                 // 上传信息
    String API_BIGFILE_UPLOADS_BY_FILE_UID = "/api/bigfile/uploads/{fileUid}";        // 根据文件 Uid 对应的上传信息
    String API_BIGFILE_UPLOADS_CHUNK       = "/api/bigfile/uploads/{fileUid}/chunks"; // 文件上传的分片

    // SQL 文件导入
    String API_SQL_FILE_IMPORTS         = "/api/dsc/sql-file-imports";  // DSC 文件导入
    String API_SQL_FILE_IMPORTS_BY_ID   = "/api/dsc/sql-file-imports/{importId}"; // DSC 文件导入查询
    String API_SQL_FILE_IMPORTS_PROCESS = "/api/dsc/sql-file-imports/{importTaskId}/process"; // DSC 文件导入进度
    String API_SQL_FILE_IMPORTS_EXEC    = "/api/exec/sql-file-imports"; // SQL 执行文件导入
}
