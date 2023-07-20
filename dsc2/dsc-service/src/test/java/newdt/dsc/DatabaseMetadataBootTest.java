package newdt.dsc;

import newdt.dsc.bean.db.DatabaseType;
import newdt.dsc.service.db.DatabaseMetadataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DatabaseMetadataBootTest {
    private static final DatabaseType DB_TYPE = DatabaseType.MYSQL;
    private static final int DBID = 1;
    private static final String CATALOG = "test";
    private static final String SCHEMA = "test";

    @Autowired
    private DatabaseMetadataService metaService;

    // 查询建表语句。
    @Test
    public void testTableDdl() throws Exception {
        String ddl = metaService.findTableDdl(DB_TYPE, DBID, CATALOG, SCHEMA, "sp_test");
        System.out.println(ddl);
    }

    // 查询存储过程创建语句。
    @Test
    public void testProcedureDdl() throws Exception {
        String ddl = metaService.findProcedureDdl(DB_TYPE, DBID, CATALOG, SCHEMA, "proc_mix_demo");
        System.out.println(ddl);
    }

    // 查询存储函数创建语句。
    @Test
    public void testFunctionDdl() throws Exception {
        String ddl = metaService.findFunctionDdl(DB_TYPE, DBID, CATALOG, SCHEMA, "func_dateToStr");
        System.out.println(ddl);
    }
}
