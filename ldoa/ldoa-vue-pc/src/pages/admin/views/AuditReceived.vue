<!--
功能: 显示当前用户收到的审批阶段
-->
<template>
    <div class="audit-step-received list-page">
        <!-- 顶部工具栏 -->
        <div class="list-page-toolbar-top">
            <RadioGroup v-model="filter.state" type="button" button-style="solid" @on-change="searchAuditItems">
                <Radio v-for="s in window.AUDIT_ITEM_STATES" :key="s.value" :label="s.value">{{ s.label }}</Radio>
            </RadioGroup>
        </div>

        <!-- 审批项列表 -->
        <Table :data="auditItems" :columns="columns" :loading="reloading" border
            @on-column-width-resize="saveTableColumnWidths(arguments)"
        >
            <!-- 类型 -->
            <template slot-scope="{ row: auditItem }" slot="type">
                {{ auditItem.type | auditTypeName }}
            </template>

            <!-- 状态 -->
            <template slot-scope="{ row: auditItem }" slot="state">
                <Tag :color="stateColor(auditItem.state)" type="border">{{ auditItem.stateLabel }}</Tag>
            </template>

            <!-- 申请时间 -->
            <template slot-scope="{ row: auditItem }" slot="createdAt">
                {{ auditItem.createdAt | formatDate('YYYY-MM-DD HH:mm') }}
            </template>

            <!-- 操作按钮 -->
            <template slot-scope="{ row: auditItem }" slot="action">
                <Button icon="md-cloud-done" type="primary" size="small" @click="showAudit(auditItem)">审批</Button>
            </template>
        </Table>

        <!-- 底部工具栏 -->
        <div class="list-page-toolbar-bottom">
            <Button v-show="more" :loading="loading" shape="circle" icon="md-boat" @click="fetchMoreAuditItems">更多...</Button>
        </div>

        <!-- 订单详情弹窗 -->
        <OrderDetails v-model="orderModal" :order-id="orderId"/>

        <!-- 物料出库申请详情弹窗 -->
        <StockOutDetails v-model="stockRequestModal" :stock-request-id="stockRequestId"/>

        <!-- 维保订单详情弹窗 -->
        <MaintenanceOrderDetails v-model="maintenanceOrderModal" :maintenace-order-id="maintenanceOrderId"/>
    </div>
</template>

<script>
import AuditDao from '@/../public/static-p/js/dao/AuditDao';
import OrderDetails from '@/components/OrderDetails.vue';
import StockOutDetails from '@/components/StockOutDetails.vue';
import MaintenanceOrderDetails from '@/components/MaintenanceOrderDetails.vue';

export default {
    components: { OrderDetails, StockOutDetails, MaintenanceOrderDetails },
    data() {
        return {
            auditItems : [],
            state: 1,
            filter: { // 搜索条件
                name      : '',
                state     : 1,
                auditorId : this.currentUserId(),
                pageSize  : 50,
                pageNumber: 1,
            },
            more     : false, // 是否还有更多审批项
            loading  : false, // 加载中
            reloading: false,
            tableName: 'audit-received-table', // 表名
            columns  : [
                // 设置 width, minWidth，当大小不够时 Table 会出现水平滚动条
                { key : 'applicantNickname', title: '申请人', width: 150, resizable: true },
                { slot: 'type',      title: '类型', width: 150, resizable: true },
                { key : 'desc',      title: '说明', minWidth: 150 },
                { slot: 'createdAt', title: '申请时间', width: 150, align: 'center', resizable: true },
                { slot: 'state',     title: '状态', width: 120, align: 'center', resizable: true },
                { slot: 'action',    title: '操作', width: 120, align: 'center', className: 'table-action', resizable: true },
            ],
            orderId: '0', // 查看详情的订单 ID
            orderModal: false, // 是否显示订单详情弹窗是否可见

            stockRequestId: '0',
            stockRequestModal: false,

            maintenanceOrderId: '0',
            maintenanceOrderModal: false, // 维保订单详情弹窗是否可见
        };
    },
    mounted() {
        this.restoreTableColumnWidths(this.columns);
        this.searchAuditItems();
    },
    methods: {
        // 搜索审批项
        searchAuditItems() {
            this.auditItems        = [];
            this.more              = false;
            this.reloading         = true;
            this.filter.pageNumber = 1;

            this.fetchMoreAuditItems();
        },
        // 点击更多按钮加载下一页的审批项
        fetchMoreAuditItems() {
            this.loading = true;

            AuditDao.findAuditStepsByApplicantIdOrAuditorIdAndState(this.filter).then(auditItems => {
                this.auditItems.push(...auditItems);

                this.more      = auditItems.length >= this.filter.pageSize;
                this.loading   = false;
                this.reloading = false;
                this.filter.pageNumber++;
            });
        },
        // 审批
        showAudit(auditItem) {
            switch (auditItem.type) {
            case AUDIT_TYPE.ORDER:
                this.orderId = auditItem.targetId;
                this.orderModal = true;
                break;
            case AUDIT_TYPE.OUT_OF_STOCK:
                this.stockRequestId = auditItem.targetId;
                this.stockRequestModal = true;
                break;
            case AUDIT_TYPE.MAINTENANCE_ORDER:
                this.maintenanceOrderId = auditItem.targetId;
                this.maintenanceOrderModal = true;
                break;
            default:
            }
        }
    }
};
</script>

<style lang="scss">
</style>
