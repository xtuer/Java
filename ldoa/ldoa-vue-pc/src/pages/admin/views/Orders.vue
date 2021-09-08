<!-- eslint-disable vue/no-parsing-error -->
<!--
搜索订单、分页加载 (加载下一页的订单)
-->
<template>
    <div class="order-manage list-page">
        <!-- 顶部工具栏 -->
        <div class="list-page-toolbar-top">
            <!-- 搜索条件 -->
            <div class="filter">
                <!-- 状态 -->
                <Select v-model="filter.state" data-prepend-label="状态" class="prepend-label" style="width: 150px" @on-change="searchOrders">
                    <Option :value="-1">全部</Option>
                    <Option :value="0">初始化</Option>
                    <Option :value="1">审批中</Option>
                    <Option :value="2">审批拒绝</Option>
                    <Option :value="3">审批通过</Option>
                    <Option :value="4">完成</Option>
                </Select>

                <Select v-model="filter.type" data-prepend-label="类型" class="prepend-label" style="width:150px" @on-change="searchOrders">
                    <Option :value="-1">全部</Option>
                    <Option :value="0">生产订单</Option>
                    <Option :value="1">样品订单</Option>
                </Select>

                <!-- 订单日期 -->
                <div class="monthrange">
                    <DatePicker v-model="filter.orderDateStart" type="month" placeholder="开始月份" data-prepend-label="订单日期" class="prepend-label"></DatePicker>
                    <span>至</span>
                    <DatePicker v-model="filter.orderDateEnd" type="month" placeholder="结束月份"></DatePicker>
                </div>

                <!-- 选择条件的搜索 -->
                <Input v-model="filterValue" transfer placeholder="请输入查询条件" search enter-button @on-search="searchOrders">
                    <Select v-model="filterKey" slot="prepend">
                        <Option value="productNames">产品名称</Option>
                        <Option value="customerCompany">客户单位</Option>
                    </Select>
                </Input>
                <div class="stretch"></div>
            </div>
            <div>
                <Button type="primary" :disabled="!hasPermissionForOrder()" icon="md-add" class="margin-right-10" @click="editOrder()">新建订单</Button>
                <Button type="info" icon="md-arrow-down" :loading="exporting" @click="exportOrders()">导出订单</Button>
            </div>
        </div>

        <!-- 订单列表 -->
        <Table :data="orders" :columns="orderColumns" :loading="reloading" :max-height="tableMaxHeight" border
            @on-column-width-resize="saveTableColumnWidths(arguments)"
        >
            <!-- 订单编号 -->
            <template slot-scope="{ row: order }" slot="orderSn">
                <a @click="detailsOrder(order)">{{ order.orderSn }}</a>
            </template>

            <!-- 客户单位 -->
            <template slot-scope="{ row: order }" slot="customer">
                <Poptip trigger="hover" placement="bottom" transfer width="250">
                    <div>{{ order.customerCompany }}</div>

                    <div slot="content">
                        <div>联系人名: {{ order.customerContact }}</div>
                        <div>客户单位: {{ order.customerCompany }}</div>
                        <div>收货地址: {{ order.customerAddress }}</div>
                    </div>
                </Poptip>
            </template>

            <!-- 订单类型 -->
            <template slot-scope="{ row: order }" slot="type">
                {{ order.type | labelForValue(window.ORDER_TYPES) }}
            </template>

            <!-- 销售员 -->
            <template slot-scope="{ row: order }" slot="salesperson">
                {{ order.salesperson && order.salesperson.nickname }}
            </template>

            <!-- 订单日期 -->
            <template slot-scope="{ row: order }" slot="orderDate">
                {{ order.orderDate | formatDate('YYYY-MM-DD') }}
            </template>
            <template slot-scope="{ row: order }" slot="deliveryDate">
                {{ order.deliveryDate | formatDate('YYYY-MM-DD') }}
            </template>

            <!-- 状态 -->
            <template slot-scope="{ row: order }" slot="state">
                <Tag :color="stateColor(order.state)" type="border">{{ order.stateLabel }}</Tag>
            </template>

            <!-- 操作按钮 -->
            <template slot-scope="{ row: order }" slot="action">
                <!-- <Button type="info" size="small" @click="detailsOrder(order)">详情</Button> -->
                <Button :disabled="!canEditOrder(order)" icon="ios-create" type="primary" size="small" @click="editOrder(order)">编辑</Button>
            </template>
        </Table>

        <!-- 底部工具栏 -->
        <div class="list-page-toolbar-bottom">
            <Button v-show="more" :loading="loading" shape="circle" icon="md-boat" @click="fetchMoreOrders">更多...</Button>
        </div>

        <!-- 订单编辑弹窗 -->
        <OrderEdit v-model="orderEditModal" :order-id="editedOrderId" @on-ok="editOrderFinished"/>

        <!-- 订单详情弹窗 -->
        <OrderDetails v-model="orderDetailsModal" :order-id="orderDetailsOrderId" @on-ok="completeOrder(orderDetailsOrderId)"/>
    </div>
</template>

<script>
import OrderDao from '@/../public/static-p/js/dao/OrderDao';
import OrderEdit from '@/components/OrderEdit.vue';
import OrderDetails from '@/components/OrderDetails.vue';

export default {
    components: { OrderEdit, OrderDetails },
    data() {
        return {
            orders : [],
            filter: this.newFilter(),
            filterKey  : 'productNames', // 搜索的 Key
            filterValue: '',       // 搜索的 Value
            dateRange  : ['', ''], // 搜索的时间范围
            more      : false, // 是否还有更多订单
            loading   : false, // 加载中
            reloading : false, // 重新加载
            exporting : false, // 导出订单中
            orderEditModal: false, // 订单编辑弹窗是否可见
            editedOrderId : '0',   // 编辑的订单 ID
            orderDetailsModal: false, // 订单详情弹窗是否可见
            orderDetailsOrderId: '0', // 查看详情的订单
            tableName: 'orders-table', // 表名
            tableMaxHeight: 200,
            orderColumns: [
                // 设置 width, minWidth，当大小不够时 Table 会出现水平滚动条
                { slot: 'orderSn',      title: '订单号', width: 180, resizable: true },
                { slot: 'customer',     title: '客户单位', width: 250, className: 'table-poptip', resizable: true },
                { key : 'productNames', title: '产品名称', width: 150, tooltip: true, resizable: true },
                { slot: 'type',         title: '类型', width: 110, align: 'center', resizable: true },
                { slot: 'orderDate',    title: '订单日期', width: 110, align: 'center', resizable: true },
                { slot: 'deliveryDate', title: '交货日期', width: 110, align: 'center', resizable: true },
                { slot: 'salesperson',  title: '销售负责人', width: 110, resizable: true },
                { slot: 'state',        title: '状态', width: 110, align: 'center', resizable: true },
                { slot: 'action', title: '操作', width: 110, align: 'center', className: 'table-action', resizable: true },
            ],
        };
    },
    mounted() {
        this.restoreTableColumnWidths(this.orderColumns);
        this.searchOrders();
        this.tableMaxHeight = this.calculateTableMaxHeight();
    },
    methods: {
        // 搜索订单
        searchOrders() {
            this.orders    = [];
            this.more      = false;
            this.reloading = true;
            this.filter    = {
                ...this.newFilter(),
                state: this.filter.state,
                type: this.filter.type,
                orderDateStart: this.filter.orderDateStart,
                orderDateEnd: this.filter.orderDateEnd
            };
            this.filter[this.filterKey] = this.filterValue;

            this.fetchMoreOrders();
        },
        // 点击更多按钮加载下一页的订单
        fetchMoreOrders() {
            this.loading = true;

            OrderDao.findOrders(this.filter).then(orders => {
                this.orders.push(...orders);

                this.more      = orders.length >= this.filter.pageSize;
                this.loading   = false;
                this.reloading = false;
                this.filter.pageNumber++;
            });
        },
        // 编辑订单: order 为 undefined 表示创建，否则表示更新
        editOrder(order) {
            if (order) {
                // 更新订单
                this.editedOrderId = order.orderId;
            } else {
                // 创建订单
                this.editedOrderId = '0';
            }

            this.orderEditModal = true;
        },
        // 订单编辑完成
        editOrderFinished(order) {
            // 1. 查找订单的下标
            // 2. 存在的话替换原有订单，不存在的话添加到最前面
            const index = this.orders.findIndex(o => o.orderId === order.orderId);

            if (index >= 0) {
                // this.orders.replace(index, order);
                this.orders.splice(index, 1, order);
            } else {
                this.orders.splice(0, 0, order);
            }
        },
        // 查看订单详情
        detailsOrder(order) {
            this.orderDetailsOrderId = order.orderId;
            this.orderDetailsModal = true;
        },
        // 完成订单
        completeOrder(orderId) {
            const found = this.orders.find(o => o.orderId === orderId);

            if (found) {
                found.state = 4;
                found.stateLabel = '完成';
            }
        },
        // 判断订单是否可以编辑: 创建者为当前用户，且审批拒绝的订单才能编辑
        canEditOrder(order) {
            if (this.isCurrentUser(order.salespersonId) && order.state === 2) {
                return true;
            } else {
                return false;
            }
        },
        // 导出订单
        exportOrders() {
            this.exporting = true;
            this.exportFile(OrderDao.exportOrders(this.filter)).then(() => {
                this.exporting = false;
            });
        },
        // 搜索条件
        newFilter() {
            return {
                state: -1, // 状态: 0 (初始化)、1 (待审批)、2 (审批拒绝)、3 (审批完成)、4 (完成)、-1 (全部)
                type : -1, // 订单类型: 0 (生产订单)、1 (样品订单)、-1 (全部)
                customerCompany: '',
                productNames: '',
                orderDateStart: '',
                orderDateEnd: '',
                pageSize: 50,
                pageNumber: 1,
            };
        },
    }
};
</script>

<style lang="scss">
.order-manage {
    .list-page-toolbar-top {
        display: grid;
        grid-template-columns: max-content max-content;
        justify-content: space-between;
        align-items: center;

        .filter .monthrange {
            display: flex;
            align-items: center;

            > span {
                margin: 0 5px;
            }

            input {
                width: 110px;
            }
        }
    }
}

.table-poptip {
    .ivu-poptip, .ivu-poptip-rel {
        display: block;
    }
}
</style>
