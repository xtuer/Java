<!-- eslint-disable vue/no-parsing-error -->
<!--
功能: 订单选择弹窗

属性:
visible: 是否可见，可使用 v-model 双向绑定

事件:
on-ok: 点击确定时触发，参数为选择的订单
on-visible-change: 显示或隐藏时触发，显示时参数为 true，隐藏时为 false

案例:
<OrderSelect v-model="visible"/>
-->

<template>
    <Modal :value="visible" title="订单选择" :mask-closable="false" transfer width="800" class="order-select-modal"
           :styles="{ top: '40px', marginBottom: '80px' }" @on-visible-change="showEvent">
        <!-- 弹窗 Body -->
        <Scroll>
        <div class="list-page">
            <div class="list-page-toolbar-top">
                <Input v-model="filter.orderSn" placeholder="请输入订单号" @on-enter="searchOrders">
                    <span slot="prepend">订单号</span>
                </Input>
                <Input v-model="filter.productCodes" placeholder="请输入产品编码" search enter-button @on-search="searchOrders">
                    <span slot="prepend">产品编码</span>
                </Input>
            </div>

            <!-- 订单列表 -->
            <Table :data="orders" :columns="columns" :loading="reloading" border>
                <!-- 选择按钮 -->
                <template slot-scope="{ row: order, index }" slot="action">
                    <Checkbox :value="order.orderId === orderSelected.orderId" @on-change="selectOrder(index, $event)"></Checkbox>
                </template>

                <!-- 订单编号 -->
                <template slot-scope="{ row: order }" slot="orderSn">
                    {{ order.orderSn }}
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
            </Table>
        </div>
        </Scroll>

        <!-- 底部工具栏 -->
        <div slot="footer">
            <Button v-show="more" :loading="loading" icon="md-boat" style="float: left" @click="fetchMoreOrders">更多...</Button>
            <Button type="text" @click="showEvent(false)">取消</Button>
            <Button type="primary" @click="ok">确定</Button>
        </div>
    </Modal>
</template>

<script>
import OrderDao from '@/../public/static-p/js/dao/OrderDao';

export default {
    props: {
        visible: { type: Boolean, required: true }, // 是否可见
        notInStockRequest: { type: Boolean, required: false }, // 是否在出库请求中有记录
    },
    model: {
        prop : 'visible',
        event: 'on-visible-change',
    },
    data() {
        return {
            orders: [],
            orderSelected: {}, // 选中的 item
            filter: { // 搜索条件
                orderSn      : '',
                productCodes : '',
                state        : -1, // 审批通过的订单: 状态为 3
                pageSize     : 10,
                pageNumber   : 1,
                notInStockRequest: this.notInStockRequest,
            },
            more     : false, // 是否还有更多物料
            loading  : false, // 加载中
            reloading: false, // 重新加载中
            columns  : [
                // 设置 width, minWidth，当大小不够时 Table 会出现水平滚动条
                { slot: 'action',       title: '选择', width: 70, align: 'center' },
                { slot: 'orderSn',      title: '订单号', width: 180 },
                { slot: 'customer',     title: '客户单位', minWidth: 180, className: 'table-poptip' },
                // { slot: 'orderDate',    title: '订单日期', width: 120, align: 'center' },
                // { slot: 'deliveryDate', title: '交货日期', width: 120, align: 'center' },
                { key : 'productCodes', title: '产品编码', width: 150, tooltip: true },
                { slot: 'salesperson',  title: '销售负责人', width: 120 },
                // { key : 'stateLabel',   title: '状态', width: 120, align: 'center' },
            ],
        };
    },
    methods: {
        // 显示隐藏事件
        showEvent(visible) {
            this.$emit('on-visible-change', visible);

            // 显示弹窗时 visible 为 true，初始化
            if (visible) {
                this.init();
            }
        },
        // 点击确定按钮的回调函数
        ok() {
            if (!this.orderSelected.orderId) {
                this.$Message.warning('请选择订单');
                return;
            }

            this.$emit('on-ok', this.orderSelected);
            this.showEvent(false); // 关闭弹窗
        },
        // 初始化
        init() {
            this.orderSelected = {};
            this.searchOrders();
        },
        // 搜索物料
        searchOrders() {
            this.orders            = [];
            this.more              = false;
            this.reloading         = true;
            this.filter.pageNumber = 1;

            this.fetchMoreOrders();
        },
        // 点击更多按钮加载下一页的物料
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
        // 选择物料
        selectOrder(index, selected) {
            if (selected) {
                this.orderSelected = this.orders[index];
            } else {
                this.orderSelected = {};
            }
        }
    }
};
</script>

<style lang="scss">
.order-select-modal {
    .list-page-toolbar-top {
        grid-template-columns: 1fr 1fr;
        grid-gap: 10px;
    }

    .ivu-checkbox-wrapper {
        margin-right: 0;
    }
}
</style>
