<!--
功能: 订单编辑弹窗

属性:
visible: 是否可见，可使用 v-model 双向绑定
order-id: 订单 ID

事件:
on-ok: 点击确定时触发，参数为选择的学员
on-visible-change: 显示或隐藏时触发，显示时参数为 true，隐藏时为 false

案例:
<OrderEdit v-model="visible" :order-id="orderId"/>
-->

<template>
    <Modal :value="visible" :title="title" :mask-closable="false" :closable="false" :width="900"
            class="order-edit-modal"
            :styles="{ top: '40px', marginBottom: '80px' }"
            @on-visible-change="showEvent">
        <Form ref="orderForm" :model="orderClone" :rules="orderRules" :key="orderClone.orderId" :label-width="130">
            <div style="display: flex">
                <FormItem label="客户单位:" prop="customerCompany" style="flex: 1">
                    <Input v-model="orderClone.customerCompany" clearable placeholder="请输入客户单位"/>
                </FormItem>
                <FormItem label="客户联系人:" prop="customerContact" style="width: 300px">
                    <Input v-model="orderClone.customerContact" clearable placeholder="请输入客户联系人"/>
                </FormItem>
            </div>
            <div style="display: flex">
                <FormItem label="客户收件地址:" prop="customerAddress" style="flex: 1">
                    <Input v-model="orderClone.customerAddress" clearable placeholder="请输入客户收件地址"/>
                </FormItem>
                <FormItem label="客户联系方式:" prop="customerPhone" style="width: 300px">
                    <Input v-model="orderClone.customerPhone" clearable placeholder="请输入客户联系方式"/>
                </FormItem>
            </div>
            <div style="display: flex">
                <FormItem label="执行单位:" prop="workUnit" style="flex: 1">
                    <Input v-model="orderClone.workUnit" clearable placeholder="请输入执行单位"/>
                </FormItem>
                <FormItem label="订单类型:" style="width: 300px">
                    <Select v-model="orderClone.type">
                        <Option :value="0">生产订单</Option>
                        <Option :value="1">样品订单</Option>
                    </Select>
                </FormItem>
            </div>
            <div class="three-column" style="grid-template-columns: 1fr 1fr 1fr">
                <FormItem label="订单日期:" prop="orderDate">
                    <DatePicker v-model="orderClone.orderDate" type="date" placeholder="请选择订单日期"></DatePicker>
                </FormItem>
                <FormItem label="交货日期:" prop="deliveryDate">
                    <DatePicker v-model="orderClone.deliveryDate" type="date" placeholder="请选择交货日期"></DatePicker>
                </FormItem>
                <FormItem v-if="orderClone.type === 1" label="归还日期:" prop="returnDate">
                    <DatePicker v-model="orderClone.returnDate" type="date" placeholder="请选择规划日期"></DatePicker>
                </FormItem>
            </div>
            <FormItem>
                <div slot="label">
                    <div class="margin-bottom-5">订购产品:</div>
                    <Button type="primary" ghost size="small" @click="productSelectVisible = true">添加产品</Button>
                </div>
                <!-- 产品列表 -->
                <Table :data="orderClone.items" :columns="orderItemColumns" size="small" border>
                    <!-- 产品名称 -->
                    <template slot-scope="{ row: item }" slot="name">
                        {{ item.product.name }}
                    </template>
                    <!-- 产品编码 -->
                    <template slot-scope="{ row: item }" slot="code">
                        {{ item.product.code }}
                    </template>
                    <!-- 规格/型号 -->
                    <template slot-scope="{ row: item }" slot="model">
                        {{ item.product.model }}
                    </template>
                    <!-- 数量 -->
                    <template slot-scope="{ row: item, index }" slot="count">
                        <InputNumber :min="1" :step="1" v-model="orderClone.items[index].count"></InputNumber>
                    </template>
                    <!-- 备注 -->
                    <template slot-scope="{ row: item, index }" slot="comment">
                        <Input v-model="orderClone.items[index].comment"/>
                    </template>
                    <!-- 操作按钮 -->
                    <template slot-scope="{ index }" slot="action">
                        <div class="column-buttons">
                            <!-- 删除子项物料 -->
                            <Icon type="ios-close-circle" size="18" class="clickable" @click="orderClone.items.remove(index)"/>
                        </div>
                    </template>
                </Table>
            </FormItem>
            <FormItem>
                <div slot="label">
                    <div class="margin-bottom-5">订货附件&nbsp;</div>
                    <div>及其他要求:</div>
                </div>
                <Input v-model="orderClone.requirement" type="textarea" :autosize="{ minRows: 3, maxRows: 5 }" placeholder="请输入订货附件及其他要求"/>
            </FormItem>
            <FormItem label="是否校准:">
                <i-switch v-model="orderClone.calibrated" size="large">
                    <span slot="open">是</span>
                    <span slot="close">否</span>
                </i-switch>
            </FormItem>
            <FormItem v-if="orderClone.calibrated" label="校准信息:">
                <Input v-model="orderClone.calibrationInfo" type="textarea" :autosize="{ minRows: 3, maxRows: 7 }" placeholder="请输入校准信息"/>
            </FormItem>
            <FormItem label="订单附件:">
                <a v-if="orderClone.attachment.id" style="margin-right: 10px">{{ orderClone.attachment.filename }}</a>
                <FileUpload @on-success="fileUploaded"/>
            </FormItem>
        </Form>

        <div slot="footer" class="footer">
            <AuditorSelect v-model="orderClone.currentAuditorId" :step="1" type="ORDER"/>
            <span class="stretch"></span>
            <Button type="text" @click="showEvent(false)">取消</Button>
            <Button type="primary" :loading="saving" @click="saveOrder">保存</Button>
        </div>

        <!-- 产品选择弹窗 -->
        <ProductSelect v-model="productSelectVisible" @on-ok="selectProduct"/>
    </Modal>
</template>

<script>
import OrderDao from '@/../public/static-p/js/dao/OrderDao';
import FileUpload from '@/components/FileUpload.vue';
import ProductSelect from '@/components/ProductSelect.vue';
import AuditorSelect from '@/components/AuditorSelect.vue';

export default {
    props: {
        visible: { type: Boolean, required: true }, // 是否可见
        orderId: { type: String,  required: true }, // 订单
    },
    model: {
        prop : 'visible',
        event: 'on-visible-change',
    },
    components: { FileUpload, ProductSelect, AuditorSelect },
    data() {
        return {
            orderClone: this.newOrder(),
            saving    : false,
            productSelectVisible: false, // 产品选择弹窗是否可见
            orderRules: {
                customerCompany: [
                    { required: true, whitespace: true, message: '客户单位不能为空', trigger: 'blur' }
                ],
                customerContact: [
                    { required: true, whitespace: true, message: '客户联系人不能为空', trigger: 'blur' }
                ],
                customerAddress: [
                    { required: true, whitespace: true, message: '客户收件地址不能为空', trigger: 'blur' }
                ],
                orderDate: [
                    { required: true, type: 'date', message: '请选择订单日期', trigger: 'change' }
                ],
                deliveryDate: [
                    { required: true, type: 'date', message: '请选择要求交货日期', trigger: 'change' }
                ],
            },
            // 订单项的列 (产品)
            orderItemColumns: [
                { slot: 'name',    title: '产品名称', width: 130 },
                { slot: 'code',    title: '产品编码', width: 130 },
                { slot: 'model',   title: '规格/型号', width: 130 },
                { slot: 'count',   title: '数量', minWidth: 100 },
                { slot: 'comment', title: '备注', minWidth: 150 },
                { slot: 'action',  title: '操作', width: 70, align: 'center' },
            ],
        };
    },
    computed: {
        title() {
            if (this.orderClone.orderId === '0') {
                return '订单编辑';
            } else {
                return `订单编辑: ${this.orderClone.orderSn}`;
            }
        }
    },
    methods: {
        // 显示隐藏事件
        showEvent(visible) {
            this.$emit('on-visible-change', visible);

            // 显示弹窗时初始化订单
            if (visible) {
                this.init(this.orderId);
            }
        },
        // 初始化订单
        init(orderId) {
            // 重置表单的状态
            this.$refs.orderForm.resetFields();

            if (orderId === '0') {
                // 新建
                this.orderClone = this.newOrder();
            } else {
                // 更新
                OrderDao.findOrderById(orderId).then(order => {
                    this.orderClone = order;
                });
            }
        },
        // 保存订单
        saveOrder() {
            this.$refs.orderForm.validate(valid => {
                if (!valid) { return; }

                // 审批员不能为空
                if (!Utils.isValidId(this.orderClone.currentAuditorId)) {
                    this.$Message.error('请选择审批员');
                    return;
                }

                this.saving = true;

                OrderDao.upsertOrder(this.orderClone).then(order => {
                    this.$emit('on-ok', order);
                    this.saving = false;
                    this.showEvent(false); // 关闭弹窗
                }).catch(() => {
                    this.saving = false;
                });
            });
        },
        // 文件上传完成
        fileUploaded(file) {
            this.orderClone.attachmentId = file.id;
            this.orderClone.attachment = file;
        },
        // 选择产品
        selectProduct(product) {
            // 1. 如果产品已经选择，提示已经存在
            // 2. 创建新订单项

            // [1] 如果产品已经选择，提示已经存在
            if (this.orderClone.items.map(item => item.productId).includes(product.productId)) {
                this.$Message.warning('产品已经存在');
                return;
            }

            // [2] 创建新订单项
            const orderItem = this.newOrderItem(product);
            this.orderClone.items.push(orderItem);
        },
        // 创建新订单
        newOrder() {
            return {
                orderId        : '0',   // 订单 ID
                orderSn        : '',    // 订单编号
                type           : 0,     // 订单类型: 0 (生产订单)、1 (样品订单)
                customerCompany: '',    // 客户单位
                customerContact: '',    // 客户联系人
                customerAddress: '',    // 客户收件地址
                customerPhone  : '',    // 客户联系方式
                workUnit       : '',    // 执行单位
                orderDate      : '',    // 订单日期
                deliveryDate   : '',    // 交货日期
                returnDate     : '',    // 规划日期
                salespersonId  : '0',   // 销售员
                calibrated     : false, // 是否校准
                calibrationInfo: '证书单位：\n证书地址：\n制造商：\n设备名称：\n设备类型：\n校准内容：\n其他校准信息：', // 校准信息
                requirement    : '',    // 要求
                attachmentId   : '0',   // 附件 ID
                createdAt      : '',    // 订单创建日期
                state          : 0,     // 状态: 0 (初始化), 1 (待审批), 2 (审批拒绝), 3 (审批完成), 4 (完成)
                productCodes   : '',    // 订单的产品编码，使用逗号分隔，方便搜索
                items          : [],    // 订单项
                attachment     : {},    // 附件,
                currentAuditorId: '0',  // 当前审批员 ID
            };
        },
        // 创建订单项
        newOrderItem(product) {
            return {
                orderItemId: '0',
                orderId    : '0',
                productId  : product.productId,
                count      : 1,
                comment    : '',
                product    : product,
            };
        },
    }
};
</script>

<style lang="scss">

.order-edit-modal {
    .ivu-date-picker, .ivu-input-number {
        width: 100%;
    }

    .ivu-table-header thead tr th, .ivu-table-fixed-header thead tr th {
        padding: 0 !important;
    }

    .footer {
        display: flex;

        .auditor-select {
            width: 100px;
        }
    }
}
</style>
