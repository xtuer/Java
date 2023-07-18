package newdt.dsc.bean;

/**
 * 整个系统的 URL 集中管理。
 */
public interface Urls {
    String API_DATABASE_CATALOGS = "/api/dsc/databases/{dbid}/catalogs"; // 数据库的 catalog。
    String API_DATABASE_SCHEMAS = "/api/dsc/databases/{dbid}/schemas"; // 数据库的 schema。
    String API_DATABASE_TABLES = "/api/dsc/databases/{dbid}/tables"; // 数据库的 table。
    String API_DATABASE_VIEWS = "/api/dsc/databases/{dbid}/views"; // 数据库的 view。
    String API_DATABASE_TABLE_COLUMNS = "/api/dsc/databases/{dbid}/tableColumns"; // 数据库表的列。
}
