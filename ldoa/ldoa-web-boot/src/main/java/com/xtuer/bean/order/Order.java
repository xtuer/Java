package com.xtuer.bean.order;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.xtuer.bean.UploadedFile;
import com.xtuer.bean.User;
import com.xtuer.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * 订单
 */
@Getter
@Setter
@Accessors(chain = true)
public class Order {
    public static final int STATE_INIT = 0;
    public static final int STATE_AUDITING = 1;
    public static final int STATE_REJECTED = 2;
    public static final int STATE_ACCEPTED = 3;
    public static final int STATE_COMPLETE = 4;

    /**
     * 状态值与对应的 Label: 数组的下标为状态值，对应的数组元素值为状态的 Label
     */
    private static final String[] STATE_LABELS = { "初始化", "审批中", "审批拒绝", "审批通过", "完成" };

    /**
     * 订单 ID
     */
    private long orderId;

    /**
     * 订单编号
     */
    @Excel(name = "订单编号", width = 24, orderNum = "1")
    private String orderSn;

    /**
     * 订单类型: 0 (销售订单)、1 (样品订单)
     */
    @Excel(name = "订单类型", width = 20, orderNum = "4", replace = {"销售订单_0", "样品订单_1"})
    private int type;

    /**
     * 客户单位
     */
    @NotBlank(message = "客户单位不能为空")
    @Excel(name = "客户单位", width = 40, orderNum = "2")
    private String customerCompany;

    /**
     * 客户联系人
     */
    @NotBlank(message = "客户联系人不能为空")
    private String customerContact;

    /**
     * 客户收件地址
     */
    @NotBlank(message = "客户收件地址不能为空")
    private String customerAddress;

    /**
     * 订单日期
     */
    @NotNull(message = "订单日期不能为空")
    @Excel(name = "订单日期", width = 20, orderNum = "5", exportFormat = "yyyy-MM-dd")
    private Date orderDate;

    /**
     * 交货日期
     */
    @NotNull(message = "交货日期不能为空")
    @Excel(name = "交货日期", width = 20, orderNum = "6", exportFormat = "yyyy-MM-dd")
    private Date deliveryDate;

    /**
     * 归还日期
     */
    private Date returnDate;

    /**
     * 销售员 ID
     */
    private long salespersonId;

    /**
     * 销售员名字
     */
    @Excel(name = "销售负责人", width = 20, orderNum = "7")
    private String salespersonName;

    /**
     * 是否校准
     */
    private boolean calibrated;

    /**
     * 校准信息
     */
    private String calibrationInfo;

    /**
     * 要求
     */
    private String requirement;

    /**
     * 附件 ID
     */
    private long attachmentId;

    /**
     * 订单创建日期
     */
    private Date createdAt;

    /**
     * 状态: 0 (初始化), 1 (待审批), 2 (审批拒绝), 3 (审批完成), 4 (完成)
     */
    @Excel(name = "状态", width = 20, orderNum = "8", replace = {"初始化_0", "待审批_1", "审批拒绝_2", "审批完成_3", "完成_4"})
    private int state;

    /**
     * 订单的产品编码，使用逗号分隔，方便搜索
     */
    private String productCodes;

    /**
     * 订单的产品名称，使用逗号分隔，方便搜索
     */
    @Excel(name = "产品名称", width = 20, orderNum = "3")
    private String productNames;

    /**
     * 订单项
     */
    private List<OrderItem> items = new LinkedList<>();

    /**
     * 销售员
     */
    private User salesperson = new User();

    /**
     * 附件
     */
    private UploadedFile attachment = new UploadedFile();

    /**
     * 当前审批员 ID
     */
    private long currentAuditorId;

    /**
     * 获取订单状态 Label
     *
     * @return 返回订单状态的 Label
     */
    public String getStateLabel() {
        return Utils.getStateLabel(STATE_LABELS, state);
    }

    public String getSalespersonName() {
        return salesperson.getNickname();
    }
}
