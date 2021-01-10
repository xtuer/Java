package com.xtuer.bean.order;

import com.xtuer.util.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 维保订单 (维修保养订单)
 */
@Getter
@Setter
public class MaintenanceOrder {
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
     * 维保订单 ID
     */
    private long maintenanceOrderId;

    /**
     * 维保订单 SN
     */
    private String maintenanceOrderSn;

    /**
     * 售后服务人员 ID
     */
    private long servicePersonId;

    /**
     * 售后服务人员名字
     */
    private String servicePersonName;

    /**
     * 客户名字
     */
    private String customerName;

    /**
     * 保养
     */
    private boolean maintainable;

    /**
     * 维修
     */
    private boolean repairable;

    /**
     * 销售人员名字
     */
    private String salespersonName;

    /**
     * 收货日期
     */
    private Date receivedDate;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 产品序列号
     */
    private String productSn;

    /**
     * 规格/型号
     */
    private String model;

    /**
     * 物料编码
     */
    private String productItemCode;

    /**
     * 批次
     */
    private String batch;

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 数量
     */
    private int count;

    /**
     * 配件
     */
    private String accessories;

    /**
     * 是否需要证书
     */
    private boolean needCertificate;

    /**
     * 客户反应的问题
     */
    private String problem;

    /**
     * 状态: 0 (初始化), 1 (待审批), 2 (审批拒绝), 3 (审批完成), 4 (完成)
     */
    private int state;

    /**
     * 获取订单状态 Label
     *
     * @return 返回订单状态的 Label
     */
    public String getStateLabel() {
        return Utils.getStateLabel(STATE_LABELS, state);
    }
}
