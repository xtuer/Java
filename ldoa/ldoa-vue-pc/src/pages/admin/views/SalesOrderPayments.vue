<!-- eslint-disable vue/no-parsing-error -->

<!-- 订单收款情况 -->
<template>
    <div class="sales-order-payments list-page">
        <!-- 顶部工具栏 -->
        <div class="list-page-toolbar-top">
            <RadioGroup v-model="filter.searchType" type="button" button-style="solid" @on-change="searchSalesOrders">
                <Radio :label="0">所有订单</Radio>
                <Radio :label="1">应收款订单</Radio>
                <Radio :label="2">本月已收款</Radio>
                <Radio :label="3">本年已收款</Radio>
            </RadioGroup>

            <div class="filter">
                <!-- 时间范围 -->
                <DatePicker v-if="filter.searchType === 0 || filter.searchType === 1"
                            v-model="dateRange"
                            format="MM-dd"
                            separator=" 至 "
                            type="daterange"
                            data-prepend-label="签约时间"
                            class="prepend-label"
                            split-panels
                            placeholder="请选择签约时间范围">
                </DatePicker>

                <!-- 选择条件的搜索 -->
                <Input v-model="filterValue" transfer placeholder="请输入查询条件" search enter-button @on-search="searchSalesOrders">
                    <Select v-model="filterKey" slot="prepend">
                        <Option value="customerName">客户</Option>
                        <Option value="business">行业</Option>
                        <Option value="topic">主题</Option>
                    </Select>
                </Input>

                <Button type="info" icon="md-arrow-down" :loading="exporting" class="margin-left-10" @click="exportSalesOrdersForPayment()">导出收款订单</Button>
            </div>
        </div>

        <!-- 销售订单列表 -->
        <Table :data="salesOrders" :columns="columns" :loading="reloading" border
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

            <!-- 收款类型 -->
            <template slot-scope="{ row: salesOrder }" slot="paidType">
                {{ salesOrder.paidType | labelForValue(window.SALES_PAID_TYPES) }}
            </template>

            <!-- 收款日期 -->
            <template slot-scope="{ row: salesOrder }" slot="paidAt">
                {{ salesOrder.paidAt | formatDateSimple }}
            </template>

            <!-- 订单状态 -->
            <template slot-scope="{ row: salesOrder }" slot="state">
                <Button v-if="shouldPay(salesOrder)" type="primary" size="small" :disabled="!hasPermissionForFinance()" @click="showPay(salesOrder)">点击收款</Button>
                <Tag v-else :color="salesOrder.state | colorForValue(window.SALES_ORDER_STATES)" type="border">{{ salesOrder.state | labelForValue(window.SALES_ORDER_STATES) }}</Tag>
            </template>
        </Table>

        <!-- 底部工具栏 -->
        <div class="list-page-toolbar-bottom">
            <Button v-show="more" :loading="loading" shape="circle" icon="md-boat" @click="fetchMoreSalesOrders">更多...</Button>
        </div>

        <!-- 支付弹窗 -->
        <Modal v-model="payModal" :title="`订单收款: ${salesOrderToPay.salesOrderSn}`" width="400" class="sales-order-pay-modal">
            <div class="body-wrapper">
                <div class="text-color-gray">总成交金额:</div> {{ salesOrderToPay.dealAmount }}
                <div class="text-color-gray">应收金额:</div> {{ salesOrderToPay.shouldPayAmount }}
                <div class="text-color-gray">收款金额:</div> <InputNumber v-model="salesOrderToPay.paidAmount" :min="0"/>

                <div class="text-color-gray">收款类型:</div>
                <Select v-model="salesOrderToPay.paidType">
                    <Option :value="1">预付</Option>
                    <Option :value="2">全款</Option>
                </Select>

                <div class="text-color-gray">收款日期:</div>
                <DatePicker v-model="salesOrderToPay.paidAt" type="date" placeholder="请选择收款日期"></DatePicker>
            </div>
            <div slot="footer">
                <Button type="text" size="small" @click="payModal = false">取消</Button>

                <Poptip confirm transfer title="确定收款 ?" @on-ok="pay(salesOrderToPay)">
                    <Button type="primary" size="small" :loading="paying">收款</Button>
                </Poptip>
            </div>
        </Modal>
    </div>
</template>

<script>
import SalesOrderDao from '@/../public/static-p/js/dao/SalesOrderDao';

export default {
    data() {
        return {
            salesOrders: [],
            filter     : this.newFilter(), // 搜索条件
            filterKey  : 'customerName',   // 搜索的 Key
            filterValue: '',       // 搜索的 Value
            dateRange  : ['', ''], // 搜索的时间范围
            more     : false, // 是否还有更多销售订单
            loading  : false, // 加载中
            reloading: false,
            salesOrderToPay: {},    // 当前支付的销售订单
            payModal       : false, // 支付弹窗是否可见
            paying         : false, // 支付中
            completing     : false, // 完成订单中
            exporting      : false, // 导出中

            columns  : [
                // 设置 width, minWidth，当大小不够时 Table 会出现水平滚动条
                { key: 'salesOrderSn', title: '订单编号', width: 150, resizable: true },
                { key : 'customerName', title: '客户', width: 150, resizable: true },
                { key : 'topic', title: '主题', width: 150, resizable: true },
                { key : 'business', title: '行业', width: 150, resizable: true },
                { slot: 'agreementDate', title: '签约日期', width: 110, align: 'center' },
                { key : 'costDealAmount', title: '净销售额', width: 110, resizable: false },
                { key : 'dealAmount', title: '总成交金额', width: 110, resizable: false },
                { key : 'shouldPayAmount', title: '应收金额', width: 110, resizable: false },
                { key : 'paidAmount', title: '已收金额', width: 110, resizable: false },
                { slot: 'paidType', title: '收款类型', width: 110, align: 'center' },
                { slot: 'paidAt', title: '收款日期', width: 110, align: 'center' },
                { slot: 'state', title: '状态', width: 100, resizable: true, align: 'center', fixed: 'right' },
                { key : 'ownerName', title: '负责人', width: 150, resizable: true },
            ],
            tableName: 'sales-orders-payment-table',
        };
    },
    mounted() {
        this.restoreTableColumnWidths(this.columns);
        this.searchSalesOrders();
    },
    methods: {
        // 搜索销售订单
        searchSalesOrders() {
            this.salesOrders = [];
            this.more        = false;
            this.reloading   = true;
            this.filter      = { ...this.newFilter(), searchType: this.filter.searchType };
            this.filter[this.filterKey] = this.filterValue;

            // 如果不需要时间范围，则删除
            if (this.dateRange[0] && this.dateRange[1]) {
                this.filter.agreementStart = this.dateRange[0].format('yyyy-MM-dd');
                this.filter.agreementEnd   = this.dateRange[1].format('yyyy-MM-dd');
            } else {
                this.filter.agreementStart = '';
                this.filter.agreementEnd   = '';
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
        // 应支付
        shouldPay(salesOrder) {
            return salesOrder.state === SALES_ORDER_STATE.STATE_WAIT_PAY || salesOrder.state === SALES_ORDER_STATE.STATE_PAID;
        },
        // 显示支付弹窗
        showPay(salesOrder) {
            this.payModal = true;
            this.salesOrderToPay = Utils.clone(salesOrder);
            this.salesOrderToPay.paidAmountBefore = salesOrder.paidAmount; // 本次支付前的已收金额
            this.salesOrderToPay.paidAmount = Math.max(salesOrder.shouldPayAmount - salesOrder.paidAmount, 0);
        },
        // 订单收款
        pay(salesOrder) {
            // 1. 提交支付
            // 2. 支付成功后更新表格中的已收金额和收款日期、收款状态、订单状态

            // 确认收款日期
            if (!salesOrder.paidAt) {
                this.$Message.error('请选择收款日期');
                return;
            }

            // [1] 提交支付
            this.paying = true;
            SalesOrderDao.pay(salesOrder.salesOrderId, salesOrder.paidAmount, salesOrder.paidType, salesOrder.paidAt).then((state) => {
                // [2] 支付成功后更新表格中的已收金额和收款日期、收款状态、订单状态
                const found = this.salesOrders.find(o => o.salesOrderId === salesOrder.salesOrderId);
                if (found) {
                    found.paidAmount = salesOrder.paidAmountBefore + salesOrder.paidAmount; // 以前收款金额加上本次收款金额
                    found.paidAt = salesOrder.paidAt;
                    found.state = state;
                    found.paidType = salesOrder.paidType;
                }

                this.$Message.success('收款成功');
                this.paying = false;
                this.payModal = false;
            }).catch(() => {
                this.paying = false;
            });
        },
        // 判断订单是否可完成
        canComplete(salesOrder) {
            // 已收金额大于等于应收金额才可以完成订单
            return salesOrder.paidAmountBefore >= salesOrder.shouldPayAmount;
        },
        // 完成订单
        completeSalesOrder(salesOrder) {
            this.completing = true;
            SalesOrderDao.completeSalesOrder(salesOrder.salesOrderId).then(() => {
                const found = this.salesOrders.find(o => o.salesOrderId === salesOrder.salesOrderId);
                if (found) {
                    found.state = window.SALES_ORDER_STATE.STATE_COMPLETE;
                }

                this.$Message.success('订单完成');
                this.completing = false;
                this.payModal = false;
            });
        },
        // 导出支付信息的销售订单
        exportSalesOrdersForPayment() {
            this.exporting = true;
            this.exportFile(SalesOrderDao.exportSalesOrdersForPayment(this.filter)).then(() => {
                this.exporting = false;
            });
        },
        // 新建搜索条件
        newFilter() {
            return { // 搜索条件
                searchType  : 0, // 搜索类型
                topic       : '',
                customerName: '',
                business    : '',
                pageSize    : 50,
                pageNumber  : 1,
            };
        },
    }
};
</script>

<style lang="scss">
.sales-order-pay-modal {
    .body-wrapper {
        display: grid;
        grid-template-columns: max-content 150px;
        grid-gap: 20px 5px;
        align-items: center;

        .ivu-input-number {
            width: 100%;
        }
    }

    .ivu-modal-footer {
        button {
            min-width: 70px;
        }

        .ivu-poptip {
            margin-left: 8px;
        }
    }
}
</style>
