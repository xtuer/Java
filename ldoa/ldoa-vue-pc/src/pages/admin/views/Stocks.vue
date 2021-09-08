<!-- eslint-disable vue/no-parsing-error -->
<!--
搜索物料、分页加载 (加载下一页的物料)
-->
<template>
    <div class="stock list-page">
        <!-- 顶部工具栏 -->
        <div class="list-page-toolbar-top">
            <div class="filter">
                <Input v-model="filter.name" placeholder="请输入物料名称" @on-enter="searchProductItems">
                    <span slot="prepend">物料名称</span>
                </Input>
                <Input v-model="filter.code" placeholder="请输入物料编码" @on-enter="searchProductItems">
                    <span slot="prepend">物料编码</span>
                </Input>
                <Input v-model="filter.batch" placeholder="入库批次" @on-enter="searchProductItems">
                    <span slot="prepend">入库批次</span>
                </Input>
                <Input v-model="filter.count" placeholder="请输入物料编码" search enter-button @on-search="searchProductItems">
                    <span slot="prepend">数量小于</span>
                </Input>
            </div>

            <Button type="info" icon="md-arrow-down" :loading="exporting" @click="exportStocks()">导出库存</Button>
        </div>

        <!-- 物料列表 -->
        <Table :data="items" :columns="columns" :loading="reloading" :max-height="tableMaxHeight" border
            @on-column-width-resize="saveTableColumnWidths(arguments)"
        >
            <!-- 数量 -->
            <template slot-scope="{ row: item }" slot="count">
                <span :class="itemClass(item)">{{ item.count }}</span> {{item.unit}}
            </template>

            <!-- 批次 -->
            <template slot-scope="{ row: item }" slot="batch">
                <span class="text-color-gray">{{ item.batch || '未入库' }}</span>
            </template>
        </Table>

        <!-- 底部工具栏 -->
        <div class="list-page-toolbar-bottom">
            <Button v-show="more" :loading="loading" shape="circle" icon="md-boat" @click="fetchMoreProductItems">更多...</Button>
        </div>
    </div>
</template>

<script>
import StockDao from '@/../public/static-p/js/dao/StockDao';

export default {
    data() {
        return {
            items: [],
            filter: { // 搜索条件
                name      : '',
                code      : '',
                batch     : '',
                count     : 10000,
                pageSize  : 50,
                pageNumber: 1,
            },
            more     : false, // 是否还有更多物料
            loading  : false, // 加载中
            reloading: false, // 重新加载中
            modal    : false, // 是否显示编辑对话框
            saving   : false, // 保存中
            exporting: false, // 是否导出中
            tableName: 'stocks-table', // 表名
            tableMaxHeight: 200,
            columns  : [
                // 设置 width, minWidth，当大小不够时 Table 会出现水平滚动条
                { key : 'name',     title: '物料名称', width: 200, resizable: true },
                { key : 'code',     title: '物料编码', width: 110, resizable: true },
                { key : 'type',     title: '物料类型', width: 110, resizable: true },
                { key : 'model',    title: '规格/型号', width: 110, resizable: true },
                { key : 'standard', title: '标准/规范', width: 110, resizable: true },
                { key : 'material', title: '材质', width: 110, resizable: true },
                { slot: 'count',    title: '数量', width: 110, align: 'right', resizable: true },
                { slot: 'batch',    title: '批次', width: 110, resizable: true },
                { key : 'desc',     title: '物料描述', minWidth: 150 },
            ],
        };
    },
    mounted() {
        this.restoreTableColumnWidths(this.columns);
        this.searchProductItems();
        this.tableMaxHeight = this.calculateTableMaxHeight();
    },
    methods: {
        // 搜索物料
        searchProductItems() {
            this.items             = [];
            this.more              = false;
            this.reloading         = true;
            this.filter.pageNumber = 1;
            this.filter.count      = parseInt(this.filter.count) || 10000;

            this.fetchMoreProductItems();
        },
        // 点击更多按钮加载下一页的物料
        fetchMoreProductItems() {
            this.loading = true;

            StockDao.findStocks(this.filter).then(items => {
                this.items.push(...items);

                this.more      = items.length >= this.filter.pageSize;
                this.loading   = false;
                this.reloading = false;
                this.filter.pageNumber++;
            });
        },
        itemClass(item) {
            return {
                'text-color-error': item.count <= item.warnCount
            };
        },
        // 导出库存
        exportStocks() {
            this.exporting = true;
            this.exportFile(StockDao.exportStocks(this.filter)).then(() => {
                this.exporting = false;
            });
        },
    },
};
</script>

<style lang="scss">
</style>
