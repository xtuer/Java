package com.xtuer.bean.product;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 查询产品的过滤器
 */
@Getter
@Setter
public class ProductFilter {
    /**
     * 产品名称
     */
    private String name;

    /**
     * 产品编码
     */
    private String code;

    /**
     * 产品规格/型号
     */
    private String model;

    /**
     * 产品 ID
     */
    private List<Long> productIds;
}
