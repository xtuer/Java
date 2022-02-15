<!--
功能: 物料出库弹窗

属性:
visible: 是否可见，可使用 v-model 双向绑定

事件:
on-ok: 点击确定时触发，参数为出库申请
on-visible-change: 显示或隐藏时触发，显示时参数为 true，隐藏时为 false

案例:
<StockOutProductItemModal v-model="visible" @on-ok="stockOutRequestOk"/>
-->
<template>
    <Modal :value="visible" title="物料出库" transfer width="900" :mask-closable="false" class="stock-out-product-item-modal" @on-visible-change="showEvent">
        <!-- 弹窗 Body -->
        <div class="body-wrapper">
            <!-- 物料列表 -->
            <Table :data="items" :columns="productItemColumns" border>
                <!-- 出库数量 -->
                <template slot-scope="{ index }" slot="count">
                    <InputNumber :min="1" v-model="items[index].count"></InputNumber> {{ items[index].unit }}
                </template>

                <template slot-scope="{ index }" slot="action">
                    <!-- 删除所在行 -->
                    <Icon type="md-close" size="20" class="clickable" @click="items.remove(index)"/>
                </template>
            </Table>

            <!-- 出库备注 -->
            <div class="comment">
                <span>出库备注:</span>
                <Input v-model="comment" type="textarea" placeholder="请输入出库备注"/>
            </div>
        </div>

        <!-- 底部工具栏 -->
        <div slot="footer" class="footer">
            <Button type="primary" ghost icon="md-add" class="margin-right-10" @click="itemSelectVisible = true">选择物料</Button>
            <AuditorSelect v-model="currentAuditorId" :step="1" type="OUT_OF_STOCK"/>
            <div class="stretch"></div>
            <Button type="text" @click="showEvent(false)">取消</Button>
            <Button type="primary" :loading="saving" @click="stockOutRequest">确定</Button>
        </div>

        <!-- 物料选择弹窗 -->
        <ProductItemSelect v-model="itemSelectVisible" @on-ok="productItemSelected"/>
    </Modal>
</template>

<script>
import StockDao from '@/../public/static-p/js/dao/StockDao';
import ProductItemSelect from '@/components/ProductItemSelect.vue';
import AuditorSelect from '@/components/AuditorSelect.vue';

export default {
    props: {
        visible: { type: Boolean, required: true }, // 是否可见
    },
    model: {
        prop : 'visible',
        event: 'on-visible-change',
    },
    components: { ProductItemSelect, AuditorSelect },
    data() {
        return {
            items: [], // 出库的物料数组
            comment: '', // 出库备注
            itemSelectVisible: false, // 物料选择窗口是否可见
            currentAuditorId: '0', // 当前审批员 ID
            productItemColumns: [
                { key : 'name',     title: '物料名称', minWidth: 150 },
                { key : 'code',     title: '物料编码', width: 150 },
                { key : 'type',     title: '物料类型', width: 110 },
                { key : 'model',    title: '规格/型号', width: 110 },
                { key : 'standard', title: '标准/规范', width: 110 },
                { slot: 'count',    title: '数量', width: 120, className: 'table-column-number-input-with-unit' },
                { slot: 'action',   title: '操作', width: 70, align: 'center' },
            ],
            saving: false, // 保存中
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
        // 初始化
        init() {
            this.items = [];
            this.itemSelectVisible = true;
            this.currentAuditorId = '0';
            this.comment = '';
        },
        // 选择了出库的物料 (可以有多个物料)
        productItemSelected(item) {
            // 提示:
            // A. 只有在物料直接出库时才调用这个方法
            // B. 如果物料已经存在，不要重复添加
            const found = this.items.some(i => i.productItemId === item.productItemId);

            if (!found) {
                this.items.push(item);
            } else {
                this.$Message.info('物料已经存在');
            }
        },
        // 物料出库申请
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
                targetId        : '0',
                targetType      : window.STOCK_OUT_TYPE.PRODUCT_ITEM,
                currentAuditorId: this.currentAuditorId,
                desc            : '物料出库',
                comment         : this.comment,
                productItemNames: '', // 物料名字
                records         : [], // [{ productId, productItemId, count }]
            };
            const itemNames = [];

            for (let item of this.items) {
                // [3] 出库数量不为 0 的每一个物料创建一个出库记录
                if (item.count > 0) {
                    request.records.push({
                        productId: item.productId,
                        productItemId: item.productItemId,
                        count: item.count,
                    });
                    itemNames.push(item.name);
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
.stock-out-product-item-modal {
    .body-wrapper .comment {
        margin-top: 20px;
    }

    .footer {
        display: flex;

        .auditor-select {
            width: 200px;
        }
    }
}
</style>
