<!-- eslint-disable vue/no-parsing-error -->

<!--
搜索客户、分页加载 (加载下一页的客户)
-->
<template>
    <div class="customers list-page">
        <!-- 顶部工具栏 -->
        <div class="list-page-toolbar-top">
            <!-- 搜索条件 -->
            <div class="filter">
                <!-- 指定条件的搜索 -->
                <Input v-model="filter.name" placeholder="请输入查询条件" @on-enter="searchCustomers">
                    <span slot="prepend">客户名称</span>
                </Input>

                <!-- 选择条件的搜索 -->
                <Input v-model="filterValue" transfer placeholder="请输入查询条件" search enter-button @on-search="searchCustomers">
                    <Select v-model="filterKey" slot="prepend">
                        <Option value="customerSn">编号</Option>
                        <Option value="business">行业</Option>
                        <Option value="region">区域</Option>
                    </Select>
                </Input>
            </div>

            <!-- 其他按钮 -->
            <div>
                <Button type="default" icon="md-add" @click="editCustomer()">添加客户</Button>
                <FileUpload class="margin-left-10 margin-right-10" excel @on-success="importCustomers">导入客户</FileUpload>
                <Button type="info" icon="md-arrow-down" :loading="exporting" @click="exportCustomers()">导出客户</Button>
            </div>
        </div>

        <!-- 客户列表 -->
        <Table :data="customers" :columns="columns" :loading="reloading" :max-height="tableMaxHeight" border @on-column-width-resize="saveTableColumnWidths(arguments)">
            <!-- 客户名称 -->
            <template slot-scope="{ row: customer }" slot="name">
                <a @click="showCustomerDetails(customer)">{{ customer.name }}</a>
            </template>

            <!-- 联系人 -->
            <template slot-scope="{ row: customer }" slot="contacts">
                <!-- <span v-html="contactsToString(customer.contacts)"></span> -->
                <Dropdown transfer>
                    <a href="javascript:void(0)">
                        <Icon type="ios-people" size="20"/>
                        <Icon type="ios-arrow-down"/>
                    </a>
                    <DropdownMenu slot="list">
                        <DropdownItem v-for="(c, i) in customer.contacts" :key="i">
                            {{ c.name }} - {{ c.department }} - {{ c.phone }}
                        </DropdownItem>
                        <DropdownItem v-if="customer.contacts.length === 0">无</DropdownItem>
                    </DropdownMenu>
                </Dropdown>
            </template>

            <!-- 操作按钮 -->
            <template slot-scope="{ row: customer }" slot="action">
                <a @click="editCustomer(customer)">编辑</a>
                <a class="delete" @click="deleteCustomer(customer)">删除</a>
            </template>
        </Table>

        <!-- 底部工具栏 -->
        <div class="list-page-toolbar-bottom">
            <Button v-show="more" :loading="loading" shape="circle" icon="md-boat" @click="fetchMoreCustomers">更多...</Button>
        </div>

        <!-- 客户编辑弹窗 -->
        <CustomerEdit v-model="editCustomerModal" :customer-id="editCustomerId" @on-ok="customerSaved"/>

        <!-- 客户详情弹窗 -->
        <CustomerDetails v-model="customerDetailsModal" :customer-id="customerIdForDetails"/>
    </div>
</template>

<script>
import CustomerDao from '@/../public/static-p/js/dao/CustomerDao';
import FileUpload from '@/components/FileUpload.vue';
import CustomerEdit from '@/components/CustomerEdit.vue';
import CustomerDetails from '@/components/CustomerDetails.vue';

export default {
    components: { FileUpload, CustomerEdit, CustomerDetails },
    data() {
        return {
            customers : [],
            filter: this.newFilter(),
            filterKey  : 'customerSn', // 搜索的 Key
            filterValue: '',           // 搜索的 Value
            more     : false, // 是否还有更多客户
            loading  : false, // 加载中
            reloading: false,
            exporting: false, // 导出中
            tableName: 'customers-table',
            tableMaxHeight: 200,
            columns  : [
                // 设置 width, minWidth，当大小不够时 Table 会出现水平滚动条
                { key : 'customerSn', title: '客户编号', width: 150, resizable: true },
                { slot: 'name',       title: '客户名称', width: 150, resizable: true },
                { key : 'business',   title: '行业', width: 150, resizable: true },
                { key : 'region',     title: '区域', width: 150, resizable: true },
                { key : 'phone',      title: '电话', width: 150, resizable: true },
                // { key : 'address',    title: '地址', width: 150, resizable: true },
                // { key : 'owner',      title: '负责人', width: 150, resizable: true },
                // { key : 'remark',     title: '备注', width: 150, resizable: true },
                { slot: 'contacts',   title: '联系人', width: 80, resizable: false },
                { slot: 'action',     title: '操作', width: 110, align: 'center', className: 'table-action' },
            ],
            editCustomerId   : '0',
            editCustomerModal: false,
            customerIdForDetails: '0',   // 客户 ID，用于显示客户详情
            customerDetailsModal: false, // 客户详情弹窗是否可见
        };
    },
    mounted() {
        this.restoreTableColumnWidths(this.columns);
        this.searchCustomers();
        this.tableMaxHeight = this.calculateTableMaxHeight();
    },
    methods: {
        // 搜索客户
        searchCustomers() {
            this.customers              = [];
            this.more                   = false;
            this.reloading              = true;
            this.filter                 = { ...this.newFilter(), name: this.filter.name };
            this.filter[this.filterKey] = this.filterValue;

            this.fetchMoreCustomers();
        },
        // 点击更多按钮加载下一页的客户
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
        // 导出客户
        exportCustomers() {
            this.exporting = true;
            this.exportFile(CustomerDao.exportCustomers(this.filter)).then(() => {
                this.exporting = false;
            });
        },
        // 导入客户
        importCustomers(file) {
            CustomerDao.importCustomers(file.url).then(() => {
                this.$Message.success('导入成功');
                this.searchCustomers();
            });
        },
        // 编辑客户: customer 为 undefined 表示创建，否则表示更新
        editCustomer(customer) {
            // 1. 重置表单，避免上一次的验证信息影响到本次编辑
            // 2. 生成编辑对象的副本
            // 3. 显示编辑对话框

            if (customer) {
                // 更新
                this.editCustomerId = customer.customerId;
            } else {
                // 创建
                this.editCustomerId = '0';
            }

            this.editCustomerModal = true;
        },
        // 客户保存成功
        customerSaved(customer) {
            const index = this.customers.findIndex(c => c.customerId === customer.customerId);

            if (index >= 0) {
                this.customers.replace(index, customer);
            } else {
                this.customers.insert(0, customer);
            }
        },
        // 删除客户
        deleteCustomer(customer) {
            // 1. 删除提示
            // 2. 从服务器删除成功后才从本地删除
            // 3. 提示删除成功

            this.$Modal.confirm({
                title: `确定删除客户 <font color="red">${customer.name}</font> 吗?`,
                loading: true,
                onOk: () => {
                    CustomerDao.deleteCustomer(customer.customerId).then(() => {
                        const index = this.customers.findIndex(c => c.customerId === customer.customerId); // 客户下标
                        this.customers.splice(index, 1); // [2] 从服务器删除成功后才从本地删除
                        this.$Modal.remove();
                        this.$Message.success('删除成功');
                    });
                }
            });
        },
        // 客户联系人转为字符串
        contactsToString(contacts) {
            let text = '';
            for (let c of contacts) {
                text += `<div class="customer-contact">${c.name}-${c.department}-${c.phone}</div>`;
            }

            return text;
        },
        // 显示客户详情弹窗
        showCustomerDetails(customer) {
            this.customerIdForDetails = customer.customerId;
            this.customerDetailsModal = true;
        },
        // 新建搜索条件
        newFilter() {
            return {
                // customerSn : '',
                // business: '',
                // regin: '',
                name      : '',
                pageSize  : 50,
                pageNumber: 1,
            };
        },
    }
};
</script>

<style lang="scss">
.customers {
    .customer-contact {
        color: $iconColor;
        border: 1px solid $borderColor;
        border-radius: 4px;

        &:not(:first-child) {
            margin-top: 5px;
        }
    }
}
</style>
