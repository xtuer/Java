package newdt.dsc.controller;

import newdt.dsc.bean.Response;
import newdt.dsc.bean.Urls;
import newdt.dsc.bean.db.DatabaseMetadataConfig;
import newdt.dsc.bean.db.DatabaseType;
import newdt.dsc.config.DatabaseMetadataConfigs;
import newdt.dsc.service.db.DatabaseMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 获取数据库元数据的控制器。可以获取数据有 catalog，schema，table，view，columns，procedure，function 等。
 */
@RestController
public class DatabaseMetadataController {
    @Autowired
    private DatabaseMetadataService metaService;

    @Autowired
    private DatabaseMetadataConfigs metaConfigs;

    /**
     * 获取数据库元数据配置。
     *
     * 网址: http://localhost:8080/api/dsc/databases/configs
     * 参数: 无
     * 测试: curl http://localhost:8080/api/dsc/databases/configs
     *
     * @return payload 为元数据配置的数组。
     */
    @GetMapping(Urls.API_DATABASE_CONFIGS)
    public Response<List<DatabaseMetadataConfig>> findDatabaseMetadataConfigs() {
        return Response.ok(metaConfigs.getDatabaseMetadataConfigs());
    }

    /**
     * 获取数据库的 catalog 名字。
     *
     * 网址: http://localhost:8080/api/dsc/databases/{dbid}/catalogNames
     * 参数:
     *     type (必要): 数据库类型。
     * 测试:
     *     curl 'http://localhost:8080/api/dsc/databases/1/catalogNames?type=MYSQL'
     *     curl 'http://localhost:8080/api/dsc/databases/3/catalogNames?type=POSTGRES'
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @return payload 为 catalog 名字数组。
     */
    @GetMapping(Urls.API_DATABASE_CATALOG_NAMES)
    public Response<List<String>> findCatalogNames(@RequestParam DatabaseType type, @PathVariable int dbid) throws SQLException {
        return Response.ok(metaService.findCatalogNames(type, dbid));
    }

    /**
     * 获取数据库的 schema 名字。
     *
     * 网址: http://localhost:8080/api/dsc/databases/{dbid}/schemaNames
     * 参数:
     *     type    (必要): 数据库类型。
     *     catalog [可选]: Schema 所属 catalog。
     * 测试:
     *     curl 'http://localhost:8080/api/dsc/databases/2/schemaNames?type=ORACLE'
     *     curl 'http://localhost:8080/api/dsc/databases/3/schemaNames?type=POSTGRES&catalog=postgres'
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @param catalog Schema 所属 catalog。
     * @return payload 为 schema 名字数组。
     */
    @GetMapping(Urls.API_DATABASE_SCHEMA_NAMES)
    public Response<List<String>> findSchemaNames(@RequestParam DatabaseType type,
                                                  @PathVariable int dbid,
                                                  @RequestParam(required = false) String catalog) throws SQLException {
        return Response.ok(metaService.findSchemaNames(type, dbid, catalog));
    }

    /**
     * 获取数据库指定 catalog 和 schema 下的 table 名字。
     *
     * 网址: http://localhost:8080/api/dsc/databases/{dbid}/tableNames
     * 参数:
     *     type    (必要): 数据库类型
     *     catalog [可选]: 根据数据库而定
     *     schema  [可选]: 根据数据库而定
     * 测试:
     *     curl 'http://localhost:8080/api/dsc/databases/1/tableNames?type=MYSQL&catalog=meta_test_catalog'
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @param catalog 表所属 catalog。
     * @param schema 表所属 schema。
     * @return payload 为 table 名字数组。
     */
    @GetMapping(Urls.API_DATABASE_TABLE_NAMES)
    public Response<List<String>> findTableNames(@RequestParam DatabaseType type,
                                                 @PathVariable int dbid,
                                                 @RequestParam(required = false) String catalog,
                                                 @RequestParam(required = false) String schema) throws SQLException {
        return Response.ok(metaService.findTableNames(type, dbid, catalog, schema));
    }

    /**
     * 获取数据库指定 catalog 和 schema 下的 view 名字。
     *
     * 网址: http://localhost:8080/api/dsc/databases/{dbid}/viewNames
     * 参数:
     *     type    (必要): 数据库类型
     *     catalog [可选]: 根据数据库而定
     *     schema  [可选]: 根据数据库而定
     * 测试:
     *     curl 'http://localhost:8080/api/dsc/databases/1/viewNames?type=MYSQL&catalog=meta_test_catalog'
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @param catalog 表所属 catalog。
     * @param schema 表所属 schema。
     * @return payload 为 view 名字数组。
     */
    @GetMapping(Urls.API_DATABASE_VIEW_NAMES)
    public Response<List<String>> findViewNames(@RequestParam DatabaseType type,
                                                @PathVariable int dbid,
                                                @RequestParam(required = false) String catalog,
                                                @RequestParam(required = false) String schema) throws SQLException {
        return Response.ok(metaService.findViewNames(type, dbid, catalog, schema));
    }

    /**
     * 获取数据库的表的列。
     *
     * 网址: http://localhost:8080/api/dsc/databases/{dbid}/tableColumns
     * 参数:
     *    type    (必要): 数据库类型
     *    catalog [可选]: 根据数据库而定
     *    schema  [可选]: 根据数据库而定
     *    table   (必要): 表名
     * 测试:
     *     curl 'http://localhost:8080/api/dsc/databases/1/tableColumns?type=MYSQL&catalog=meta_test_catalog&table=meta_test_table'
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @param catalog 表所属 catalog。
     * @param schema 表所属 schema。
     * @param table 表名。
     * @return payload 为表的列的数组。
     */
    @GetMapping(Urls.API_DATABASE_TABLE_COLUMNS)
    public Response<List<Map<String, Object>>> findTableColumns(@RequestParam DatabaseType type,
                                                                @PathVariable int dbid,
                                                                @RequestParam(required = false) String catalog,
                                                                @RequestParam(required = false) String schema,
                                                                @RequestParam String table) throws SQLException {
        return Response.ok(metaService.findTableColumns(type, dbid, catalog, schema, table));
    }

    /**
     * 获取数据库的表的列名。
     *
     * 网址: http://localhost:8080/api/dsc/databases/{dbid}/tableColumnNames
     * 参数:
     *    type    (必要): 数据库类型
     *    catalog [可选]: 根据数据库而定
     *    schema  [可选]: 根据数据库而定
     *    table   (必要): 表名
     * 测试:
     *     curl 'http://localhost:8080/api/dsc/databases/1/tableColumnNames?type=MYSQL&catalog=meta_test_catalog&table=meta_test_table'
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @param catalog 表所属 catalog。
     * @param schema 表所属 schema。
     * @param table 表名。
     * @return payload 为表的列名的数组。
     */
    @GetMapping(Urls.API_DATABASE_TABLE_COLUMN_NAMES)
    public Response<List<String>> findTableColumnNames(@RequestParam DatabaseType type,
                                                       @PathVariable int dbid,
                                                       @RequestParam(required = false) String catalog,
                                                       @RequestParam(required = false) String schema,
                                                       @RequestParam String table) throws SQLException {
        return Response.ok(metaService.findTableColumnNames(type, dbid, catalog, schema, table));
    }

    /**
     * 获取表的建表语句。
     *
     * 网址: http://localhost:8080/api/dsc/databases/{dbid}/tableDdls
     * 参数:
     *     type    (必要): 数据库类型
     *     catalog [可选]: 根据数据库而定
     *     schema  [可选]: 根据数据库而定
     *     table   (必要): 表名
     * 测试: curl 'http://localhost:8080/api/dsc/databases/1/tableDdls?type=MYSQL&catalog=meta_test_catalog&table=meta_test_table'
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @param catalog 表所属 catalog。
     * @param schema 表所属 schema。
     * @param table 表名。
     * @return payload 为表的建表语句。
     */
    @GetMapping(Urls.API_DATABASE_TABLE_DDLS)
    public Response<String> findTableDdl(@RequestParam DatabaseType type,
                                         @PathVariable int dbid,
                                         @RequestParam(required = false) String catalog,
                                         @RequestParam(required = false) String schema,
                                         @RequestParam String table) throws SQLException {
        return Response.ok(metaService.findTableDdl(type, dbid, catalog, schema, table));
    }

    /**
     * 获取视图的创建语句。
     *
     * 网址: http://localhost:8080/api/dsc/databases/{dbid}/viewDdls
     * 参数:
     *     type    (必要): 数据库类型
     *     catalog [可选]: 根据数据库而定
     *     schema  [可选]: 根据数据库而定
     *     view    (必要): 视图名
     * 测试: curl 'http://localhost:8080/api/dsc/databases/1/viewDdls?type=MYSQL&catalog=meta_test_catalog&view=meta_test_view'
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @param catalog 视图所属 catalog。
     * @param schema 视图所属 schema。
     * @param view 视图名。
     * @return payload 为视图的创建语句。
     */
    @GetMapping(Urls.API_DATABASE_VIEW_DDLS)
    public Response<String> findViewDdl(@RequestParam DatabaseType type,
                                        @PathVariable int dbid,
                                        @RequestParam(required = false) String catalog,
                                        @RequestParam(required = false) String schema,
                                        @RequestParam String view) throws SQLException {
        return Response.ok(metaService.findViewDdl(type, dbid, catalog, schema, view));
    }

    /**
     * 获取存储过程的创建语句。
     *
     * 网址: http://localhost:8080/api/dsc/databases/{dbid}/procedureDdls
     * 参数:
     *     type      (必要): 数据库类型
     *     catalog   [可选]: 根据数据库而定
     *     schema    [可选]: 根据数据库而定
     *     procedure (必要): 存储过程名
     * 测试: curl 'http://localhost:8080/api/dsc/databases/1/procedureDdls?type=MYSQL&catalog=meta_test_catalog&procedure=meta_test_procedure'
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @param catalog 存储过程所属 catalog。
     * @param schema 存储过程所属 schema。
     * @param procedure 存储过程名。
     * @return payload 为存储过程的创建语句。
     */
    @GetMapping(Urls.API_DATABASE_PROCEDURE_DDLS)
    public Response<String> findProcedureDdl(@RequestParam DatabaseType type,
                                                   @PathVariable int dbid,
                                                   @RequestParam(required = false) String catalog,
                                                   @RequestParam(required = false) String schema,
                                                   @RequestParam String procedure) throws SQLException {
        return Response.ok(metaService.findProcedureDdl(type, dbid, catalog, schema, procedure));
    }

    /**
     * 获取存储函数的创建语句。
     *
     * 网址: http://localhost:8080/api/dsc/databases/{dbid}/functionDdls
     * 参数:
     *     type     (必要): 数据库类型
     *     catalog  [可选]: 根据数据库而定
     *     schema   [可选]: 根据数据库而定
     *     function (必要): 存储函数名
     * 测试: curl 'http://localhost:8080/api/dsc/databases/1/functionDdls?type=MYSQL&catalog=meta_test_catalog&function=meta_test_function'
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @param catalog 存储函数所属 catalog。
     * @param schema 存储函数所属 schema。
     * @param function 存储函数名。
     * @return payload 为存储函数的创建语句。
     */
    @GetMapping(Urls.API_DATABASE_FUNCTION_DDLS)
    public Response<String> findFunctionDdl(@RequestParam DatabaseType type,
                                            @PathVariable int dbid,
                                            @RequestParam(required = false) String catalog,
                                            @RequestParam(required = false) String schema,
                                            @RequestParam String function) throws SQLException {
        return Response.ok(metaService.findFunctionDdl(type, dbid, catalog, schema, function));
    }
}
