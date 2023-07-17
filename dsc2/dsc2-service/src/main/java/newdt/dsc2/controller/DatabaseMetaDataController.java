package newdt.dsc2.controller;

import newdt.dsc2.bean.DatabaseType;
import newdt.dsc2.bean.Response;
import newdt.dsc2.bean.Urls;
import newdt.dsc2.service.DatabaseMetaDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

/**
 * 获取数据库元数据的控制器。
 */
@RestController
public class DatabaseMetaDataController {
    @Autowired
    private DatabaseMetaDataService metaService;

    /**
     * 获取数据库的 catalogs。
     *
     * 网址: http://localhost:8080/api/dsc/databases/{dbid}/catalogs
     * 参数: type
     * 测试:
     *     curl 'http://localhost:8080/api/dsc/databases/1/catalogs?type=MYSQL'
     *     curl 'http://localhost:8080/api/dsc/databases/3/catalogs?type=POSTGRES'
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @return payload 为 catalog 数组。
     */
    @GetMapping(Urls.API_DATABASE_CATALOGS)
    public Response<List<String>> findCatalogs(@RequestParam DatabaseType type, @PathVariable int dbid) throws SQLException {
        return Response.ok(metaService.findCatalogs(type, dbid));
    }

    /**
     * 获取数据库的 schemas。
     *
     * 网址: http://localhost:8080/api/dsc/databases/{dbid}/schemas
     * 参数: type
     * 测试:
     *     curl 'http://localhost:8080/api/dsc/databases/2/schemas?type=ORACLE'
     *     curl 'http://localhost:8080/api/dsc/databases/3/schemas?type=POSTGRES&catalog=postgres'
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @return payload 为 schema 数组。
     */
    @GetMapping(Urls.API_DATABASE_SCHEMAS)
    public Response<List<String>> findSchemas(@RequestParam DatabaseType type, @PathVariable int dbid,
                                              @RequestParam(required = false) String catalog) throws SQLException {
        return Response.ok(metaService.findSchemas(type, dbid, catalog));
    }

    /**
     * 获取数据库指定 catalog 和 schema 下的 tables。
     *
     * 网址: http://localhost:8080/api/dsc/databases/{dbid}/tables
     * 参数:
     *     type (必要): 数据库类型
     *     catalog [可选]: 根据数据库而定
     *     schema  [可选]: 根据数据库而定
     * 测试:
     *     curl 'http://localhost:8080/api/dsc/databases/1/tables?type=MYSQL&catalog=test'
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @param catalog 表所属 catalog。
     * @param schema 表所属 schema。
     * @return payload 为 table 数组。
     */
    @GetMapping(Urls.API_DATABASE_TABLES)
    public Response<List<String>> findTables(@RequestParam DatabaseType type, @PathVariable int dbid,
                                             @RequestParam(required = false) String catalog,
                                             @RequestParam(required = false) String schema) throws SQLException {
        return Response.ok(metaService.findTables(type, dbid, catalog, schema));
    }

    /**
     * 获取数据库指定 catalog 和 schema 下的 views。
     *
     * 网址: http://localhost:8080/api/dsc/databases/{dbid}/views
     * 参数:
     *     type (必要): 数据库类型
     *     catalog [可选]: 根据数据库而定
     *     schema  [可选]: 根据数据库而定
     * 测试:
     *     curl 'http://localhost:8080/api/dsc/databases/1/views?type=MYSQL&catalog=test'
     *
     * @param type 数据库类型。
     * @param dbid 数据库 ID。
     * @param catalog 表所属 catalog。
     * @param schema 表所属 schema。
     * @return payload 为 view 数组。
     */
    @GetMapping(Urls.API_DATABASE_VIEWS)
    public Response<List<String>> findViews(@RequestParam DatabaseType type, @PathVariable int dbid,
                                            @RequestParam(required = false) String catalog,
                                            @RequestParam(required = false) String schema) throws SQLException {
        return Response.ok(metaService.findViews(type, dbid, catalog, schema));
    }
}
