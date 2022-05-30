package misc;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.languages.Dialect;

/**
 * 格式化 SQL 语句: implementation 'com.github.vertical-blank:sql-formatter:2.0.3'
 */
public class SqlFormat {
    public static void main(String[] args) {
        String sql = "CREATE TABLE \"C##TEST\".\"TEST01\"     ( \"id\" NUMBER(*,0),   \"ID1\" NUMBER(*,0),   \"ID2\" NUMBER(*,0),   \"NAME\" VARCHAR2(10),   \"AGE\" NUMBER(*,0),    PRIMARY KEY (\"ID\")   USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS NOCOMPRESS LOGGING   TABLESPACE \"USERS\"  ENABLE    ) SEGMENT CREATION DEFERRED    PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING   TABLESPACE \"USERS\"";
        System.out.println(SqlFormatter.of(Dialect.PlSql).format(sql));
    }
}
