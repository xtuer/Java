package newdt.dsc;

import newdt.dsc.bean.db.DatabaseType;
import newdt.dsc.service.db.DatabaseMetadataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class DatabaseMetadataBootTest {
    private static final DatabaseType DB_TYPE = DatabaseType.MYSQL;
    private static final int    DBID      = 1;
    private static final String CATALOG   = "meta_test_catalog";
    private static final String SCHEMA    = "meta_test_schema";
    private static final String TABLE     = "meta_test_table";
    private static final String VIEW      = "meta_test_view";
    private static final String PROCEDURE = "meta_test_procedure";
    private static final String FUNCTION  = "meta_test_function";

    @Autowired
    private DatabaseMetadataService metaService;

    // 查询 catalog。
    @Test
    public void testCatalogNames() throws Exception {
        List<String> names = metaService.findCatalogNames(DB_TYPE, DBID);
        System.out.println(names);
    }

    // 查询 schema。
    @Test
    public void testSchemaNames() throws Exception {
        List<String> names = metaService.findSchemaNames(DB_TYPE, DBID, CATALOG);
        System.out.println(names);
    }

    // 查询 table。
    @Test
    public void testTableNames() throws Exception {
        List<String> names = metaService.findTableNames(DB_TYPE, DBID, CATALOG, SCHEMA);
        System.out.println(names);
    }

    // 查询 view。
    @Test
    public void testViewNames() throws Exception {
        List<String> names = metaService.findViewNames(DB_TYPE, DBID, CATALOG, SCHEMA);
        System.out.println(names);
    }

    // 查询 table columns。
    @Test
    public void testTableColumns() throws Exception {
        List<?> columns = metaService.findTableColumns(DB_TYPE, DBID, CATALOG, SCHEMA, TABLE);
        System.out.println(columns);
    }

    // 查询建表语句。
    @Test
    public void testTableDdl() throws Exception {
        String ddl = metaService.findTableDdl(DB_TYPE, DBID, CATALOG, SCHEMA, TABLE);
        System.out.println(ddl);
    }

    // 查询视图创建语句。
    @Test
    public void testViewDdl() throws Exception {
        String ddl = metaService.findViewDdl(DB_TYPE, DBID, CATALOG, SCHEMA, VIEW);
        System.out.println(ddl);
    }

    // 查询存储过程创建语句。
    @Test
    public void testProcedureDdl() throws Exception {
        String ddl = metaService.findProcedureDdl(DB_TYPE, DBID, CATALOG, SCHEMA, PROCEDURE);
        System.out.println(ddl);
    }

    // 查询存储函数创建语句。
    @Test
    public void testFunctionDdl() throws Exception {
        String ddl = metaService.findFunctionDdl(DB_TYPE, DBID, CATALOG, SCHEMA, FUNCTION);
        System.out.println(ddl);
    }
}
