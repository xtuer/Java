<!--
功能: 物料出库申请

属性:
visible: 是否可见，可使用 v-model 双向绑定
stock-request-id: 出库申请 ID

事件:
on-ok: 点击确定时触发，参数为无
on-visible-change: 显示或隐藏时触发，显示时参数为 true，隐藏时为 false

案例:
<StockOutDetails v-model="visible" :stock-request-id="stockRequestId"/>
-->

<template>
    <Modal :value="visible" title="物料出库" transfer width="900" class="stock-out-details-modal"
           :styles="{ top: '40px', marginBottom: '80px' }"
           @on-visible-change="showEvent">
        <!-- 弹窗 Body -->
        <div class="body-wrapper relative">
            <Spin v-if="loading" fix size="large"></Spin>
            <div v-show="!direct" class="order"><b>订单编号:</b> {{ orderSn }}</div>

            <!-- 产品列表 -->
            <div v-for="product in products" :key="product.productId" class="product">
                <!-- 产品信息 -->
                <div v-show="!direct" class="product-info">
                    <div><b>产品名称:</b> {{ product.name }}</div>
                    <div><b>产品编码:</b> {{ product.code }}</div>
                    <div><b>规格/型号:</b> {{ product.model }}</div>
                </div>

                <!-- 物料列表 -->
                <Table :data="product.items" :columns="productItemColumns" border>
                    <!-- 数量 -->
                    <template slot-scope="{ row: productItem }" slot="count">
                        {{ productItem.count }} {{ productItem.unit }}
                    </template>

                    <!-- 批次 / 数量 -->
                    <template slot-scope="{ row: productItem }" slot="batch-count">
                        <!-- <Tag v-for="bc in productItem.batchCounts" :key="bc.batch" color="cyan">
                            {{ bc.batch }} ({{ bc.count }})
                        </Tag> -->
                        <Tag color="cyan">
                            {{ productItem.batch }} ({{ productItem.count }})
                        </Tag>
                    </template>
                </Table>
            </div>

            <!-- 出库备注 -->
            <div class="comment">
                出库备注: <b>{{ comment }}</b>
            </div>

            <!-- 审批信息 -->
            <div class="audit">
                <div v-for="step in audit.steps" :key="step.step" class="audit-item-wrapper">
                    <AuditStep :step="step"/>
                </div>
            </div>
        </div>

        <!-- 底部工具栏 -->
        <div slot="footer">
            <!-- <Button type="text" @click="showEvent(false)">取消</Button>
            <Button type="primary" @click="ok">确定</Button> -->

            <Button v-if="auditPass" :loading="saving" type="primary" @click="stockOut">领取物料</Button>
        </div>
    </Modal>
</template>

<script>
import StockDao from '@/../public/static-p/js/dao/StockDao';
import OrderDao from '@/../public/static-p/js/dao/OrderDao';
import AuditDao from '@/../public/static-p/js/dao/AuditDao';
import AuditStep from '@/components/AuditStep.vue';

export default {
    props: {
        visible: { type: Boolean, required: true }, // 是否可见
        stockRequestId: { type: String,  required: true }, // 出库申请 ID
    },
    model: {
        prop : 'visible',
        event: 'on-visible-change',
    },
    components: { AuditStep },
    data() {
        return {
            comment: '',
            audit: {},
            orderSn: '',
            products: [],
            batchCounts: [], // 物料出库的批次数量
            productItemColumns: [
                // 设置 width, minWidth，当大小不够时 Table 会出现水平滚动条
                { key : 'name',     title: '物料名称', minWidth: 150 },
                { key : 'code',     title: '物料编码', width: 150 },
                { key : 'type',     title: '物料类型', width: 110 },
                { key : 'model',    title: '规格/型号', width: 110 },
                { key : 'standard', title: '标准/规范', width: 110 },
                { slot: 'count',    title: '数量', width: 110, align: 'center' },
                // { slot: 'batch-count', title: '出库批次 / 数量', width: 150, align: 'center' },
            ],
            loading  : false,
            saving   : false,
            auditPass: false, // 审批是否通过
        };
    },
    computed: {
        direct() {
            return !Utils.isValidId(this.orderSn);
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
        // 出库，物料领取
        stockOut() {
            this.saving = true;
            StockDao.stockOut(this.stockRequestId).then(() => {
                this.$Message.success('物料领取成功');
                this.saving = false;
                this.$emit('on-ok', this.stockRequestId);
                this.showEvent(false); // 关闭弹窗
            }).catch(() => {
                this.saving = false;
            });
        },
        // 初始化
        init() {
            // 1. 加载库存操作申请
            // 2. 从出库申请中提取物料的出库批次数量
            // 3. 如果订单号有效，则为订单的物料出库，加载订单信息
            // 4. 根据出库类型进行处理: 订单的物料还是物料直接出库
            //    4.1 订单的物料出库: 把库存操作中的产品项的数量设置到订单的对应产品项中
            //    4.2 直接物料出库则创建虚拟产品
            // 5. 合并出库数量到产品的物料中
            // 6. 查询库存操作申请的审批

            if (!Utils.isValidId(this.stockRequestId)) {
                return;
            }

            this.audit    = {};
            this.orderSn  = '';
            this.products = [];
            this.loading  = true;

            const recordProductItems = []; // 临时变量，缓存操作的物料

            // [1] 加载库存操作申请
            StockDao.findStockRequestById(this.stockRequestId).then(request => {
                const productItems = request.records.map(record => record.productItem);
                recordProductItems.push(...productItems);
                this.auditPass = request.state === 3; // "初始化", "审批中", "审批拒绝", "审批通过", "完成"
                this.comment = request.comment;

                // [2] 从出库申请中提取物料的出库批次数量
                this.batchCounts = this.extractBatchCountsFromStockRequest(request.records);

                // [3] 如果订单号有效，则为订单的物料出库，加载订单信息
                if (Utils.isValidId(request.orderId)) {
                    return OrderDao.findOrderById(request.orderId);
                } else {
                    return null;
                }
            }).then(order => {
                // [4] 根据出库类型进行处理: 订单的物料还是物料直接出库
                if (order) {
                    // [4.1] 订单的物料出库: 把库存操作中的产品项的数量设置到订单的对应产品项中
                    this.orderSn = order.orderSn;
                    this.products = order.items.map(oi => oi.product);

                    this.products
                        .flat(product => product.items)
                        .forEach(item => {
                            const found = recordProductItems.find(i => i.productItemId === item.productItemId);

                            if (found) {
                                item.count = found.count;
                            }
                        });
                } else {
                    // [4.2] 直接物料出库则创建虚拟产品
                    const virtualProduct = { productId: '0', items: recordProductItems };
                    this.products.push(virtualProduct);
                }

                // [5] 合并出库数量到产品的物料中
                // 先找产品，后找物料
                for (let product of this.products) {
                    const productBcs = this.batchCounts.filter(bc => bc.productId === product.productId);

                    for (let item of product.items) {
                        const itemBcs = productBcs.filter(bc => bc.productItemId === item.productItemId);
                        item.batchCounts = itemBcs;
                    }
                }
            }).then(() => {
                // [6] 查询库存操作申请的审批
                return AuditDao.findAuditOfTarget(this.stockRequestId);
            }).then(audit => {
                this.audit = audit;
                this.loading = false;
            }).catch(err => {
                this.loading = false;
                console.error(err);
            });
        },
        // 从出库申请中提取物料的出库批次数量
        extractBatchCountsFromStockRequest(requests) {
            const batchCounts = []; // { productId, productItemId, batch, count }

            for (let r of requests) {
                batchCounts.push({
                    productId: r.productItem.productId,
                    productItemId: r.productItem.productItemId,
                    batch: r.batch,
                    count: r.productItem.count,
                });
            }

            return batchCounts;
        }
    }
};
</script>

<style lang="scss">
.stock-out-details-modal {
    .comment {
        margin-top: 20px;
    }

    .product {
        .product-info {
            display: flex;
            margin: 20px 0 10px 0;

            > div {
                margin-right: 30px;
            }
        }
    }

    .audit {
        margin-top: 20px;

        .audit-item-wrapper {
            padding: 10px;
            border: 1px solid $borderColor;

            &:not(:first-child) {
                border-top: none;
            }
        }
    }
}
</style>
