package newdt.dsc2.bean;

/**
 * 整个系统的 URL 集中管理。
 */
public interface Urls {
    String API_DATABASE_CATALOGS = "/api/dsc/databases/{dbid}/catalogs"; // 数据库的 catalog。
    String API_DATABASE_SCHEMAS = "/api/dsc/databases/{dbid}/schemas"; // 数据库的 schema。
}
