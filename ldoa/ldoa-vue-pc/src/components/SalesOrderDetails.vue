<!--
功能: 销售订单详情弹窗

属性:
visible: 是否可见，可使用 v-model 双向绑定
sales-order-id: 销售订单 ID

事件:
on-visible-change: 显示或隐藏时触发，显示时参数为 true，隐藏时为 false

案例:
<SalesOrderDetails v-model="visible" :sales-order-id="salesOrderId"/>
-->

<template>
    <Modal :value="visible" :title="title" width="900" class="sales-order-details-modal relative"
        :styles="{ top: '20px', marginBottom: '40px' }" @on-visible-change="showEvent">
        <Spin v-if="loading" fix size="large"></Spin>

        <div class="box">
            <div class="title">基本信息</div>
            <div class="content base-info">
                <table class="sales-order-table">
                    <tr>
                        <td class="text-color-gray">客户:</td>
                        <td>{{ salesOrder.customerName }}</td>
                        <td class="text-color-gray">主题:</td>
                        <td>{{ salesOrder.topic }}</td>
                        <td class="text-color-gray">联系人:</td>
                        <td>{{ salesOrder.customerContact }}</td>
                    </tr>
                    <tr>
                        <td class="text-color-gray">行业:</td>
                        <td>{{ salesOrder.business }}</td>
                        <td class="text-color-gray">执行单位:</td>
                        <td>{{ salesOrder.workUnit }}</td>
                        <td class="text-color-gray">负责人:</td>
                        <td>{{ salesOrder.ownerName }}</td>
                    </tr>
                    <tr>
                        <td class="text-color-gray">收件地址:</td>
                        <td colspan="3">{{ salesOrder.produceOrder.customerAddress }}</td>
                        <td class="text-color-gray">订单类型:</td>
                        <td>{{ salesOrder.produceOrder.type | labelForValue(window.ORDER_TYPES) }}</td>
                    </tr>
                    <tr>
                        <td class="text-color-gray">签约日期:</td>
                        <td>{{ salesOrder.produceOrder.orderDate | formatDateSimple }}</td>
                        <td class="text-color-gray">交货日期:</td>
                        <td>{{ salesOrder.produceOrder.deliveryDate | formatDateSimple }}</td>
                        <td class="text-color-gray">
                            <template v-if="salesOrder.produceOrder.type === 1">归还日期</template>
                        </td>
                        <td>
                            <template v-if="salesOrder.produceOrder.type === 1">{{ salesOrder.produceOrder.returnDate | formatDateSimple }}</template>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="6" style="text-align: left">
                            <div>
                                <span class="text-color-gray">订货附件及其他要求:</span>
                                {{ salesOrder.produceOrder.requirement || '无' }}
                            </div>

                            <div class="margin-top-10">
                                <span class="text-color-gray margin-right-5">订单附件:</span>
                                <a v-if="salesOrder.produceOrder.attachment && salesOrder.produceOrder.attachment.id !== '0'"
                                    :href="salesOrder.produceOrder.attachment.url">
                                    {{ salesOrder.produceOrder.attachment.filename }}
                                </a>
                                <span v-else>无</span>
                            </div>

                            <div class="margin-top-10">
                                <template v-if="salesOrder.produceOrder.calibrated">
                                    <span class="text-color-gray margin-right-5">校准信息:</span>
                                    <pre style="margin: 0 20px">{{ salesOrder.produceOrder.calibrationInfo }}</pre>
                                </template>
                                <template v-else>
                                    <span class="text-color-gray margin-right-5">校准信息:</span> 无
                                </template>
                            </div>
                        </td>
                    </tr>
                </table>
            </div>
        </div>

        <div class="box margin-top-20">
            <div class="title">销售订单明细</div>
            <div class="content padding-0" style="padding-top: 10px !important">
                <!-- 订单项列表 -->
                <Table :data="salesOrder.produceOrder.items" :columns="produceOrderItemColumns" border>
                    <!-- 产品名称 -->
                    <template slot-scope="{ row: item }" slot="name">
                        {{ item.product.name }}
                    </template>
                </Table>

                <!-- 应收金额 -->
                <div class="payment-info">
                    <div class="text-color-gray">净销售金额: {{ salesOrder.costDealAmount }}</div>
                    <div class="text-color-gray">咨询费: {{ salesOrder.consultationFee }}</div>
                    <div class="text-color-gray">总成交金额: {{ salesOrder.dealAmount }}</div>
                    <div class="text-color-gray">应收金额: {{ salesOrder.shouldPayAmount }}</div>
                </div>
            </div>
        </div>

        <!-- 底部工具栏 -->
        <div slot="footer"></div>
    </Modal>
</template>

<script>
import SalesOrderDao from '@/../public/static-p/js/dao/SalesOrderDao';

export default {
    props: {
        visible     : { type: Boolean, required: true }, // 是否可见
        salesOrderId: { type: String,  required: true }, // 销售订单 ID
    },
    model: {
        prop : 'visible',
        event: 'on-visible-change',
    },
    data() {
        return {
            salesOrder: this.newSalesOrder(),
            loading: false,
            produceOrderItemColumns: [ // 订单项的列
                { slot: 'name',      title: '产品' },
                { key : 'price',     title: '单价', width: 110 },
                { key : 'costPrice', title: '成本价', width: 110 },
                { key : 'count',     title: '数量', width: 110 },
                { key : 'consultationFee', title: '咨询费', width: 110 },
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
            this.$emit('on-ok', []);
            this.showEvent(false); // 关闭弹窗
        },
        // 初始化
        init() {
            // 例如从服务器加载数据
            if (Utils.isValidId(this.salesOrderId)) {
                this.loading = true;
                SalesOrderDao.findSalesOrder(this.salesOrderId).then(salesOrder => {
                    this.salesOrder = salesOrder;
                    this.loading = false;
                });
            } else {
                this.salesOrder = this.newSalesOrder();
            }
        },
        // 创建销售订单，每个销售订单带有一个生产订单
        newSalesOrder() {
            return {
                salesOrderId   : '0', // 订单 ID
                salesOrderSn   : '', // 订单编号
                topic          : '', // 主题
                agreementDate  : '', // 签约日期
                deliveryDate   : '', // 交货日期
                ownerName      : '', // 负责人
                ownerId        : '', // 负责人 ID
                customerId     : '0', // 客户 ID
                customerName   : '', // 客户
                customerContact: '', // 联系人
                business       : '', // 行业
                workUnit       : '', // 执行单位
                remark         : '', // 备注
                produceOrder   : this.newProduceOrder(), // 生产订单
            };
        },
        // 创建生产订单
        newProduceOrder() {
            return {
                items: [this.newProduceOrderItem()], // 订单项
            };
        },
        // 创建生产订单项
        newProduceOrderItem() {
            return {
                orderItemId    : Utils.nextId(),
                price          : 0, // 单价
                costPrice      : 0, // 成本价
                consultationFee: 0, // 咨询费
                count          : 0, // 数量
                product        : {},
                neu            : true, // 是否新创建的
            };
        },
    },
    computed: {
        title() {
            return this.salesOrder.salesOrderSn ? `销售订单: ${this.salesOrder.salesOrderSn}` : '销售订单';
        }
    }
};
</script>

<style lang="scss">
.sales-order-details-modal {
    .base-info {
        // display: grid;
        grid-template-columns: max-content 1fr max-content 1fr;
        grid-gap: 10px 5px;
        width: 100%;
        padding: 10px 0;
    }

    .payment-info {
        display: grid;
        grid-template-columns: repeat(3, max-content) 200px;
        align-items: center;
        grid-gap: 30px;
        margin-top: 10px;
        margin-left: 10px;
        margin-bottom: 10px;
    }

    .sales-order-table {
        border-collapse: collapse;
        width: 100%;
        table-layout: fixed;

        td:nth-child(1), td:nth-child(3), td:nth-child(5) {
            width: 100px;
            text-align: right;
        }

        td {
            border: 1px solid $borderColor;
            padding: 8px 12px;

            &.center {
                text-align: center;
            }
        }

        .audit-item .ivu-input-group {
            border-collapse: collapse;
        }
    }
}
</style>
