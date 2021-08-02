package office;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class Student {
    private long id;

    @Excel(name = "学生姓名")
    private String name;

    @Excel(name = "性别", replace = {"男_1", "女_2"}, suffix = "生")
    private int gender;

    @Excel(name = "出生日期", format = "yyyy-MM-dd")
    private Date birthday;

    @Excel(name = "入学日期", format = "yyyy-MM-dd")
    private Date registrationDate;
}
