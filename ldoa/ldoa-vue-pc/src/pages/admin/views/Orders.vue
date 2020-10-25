<!-- eslint-disable vue/no-parsing-error -->
<!--
搜索订单、分页加载 (加载下一页的订单)
-->
<template>
    <div class="order-manage list-page">
        <!-- 顶部工具栏 -->
        <div class="list-page-toolbar-top">
            <div style="display: flex">
                <Input v-model="filter.orderSn" placeholder="请输入订单号" @on-enter="searchOrders">
                    <span slot="prepend">订单号</span>
                </Input>
                <Input v-model="filter.productCodes" placeholder="请输入产品编码" search enter-button @on-search="searchOrders">
                    <span slot="prepend">产品编码</span>
                </Input>
            </div>
            <Button type="primary" icon="md-add" @click="editOrder()">创建订单</Button>
        </div>

        <!-- 订单列表 -->
        <Table :data="orders" :columns="orderColumns" :loading="reloading" border>
            <!-- 订单编号 -->
            <template slot-scope="{ row: order }" slot="orderSn">
                <a>{{ order.orderSn }}</a>
            </template>

            <!-- 客户单位 -->
            <template slot-scope="{ row: order }" slot="customer">
                <Poptip trigger="hover" placement="top" transfer width="250">
                    <div>{{ order.customerCompany }}</div>

                    <div slot="content">
                        <div>联系人名: {{ order.customerContact }}</div>
                        <div>客户单位: {{ order.customerCompany }}</div>
                        <div>收货地址: {{ order.customerAddress }}</div>
                    </div>
                </Poptip>
            </template>

            <!-- 销售员 -->
            <template slot-scope="{ row: order }" slot="salesperson">
                {{ order.salesperson && order.salesperson.nickname }}
            </template>

            <!-- 订单日期 -->
            <template slot-scope="{ row: order }" slot="orderDate">
                {{ order.orderDate | formatDate}}
            </template>
            <template slot-scope="{ row: order }" slot="deliveryDate">
                {{ order.deliveryDate | formatDate }}
            </template>

            <!-- 操作按钮 -->
            <template slot-scope="{ row: order }" slot="action">
                <Button type="primary" size="small" @click="editOrder(order)">编辑</Button>
                <Button type="info" size="small" @click="detailsOrder(order)">详情</Button>
            </template>
        </Table>

        <!-- 底部工具栏 -->
        <div class="list-page-toolbar-bottom">
            <Button v-show="more" :loading="loading" shape="circle" icon="md-boat" @click="fetchMoreOrders">更多...</Button>
        </div>

        <!-- 订单编辑弹窗 -->
        <OrderEdit v-model="orderEditModal" :order-id="editedOrderId" @on-ok="editOrderFinished"/>

        <!-- 订单详情弹窗 -->
        <OrderDetails v-model="orderDetailsModal" :order-id="detailsOrderId"/>
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
            filter: { // 搜索条件
                orderSn     : '',
                productCodes: '',
                status      : -1,
                pageSize    : 20,
                pageNumber  : 1,
            },
            more      : false, // 是否还有更多订单
            loading   : false, // 加载中
            reloading : false, // 重新加载
            orderEditModal: false, // 订单编辑弹窗是否可见
            editedOrderId : '0',   // 编辑的订单 ID
            orderDetailsModal: false, // 订单详情弹窗是否可见
            detailsOrderId: '0',      // 查看详情的订单
            orderColumns: [
                // 设置 width, minWidth，当大小不够时 Table 会出现水平滚动条
                { slot: 'orderSn',   title: '订单号', width: 180 },
                { slot: 'customer', title: '客户单位', minWidth: 180, className: 'table-poptip' },
                { slot: 'orderDate',   title: '订单日期', width: 120, align: 'center' },
                { slot: 'deliveryDate',   title: '交货日期', width: 120, align: 'center' },
                { key : 'productCodes',   title: '产品编码', width: 150, tooltip: true },
                { slot: 'salesperson',   title: '销售负责人', width: 120 },
                { key : 'statusLabel',   title: '状态', width: 120, align: 'center' },
                { slot: 'action', title: '操作', width: 150, align: 'center', className: 'table-action' },
            ],
        };
    },
    mounted() {
        this.searchOrders();
    },
    methods: {
        // 搜索订单
        searchOrders() {
            this.orders             = [];
            this.more              = false;
            this.reloading         = true;
            this.filter.pageNumber = 1;

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
            this.detailsOrderId = order.orderId;
            this.orderDetailsModal = true;
        }
    }
};
</script>

<style lang="scss">
.order-manage {
    .list-page-toolbar-top {
        .ivu-input-wrapper {
            width: 250px;
            margin-right: 10px;

            &:last-child {
                width: 300px;
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