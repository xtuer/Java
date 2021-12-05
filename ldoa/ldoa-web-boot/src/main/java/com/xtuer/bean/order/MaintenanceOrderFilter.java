package com.xtuer.bean.order;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 维保订单过滤条件
 */
@Getter
@Setter
public class MaintenanceOrderFilter {
    /**
     * 状态，为 -1 则查询所有
     */
    private int state = -1;

    /**
     * 维保单号
     */
    private String maintenanceOrderSn;

    /**
     * 销售人员
     */
    private String salespersonName;

    /**
     * 售后服务人员名字
     */
    private String servicePersonName;

    /**
     * 客户
     */
    private String customerName;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 规格/型号
     */
    private String productModel;

    /**
     * 收货开始时间
     */
    private Date receivedStartAt;

    /**
     * 收货结束时间
     */
    private Date receivedEndAt;
}
