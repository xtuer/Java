<!-- eslint-disable vue/no-parsing-error -->

<!--
搜索销售订单、分页加载 (加载下一页的销售订单)
-->
<template>
    <div class="sales-orders list-page">
        <!-- 顶部工具栏 -->
        <div class="list-page-toolbar-top">
            <!-- 搜索条件 -->
            <div class="filter">
                <!-- 指定条件的搜索 -->
                <Input v-model="filter.customerName" placeholder="请输入查询条件" @on-enter="searchSalesOrders">
                    <span slot="prepend">客户</span>
                </Input>
                <Input v-model="filter.business" placeholder="请输入查询条件" @on-enter="searchSalesOrders">
                    <span slot="prepend">行业</span>
                </Input>
                <Input v-model="filter.topic" placeholder="请输入查询条件" search enter-button @on-search="searchSalesOrders">
                    <span slot="prepend">主题</span>
                </Input>
            </div>

            <!-- 其他按钮 -->
            <div>
                <Button type="primary" icon="md-add" :disabled="!hasPermissionForSalesOrder()" @click="editSalesOrder('0')">创建销售订单</Button>
                <Button type="info" icon="md-arrow-down" :loading="exporting" class="margin-left-10" @click="exportSalesOrders()">导出销售订单</Button>
            </div>
        </div>

        <!-- 销售订单列表 -->
        <Table :data="salesOrders" :columns="columns" :loading="reloading" :max-height="tableMaxHeight" border
            @on-column-width-resize="saveTableColumnWidths(arguments)"
        >
            <!-- 订单编号 -->
            <template slot-scope="{ row: salesOrder }" slot="salesOrderSn">
                <a @click="showSalesOrderDetails(salesOrder)">{{ salesOrder.salesOrderSn }}</a>
            </template>

            <!-- 签约日期 -->
            <template slot-scope="{ row: salesOrder }" slot="agreementDate">
                {{ salesOrder.agreementDate | formatDateSimple }}
            </template>
            <!-- 交货日期 -->
            <template slot-scope="{ row: salesOrder }" slot="deliveryDate">
                {{ salesOrder.deliveryDate | formatDateSimple }}
            </template>

            <!-- 订单状态 -->
            <template slot-scope="{ row: salesOrder }" slot="state">
                <Tag :color="salesOrder.state | colorForValue(window.SALES_ORDER_STATES)" type="border">{{ salesOrder.state | labelForValue(window.SALES_ORDER_STATES) }}</Tag>
            </template>

            <!-- 操作按钮 -->
            <template slot-scope="{ row: salesOrder }" slot="action">
                <a :disabled="!canEdit(salesOrder)" @click="editSalesOrder(salesOrder.salesOrderId)">编辑</a>
                <a :disabled="!canDelete(salesOrder)" class="delete" @click="deleteSalesOrder(salesOrder)">删除</a>
            </template>
        </Table>

        <!-- 底部工具栏 -->
        <div class="list-page-toolbar-bottom">
            <Button v-show="more" :loading="loading" shape="circle" icon="md-boat" @click="fetchMoreSalesOrders">更多...</Button>
        </div>

        <!-- 销售订单编辑弹窗 -->
        <SalesOrderEdit v-model="salesOrderEdit" :sales-order-id="salesOrderId" @on-ok="salesOrderSaved"/>

        <!-- 销售订单详情弹窗 -->
        <SalesOrderDetails v-model="salesOrderDetails" :sales-order-id="salesOrderId"/>
    </div>
</template>

<script>
import SalesOrderDao from '@/../public/static-p/js/dao/SalesOrderDao';
import SalesOrderEdit from '@/components/SalesOrderEdit.vue';
import SalesOrderDetails from '@/components/SalesOrderDetails.vue';

export default {
    components: { SalesOrderEdit, SalesOrderDetails },
    data() {
        return {
            salesOrders: [],
            filter     : this.newFilter(), // 搜索条件
            filterKey  : 'email',  // 搜索的 Key
            filterValue: '',       // 搜索的 Value
            dateRange  : ['', ''], // 搜索的时间范围
            more     : false, // 是否还有更多销售订单
            loading  : false, // 加载中
            reloading: false,
            exporting: false, // 导出中
            tableName: 'sales-orders-table',
            tableMaxHeight: 200,
            columns  : [
                // 设置 width, minWidth，当大小不够时 Table 会出现水平滚动条
                { slot: 'salesOrderSn', title: '订单编号', width: 150, resizable: true },
                { key : 'customerName', title: '客户', width: 150, resizable: true },
                { key : 'topic', title: '主题', width: 150, resizable: true },
                { slot: 'state', title: '状态', width: 100, align: 'center', className: 'table-state' },
                { key : 'ownerName', title: '负责人', width: 150, resizable: true },
                { key : 'business', title: '行业', width: 150, resizable: true },
                { key : 'workUnit', title: '执行单位', width: 150, resizable: true },
                { key : 'customerContact', title: '联系人', width: 150, resizable: true },
                { slot: 'agreementDate', title: '签约日期', width: 110, align: 'center' },
                { slot: 'deliveryDate', title: '交货日期', width: 110, align: 'center' },
                { key : 'costDealAmount', title: '净销售额', width: 120 },
                { key : 'consultationFee', title: '咨询费', width: 120 },
                { key : 'dealAmount', title: '总成交金额', width: 120 },
                { key : 'shouldPayAmount', title: '应收金额', width: 120 },
                { key : 'remark', title: '备注', minWidth: 250 },
                { slot: 'action', title: '操作', width: 110, align: 'center', className: 'table-action', fixed: 'right' },
            ],
            salesOrderId  : '0',
            salesOrderEdit: false,
            salesOrderDetails: false,
        };
    },
    mounted() {
        this.restoreTableColumnWidths(this.columns);
        this.searchSalesOrders();
        this.tableMaxHeight = this.calculateTableMaxHeight();
    },
    methods: {
        // 搜索销售订单
        searchSalesOrders() {
            this.salesOrders = [];
            this.more        = false;
            this.reloading   = true;
            this.filter      = {
                ...this.newFilter(),
                customerName: this.filter.customerName,
                topic:  this.filter.topic,
                business: this.filter.business
            };
            this.filter[this.filterKey] = this.filterValue;

            // 如果不需要时间范围，则删除
            // if (this.dateRange[0] && this.dateRange[1]) {
            //     this.filter.startAt = this.dateRange[0].format('yyyy-MM-dd');
            //     this.filter.endAt   = this.dateRange[1].format('yyyy-MM-dd');
            // } else {
            //     this.filter.startAt = '';
            //     this.filter.endAt   = '';
            // }

            // 如果是销售则只查询他创建的销售订单，管理员和财务查询所有的销售订单
            if (this.$store.getters.roles.includes('ROLE_SALE_SALESPERSON')) {
                this.filter.salesPersonId = this.$store.getters.currentUserId;
            } else {
                this.filter.salesPersonId = 0;
            }

            this.fetchMoreSalesOrders();
        },
        // 点击更多按钮加载下一页的销售订单
        fetchMoreSalesOrders() {
            this.loading = true;

            SalesOrderDao.findSalesOrders(this.filter).then(salesOrders => {
                this.salesOrders.push(...salesOrders);

                this.more      = salesOrders.length >= this.filter.pageSize;
                this.loading   = false;
                this.reloading = false;
                this.filter.pageNumber++;
            });
        },
        // 编辑销售订单
        editSalesOrder(salesOrderId) {
            this.salesOrderId = salesOrderId;
            this.salesOrderEdit = true;
        },
        // 销售订单保存成功
        salesOrderSaved(salesOrder) {
            const index = this.salesOrders.findIndex(o => o.salesOrderId === salesOrder.salesOrderId);

            if (index >= 0) {
                this.salesOrders.replace(index, salesOrder);
            } else {
                this.salesOrders.insert(0, salesOrder);
            }
        },
        // 删除销售订单
        deleteSalesOrder(salesOrder) {
            // 1. 删除提示
            // 2. 从服务器删除成功后才从本地删除
            // 3. 提示删除成功

            this.$Modal.confirm({
                title: `确定删除销售订单 <font color="red">${salesOrder.salesOrderSn}</font> 吗?`,
                loading: true,
                onOk: () => {
                    SalesOrderDao.deleteSalesOrder(salesOrder.salesOrderId).then(() => {
                        const index = this.salesOrders.findIndex(o => o.salesOrderId === salesOrder.salesOrderId);
                        this.salesOrders.splice(index, 1); // [2] 从服务器删除成功后才从本地删除
                        this.$Modal.remove();
                        this.$Message.success('删除成功');
                    });
                }
            });
        },
        // 显示销售订单详情
        showSalesOrderDetails(salesOrder) {
            this.salesOrderId = salesOrder.salesOrderId;
            this.salesOrderDetails = true;
        },
        // 导出销售订单
        exportSalesOrders() {
            this.exporting = true;
            this.exportFile(SalesOrderDao.exportSalesOrders(this.filter)).then(() => {
                this.exporting = false;
            });
        },
        // 新建搜索条件
        newFilter() {
            return { // 搜索条件
                topic       : '',
                customerName: '',
                business    : '',
                pageSize    : 50,
                pageNumber  : 1,
            };
        },
        // 判断销售订单是否可编辑
        canEdit(salesOrder) {
            // 可编辑的逻辑:
            // 1. 有销售权限
            // 2. 新建或者暂存状态
            // 3. 是自己创建的订单

            // [1] 有销售权限
            if (!this.hasPermissionForSalesOrder()) {
                return false;
            }

            // [2] 新建、暂存状态或者待支付 (支付前都可以修改)
            if (salesOrder.state >= 2) {
                return false;
            }

            // [3] 是自己创建的订单
            if (salesOrder.salesPersonId !== this.$store.getters.currentUserId) {
                return false;
            }

            return true;
        },
        // 判断销售订单是否可删除
        canDelete(salesOrder) {
            return this.canEdit(salesOrder);
        },
    }
};
</script>

<style lang="scss">
</style>
