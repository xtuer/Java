package misc;

import com.alibaba.druid.sql.SQLUtils;
import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.languages.Dialect;

/**
 * 格式化 SQL 语句:
 * A. implementation 'com.github.vertical-blank:sql-formatter:2.0.3'
 * B. implementation 'com.alibaba:druid:1.2.11'
 */
public class SqlFormat {
    public static void main(String[] args) {
        String sql = "CREATE TABLE \"C##TEST\".\"TEST01\"     ( \"id\" NUMBER(*,0),   \"ID1\" NUMBER(*,0),   \"ID2\" NUMBER(*,0),   \"NAME\" VARCHAR2(10),   \"AGE\" NUMBER(*,0),    PRIMARY KEY (\"ID\")   USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS NOCOMPRESS LOGGING   TABLESPACE \"USERS\"  ENABLE    ) SEGMENT CREATION DEFERRED    PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING   TABLESPACE \"USERS\"";
        // sql = "CREATE TABLE \"APEX_030200\".\"TEST_HUYAOYAO\" (\"ID\" NUMBER(22, 0), \"NAME\" VARCHAR2(255)) SEGMENT CREATION IMMEDIATE PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING STORAGE(\n" +
        //         "  INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645 PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT\n" +
        //         ") TABLESPACE \"SYSAUX\"";
        // sql = "create table test(id int)";
        System.out.println(SqlFormatter.format(sql));
        System.out.println(SQLUtils.formatOracle(sql));
    }
}
