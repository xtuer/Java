package com.xtuer.bean.impex;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelCollection;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class ImpexMaintenanceOrder {
    /**
     * 维保订单 ID
     */
    private long maintenanceOrderId;

    /**
     * 维保订单 SN
     */
    @Excel(name = "维保单号", width = 24, orderNum = "1", needMerge = true)
    private String maintenanceOrderSn;

    /**
     * 售后服务人员名字
     */
    @Excel(name = "售后服务人员", width = 20, orderNum = "10", needMerge = true)
    private String servicePersonName;

    /**
     * 客户名字
     */
    @Excel(name = "客户", width = 40, orderNum = "2", needMerge = true)
    private String customerName;

    /**
     * 销售人员名字
     */
    @Excel(name = "销售人员", width = 20, orderNum = "11", needMerge = true)
    private String salespersonName;

    /**
     * 收货日期
     */
    @Excel(name = "收货日期", width = 20, orderNum = "12", exportFormat = "yyyy-MM-dd", needMerge = true)
    private Date receivedDate;

    /**
     * 客户反馈的问题
     */
    @Excel(name = "反馈的问题", width = 40, orderNum = "8", needMerge = true)
    private String problem;

    /**
     * 进度
     */
    @Excel(name = "处理进度", width = 20, orderNum = "9", needMerge = true)
    private String progress;

    /**
     * 状态: 0 (初始化), 1 (待审批), 2 (审批拒绝), 3 (审批完成), 4 (完成)
     */
    @Excel(name = "状态", width = 20, orderNum = "7", replace = { "初始化_0", "待审批_1", "审批拒绝_2", "审批完成_3", "完成_4" }, needMerge = true)
    private int state;

    /**
     * 维保订单项
     */
    @ExcelCollection(name = "订单项", orderNum = "13")
    private List<ImpexMaintenanceOrderItem> items = new LinkedList<>();
}
