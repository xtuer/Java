<!-- eslint-disable vue/no-parsing-error -->

<!--
搜索维保订单、分页加载 (加载下一页的维保订单)
-->
<template>
    <div class="maintenance-orders list-page">
        <!-- 顶部工具栏 -->
        <div class="list-page-toolbar-top">
            <!-- 搜索条件 -->
            <div class="filter">
                <!-- 状态 -->
                <Select v-model="filter.state" data-prepend-label="状态" class="prepend-label" style="width: 100%; min-width: 150px" @on-change="searchOrders">
                    <Option :value="-1">全部</Option>
                    <Option :value="0">初始化</Option>
                    <Option :value="1">审批中</Option>
                    <Option :value="2">审批拒绝</Option>
                    <Option :value="3">审批通过</Option>
                    <Option :value="4">完成</Option>
                </Select>

                <!-- 时间范围 -->
                <DatePicker v-model="dateRange"
                            format="MM-dd"
                            separator=" 至 "
                            type="daterange"
                            data-prepend-label="收货时间"
                            class="prepend-label"
                            split-panels
                            placeholder="请选择收货时间范围">
                </DatePicker>

                <!-- 选择条件的搜索 -->
                <Input v-model="filterValue" transfer placeholder="请输入查询条件" search enter-button @on-search="searchOrders">
                    <Select v-model="filterKey" slot="prepend">
                        <Option value="maintenanceOrderSn">维保单号</Option>
                        <Option value="salespersonName">销售人员</Option>
                        <Option value="servicePersonName">售后人员</Option>
                        <Option value="productName">产品名称</Option>
                        <Option value="productCode">产品编码</Option>
                        <Option value="customerName">客户</Option>
                    </Select>
                </Input>
            </div>

            <!-- 其他按钮 -->
            <div>
                <Button type="primary" icon="md-add" :disabled="!hasPermissionForMaintenance()" @click="editOrder()">新建维保订单</Button>
                <Button type="info" icon="md-arrow-down" :loading="exporting" class="margin-left-10" @click="exportMaintenanceOrders()">导出维保订单</Button>
            </div>
        </div>

        <!-- 维保订单列表 -->
        <Table :data="orders" :columns="columns" :loading="reloading" border
            @on-column-width-resize="saveTableColumnWidths(arguments)"
        >
            <!-- 订单号 -->
            <template slot-scope="{ row: order }" slot="maintenanceOrderSn">
                <a @click="detailsOrder(order)">{{ order.maintenanceOrderSn }}</a>
            </template>

            <!-- 类型 -->
            <template slot-scope="{ row: order }" slot="type">
                {{ orderType(order) }}
            </template>

            <template slot-scope="{ row: order }" slot="productName">
                <Tooltip :content="order.productName" max-width="300" transfer placement="top-start">
                    <div v-html="commaToNewLine(order.productName)" class="long-text-block"></div>
                </Tooltip>
            </template>
            <template slot-scope="{ row: order }" slot="productCode">
                <Tooltip :content="order.productCode" max-width="300" transfer placement="top-start">
                    <div v-html="commaToNewLine(order.productCode)" class="long-text-block"></div>
                </Tooltip>
            </template>
            <template slot-scope="{ row: order }" slot="productModel">
                <Tooltip :content="order.productModel" max-width="300" transfer placement="top-start">
                    <div v-html="commaToNewLine(order.productModel)" class="long-text-block"></div>
                </Tooltip>
            </template>

            <!-- 收货日期 -->
            <template slot-scope="{ row: order }" slot="receivedDate">
                {{ order.receivedDate | formatDateSimple }}
            </template>

            <!-- 状态 -->
            <template slot-scope="{ row: order }" slot="state">
                <Tag :color="stateColor(order.state)" type="border">{{ order.stateLabel }}</Tag>
            </template>

            <!-- 进度 -->
            <template slot-scope="{ row: order }" slot="progress">
                <div v-show="progressEditedOrder !== order" class="progress-content">
                    {{ order.progress }}
                    <Icon v-show="canEditProgress(order)" type="md-create" class="clickable" @click="progressEditedOrder = order"/>
                </div>
                <Input v-show="progressEditedOrder === order" v-model="order.progress"
                        v-focus="progressEditedOrder === order"
                        @on-enter="saveProgress(order)"
                        @on-keyup="keyupForProgress(order, $event)"
                        @on-blur="cancelEditProgress(order)"/>
            </template>

            <!-- 操作按钮 -->
            <template slot-scope="{ row: order }" slot="action">
                <Button :disabled="!canEditOrder(order)" type="primary" size="small" @click="editOrder(order)">编辑</Button>
                <Button :disabled="!canEditOrder(order)" type="error" size="small" @click="deleteOrder(order)">删除</Button>
            </template>

            <!-- 维修信息明细 -->
            <template slot-scope="{ row: order }" slot="details">
                <Poptip trigger="click" placement="left" width="450" transfer @on-popper-show="findMaintenanceOrderItems(order.maintenanceOrderId)">
                    <Icon type="md-search" class="clickable"/>

                    <div slot="content" class="maintenance-details-content-wrapper">
                        <Spin v-if="currentOrderItemsLoading" fix size="large"></Spin>

                        <div v-for="item in currentOrderItems" :key="item.maintenanceOrderItemId" class="maintenance-details-content">
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
                        <div v-if="currentOrderItems.length === 0" style="text-align: center">
                            无数据
                        </div>
                    </div>
                </Poptip>
            </template>
        </Table>

        <!-- 底部工具栏 -->
        <div class="list-page-toolbar-bottom">
            <Button v-show="more" :loading="loading" shape="circle" icon="md-boat" @click="fetchMoreOrders">更多...</Button>
        </div>

        <!-- 维保订单编辑弹窗 -->
        <MaintenanceOrderEdit v-model="editModal" :maintenace-order-id="maintenanceOrderId" @on-ok="orderSaved"/>

        <!-- 维保订单详情弹窗 -->
        <MaintenanceOrderDetails v-model="detailsModal" :maintenace-order-id="maintenanceOrderId" @on-ok="orderCompleted(maintenanceOrderId)"/>
    </div>
</template>

<script>
import MaintenanceOrderDao from '@/../public/static-p/js/dao/MaintenanceOrderDao';
import MaintenanceOrderEdit from '@/components/MaintenanceOrderEdit.vue';
import MaintenanceOrderDetails from '@/components/MaintenanceOrderDetails.vue';

export default {
    components: { MaintenanceOrderEdit, MaintenanceOrderDetails },
    data() {
        return {
            orders: [],
            filter: this.newFilter(),
            filterKey  : 'maintenanceOrderSn', // 搜索的 Key
            filterValue: '',       // 搜索的 Value
            dateRange  : ['', ''], // 搜索的时间范围
            more     : false, // 是否还有更多维保订单
            loading  : false, // 加载中
            reloading: false,
            exporting: false, // 是否导出中
            tableName: 'maintenance-orders-table', // 表名
            columns  : [
                // 设置 width, minWidth，当大小不够时 Table 会出现水平滚动条
                { slot: 'maintenanceOrderSn', title: '维保单号', width: 180, resizable: true },
                { key : 'customerName', title: '客户', width: 150, resizable: true },
                { slot: 'type',   title: '类型', width: 110, resizable: true },
                { slot: 'productName', title: '产品名称', width: 150, resizable: true },
                { slot: 'productCode', title: '产品编码', width: 110, resizable: true },
                { slot: 'productModel', title: '型号/规格', width: 110, resizable: true },
                { key : 'productCount', title: '产品数量', width: 110, resizable: true },
                { slot: 'state', title: '状态', width: 110, align: 'center', resizable: true },
                { key : 'problem',   title: '反馈的问题', minWidth: 400 },
                { slot: 'progress',   title: '处理进度', width: 200, className: 'order-progress', resizable: true },
                { key : 'servicePersonName', title: '售后服务人员', width: 130, resizable: true },
                { key : 'salespersonName', title: '销售人员', width: 120, resizable: true },
                { slot: 'receivedDate', title: '收货日期', width: 130, align: 'center', resizable: true },
                { slot: 'action', title: '操作', width: 150, align: 'center', className: 'table-action', resizable: true },
                { slot: 'details', width: 50, align: 'center', fixed: 'right' },
            ],
            editModal: false, // 编辑弹窗是否可见
            detailsModal: false, // 维保订单详情弹窗是否可见
            maintenanceOrderId: '0', // 维保订单 ID
            progressEditedOrder: {}, // 选中编辑进度的维保订单

            currentOrderItems: [], // 当前点击选中的维保订单的项目
            currentOrderItemsLoading: false,
        };
    },
    mounted() {
        this.restoreTableColumnWidths(this.columns);
        this.searchOrders();
    },
    methods: {
        // 搜索维保订单
        searchOrders() {
            this.orders                 = [];
            this.more                   = false;
            this.reloading              = true;
            this.filter                 = { ...this.newFilter(), state: this.filter.state };
            this.filter[this.filterKey] = this.filterValue;

            // 如果不需要时间范围，则删除
            if (this.dateRange[0] && this.dateRange[1]) {
                this.filter.receivedStartAt = this.dateRange[0].format('yyyy-MM-dd');
                this.filter.receivedEndAt   = this.dateRange[1].format('yyyy-MM-dd');
            } else {
                this.filter.receivedStartAt = '';
                this.filter.receivedEndAt   = '';
            }

            this.fetchMoreOrders();
        },
        // 点击更多按钮加载下一页的维保订单
        fetchMoreOrders() {
            this.loading = true;

            MaintenanceOrderDao.findMaintenanceOrders(this.filter).then(orders => {
                this.orders.push(...orders);

                this.more      = orders.length >= this.filter.pageSize;
                this.loading   = false;
                this.reloading = false;
                this.filter.pageNumber++;
            });
        },
        // 订单类型
        orderType(order) {
            const ns = [];

            if (order.maintainable) {
                ns.push('维修');
            }
            if (order.repairable) {
                ns.push('保养');
            }

            return ns.join(', ');
        },
        // 编辑订单
        editOrder(order) {
            // 弹窗订单编辑窗口
            // order 存在则是更新，不存在这是新建
            if (order) {
                this.maintenanceOrderId = order.maintenanceOrderId;
            } else {
                this.maintenanceOrderId = '0';
            }

            this.editModal = true;
        },
        // 订单保存成功
        orderSaved(order) {
            // 查找订单，如果存在则替换，否则插入都第一行
            const index = this.orders.findIndex(o => o.maintenanceOrderId === order.maintenanceOrderId);

            if (index >= 0) {
                this.orders.replace(index, order);
            } else {
                this.orders.insert(0, order);
            }
        },
        // 完成订单
        orderCompleted(maintenanceOrderId) {
            const found = this.orders.find(o => o.maintenanceOrderId === maintenanceOrderId);

            if (found) {
                found.state = 4;
                found.stateLabel = '完成';
            }
        },
        // 显示订单详情
        detailsOrder(order) {
            this.maintenanceOrderId = order.maintenanceOrderId;
            this.detailsModal = true;
        },
        // 判断订单是否可以编辑: 售后服务人员为当前用户，且初始化或者审批拒绝的订单才能编辑
        canEditOrder(order) {
            if (this.isCurrentUser(order.servicePersonId) && (order.state === 0 || order.state === 2)) {
                return true;
            } else {
                return false;
            }
        },
        // 判断是否可以编辑进度: 售后服务人员为当前用户
        canEditProgress(order) {
            return this.isCurrentUser(order.servicePersonId);
        },
        // 取消编辑进度
        cancelEditProgress(order) {
            const found = this.orders.find(o => o.maintenanceOrderId === order.maintenanceOrderId);

            if (found) {
                order.progress = found.progress;
            }

            this.progressEditedOrder = {};
        },
        // 保存进度
        saveProgress(order) {
            // 1. 在 orders 中查找表格中行的 order 对应的原始订单对象 found
            // 2. 保存进度到服务器
            // 3. 更新成功后更新 found 的进度
            // 4. 重置选中的订单 progressEditedOrder，隐藏进度输入框

            // [1] 在 orders 中查找表格中行的 order 对应的原始订单对象 found
            const found = this.orders.find(o => o.maintenanceOrderId === order.maintenanceOrderId);

            if (!found) {
                return;
            }

            // [2] 保存进度到服务器
            MaintenanceOrderDao.updateProgress(order.maintenanceOrderId, order.progress).then(() => {
                // [3] 更新成功后更新 found 的进度
                found.progress = order.progress;

                // [4] 重置选中的订单 progressEditedOrder，隐藏进度输入框
                this.progressEditedOrder = {};

                this.$Message.success('进度保存成功');
            });
        },
        // 进度的输入框键盘事件
        keyupForProgress(order, event) {
            if (event.keyCode === 27) {
                this.cancelEditProgress(order);
            }
        },
        // 删除维保订单
        deleteOrder(order) {
            // 1. 删除提示
            // 2. 从服务器删除成功后才从本地删除
            // 3. 提示删除成功

            this.$Modal.confirm({
                title: `确定删除维保订单 <font color="red">${order.maintenanceOrderSn}</font> 吗?`,
                loading: true,
                onOk: () => {
                    MaintenanceOrderDao.deleteMaintenanceOrder(order.maintenanceOrderId).then(() => {
                        const index = this.orders.findIndex(o => o.maintenanceOrderId === order.maintenanceOrderId); // 用户下标
                        this.orders.splice(index, 1); // [2] 从服务器删除成功后才从本地删除
                        this.$Modal.remove();
                        this.$Message.success('删除成功');
                    });
                }
            });
        },
        // 导出维保订单
        exportMaintenanceOrders() {
            this.exporting = true;
            this.exportFile(MaintenanceOrderDao.exportMaintenanceOrders(this.filter)).then(() => {
                this.exporting = false;
            });
        },
        // 新建搜索条件
        newFilter() {
            return { // 搜索条件
                // maintenanceOrderSn: '',
                // salespersonName   : '',
                // servicePersonName : '',
                // productName : '',
                // productCode : '',
                // customerName: '',
                state          : -1,
                receivedStartAt: '',
                receivedEndAt  : '',
                pageSize       : 50,
                pageNumber     : 1,
            };
        },
        // 查询维保订单项
        findMaintenanceOrderItems(orderId) {
            this.currentOrderItems = [];
            this.currentOrderItemsLoading = true;
            MaintenanceOrderDao.findMaintenanceOrderItems(orderId).then(items => {
                this.currentOrderItems = items;
                this.currentOrderItemsLoading = false;
            });
        },
        // 逗号替换为回车
        commaToNewLine(str) {
            return str.replace(/,/g, ',<br>');
        },
    },
    directives: {
        // 输入框获取焦点命令 (binding.value 为 v-focus="xxx" 的参数 xxx)
        focus: function(el, binding) {
            if (binding.value) {
                setTimeout(() => {
                    el.querySelector('input').focus();
                }, 0);
            }
        }
    },
};
</script>

<style lang="scss">
/* 页面布局 */
.list-page {
    display: grid;
    grid-gap: 24px;

    .list-page-toolbar-top {
        display: grid;
        grid-template-columns: max-content max-content;
        justify-content: space-between;
        align-items: center;
    }

    .list-page-toolbar-bottom {
        display: grid;
        justify-content: center;
        align-items: center;
    }

    .order-progress .progress-content {
        .ivu-icon {
            display: none;
        }
    }
    .order-progress:hover .progress-content {
        .ivu-icon {
            display: inline-block;
        }
    }
}

.maintenance-details-content-wrapper {
    position: relative;
    height: 400px;
    overflow: auto;

    .maintenance-details-content {
        display: grid;
        grid-template-columns: max-content 1fr;
        grid-gap: 3px 10px;

        span:nth-child(odd) {
            text-align: right;
            color: $iconColor;
        }

        &:not(:first-child) {
            border-top: 1px solid $borderColor;
            margin-top: 20px;
            padding-top: 20px;
        }
    }
}

.maintenance-orders {
    .long-text-block {
        max-height: 200px;
        overflow: hidden;
        cursor: default;
    }
}
</style>
