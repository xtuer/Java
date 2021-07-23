package com.xtuer.mapper;

import com.xtuer.bean.Page;
import com.xtuer.bean.impex.ImpexMaintenanceOrder;
import com.xtuer.bean.order.MaintenanceOrderFilter;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 通用导出的 Mapper
 */
@Mapper
public interface ImpexMapper {
    /**
     * 查询符合条件的维保订单
     *
     * @param filter 过滤条件
     * @param page 分页
     * @return 返回维保订单的数组
     */
    List<ImpexMaintenanceOrder> findMaintenanceOrders(MaintenanceOrderFilter filter, Page page);
}
