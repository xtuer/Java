<!--
功能: 订单详情弹窗

属性:
visible: 是否可见，可使用 v-model 双向绑定
order-id: 订单 ID

事件:
on-ok: 点击完成订单时触发，参数为订单 ID
on-visible-change: 显示或隐藏时触发，显示时参数为 true，隐藏时为 false

案例:
<OrderDetails v-model="visible" :order-id="orderId"/>
-->

<template>
    <Modal :value="visible" :title="'订单详情: ' + order.orderSn" width="800"
           :styles="{ top: '40px', marginBottom: '80px' }"
           class="order-details-modal" @on-visible-change="showEvent">
        <!-- 内容显示 -->
        <table class="table-order table-common relative">
            <Spin v-if="loading" fix size="large"></Spin>

            <!-- 客户信息 -->
            <tr>
                <td class="text-color-gray">客户单位</td>
                <td colspan="4">{{ order.customerCompany }}</td>
            </tr>
            <tr>
                <td class="text-color-gray">客户联系人</td>
                <td>{{ order.customerContact }}</td>
                <td class="text-color-gray text-align-right">客户联系方式</td>
                <td colspan="2">{{ order.customerPhone }}</td>
            </tr>
            <tr>
                <td class="text-color-gray">客户收件地址</td>
                <td colspan="4">{{ order.customerAddress }}</td>
            </tr>
            <tr>
                <td class="text-color-gray">执行单位</td>
                <td colspan="4">{{ order.workUnit }}</td>
            </tr>
            <tr>
                <td class="text-color-gray">销售负责人</td>
                <td colspan="1">{{ salesperson }}</td>
                <td class="text-color-gray text-align-right">订单类型</td>
                <td colspan="2">{{ order.type | labelForValue(window.ORDER_TYPES) }}</td>
            </tr>

            <tr>
                <td colspan="5">
                    <div style="display: grid; grid-template-columns: max-content max-content 1fr; grid-gap: 60px">
                        <div>订单日期: {{ order.orderDate | formatDateSimple }}</div>
                        <div>交货日期: {{ order.deliveryDate | formatDateSimple }}</div>
                        <div v-if="order.type === 1">归还日期: {{ order.returnDate | formatDateSimple }}</div>
                    </div>
                </td>
            </tr>

            <!-- 产品 -->
            <tr>
                <td class="text-color-gray">产品名称</td>
                <td class="text-color-gray">产品编码</td>
                <td class="text-color-gray">规格/型号</td>
                <td class="text-color-gray text-align-center">数量</td>
                <td class="text-color-gray">备注</td>
            </tr>
            <tr v-for="item in items" :key="item.productId">
                <td>{{ item.product && item.product.name }}</td>
                <td>{{ item.product && item.product.code }}</td>
                <td>{{ item.product && item.product.model }}</td>
                <td class="text-align-center">{{ item.count }}</td>
                <td>{{ item.comment }}</td>
            </tr>
            <tr v-if="items.length === 0">
                <td colspan="5" class="text-color-gray text-align-center">无</td>
            </tr>

            <!-- 其他要求 -->
            <tr>
                <td colspan="5">
                    <div>
                        <span class="text-color-gray margin-right-5">订货附件及其他要求:</span>
                        {{ order.requirement || '无' }}
                    </div>

                    <div class="margin-top-10">
                        <span class="text-color-gray margin-right-5">订单附件:</span>
                        <a v-if="attachment.id !== '0'" :href="attachment.url">{{ attachment.filename }}</a>
                        <span v-else>无</span>
                    </div>

                    <!-- 校准 -->
                    <div class="margin-top-10">
                        <template v-if="order.calibrated">
                            <span class="text-color-gray">校准信息:</span>
                            <pre class="margin-0 margin-left-20 pre-wrap">{{ order.calibrationInfo }}</pre>
                        </template>
                        <template v-else>
                            <span class="text-color-gray">校准信息:</span> 无
                        </template>
                    </div>

                    <div class="margin-top-10">
                        <span class="text-color-gray margin-right-5">销售人员:</span>
                        {{ salesperson }} / {{ order.createdAt | formatDate }}
                    </div>
                </td>
            </tr>

            <tr>
                <td colspan="5" class="text-align-center background-gray">审批</td>
            </tr>

            <!-- 审批信息 -->
            <tr v-for="step in audit.steps" :key="step.step">
                <td colspan="5">
                    <AuditStep :step="step"/>
                </td>
            </tr>
        </table>

        <!-- 底部工具栏 -->
        <div slot="footer">
            <!-- <Button type="text" @click="showEvent(false)">取消</Button> -->
            <!-- <Button type="primary" @click="showEvent(false)">确定</Button> -->
            <Button v-if="canCompleteOrder" :loading="saving" type="primary" @click="completeOrder">完成订单</Button>
        </div>
    </Modal>
</template>

<script>
import OrderDao from '@/../public/static-p/js/dao/OrderDao';
import AuditDao from '@/../public/static-p/js/dao/AuditDao';
import AuditStep from '@/components/AuditStep.vue';

export default {
    props: {
        visible: { type: Boolean, required: true }, // 是否可见
        orderId: { type: String,  required: true }, // 订单 ID
    },
    model: {
        prop : 'visible',
        event: 'on-visible-change',
    },
    components: { AuditStep },
    data() {
        return {
            order: {}, // 订单
            audit: {}, // 审批
            loading: false,
            saving : false,
        };
    },
    computed: {
        // 销售负责人
        salesperson() {
            if (this.order.salesperson) {
                return this.order.salesperson.nickname;
            } else {
                return '未知';
            }
        },
        // 订单项
        items() {
            if (this.order.items) {
                return this.order.items;
            } else {
                return [];
            }
        },
        // 订单附件
        attachment() {
            if (this.order.attachment && this.order.attachment.id !== '0') {
                return this.order.attachment;
            } else {
                return { id: '0' };
            }
        },
        // 是否可以完成订单
        canCompleteOrder() {
            // 当前状态为 3 且其销售员为当前登陆用户
            // order.state === 3; // "初始化", "审批中", "审批拒绝", "审批通过", "完成"
            if (this.order.state === 3 && this.isCurrentUser(this.order.salespersonId)) {
                return true;
            } else {
                return false;
            }
        }
    },
    methods: {
        // 显示隐藏事件
        showEvent(visible) {
            this.$emit('on-visible-change', visible);

            // 显示时加载订单
            if (visible) {
                this.init();
            }
        },
        // 初始化
        init() {
            // 查询订单和订单的审批
            this.loading = true;

            Promise.all([
                OrderDao.findOrderById(this.orderId),
                AuditDao.findAuditOfTarget(this.orderId)
            ]).then(([order, audit]) => {
                this.order = order;
                this.audit = audit;
                this.loading = false;
            }).catch(error => {
                this.loading = false;
                console.error(error);
            });
        },
        // 完成订单
        completeOrder() {
            this.saving = true;
            OrderDao.completeOrder(this.orderId).then(() => {
                this.$Message.success('订单完成');
                this.saving = false;
                this.$emit('on-ok', this.orderId);
                this.showEvent(false); // 关闭弹窗
            }).catch(() => {
                this.saving = false;
            });
        }
    }
};
</script>

<style lang="scss">
.order-details-modal {
    .table-order {
        td:nth-child(1), td:nth-child(2), td:nth-child(3) {
            width: 150px;
        }
        td:nth-child(4) {
            width: 70px;
        }

        .audit-item .ivu-input-group {
            border-collapse: collapse;
        }
    }
}
</style>
