package newdt.dsc.bean;

/**
 * 整个系统的 URL 集中管理。
 */
public interface Urls {
    // 数据库元数据。
    String API_DATABASE_CATALOG_NAMES = "/api/dsc/databases/{dbid}/catalogNames"; // 数据库的 catalog。
    String API_DATABASE_SCHEMA_NAMES  = "/api/dsc/databases/{dbid}/schemaNames";  // 数据库的 schema。
    String API_DATABASE_TABLE_NAMES   = "/api/dsc/databases/{dbid}/tableNames";   // 数据库的 table。
    String API_DATABASE_VIEW_NAMES    = "/api/dsc/databases/{dbid}/viewNames";    // 数据库的 view。
    String API_DATABASE_TABLE_COLUMNS = "/api/dsc/databases/{dbid}/tableColumns"; // 数据库表的列。
}
