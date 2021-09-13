<!--
功能: 销售订单编辑弹窗

属性:
visible: 是否可见，可使用 v-model 双向绑定
sales-order-id: 销售订单 ID

事件:
on-ok: 点击确定时触发，参数为销售订单
on-visible-change: 显示或隐藏时触发，显示时参数为 true，隐藏时为 false

案例:
<SalesOrderEdit v-model="visible" :sales-order-id="salesOrderId"/>
-->

<template>
    <Modal :value="visible" :title="title" :mask-closable="false" :closable="false" class="sales-order-edit-modal relative"
        :width="900" :styles="{ top: '20px', marginBottom: '40px' }"
        @on-visible-change="showEvent">
        <!-- 弹窗 Body -->
        <Spin v-if="loading" fix size="large"></Spin>

        <div class="box">
            <div class="title">基本信息</div>
            <div class="content padding-left-0 padding-bottom-0">
                <Form ref="salesOrderForm" :model="salesOrder" :rules="salesOrderRules" :key="salesOrder.salesOrderId" :label-width="100" class="column-3">
                    <FormItem label="主题:" prop="topic">
                        <Input v-model="salesOrder.topic" placeholder="请输入主题"/>
                    </FormItem>
                    <FormItem label="客户:" prop="customerName">
                        <Input v-model="salesOrder.customerName" readonly search placeholder="请选择客户" @on-search="customerSelect = true"/>
                    </FormItem>
                    <FormItem label="联系人:" prop="customerContact">
                        <Select v-model="salesOrder.customerContact" placeholder="请选择联系人" filterable allow-create @on-create="createTempCustomerContact">
                            <Option v-for="contact in customerContacts" :value="contact" :key="contact">{{ contact }}</Option>
                        </Select>
                    </FormItem>
                    <FormItem label="行业:" prop="business">
                        <Input v-model="salesOrder.business" placeholder="请输入行业"/>
                    </FormItem>
                    <FormItem label="执行单位:" prop="workUnit">
                        <Input v-model="salesOrder.workUnit" placeholder="请输入执行单位"/>
                    </FormItem>
                    <!-- <FormItem label="负责人:" prop="ownerName"> -->
                        <!-- <Input v-model="salesOrder.ownerName" readonly/> -->
                        <!-- <Input v-model="salesOrder.ownerName" readonly search placeholder="请选择负责人" @on-search="userSelect = true"/> -->
                    <!-- </FormItem> -->
                    <FormItem label="联系方式:" prop="customerPhone">
                        <Input v-model="salesOrder.customerPhone"/>
                    </FormItem>
                    <FormItem label="收件地址:" prop="customerAddress" style="grid-column: span 2">
                        <Input v-model="salesOrder.produceOrder.customerAddress" clearable placeholder="请输入客户收件地址"/>
                    </FormItem>
                    <FormItem label="订单类型:" style="width: 300px">
                        <Select v-model="salesOrder.produceOrder.type">
                            <Option :value="0">生产订单</Option>
                            <Option :value="1">样品订单</Option>
                        </Select>
                    </FormItem>

                    <FormItem label="签约日期:" prop="agreementDate">
                        <DatePicker v-model="salesOrder.agreementDate" type="date" placeholder="请选择签约日期" style="width: 100%"/>
                    </FormItem>
                    <FormItem label="交货日期:" prop="deliveryDate">
                        <DatePicker v-model="salesOrder.deliveryDate" type="date" placeholder="请选择交货日期" style="width: 100%"/>
                    </FormItem>
                    <FormItem v-if="salesOrder.produceOrder.type === 1" label="归还日期:" prop="returnDate">
                        <DatePicker v-model="salesOrder.produceOrder.returnDate" type="date" placeholder="请选择规划日期"></DatePicker>
                    </FormItem>
                    <FormItem prop="remark" style="grid-column: 1 / span 2">
                        <div slot="label">
                            <div class="margin-bottom-5">订货附件&nbsp;</div>
                            <div>及其他要求:</div>
                        </div>
                        <Input v-model="salesOrder.remark" type="textarea" placeholder="请输入订货附件及其他要求"/>
                    </FormItem>
                    <FormItem label="订单附件:">
                        <a v-if="salesOrder.produceOrder.attachment.id" style="margin-right: 10px">{{ salesOrder.produceOrder.attachment.filename }}</a>
                        <FileUpload @on-success="fileUploaded"/>
                    </FormItem>

                    <FormItem style="grid-column: span 2">
                        <div slot="label">
                            是否校准:
                            <i-switch v-model="salesOrder.produceOrder.calibrated" size="large" class="margin-top-5">
                                <span slot="open">是</span>
                                <span slot="close">否</span>
                            </i-switch>
                        </div>
                        <Input v-if="salesOrder.produceOrder.calibrated" v-model="salesOrder.produceOrder.calibrationInfo" type="textarea" :autosize="{ minRows: 3, maxRows: 7 }" placeholder="请输入校准信息"/>
                    </FormItem>
                </Form>
            </div>
        </div>

        <div class="box margin-top-20">
            <div class="title">
                销售订单明细
                <Icon type="md-add-circle" class="clickable margin-left-10" size="18" @click="addProduceOrderProduct"/>
            </div>
            <div class="content" style="padding-left: 0; padding-right: 0">
                <!-- 订单项列表 -->
                <Table :data="salesOrder.produceOrder.items" :columns="produceOrderItemColumns" border>
                    <!-- 产品名称 -->
                    <template slot-scope="{ index }" slot="name">
                        <div style="display: flex; align-items: center">
                            <span v-if="salesOrder.produceOrder.items[index].product.name" style="flex: 1">
                                {{ salesOrder.produceOrder.items[index].product.name }}
                            </span>
                            <span v-else class="text-color-gray-l" style="flex: 1">请选择产品</span>
                            <Icon type="ios-search" class="clickable margin-left-10" size="18" @click="openProductSelect(salesOrder.produceOrder.items[index])"/>
                        </div>
                    </template>

                    <!-- 单价 -->
                    <template slot-scope="{ index }" slot="price">
                        <InputNumber v-model="salesOrder.produceOrder.items[index].price" @on-change="calculatePayment"/>
                    </template>

                    <!-- 成本价 -->
                    <template slot-scope="{ index }" slot="costPrice">
                        <InputNumber v-model="salesOrder.produceOrder.items[index].costPrice"/>
                    </template>

                    <!-- 数量 -->
                    <template slot-scope="{ index }" slot="count">
                        <InputNumber v-model="salesOrder.produceOrder.items[index].count" :min="0" @on-change="onProductCountChanged(index)"/>
                    </template>

                    <!-- 咨询费用 -->
                    <template slot-scope="{ index }" slot="consultationFee">
                        <InputNumber v-model="salesOrder.produceOrder.items[index].consultationFee" @on-change="calculatePayment"/>
                    </template>

                    <!-- 操作按钮 -->
                    <template slot-scope="{ index }" slot="action">
                        <Poptip
                            confirm
                            transfer
                            title="确定删除?"
                            @on-ok="deleteProduceOrderItem(index)">
                            <Icon type="md-close" size="16" class="clickable"/>
                        </Poptip>
                    </template>
                </Table>

                <!-- 应收金额 -->
                <div class="payment-info">
                    <div class="text-color-gray">净销售金额: {{ salesOrder.costDealAmount }}</div>
                    <div class="text-color-gray">咨询费: {{ salesOrder.consultationFee }}</div>
                    <div class="text-color-gray">总成交金额: {{ salesOrder.dealAmount }}</div>
                    <div class="text-color-gray should_pay">
                        应收金额: <InputNumber v-model="salesOrder.shouldPayAmount" :min="0"/>
                    </div>
                </div>
            </div>
        </div>

        <!-- 底部工具栏 -->
        <div slot="footer">
            <Button type="text" @click="showEvent(false)">取消</Button>
            <Button v-if="salesOrder.state === 0" type="success" :loading="saving" @click="saveSalesOrder(0)">保存</Button>

            <Poptip confirm transfer title="确定提交订单 ?" @on-ok="saveSalesOrder(1)">
                <Button type="primary" :loading="saving">提交</Button>
            </Poptip>
        </div>

        <!-- 用户选择弹窗 -->
        <UserSelect v-model="userSelect" @on-ok="ownerSelected"/>

        <!-- 客户选择弹窗 -->
        <CustomerSelect v-model="customerSelect" @on-ok="customerSelected"/>

        <!-- 产品选择弹窗 -->
        <ProductSelect v-model="productSelect" @on-ok="productSelected"/>
    </Modal>
</template>

<script>
import SalesOrderDao from '@/../public/static-p/js/dao/SalesOrderDao';
import CustomerDao from '@/../public/static-p/js/dao/CustomerDao';
import UserSelect from '@/components/UserSelect.vue';
import CustomerSelect from '@/components/CustomerSelect.vue';
import ProductSelect from '@/components/ProductSelect.vue';
import FileUpload from '@/components/FileUpload.vue';

export default {
    components: { CustomerSelect, UserSelect, ProductSelect, FileUpload },
    props: {
        visible     : { type: Boolean, required: true }, // 是否可见
        salesOrderId: { type: String,  required: true }, // 销售订单 ID
    },
    data() {
        return {
            salesOrder: this.newSalesOrder(), // 用于编辑的销售订单
            saving    : false,
            loading   : false,
            userSelect      : false, // 用户选择弹窗是否可见
            customerSelect  : false, // 客户选择弹窗是否可见
            productSelect   : false, // 产品选择弹窗是否可见
            customerContacts: [],    // 客户的联系人名字
            produceOrderItemSelected: {}, // 当前选择的生产订单项
            produceOrderItemColumns: [ // 订单项的列
                { type: 'index', width: 50, align: 'center', className: 'table-index' },
                { slot: 'name', title: '产品' },
                { slot: 'price', title: '单价', width: 150, resizable: true },
                { slot: 'costPrice', title: '成本价', width: 150, resizable: true },
                { slot: 'count', title: '数量', width: 150, resizable: true },
                { slot: 'consultationFee', title: '咨询费', width: 150, resizable: true },
                { slot: 'action', title: '', width: 50, align: 'center', className: 'table-action' },
            ],
            salesOrderRules: {
                topic: [
                    { required: true, whitespace: true, message: '主题不能为空', trigger: 'blur' }
                ],
                // ownerName: [
                //     { required: true, whitespace: true, message: '负责人不能为空', trigger: 'change' }
                // ],
                customerName: [
                    { required: true, whitespace: true, message: '客户不能为空', trigger: 'change' }
                ],
                agreementDate: [
                    { required: true, type: 'date', message: '请选择签约日期', trigger: 'blur' }
                ],
                deliveryDate: [
                    { required: true, type: 'date', message: '请选择交货日期', trigger: 'blur' }
                ],
            },
        };
    },
    model: {
        prop : 'visible',
        event: 'on-visible-change',
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
            // 例如从服务器加载数据
            this.$refs.salesOrderForm.resetFields();

            if (this.salesOrderId === '0') {
                this.salesOrder = this.newSalesOrder();
            } else {
                // 1. 查询销售订单
                // 2. 查询客户的联系人
                this.loading = true;

                // [1] 查询销售订单
                SalesOrderDao.findSalesOrder(this.salesOrderId).then(salesOrder => {
                    this.salesOrder = salesOrder;

                    // [2] 查询客户的联系人
                    CustomerDao.findCustomerById(salesOrder.customerId).then(customer => {
                        this.customerContacts = customer.contacts.map(contact => contact.name);
                        this.loading = false;

                        // 如果订单中的客户联系人不在客户的联系人列表，则加入 (订单的联系人是手动输入的)
                        if (!this.customerContacts.includes(salesOrder.customerContact)) {
                            this.createTempCustomerContact(salesOrder.customerContact);
                        }
                    });
                });
            }
        },
        // 负责人选择完成
        ownerSelected(owner) {
            this.salesOrder.ownerName = owner.nickname;
            this.salesOrder.ownerId   = owner.userId;
        },
        // 客户选择完成
        customerSelected(customer) {
            this.salesOrder.customerName    = customer.name;
            this.salesOrder.customerId      = customer.customerId;
            this.salesOrder.business        = customer.business;
            this.salesOrder.customerContact = '';
            this.customerContacts           = customer.contacts.map(contact => contact.name);
        },
        // 弹出产品选择弹出
        openProductSelect(produceOrderItem) {
            this.productSelect = true;
            this.produceOrderItemSelected = produceOrderItem;
        },
        // 产品选择完成
        productSelected(product) {
            this.produceOrderItemSelected.product = product;
        },
        // 添加生产订单项
        addProduceOrderProduct() {
            this.salesOrder.produceOrder.items.push(this.newProduceOrderItem());
        },
        // 删除生产订单项
        deleteProduceOrderItem(index) {
            this.salesOrder.produceOrder.items.remove(index);
            this.calculatePayment();
        },
        // 保存销售订单
        // saveType: 保存类型, 0 (临时保存)、1 (提交生成生产订单)
        saveSalesOrder(saveType) {
            // 表单验证
            this.$refs.salesOrderForm.validate(valid => {
                // 1. 使用规则校验
                // 2. 过滤掉没有选择产品的生产订单项
                // 3. 生产订单至少选择一个产品
                // 4. 新创建的生产订单项的 ID 设置为 0
                // 5. 提交到服务器，成功则关闭弹窗

                // [1] 使用规则校验
                if (!valid) { return; }

                // 生产订单至少选择一个产品
                const salesOrder = Utils.clone(this.salesOrder);

                // [2] 过滤掉没有选择产品的生产订单项
                salesOrder.produceOrder.items = salesOrder.produceOrder.items.filter(item => {
                    return Utils.isValidId(item.product.productId);
                });

                // [3] 生产订单至少选择一个产品
                if (salesOrder.produceOrder.items.length === 0) {
                    this.$Message.error('请选择产品');
                    return;
                }

                // [4] 新创建的生产订单项的 ID 设置为 0
                salesOrder.produceOrder.items.forEach(item => {
                    if (item.neu) {
                        item.orderItemId = '0';
                    }
                });

                // [5] 提交到服务器，成功则关闭弹窗
                this.saving = true;
                SalesOrderDao.upsertSalesOrder(salesOrder, saveType).then(modifiedSalesOrder => {
                    this.$emit('on-ok', modifiedSalesOrder);
                    this.showEvent(false); // 关闭弹窗
                    this.saving = false;
                }).catch(() => {
                    this.saving = false;
                });
            });
        },
        // 产品的数量变化
        onProductCountChanged(index) {
            // 1. 确保数量是整数
            // 2. 计算应收金额
            this.$nextTick(() => {
                let count = this.salesOrder.produceOrder.items[index].count;
                count = parseInt(count) || 0;
                this.salesOrder.produceOrder.items[index].count = count;
                this.calculatePayment();
            });
        },
        // 计算支付金额
        calculatePayment() {
            // 总成交金额: 所有产品的数量*单价 + 咨询费
            // 净销售额: 所有产品的数量*单价

            this.salesOrder.dealAmount = 0;
            this.salesOrder.costDealAmount = 0;
            this.salesOrder.consultationFee = 0;

            for (let item of this.salesOrder.produceOrder.items) {
                this.salesOrder.dealAmount += item.price * item.count + item.consultationFee;
                this.salesOrder.costDealAmount += item.costPrice * item.count;
                this.salesOrder.consultationFee += item.consultationFee;
            }

            // 应收金额默认为总成交金额
            this.salesOrder.shouldPayAmount = this.salesOrder.dealAmount;
        },
        // 创建销售订单，每个销售订单带有一个生产订单
        newSalesOrder() {
            return {
                state          : 0,   // 状态
                salesOrderId   : '0', // 订单 ID
                salesOrderSn   : '', // 订单编号
                topic          : '', // 主题
                agreementDate  : '', // 签约日期
                deliveryDate   : '', // 交货日期
                ownerName      : this.$store.getters.currentUsername, // 负责人
                ownerId        : this.$store.getters.currentUserId,   // 负责人 ID
                customerId     : '0', // 客户 ID
                customerName   : '', // 客户
                customerContact: '', // 联系人
                customerPhone  : '', // 联系方式
                business       : '', // 行业
                workUnit       : '', // 执行单位
                remark         : '', // 备注
                produceOrder   : this.newProduceOrder(), // 生产订单

                dealAmount     : 0,  // 总成交金额
                costDealAmount : 0,  // 净销售额
                consultationFee: 0,  // 订单咨询费
                shouldPayAmount: 0,  // 应收金额
            };
        },
        // 创建生产订单
        newProduceOrder() {
            return {
                items: [this.newProduceOrderItem()], // 订单项
                type           : 0,     // 订单类型: 0 (生产订单)、1 (样品订单)
                customerAddress: '',    // 客户收件地址
                calibrated     : false, // 是否校准
                calibrationInfo: '证书单位：\n证书地址：\n制造商：\n设备名称：\n设备类型：\n校准内容：\n其他校准信息：', // 校准信息
                attachmentId   : '0',   // 附件 ID
                attachment     : {},    // 附件,
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
        // 临时创建客户联系人
        createTempCustomerContact(contact) {
            this.customerContacts.push(contact);
        },
        // 文件上传完成
        fileUploaded(file) {
            this.salesOrder.produceOrder.attachmentId = file.id;
            this.salesOrder.produceOrder.attachment = file;
        },
    },
    computed: {
        title() {
            return this.salesOrder.salesOrderSn ? `销售订单: ${this.salesOrder.salesOrderSn}` : '销售订单';
        },
    }
};
</script>

<style lang="scss">
.sales-order-edit-modal {
    .box {
        .title {
            display: flex;
            align-items: center;
        }

        .ivu-input-number {
            width: 100%;
        }
    }

    .payment-info {
        display: grid;
        grid-template-columns: repeat(3, max-content) 200px;
        align-items: center;
        grid-gap: 30px;
        margin-top: 10px;
        margin-left: 10px;

        .should_pay {
            display: grid;
            grid-template-columns: max-content 1fr;
            align-items: center;
            grid-gap: 5px;
        }
    }
}
</style>
