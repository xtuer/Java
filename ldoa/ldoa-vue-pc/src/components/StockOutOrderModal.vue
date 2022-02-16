<!--
功能: 订单出库弹窗

属性:
visible: 是否可见，可使用 v-model 双向绑定

事件:
on-ok: 点击确定时触发，参数为出库申请
on-visible-change: 显示或隐藏时触发，显示时参数为 true，隐藏时为 false

案例:
<StockOutOrderModal v-model="visible" @on-ok="stockOutRequestOk"/>
-->
<template>
    <Modal :value="visible" :title="title" transfer width="1000"
           :styles="{ top: '40px', marginBottom: '80px' }" :mask-closable="false" class="stock-out-order-modal" @on-visible-change="showEvent">
        <!-- 弹窗 Body -->
        <div class="body-wrapper">
            <!-- 物料列表 -->
            <div v-for="product in products" :key="product.productId" class="product">
                <div class="product-info">产品名称: <b>{{ product.name }}</b></div>
                <Table :data="product.items" :columns="productItemColumns" border>
                    <!-- 库存数量 -->
                    <template slot-scope="{ index }" slot="count">
                        {{ product.items[index].count }} {{ product.items[index].unit }}
                    </template>

                    <!-- 出库数量 -->
                    <template slot-scope="{ index }" slot="stockOutCount">
                        <InputNumber :min="0" v-model="product.items[index].stockOutCount"></InputNumber> {{ product.items[index].unit }}
                    </template>

                    <template slot-scope="{ index }" slot="action">
                        <!-- 删除所在行 -->
                        <Icon type="md-close" size="20" class="clickable" @click="product.items.remove(index)"/>
                    </template>
                </Table>
            </div>

            <!-- 出库备注 -->
            <div class="comment">
                <span>出库备注:</span>
                <Input v-model="comment" type="textarea" placeholder="请输入出库备注"/>
            </div>
        </div>

        <!-- 底部工具栏 -->
        <div slot="footer" class="footer">
            <!-- <Button type="primary" ghost icon="md-add" class="margin-right-10" @click="orderSelectVisible = true">选择订单</Button> -->
            <AuditorSelect v-model="currentAuditorId" :step="1" type="OUT_OF_STOCK"/>
            <div class="stretch"></div>
            <Button type="text" @click="showEvent(false)">取消</Button>
            <Button type="primary" :loading="saving" @click="stockOutRequest">确定</Button>
        </div>

        <!-- 产品选择弹窗 -->
        <OrderSelect v-model="orderSelectVisible" :not-in-stock-request="true" @on-ok="orderSelected"/>
    </Modal>
</template>

<script>
import StockDao from '@/../public/static-p/js/dao/StockDao';
import OrderDao from '@/../public/static-p/js/dao/OrderDao';
import OrderSelect from '@/components/OrderSelect.vue';
import AuditorSelect from '@/components/AuditorSelect.vue';

export default {
    props: {
        visible: { type: Boolean, required: true }, // 是否可见
    },
    model: {
        prop : 'visible',
        event: 'on-visible-change',
    },
    components: { OrderSelect, AuditorSelect },
    data() {
        return {
            order: {}, // 出库的订单
            products: [], // 出库的产品
            comment: '', // 出库备注
            orderSelectVisible: false, // 物料选择窗口是否可见
            currentAuditorId: '0', // 当前审批员 ID
            productItemColumns: [
                { key : 'name',     title: '物料名称', minWidth: 150 },
                { key : 'code',     title: '物料编码', width: 150 },
                { key : 'type',     title: '物料类型', width: 110 },
                { key : 'model',    title: '规格/型号', width: 110 },
                { key : 'standard', title: '标准/规范', width: 110 },
                { slot: 'count',    title: '库存数量', width: 110 },
                { slot: 'stockOutCount', title: '出库数量', width: 120, className: 'table-column-number-input-with-unit' },
                // { slot: 'action',   title: '操作', width: 70, align: 'center' },
            ],
            saving: false, // 保存中
        };
    },
    computed: {
        title() {
            if (this.order.orderSn) {
                return `订单出库: ${this.order.orderSn}`;
            } else {
                return '订单出库: ----';
            }
        },
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
        // 初始化
        init() {
            this.order = {};
            this.products = [];
            this.orderSelectVisible = true;
            this.currentAuditorId = '0';
            this.comment = '';
        },
        // 选择了出库的物料 (可以有多个物料)
        orderSelected(order) {
            this.order = order;

            // 请求订单的产品 (带有产品项详情)
            OrderDao.findProductsByOrderId(this.order.orderId).then(products => {
                // 去掉产品中无效的产品项
                for (let product of products) {
                    product.items = product.items
                        .filter(i => i.productItemId !== '0')
                        .map(i => {
                            i.stockOutCount = 1;
                            return i;
                        });
                }

                this.products = products;
            });
        },
        // 订单出库申请
        stockOutRequest() {
            // 逻辑:
            // 1. 审批员不能为空
            // 2. 创建出库申请对象
            // 3. 出库数量不为 0 的每一个物料创建一个出库记录
            // 4. 没有出库记录则返回
            // 5. 发送出库申请，把服务器返回的出库申请作为信号 on-ok 的参数，发射 on-ok 信号

            // [1] 审批员不能为空
            if (!Utils.isValidId(this.currentAuditorId)) {
                this.$Message.error('请选择审批员');
                return;
            }

            // [2] 创建出库申请对象
            const request = {
                targetId        : this.order.orderId,
                targetType      : window.STOCK_OUT_TYPE.ORDER,
                currentAuditorId: this.currentAuditorId,
                desc            : '订单: ' + this.order.orderSn,
                comment         : this.comment,
                productItemNames: '', // 物料名字
                records         : [], // [{ productId, productItemId, count }]
            };
            const itemNames = [];

            for (let product of this.products) {
                for (let item of product.items) {
                    // [3] 出库数量不为 0 的每一个物料创建一个出库记录
                    if (item.stockOutCount > 0) {
                        request.records.push({
                            productId: item.productId,
                            productItemId: item.productItemId,
                            count: item.stockOutCount,
                        });
                        // itemNames.push(item.name);
                        itemNames.push(`${item.code}(${item.stockOutCount})`);
                    }
                }
            }

            request.productItemNames = itemNames.join(', ');

            // [4] 没有出库记录则返回
            if (request.records.length === 0) {
                this.$Message.warning('没有需要出库的物料');
                return;
            }

            // [5] 发送出库申请，把服务器返回的出库申请作为信号 on-ok 的参数，发射 on-ok 信号
            this.saving = true;
            StockDao.stockOutRequest(request).then(responsedRequest => {
                this.$Message.success('出库申请成功');
                this.$emit('on-ok', responsedRequest);
                this.showEvent(false); // 关闭弹窗
                this.saving = false;
            }).catch(() => {
                this.saving = false;
            });
        },
    },
};
</script>

<style lang="scss">
.stock-out-order-modal {
    .body-wrapper {
        .product {
            &:not(:first-child) {
                margin-top: 15px;
            }

            .product-info {
                margin-bottom: 5px;
            }
        }

        .comment {
            margin-top: 20px;
        }
    }

    .footer {
        display: flex;

        .auditor-select {
            width: 200px;
        }
    }
}
</style>
