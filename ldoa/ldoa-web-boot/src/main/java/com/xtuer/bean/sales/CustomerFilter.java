package com.xtuer.bean.sales;

import lombok.Getter;
import lombok.Setter;

/**
 * 客户的过滤条件
 */
@Getter
@Setter
public class CustomerFilter {
    /**
     * 客户名称
     */
    private String name;

    /**
     * 客户编号
     */
    private String customerSn;

    /**
     * 行业
     */
    private String business;

    /**
     * 区域
     */
    private String region;
}
