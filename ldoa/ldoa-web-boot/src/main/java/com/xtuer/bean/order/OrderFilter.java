package com.xtuer.bean.order;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 查询订单的过滤器
 */
@Getter
@Setter
@Accessors(chain = true)
public class OrderFilter {
    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * 订单的产品编码，使用逗号分隔，方便搜索
     */
    private String productCodes;

    /**
     * 订单的产品名称，使用逗号分隔，方便搜索
     */
    private String productNames;

    /**
     * 客户单位
     */
    private String customerCompany;

    /**
     * 状态: 0 (初始化), 1 (待审批), 2 (审批拒绝), 3 (审批完成), 4 (完成)
     */
    private int state;

    /**
     * 订单开始时间
     */
    private Date orderDateStart;

    /**
     * 订单结束时间
     */
    private Date orderDateEnd;

    /**
     * 是否在出库请求中有记录
     */
    private boolean notInStockRequest;
}
