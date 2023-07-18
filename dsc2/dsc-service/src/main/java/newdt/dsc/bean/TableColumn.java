package newdt.dsc.bean;

import lombok.Data;

/**
 * 数据库表的列。
 */
@Data
public class TableColumn {
    /**
     * 列名。
     */
    private String name;

    /**
     * 列的数据类型。
     */
    private String typeName;
}
