import com.google.common.collect.ImmutableSet;
import misc.OracleDataTransferUtils;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

public class OracleDataTransferTest {
    @Test
    public void testCreateTable() {
        String originalCreateTableSql = "CREATE TABLE \"DBMON\".\"TEST\" (\n" +
                "  \"ID\" NUMBER(*, 0),\n" +
                "  \"NAME\" VARCHAR2(256),\n" +
                "  \"AGE\" NUMBER(*, 0)\n" +
                ") SEGMENT CREATION DEFERRED PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING TABLESPACE \"USERS\"";

        Set<String> columnNames = ImmutableSet.of("ID", "NAME", "AGE", "FOO");
        Map<String, String> newCreateTableSqlMap = OracleDataTransferUtils.generateDataTransferCreateTableSql("TEST", columnNames, originalCreateTableSql);
        System.out.println(newCreateTableSqlMap);
        System.out.println(OracleDataTransferUtils.generatePreparedInsertSql(newCreateTableSqlMap.get("tableName"), columnNames));
    }

    @Test
    public void testFormatSql() {
        String sql = "CREATE table \"DBMON\".\"TEST\" (\"id\" NUMBER(*, 0),\n" +
                "  \"NAME\" VARCHAR2(256),  \"AGE\" NUMBER(*, 0)\n" +
                ") SEGMENT CREATION DEFERRED PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING TABLESPACE \"USERS\"";
        System.out.println(OracleDataTransferUtils.formatSql(sql));
    }
}
