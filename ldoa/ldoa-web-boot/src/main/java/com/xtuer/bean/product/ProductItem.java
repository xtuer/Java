package com.xtuer.bean.product;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * 产品项，又叫物料
 */
@Getter
@Setter
@Accessors(chain = true)
public class ProductItem {
    /**
     * 产品项 ID
     */
    private long productItemId;

    /**
     * 所属产品 ID
     */
    private long productId;

    /**
     * 物料名称
     */
    @NotBlank(message="物料名称不能为空")
    @Excel(name = "物料名称", width = 20)
    private String name;

    /**
     * 物料编码
     */
    @NotBlank(message="物料编码不能为空")
    @Excel(name = "物料编码", width = 20)
    private String code;

    /**
     * 物料类型
     */
    @NotBlank(message="物料类型不能为空")
    @Excel(name = "物料类型", width = 20)
    private String type;

    /**
     * 物料规格/型号
     */
    @NotBlank(message="物料规格/型号不能为空")
    @Excel(name = "规格/型号", width = 20)
    private String model;

    /**
     * 标准/规范
     */
    @NotBlank(message="标准/规范不能为空")
    @Excel(name = "标准/规范", width = 20)
    private String standard;

    /**
     * 材质
     */
    @NotBlank(message="材质不能为空")
    @Excel(name = "材质", width = 20)
    private String material;

    /**
     * 物料描述
     */
    private String desc;

    /**
     * 单位
     */
    private String unit;

    /**
     * 数量
     */
    @Excel(name = "数量", width = 10)
    private int count;

    /**
     * 库存告警数量
     */
    private int warnCount;

    /**
     * 批次 (出库查询时使用)
     */
    @Excel(name = "批次", width = 20)
    private String batch;

    /**
     * 创建物料的用户 ID
     */
    private long userId;
}
