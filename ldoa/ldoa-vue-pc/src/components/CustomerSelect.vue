<!-- eslint-disable vue/no-parsing-error -->
<!--
功能: 客户择弹窗

属性:
visible: 是否可见，可使用 v-model 双向绑定

事件:
on-ok: 点击确定时触发，参数为选择的客户
on-visible-change: 显示或隐藏时触发，显示时参数为 true，隐藏时为 false

案例:
<CustomerSelect v-model="visible"/>
-->

<template>
    <Modal :value="visible" title="客户选择" :mask-closable="false" transfer width="700" class="customer-select-modal" @on-visible-change="showEvent">
        <!-- 弹窗 Body -->
        <div class="list-page">
            <!-- 搜索条件 -->
            <div class="list-page-toolbar-top">
                <span></span>
                <Input v-model="filter.name" placeholder="请输入客户名称" search enter-button @on-search="searchCustomers">
                    <span slot="prepend">客户名称</span>
                </Input>
                <!-- <Input v-model="filter.customerSn" placeholder="请输入客户编号" search enter-button @on-search="searchCustomers">
                    <span slot="prepend">客户编号</span>
                </Input> -->
            </div>

            <!-- 物料列表 -->
            <Table :data="customers" :columns="columns" :loading="reloading" border>
                <!-- 操作按钮 -->
                <template slot-scope="{ row: customer, index }" slot="action">
                    <Checkbox :value="customer.customerId === customerSelected.customerId" @on-change="selectItem(index, $event)"></Checkbox>
                </template>
            </Table>
        </div>

        <!-- 底部工具栏 -->
        <div slot="footer">
            <Button v-show="more" :loading="loading" icon="md-boat" style="float: left" @click="fetchMoreCustomers">更多...</Button>
            <Button type="text" @click="showEvent(false)">取消</Button>
            <Button type="primary" @click="ok">确定</Button>
        </div>
    </Modal>
</template>

<script>
import CustomerDao from '@/../public/static-p/js/dao/CustomerDao';

export default {
    props: {
        visible: { type: Boolean, required: true }, // 是否可见
    },
    model: {
        prop : 'visible',
        event: 'on-visible-change',
    },
    data() {
        return {
            customers: [],
            customerSelected: {}, // 选中的客户
            filter: this.newFilter(),
            more     : false, // 是否还有更多客户
            loading  : false, // 加载中
            reloading: false, // 重新加载中
            columns  : [
                // 设置 width, minWidth，当大小不够时 Table 会出现水平滚动条
                { slot: 'action', title: '选择', width: 70, align: 'center' },
                { key : 'name',   title: '客户名称', minWidth: 200 },
                { key : 'customerSn', title: '客户编号', width: 130 },
                { key : 'business',   title: '行业', width: 130 },
                { key : 'region',   title: '区域', width: 130 },
            ],
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
        // 点击确定按钮的回调函数
        ok() {
            if (!this.customerSelected.customerId) {
                this.$Message.warning('请选择客户');
                return;
            }

            this.$emit('on-ok', this.customerSelected);
            this.showEvent(false); // 关闭弹窗
        },
        // 初始化
        init() {
            this.customerSelected = {};
            this.searchCustomers();
        },
        // 搜索物料
        searchCustomers() {
            this.customers = [];
            this.more      = false;
            this.reloading = true;
            this.filter    = { ...this.newFilter(), name: this.filter.name, customerSn: this.filter.customerSn };

            this.fetchMoreCustomers();
        },
        // 点击更多按钮加载下一页的物料
        fetchMoreCustomers() {
            this.loading = true;

            CustomerDao.findCustomers(this.filter).then(customers => {
                this.customers.push(...customers);

                this.more      = customers.length >= this.filter.pageSize;
                this.loading   = false;
                this.reloading = false;
                this.filter.pageNumber++;
            });
        },
        // 选择物料
        selectItem(index, selected) {
            if (selected) {
                this.customerSelected = this.customers[index];
            } else {
                this.customerSelected = {};
            }
        },
        // 搜索条件
        newFilter() {
            return {
                name      : '',
                customerSn: '',
                pageSize  : 10,
                pageNumber: 1,
            };
        }
    }
};
</script>

<style lang="scss">
.customer-select-modal {
    .list-page-toolbar-top {
        grid-template-columns: 1fr 1fr;
        grid-gap: 10px;
    }

    .ivu-checkbox-wrapper {
        margin-right: 0;
    }
}
</style>
