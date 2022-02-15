package com.xtuer.bean.stock;

import com.xtuer.bean.product.ProductItem;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * 接收出库请求的对象
 */
@Getter
@Setter
public class StockOutRequestFormBean {
    /**
     * 出库类型: 1 (产品项出库)、2 (产品出库)、3 (订单出库)
     */
    private int targetType;

    /**
     * 出库对象的 ID: 产品 ID、订单 ID (产品项出库时为 0，因可能有多个产品项)
     */
    private long targetId;

    /**
     * 要出库的产品项 (只需要产品 ID、产品项 ID 和 count)
     */
    private List<ProductItem> productItems = new LinkedList<>();

    /**
     * 当前审批员 ID
     */
    private long currentAuditorId;
}
