package newdt.dsc.bean.db;

import lombok.Data;

/**
 * 数据库的 Schema。
 */
@Data
public class Schema {
    /**
     * Schema 名字。
     */
    private String schemName;

    /**
     * 所属 catalog 名字，可能为空。
     */
    private String catalogName;
}
