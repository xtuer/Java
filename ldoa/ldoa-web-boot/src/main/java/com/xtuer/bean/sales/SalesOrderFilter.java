package com.xtuer.bean.sales;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 销售订单过滤条件
 */
@Getter
@Setter
public class SalesOrderFilter {
    public static final int SEARCH_TYPE_ALL                     = 0;
    public static final int SEARCH_TYPE_SHOULD_PAY              = 1;
    public static final int SEARCH_TYPE_PAID_THIS_CURRENT_MONTH = 2;
    public static final int SEARCH_TYPE_PAID_THIS_CURRENT_YEAR  = 3;

    /**
     * 客户
     */
    private String customerName;

    /**
     * 主题
     */
    private String topic;

    /**
     * 行业
     */
    private String business;

    /**
     * 状态
     */
    private int state;

    /**
     * 开始支付时间
     */
    private Date paidAtStart;

    /**
     * 结束支付时间
     */
    private Date paidAtEnd;

    /**
     * 开始签约时间
     */
    private Date agreementStart;

    /**
     * 结束签约时间
     */
    private Date agreementEnd;

    /**
     * 搜索类型: 0 (所有订单)、1 (应收款订单)、2 (本月已收款订单)、3 (本年已收款订单)
     */
    private int searchType;

    /**
     * 销售员 ID
     */
    private long salesPersonId;

    /**
     * 应收金额
     */
    private double shouldPayAmount;
}
