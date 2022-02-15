package com.xtuer.bean.stock;

import com.xtuer.util.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * 库存操作申请，目前只使用了出库操作，出库分 2 种出库情况:
 * A. 物料直接出库，每个物料一个出库记录
 * B. 订单出库 (记录有订单号)，需要查询到订单的产品，每个产品的物料，每个物料一个出库记录
 *
 * 出库申请会对应创建一个审批
 */
@Getter
@Setter
public class StockRequest {
    public static final int STATE_INIT = 0;
    public static final int STATE_AUDITING = 1;
    public static final int STATE_REJECTED = 2;
    public static final int STATE_ACCEPTED = 3;
    public static final int STATE_COMPLETE = 4;

    /**
     * 状态值与对应的 Label: 数组的下标为状态值，对应的数组元素值为状态的 Label
     */
    private static final String[] STATE_LABELS = { "初始化", "审批中", "审批拒绝", "审批通过", "完成" };

    /**
     * 出库申请 ID
     */
    private long stockRequestId;

    /**
     * 出库申请 SN，显示时方便归类查看
     */
    private String stockRequestSn;

    /**
     * 库存操作类型: IN (入库), OUT (出库)
     */
    private StockRecord.Type type;

    /**
     * 出库类型: 1 (产品项出库)、2 (产品出库)、3 (订单出库)
     */
    private int targetType;

    /**
     * 出库对象的 ID: 产品 ID、订单 ID (产品项出库时为 0，因可能有多个产品项)
     */
    private long targetId;

    /**
     * 订单 ID [可选]
     */
    private long orderId;

    /**
     * 状态: 0 (初始化), 1 (待审批), 2 (审批拒绝), 3 (审批完成), 4 (完成)
     */
    private int state;

    /**
     * 申请者 ID
     */
    private long applicantId;

    /**
     * 申请者名字 (查询时从 User 中关联得到)
     */
    private String applicantUsername;

    /**
     * 描述，展示时需要使用
     */
    private String desc;

    /**
     * 物料的名字
     */
    private String productItemNames;

    /**
     * 物理的规格型号
     */
    private String productItemModels;

    /**
     * 申请时间
     */
    private Date createdAt;

    /**
     * 当前审批员 ID
     */
    private long currentAuditorId;

    /**
     * 出库的物料总数
     */
    private int totalCount;

    /**
     * 出库备注
     */
    private String comment;

    /**
     * 库存操作记录，每个操作记录管理一个物料的信息
     */
    private List<StockRecord> records = new LinkedList<>();

    /**
     * 获取订单状态 Label
     *
     * @return 返回订单状态的 Label
     */
    public String getStateLabel() {
        return Utils.getStateLabel(STATE_LABELS, state);
    }
}
