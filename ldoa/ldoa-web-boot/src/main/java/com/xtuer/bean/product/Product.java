package com.xtuer.bean.product;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.LinkedList;
import java.util.List;

/**
 * 产品，每个产品有多个产品项 (物料)
 */
@Getter
@Setter
@Accessors(chain = true)
public class Product {
    /**
     * 产品 ID
     */
    private long productId;

    /**
     * 产品名称
     */
    @NotBlank(message="产品名称不能为空")
    @Excel(name = "产品名称", width = 20)
    private String name;

    /**
     * 产品编码
     */
    @NotBlank(message="产品编码不能为空")
    @Excel(name = "产品编码", width = 20)
    private String code;

    /**
     * 产品规格/型号
     */
    @NotBlank(message="产品规格/型号不能为空")
    @Excel(name = "规格/型号", width = 20)
    private String model;

    /**
     * 产品描述
     */
    @Excel(name = "产品描述", width = 80)
    private String desc;

    /**
     * 创建产品的用户 ID
     */
    private long userId;

    /**
     * 产品项
     */
    private List<ProductItem> items = new LinkedList<>();
}
