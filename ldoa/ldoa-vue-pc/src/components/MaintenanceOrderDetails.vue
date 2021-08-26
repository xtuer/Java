<!--
功能: 维保订单详情

属性:
visible: 是否可见，可使用 v-model 双向绑定
maintenace-order-id: 维保订单 ID

事件:
on-ok: 点击完成订单时触发，参数为订单 ID
on-visible-change: 显示或隐藏时触发，显示时参数为 true，隐藏时为 false

案例:
<MaintenanceOrderDetails v-model="visible" :maintenace-order-id="maintenaceOrderId"/>
-->

<template>
    <Modal :value="visible" :title="title" class="maintenance-order-details-modal" width="900"
           :styles="{ top: '40px', marginBottom: '80px' }" @on-visible-change="showEvent">
        <!-- 弹窗 Body -->
        <Spin v-if="loading" fix size="large"></Spin>
        <table class="order-table relative">
            <tr>
                <td class="td-label text-color-gray">客户:</td>
                <td class="td-rest">{{ order.customerName }}</td>

                <td class="td-label text-color-gray">销售人员:</td>
                <td class="td-rest">{{ order.salespersonName }}</td>

                <td class="td-label text-color-gray">类型:</td>
                <td class="td-rest">{{ orderType }}</td>
            </tr>

            <!-- 产品 -->
            <!-- <tr>
                <td class="td-label text-color-gray">产品名称:</td>
                <td class="td-rest">{{ order.productName }}</td>

                <td class="td-label text-color-gray">产品编码:</td>
                <td class="td-rest">{{ order.productCode }}</td>

                <td class="td-label text-color-gray">规格/型号:</td>
                <td class="td-rest">{{ order.productModel }}</td>
            </tr> -->

            <!-- 物料 -->
            <!-- <tr>
                <td class="td-label text-color-gray">物料名称:</td>
                <td class="td-rest">{{ order.productItemName }}</td>

                <td class="td-label text-color-gray">物料批次:</td>
                <td class="td-rest">{{ order.productItemBatch }}</td>

                <td class="td-label text-color-gray">物料数量:</td>
                <td class="td-rest">{{ order.productItemCount }}</td>
            </tr> -->
            <tr>
                <td class="td-label text-color-gray">产品数量:</td>
                <td class="td-rest">{{ order.productCount == 0 ? '' : order.productCount }}</td>
                <td class="td-label text-color-gray">配件:</td>
                <td class="td-rest" colspan="3">{{ order.accessories }}</td>
            </tr>

            <!-- 其他 -->
            <tr>
                <td class="td-label text-color-gray">收货日期:</td>
                <td class="td-rest">{{ order.receivedDate | formatDateSimple }}</td>

                <td class="td-label text-color-gray">订单号:</td>
                <td class="td-rest">{{ order.orderSn }}</td>

                <td class="td-label text-color-gray">需要证书:</td>
                <td class="td-rest">{{ order.needCertificate ? '是' : '否' }}</td>
            </tr>
            <tr>
                <td class="td-label text-color-gray" style="vertical-align: top">反馈的问题:</td>
                <td class="td-rest" colspan="5">
                    <div>{{ order.problem }}</div>
                    <div style="text-align: right; margin-top: 10px">
                        {{ order.servicePersonName }} / {{ order.createdAt | formatDateSimple }}
                    </div>
                </td>
            </tr>
            <!-- 进度 -->
            <tr>
                <td class="td-label text-color-gray">处理进度:</td>
                <td class="td-rest" colspan="5">{{ order.progress }}</td>
            </tr>
        </table>

        <!-- 维保订单列表 -->
        <Table :data="order.items" :columns="orderItemColumns" :max-height="350" border>
            <!-- 维修信息明细 -->
            <template slot-scope="{ row: item }" slot="details">
                <Poptip trigger="click" placement="left" width="450" transfer>
                    <Icon type="md-search" class="clickable"/>

                    <div slot="content" class="maintenance-details-content">
                        <span>产品名称:</span> <span>{{ item.productName }}</span>
                        <span>产品编码:</span> <span>{{ item.productCode }}</span>
                        <span>规格型号:</span> <span>{{ item.productModel }}</span>
                        <span>出厂时间:</span> <span>{{ item.productionDate }}</span>
                        <span>维修前电量:</span> <span>{{ item.electricQuantityBefore }}</span>
                        <span>维修前软件版本:</span> <span>{{ item.softwareVersionBefore }}</span>
                        <span>维修前硬件版本:</span> <span>{{ item.hardwareVersionBefore }}</span>
                        <span>维修前功耗:</span> <span>{{ item.powerDissipationBefore }}</span>
                        <span>维修前高温次数:</span> <span>{{ item.temperatureBefore }}</span>
                        <span>芯片编号:</span> <span>{{ item.chipCode }}</span>
                        <span>检测问题明细:</span> <span>{{ item.checkDetails }}</span>
                        <span>维修明细:</span> <span>{{ item.maintenanceDetails }}</span>
                        <span>探头换前编号:</span> <span>{{ item.probeDetectorCodeBefore }}</span>
                        <span>维修后电量:</span> <span>{{ item.electricQuantityAfter }}</span>
                        <span>维修后软件版本:</span> <span>{{ item.softwareVersionAfter }}</span>
                        <span>维修后硬件版本:</span> <span>{{ item.hardwareVersionAfter }}</span>
                        <span>维修后功耗:</span> <span>{{ item.powerDissipationAfter }}</span>
                        <span>维修后高温次数:</span> <span>{{ item.temperatureAfter }}</span>
                        <span>探头换后编号:</span> <span>{{ item.probeDetectorCodeAfter }}</span>
                    </div>
                </Poptip>
            </template>

            <!-- 操作按钮 -->
            <template slot-scope="{ row: item }" slot="action">
                <Icon type="md-create" size="16" class="clickable margin-right-5" @click="editOrderItem(item)"/>

                <Poptip
                    confirm
                    transfer
                    title="确定删除?"
                    @on-ok="deleteOrderItem(item)">
                    <Icon type="md-close" size="16" class="clickable"/>
                </Poptip>
            </template>
        </Table>

        <table v-if="audit" class="order-table relative margin-top-10">
            <tr>
                <td colspan="6" class="text-align-center background-gray">审批</td>
            </tr>

            <!-- 审批信息 -->
            <tr v-for="step in audit.steps" :key="step.step">
                <td colspan="6">
                    <AuditStep :step="step"/>
                </td>
            </tr>
        </table>

        <!-- 底部工具栏 -->
        <div slot="footer">
            <Button v-if="canCompleteOrder" :loading="saving" type="primary" @click="completeOrder">完成订单</Button>
        </div>
    </Modal>
</template>

<script>
import MaintenanceOrderDao from '@/../public/static-p/js/dao/MaintenanceOrderDao';
import AuditDao from '@/../public/static-p/js/dao/AuditDao';
import AuditStep from '@/components/AuditStep.vue';

export default {
    props: {
        visible: { type: Boolean, required: true }, // 是否可见
        maintenaceOrderId: { type: String,  required: true }, // 维保订单 ID
    },
    model: {
        prop : 'visible',
        event: 'on-visible-change',
    },
    components: { AuditStep },
    data() {
        return {
            audit: {}, // 审批
            order: {}, // 维保订单
            loading: false, // 加载中
            saving : false,
            orderItemColumns: [ // 维保订单项表格的列
                { type: 'index', width: 50, align: 'center', className: 'table-index' },
                { key: 'productName', title: '产品名称', width: 150 },
                { key: 'productCode', title: '产品编码', width: 150 },
                { key: 'productModel', title: '规格型号', width: 150 },
                { key: 'productionDate', title: '出厂时间', width: 150 },
                { key: 'electricQuantityBefore', title: '维修前电量', width: 150 },
                { key: 'softwareVersionBefore', title: '维修前软件版本', width: 150 },
                { key: 'hardwareVersionBefore', title: '维修前硬件版本', width: 150 },
                { key: 'powerDissipationBefore', title: '维修前功耗', width: 150 },
                { key: 'temperatureBefore', title: '维修前高温次数', width: 150 },
                { key: 'chipCode', title: '芯片编号', width: 150 },
                { key: 'checkDetails', title: '检测问题明细', width: 350 },
                { key: 'maintenanceDetails', title: '维修明细', width: 350 },
                { key: 'probeDetectorCodeBefore', title: '探头换前编号', width: 150 },
                { key: 'electricQuantityAfter', title: '维修后电量', width: 150 },
                { key: 'softwareVersionAfter', title: '维修后软件版本', width: 150 },
                { key: 'hardwareVersionAfter', title: '维修后硬件版本', width: 150 },
                { key: 'powerDissipationAfter', title: '维修后功耗', width: 150 },
                { key: 'temperatureAfter', title: '维修后高温次数', width: 150 },
                { key: 'probeDetectorCodeAfter', title: '探头换后编号', width: 150 },
                { slot: 'details', width: 50, align: 'center', fixed: 'right' },
            ],
        };
    },
    computed: {
        // 标题
        title() {
            return `维保订单: ${this.order.maintenanceOrderSn}`;
        },
        // 订单类型
        orderType() {
            const ns = [];

            if (this.order.maintainable) {
                ns.push('维修');
            }
            if (this.order.repairable) {
                ns.push('保养');
            }

            return ns.join(', ');
        },
        // 是否可以完成订单
        canCompleteOrder() {
            // 当前状态为 3 且其销售员为当前登陆用户
            // order.state === 3; // "初始化", "审批中", "审批拒绝", "审批通过", "完成"
            if (this.order.state === 3 && this.isCurrentUser(this.order.servicePersonId)) {
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
            if (!Utils.isValidId(this.maintenaceOrderId)) {
                return;
            }
            this.loading = true;

            // 1. 查询维保订单
            // 2. 如果维保订单已经提交，则查询它的审批

            MaintenanceOrderDao.findMaintenanceOrderById(this.maintenaceOrderId).then(order => {
                this.order = order;

                if (order.committed) {
                    return AuditDao.findAuditOfTarget(this.maintenaceOrderId);
                } else {
                    return null;
                }
            }).then(audit => {
                this.audit = audit;
                this.loading = false;
            }).catch(error => {
                this.loading = false;
                console.error(error);
            });

            // Promise.all([
            //     MaintenanceOrderDao.findMaintenanceOrderById(this.maintenaceOrderId),
            //     AuditDao.findAuditOfTarget(this.maintenaceOrderId)
            // ]).then(([order, audit]) => {
            //     this.order = order;
            //     this.audit = audit;
            //     this.loading = false;
            // }).catch(error => {
            //     this.loading = false;
            //     console.error(error);
            // });
        },
        // 完成订单
        completeOrder() {
            this.saving = true;
            MaintenanceOrderDao.completeOrder(this.maintenaceOrderId).then(() => {
                this.$Message.success('订单完成');
                this.saving = false;
                this.$emit('on-ok', this.maintenaceOrderId);
                this.showEvent(false); // 关闭弹窗
            }).catch(() => {
                this.saving = false;
            });
        }
    }
};
</script>

<style lang="scss">
.maintenance-order-details-modal {
    .order-table {
        border-collapse: collapse;
        width: 100%;

        .td-label {
            width    : 100px;
            min-width: 100px;
            max-width: 100px;
            text-align: right;
        }

        .td-rest {
            /* 先去掉设置 min-width 的 td 占用的空间 */
            /* 有 rest-td class 的 td 从左边 td 开始占用 table 剩余空间的 50% */
            width: 33%;
        }

        td {
            border: 1px solid $borderColor;
            padding: 8px 12px;

            .gap {
                display: inline-block;
                width: 100px;
            }

            &.center {
                text-align: center;
            }
        }

        .audit-item .ivu-input-group {
            border-collapse: collapse;
        }
    }
}

.maintenance-details-content {
    display: grid;
    grid-template-columns: max-content 1fr;
    grid-gap: 3px 10px;

    span:nth-child(odd) {
        text-align: right;
        color: $iconColor;
    }
}
</style>
