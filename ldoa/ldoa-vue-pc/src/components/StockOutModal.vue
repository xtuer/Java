<!--
功能: 物料出库弹窗

属性:
visible: 是否可见，可使用 v-model 双向绑定
direct : 出库类型: true (物料出库), false (订单出库)

事件:
on-ok: 点击确定时触发，参数为出库申请
on-visible-change: 显示或隐藏时触发，显示时参数为 true，隐藏时为 false

案例:
<StockOutModal v-model="visible" :direct="true" @on-ok="stockOutRequestOk"/>
-->

<template>
    <Modal :value="visible" :title="title" transfer width="1100" :mask-closable="false" class="stock-out-modal" @on-visible-change="showEvent">
        <!-- 弹窗 Body -->
        <div class="body-wrapper">
            <!-- 产品列表 -->
            <div v-for="product in products" :key="product.productId" class="product">
                <!-- 产品信息: 订单出库时才显示 -->
                <div v-show="!direct" class="product-info">
                    <div><b>产品名称:</b> {{ product.name }}</div>
                    <div><b>产品编码:</b> {{ product.code }}</div>
                    <div><b>规格/型号:</b> {{ product.model }}</div>
                </div>

                <!-- 物料列表 -->
                <Table :data="product.items" :columns="productItemColumns" border>
                    <!-- 出库数量 -->
                    <template slot-scope="{ index }" slot="count">
                        {{ product.items[index].count }} {{ product.items[index].unit }}
                    </template>

                    <!-- 批次 / 数量 -->
                    <template slot-scope="{ index }" slot="batch-count">
                        <Tag v-for="bc in product.items[index].batchCounts" :key="bc.batch" color="cyan">
                            {{ bc.batch }} ({{ bc.count }})
                        </Tag>
                    </template>

                    <template slot-scope="{ index }" slot="select">
                        <!-- 点击打开库存选择弹窗 -->
                        <Icon type="md-git-network" size="20" class="clickable" @click="openStockSelect({ product, index })"/>
                    </template>
                    <template slot-scope="{ index }" slot="action">
                        <!-- 删除所在行 -->
                        <Icon type="md-close" size="20" class="clickable" @click="product.items.remove(index)"/>
                    </template>
                </Table>
            </div>
        </div>

        <!-- 底部工具栏 -->
        <div slot="footer" class="footer">
            <Button v-show="direct" icon="md-add" class="margin-right-10" @click="itemSelectVisible = true">添加物料</Button>
            <AuditorSelect v-model="stockOutData.currentAuditorId" :step="1" type="OUT_OF_STOCK"/>
            <div class="stretch"></div>
            <Button type="text" @click="showEvent(false)">取消</Button>
            <Button type="primary" :loading="saving" @click="stockOutRequest">确定</Button>
        </div>

        <!-- 物料选择弹窗 -->
        <ProductItemSelect v-model="itemSelectVisible" @on-ok="productItemSelected"/>

        <!-- 订单选择弹窗 -->
        <OrderSelect v-model="orderSelectVisible" not-in-stock-request @on-ok="orderSelected"/>

        <!-- 出库库存选择弹窗 -->
        <StockSelect v-model="stockSelectVisible" :product-item-id="selectedItem.productItemId" :max-count="selectedItem.maxCount" @on-ok="stockBatchCountsSelected"/>
    </Modal>
</template>

<script>
import StockDao from '@/../public/static-p/js/dao/StockDao';
import OrderDao from '@/../public/static-p/js/dao/OrderDao';
import ProductItemSelect from '@/components/ProductItemSelect.vue';
import StockSelect from '@/components/StockSelect.vue';
import OrderSelect from '@/components/OrderSelect.vue';
import AuditorSelect from '@/components/AuditorSelect.vue';

export default {
    props: {
        visible: { type: Boolean, required: true }, // 是否可见
        direct : { type: Boolean, required: true }, // 出库类型: true (物料出库), false (订单出库)
    },
    model: {
        prop : 'visible',
        event: 'on-visible-change',
    },
    components: { ProductItemSelect, OrderSelect, StockSelect, AuditorSelect },
    data() {
        return {
            // 出库的数据, product 结构下有 items，item 下有 batchCounts，如下
            // { items: [{ batchCounts: [{batch, count}] }]}
            products: [],
            stockOutData: { orderId: '0', orderSn: '', currentAuditorId: '0' },
            selectedItem: { product: {}, productItemId: '0', index: -1, maxCount: 0 }, // 用于记录选择出库的物料
            saving: false,
            itemSelectVisible : false, // 物料选择弹窗是否可见
            orderSelectVisible: false, // 订单选择弹窗是否可见
            stockSelectVisible: false, // 出库库存选择弹窗是否可见
            stockMaxCount     : 0,     // 出库的最大数量
            productItemColumns: [
                { key : 'name',        title: '物料名称', minWidth: 150 },
                { key : 'code',        title: '物料编码', width: 110 },
                { key : 'type',        title: '物料类型', width: 110 },
                { key : 'model',       title: '规格/型号', width: 110 },
                { key : 'standard',    title: '标准/规范', width: 110 },
                { slot: 'count',       title: '数量', width: 110 },
                { slot: 'batch-count', title: '出库批次 / 数量', width: 150, align: 'center' },
                { slot: 'select',      title: '选择', width: 70, align: 'center' },
                { slot: 'action',      title: '操作', width: 70, align: 'center' },
            ],
        };
    },
    computed: {
        // 弹窗的标题
        title() {
            return this.direct ? '物料出库' : `订单物料出库: ${this.stockOutData.orderSn}`;
        }
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
            if (this.direct) {
                this.itemSelectVisible  = true;
                this.orderSelectVisible = false;
            } else {
                this.itemSelectVisible  = false;
                this.orderSelectVisible = true;
            }

            this.products     = [{ items: [] }];
            this.selectedItem = { product: {}, productItemId: '0', index: -1, maxCount: 0 };
            this.stockOutData = { orderId: '0', orderSn: '', currentAuditorId: '0' };
        },
        // 选择了出库的物料 (可以有多个物料)
        productItemSelected(item) {
            // 提示:
            // A. 只有在物料直接出库时才调用这个方法
            // B. 如果物料已经存在，不要重复添加
            const items = this.products[0].items;
            const found = items.some(i => i.productItemId === item.productItemId);

            if (!found) {
                this.$set(item, 'batchCounts', []); // 批次和数量 (出库使用)
                items.push(item);
            } else {
                this.$Message.info('物料已经存在');
            }
        },
        // 选择了出库的订单 (只能有一个订单)
        orderSelected(order) {
            // 1. 查询订单
            // 2. 获取订单中的产品，且给产品的物料增加 batchCounts 属性
            // 3. 设置出库数据的订单信息

            // [1] 查询订单
            OrderDao.findOrderById(order.orderId).then(retOrder => {
                // [2] 获取订单中的产品，且给产品的物料增加 batchCounts 属性
                const products = retOrder.items.map(orderItem => orderItem.product);
                products.map(product => product.items).flat().forEach(item => {
                    item.batchCounts = [];
                });

                this.products = products;
                this.stockOutData = { orderId: retOrder.orderId, orderSn: retOrder.orderSn, currentAuditorId: '0' }; // [3] 设置出库数据的订单信息
            });
        },
        // 点击打开库存选择弹窗
        openStockSelect({ product, index }) {
            const productItem   = product.items[index];
            const productItemId = productItem.productItemId;
            const maxCount      = this.direct ? 0 : productItem.count; // 物料直接出库最大数量为 0，订单出库时最大数量为选中的物料的数量
            this.selectedItem   = { product, productItemId, index, maxCount };
            this.stockSelectVisible = true;
        },
        // 选择了批次和出库数量
        stockBatchCountsSelected(batchCounts) {
            const product = this.selectedItem.product;
            const index   = this.selectedItem.index;
            product.items[index].batchCounts = batchCounts;

            if (this.direct) {
                // 直接出库时 item.count 为 item.batchCounts 的和
                product.items[index].count = batchCounts.map(bc => bc.count).reduce((a, b) => a + b, 0);
            }
        },
        // 出库申请
        stockOutRequest() {
            // 1. 审批员不能为空
            // 2. 把出库的批次数量提取出来，物料每个批次的出库数据作为一个对象
            // 3. 构造出库的数据
            // 4. 提交到服务器

            // [1] 审批员不能为空
            if (!Utils.isValidId(this.stockOutData.currentAuditorId)) {
                this.$Message.error('请选择审批员');
                return;
            }

            // [2] 把出库的批次数量提取出来，物料每个批次的出库数据作为一个对象
            const batchCounts = [];
            for (let product of this.products) {
                for (let item of product.items) {
                    for (let bc of item.batchCounts) {
                        batchCounts.push({
                            productId: product.productId,
                            productItemId: item.productItemId,
                            productItemName: item.name,
                            productItemModel: item.model,
                            batch: bc.batch,
                            count: bc.count,
                        });
                    }
                }
            }

            // [3] 构造出库的数据
            const outData = {
                orderId: this.stockOutData.orderId,
                batchCounts: batchCounts,
                currentAuditorId: this.stockOutData.currentAuditorId, // 审批员 ID
            };

            // [4] 提交到服务器
            this.saving = true;
            StockDao.stockOutRequest(outData).then(newRequest => {
                this.$emit('on-ok', newRequest);
                this.showEvent(false); // 关闭弹窗
                this.saving = false;
            }).catch(err => {
                this.saving = false;
            });
        },
    },
};
</script>

<style lang="scss">
.stock-out-modal {
    .product {
        .product-info {
            display: flex;
            margin: 0px 0 10px 0;

            > div {
                margin-right: 30px;
            }
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
