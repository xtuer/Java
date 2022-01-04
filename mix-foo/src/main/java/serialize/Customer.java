package serialize;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Customer {
    /**
     * 客户 ID
     */
    private int id;

    /**
     * 客户名字
     */
    private String name;
}
