<!-- eslint-disable vue/no-parsing-error -->

<!--
搜索入库、分页加载 (加载下一页的入库)
-->
<template>
    <div class="stock-in list-page">
        <!-- 顶部工具栏 -->
        <div class="list-page-toolbar-top">
            <div class="filter">
                <DatePicker v-model="dateRange"
                            format="MM-dd"
                            separator=" 至 "
                            type="daterange"
                            data-prepend-label="入库时间"
                            class="prepend-label"
                            split-panels
                            placeholder="请选择入库时间范围">
                </DatePicker>
                <Input v-model="filterValue" placeholder="请输入查询条件" search enter-button @on-search="searchStockRecords">
                    <Select v-model="filterKey" transfer slot="prepend">
                        <Option value="name">物料名称</Option>
                        <Option value="code">物料编码</Option>
                        <Option value="model">规格型号</Option>
                        <Option value="manufacturer">生产厂家</Option>
                    </Select>
                </Input>
            </div>
            <Button type="primary" icon="md-arrow-down" :disabled="!hasPermissionForStockIn()" @click="productItemModal = true">物料入库</Button>
        </div>

        <!-- 入库列表 -->
        <Table :data="stockRecords" :columns="columns" :loading="reloading" :max-height="tableMaxHeight" border
            @on-column-width-resize="saveTableColumnWidths(arguments)"
        >
            <template slot-scope="{ row: record }" slot="name">
                {{ record.productItem.name}}
            </template>
            <template slot-scope="{ row: record }" slot="code">
                {{ record.productItem.code }}
            </template>
            <template slot-scope="{ row: record }" slot="model">
                {{ record.productItem.model }}
            </template>
            <template slot-scope="{ row: record }" slot="count">
                {{ record.count}} {{ record.productItem.unit }}
            </template>
            <template slot-scope="{ row: record }" slot="date">
                {{ record.createdAt | formatDate }}
            </template>
            <template slot-scope="{ row: record, index }" slot="action">
                <Tooltip content="只能删除 24 小时内的入库" placement="left">
                    <Button type="error" size="small" :disabled="!canDelete(record)" @click="deleteStockIn(index)">删除</Button>
                </Tooltip>
            </template>
        </Table>

        <!-- 底部工具栏 -->
        <div class="list-page-toolbar-bottom">
            <Button v-show="more" :loading="loading" shape="circle" icon="md-boat" @click="fetchMoreStockRecords">更多...</Button>
        </div>

        <!-- 库存 -->
        <Modal v-model="stockInModal" title="物料入库" :mask-closable="false" transfer width="400" class="stock-in-modal">
            <!-- 内容显示 -->
            <Form ref="form" :model="stockRecord" :rules="itemRules" :key="stockRecord.productItemId" :label-width="80">
                <FormItem label="名称:">
                    <b>{{ stockRecord.name }}</b>
                </FormItem>
                <FormItem label="厂家:" prop="manufacturer">
                    <Input v-model="stockRecord.manufacturer" placeholder="请输入生产厂家"/>
                </FormItem>
                <FormItem label="类型:" prop="productItemType">
                    <Select v-model="stockRecord.productItemType" placeholder="请选择类型">
                        <Option value="成品">成品</Option>
                        <Option value="零件">零件</Option>
                        <Option value="部件">部件</Option>
                    </Select>
                </FormItem>
                <FormItem label="数量:" prop="count">
                    <InputNumber v-model="stockRecord.count"
                                :min="1"
                                :step="1"
                                placeholder="请输入入库数量"
                                style="width: 100%">
                    </InputNumber>
                </FormItem>
                <FormItem label="备注:">
                    <Input v-model="stockRecord.comment" placeholder="请输入备注"/>
                </FormItem>
            </Form>

            <!-- 底部工具栏 -->
            <div slot="footer">
                <Button type="text" @click="stockInModal = false">取消</Button>
                <Button type="primary" :loading="saving" @click="stockIn">入库</Button>
            </div>
        </Modal>

        <!-- 产品项选择弹窗 -->
        <ProductItemSelect v-model="productItemModal" @on-ok="showStockInModal"/>
    </div>
</template>

<script>
import StockDao from '@/../public/static-p/js/dao/StockDao';
import ProductItemSelect from '@/components/ProductItemSelect.vue';

export default {
    components: { ProductItemSelect },
    data() {
        return {
            stockRecords: [],
            filter      : {}, // 搜索条件
            filterKey   : 'name',   // 过滤条件的键
            filterValue : '',       // 过滤条件的值
            dateRange   : ['', ''], // 时间范围
            more     : false, // 是否还有更多入库
            loading  : false, // 加载中
            reloading: false,
            saving   : false, // 保存中
            tableName: 'stock-in-table', // 表名
            tableMaxHeight: 200,
            columns  : [
                { slot: 'name',            title: '物料名称', width: 180, fixed: 'left', resizable: true },
                { slot: 'code',            title: '物料编码', width: 150, resizable: true },
                { slot: 'model',           title: '规格/型号', width: 150, resizable: true },
                { key : 'productItemType', title: '类型', width: 110, align: 'center', resizable: true },
                { slot: 'count',           title: '数量', width: 110, align: 'right', resizable: true },
                { slot: 'date',            title: '日期', width: 150, align: 'center', resizable: true },
                { key : 'username',        title: '操作员', width: 110, resizable: true },
                { key : 'manufacturer',    title: '生产厂家', width: 150, resizable: true },
                { key : 'comment',         title: '备注', minWidth: 250 },
                { slot: 'action',          title: '操作', width: 80, align: 'center', resizable: false },
            ],
            stockInModal: false,
            productItemModal: false,
            stockRecord: { count: 10 },
            itemRules: {
                productItemType: [
                    { required: true, whitespace: true, message: '类型不能为空', trigger: 'blur' }
                ],
                count: [
                    { required: true, type: 'number', min: 1, message: '请输入数量', trigger: 'blur' }
                ],
            },
        };
    },
    mounted() {
        this.restoreTableColumnWidths(this.columns);
        this.searchStockRecords();
        this.tableMaxHeight = this.calculateTableMaxHeight();
    },
    methods: {
        // 搜索入库记录
        searchStockRecords() {
            this.stockRecords           = [];
            this.more                   = false;
            this.reloading              = true;
            this.filter                 = this.newFilter();
            this.filter[this.filterKey] = this.filterValue;

            if (this.dateRange[0] && this.dateRange[1]) {
                this.filter.startAt = this.dateRange[0].format('yyyy-MM-dd');
                this.filter.endAt   = this.dateRange[1].format('yyyy-MM-dd');
            } else {
                this.filter.startAt = '';
                this.filter.endAt   = '';
            }

            this.fetchMoreStockRecords();
        },
        // 点击更多按钮加载下一页的入库记录
        fetchMoreStockRecords() {
            this.loading = true;

            StockDao.findStockRecords(this.filter).then(stockRecords => {
                this.stockRecords.push(...stockRecords);

                this.more      = stockRecords.length >= this.filter.pageSize;
                this.loading   = false;
                this.reloading = false;
                this.filter.pageNumber++;
            });
        },
        // 显示入库弹窗
        showStockInModal(productItem) {
            this.stockRecord = {
                productItemId: productItem.productItemId,
                name         : productItem.name,
                batch        : '',
                warehouse    : '',
                unit         : productItem.unit || '',
                count        : 10,
                comment      : '',
            };
            this.stockInModal = true;
        },
        // 物料入库
        stockIn() {
            // 1. 表单验证不通过则返回
            // 2. 保存成功后添加到最前面
            // 3. 提示保存成功，隐藏编辑对话框

            this.$refs.form.validate(valid => {
                if (!valid) { return; }

                this.saving = true;
                StockDao.stockIn(this.stockRecord).then(newRecord => {
                    this.stockRecords.unshift(newRecord);
                    this.saving = false;
                    this.stockInModal = false;
                }).catch(() => {
                    this.saving = false;
                });
            });
        },
        // 删除入库
        deleteStockIn(index) {
            // 1. 删除提示
            // 2. 从服务器删除成功后才从本地删除
            // 3. 提示删除成功

            const record = this.stockRecords[index];
            this.$Modal.confirm({
                title: `确定删除物料 <font color="red">${record.productItem.name}</font> 的入库吗?`,
                loading: true,
                onOk: () => {
                    StockDao.deleteStockRecord(record.stockRecordId).then(() => {
                        this.stockRecords.splice(index, 1); // [2] 从服务器删除成功后才从本地删除
                        this.$Modal.remove();
                        this.$Message.success('删除成功');
                    });
                }
            });
        },
        // 是否可删除
        canDelete(reord) {
            // Feature: 您帮我把物料管理和库存部分的编辑和删除权限开一下 (质量保证, 或者您给管理员开通也行)
            if (this.hasPermission(['ROLE_ADMIN_SYSTEM'])) {
                return true;
            }

            // 不是自己不可删除
            if (!this.isCurrentUser(reord.userId)) {
                return false;
            }

            const before  = dayjs(reord.createdAt).unix();
            const current = dayjs().unix();

            // 大于 24 个小时不可删除
            if ((current - before) / 3600 >= 24) {
                return false;
            } else {
                return true;
            }
        },
        // 新建搜索条件
        newFilter() {
            return { // 搜索条件
                type      : 'IN',
                // name   : '',
                // code   : '',
                // batch  : '',
                // manufacturer: '',
                startAt   : '',
                endAt     : '',
                pageSize  : 50,
                pageNumber: 1,
            };
        }
    }
};
</script>

<style lang="scss">

.stock-in-modal {
    .stock-in-form {
        display: grid;
        grid-template-columns: max-content 1fr;
        grid-gap: 10px;
        align-items: center;

        .ivu-input-number {
            width: 100%;
        }
    }
}
</style>
