<!-- eslint-disable vue/no-parsing-error -->
<!--
搜索物料、分页加载 (加载下一页的物料)
-->
<template>
    <div class="product-item-manage list-page">
        <!-- 顶部工具栏 -->
        <div class="list-page-toolbar-top">
            <div style="display: flex">
                <Input v-model="filter.name" placeholder="请输入物料名称" @on-enter="searchProductItems">
                    <span slot="prepend">物料名称</span>
                </Input>
                <Input v-model="filter.code" placeholder="请输入物料编码" search enter-button @on-search="searchProductItems">
                    <span slot="prepend">物料编码</span>
                </Input>
            </div>

            <Button type="primary" icon="md-add" @click="editItem()">添加物料</Button>
        </div>

        <!-- 物料列表 -->
        <Table :data="items" :columns="columns" :loading="reloading" :max-height="tableMaxHeight" border
            @on-column-width-resize="saveTableColumnWidths(arguments)"
        >
            <!-- 库存告警 -->
            <template slot-scope="{ row: item }" slot="warnCount">
                {{ item.warnCount }} {{ item.unit }}
            </template>
            <!-- 操作按钮 -->
            <template slot-scope="{ row: item }" slot="action">
                <Button type="primary" size="small" :disabled="!canEdit(item)" @click="editItem(item)">编辑</Button>
                <Button type="error" size="small" :disabled="!canEdit(item)" @click="deleteItem(item)">删除</Button>
            </template>
        </Table>

        <!-- 底部工具栏 -->
        <div class="list-page-toolbar-bottom">
            <Button v-show="more" :loading="loading" shape="circle" icon="md-boat" @click="fetchMoreProductItems">更多...</Button>
        </div>

        <!-- 编辑物料对话框 -->
        <Modal v-model="modal" title="物料编辑" width="650" :mask-closable="false" class="edit-item-modal" :styles="{ top: '60px'}">
            <Form ref="form" :model="itemClone" :rules="itemRules" :key="itemClone.productItemId" :label-width="90">
                <FormItem label="物料编码:" prop="code">
                    <Input v-model="itemClone.code" placeholder="请输入物料编码"/>
                </FormItem>
                <FormItem label="物料名称:" prop="name">
                    <Input v-model="itemClone.name" placeholder="请输入物料名称"/>
                </FormItem>
                <FormItem label="物料类型:" prop="type">
                    <Input v-model="itemClone.type" placeholder="请输入物料类型"/>
                </FormItem>
                <FormItem label="规格/型号:" prop="model">
                    <Input v-model="itemClone.model" placeholder="请输入规格/型号"/>
                </FormItem>
                <FormItem label="标准/规范:" prop="standard">
                    <Input v-model="itemClone.standard" placeholder="请输入标准/规范"/>
                </FormItem>
                <FormItem label="物料材质:" prop="material">
                    <Input v-model="itemClone.material" placeholder="请输入物料材质"/>
                </FormItem>
                <FormItem label="物料单位:">
                    <Input v-model="itemClone.unit" placeholder="请输入物料单位"/>
                </FormItem>
                <FormItem label="库存告警:">
                    <InputNumber v-model="itemClone.warnCount" :min="1" placeholder="请输入库存告警数量" style="width: 100%"/>
                </FormItem>
                <FormItem label="物料描述:" style="grid-column: 2 span">
                    <Input v-model="itemClone.desc" type="textarea" :rows="4" placeholder="请输入物料描述"/>
                </FormItem>
            </Form>

            <div slot="footer">
                <Button type="text" @click="modal = false">取消</Button>
                <Button type="primary" :loading="saving" @click="saveItem">保存</Button>
            </div>
        </Modal>
    </div>
</template>

<script>
import ProductDao from '@/../public/static-p/js/dao/ProductDao';

export default {
    data() {
        return {
            items: [],
            itemClone: {},
            filter: { // 搜索条件
                name      : '',
                code      : '',
                pageSize  : 50,
                pageNumber: 1,
            },
            more     : false, // 是否还有更多物料
            loading  : false, // 加载中
            reloading: false, // 重新加载中
            modal    : false, // 是否显示编辑对话框
            saving   : false, // 保存中
            tableName: 'product-items-table', // 表名
            tableMaxHeight: 200,
            columns  : [
                // 设置 width, minWidth，当大小不够时 Table 会出现水平滚动条
                { key : 'name',      title: '物料名称', width: 200, resizable: true },
                { key : 'code',      title: '物料编码', width: 110, resizable: true },
                { key : 'type',      title: '物料类型', width: 110, resizable: true },
                { key : 'model',     title: '规格/型号', width: 110, resizable: true },
                { key : 'standard',  title: '标准/规范', width: 110, resizable: true },
                { key : 'material',  title: '材质', width: 110, resizable: true },
                { slot: 'warnCount', title: '库存告警', width: 110, align: 'right', resizable: true },
                { key : 'desc',      title: '物料描述', minWidth: 150 },
                { slot: 'action',    title: '操作', width: 150, align: 'center', className: 'table-action', resizable: true },
            ],
            itemRules: {
                code: [
                    { required: true, whitespace: true, message: '物料编码不能为空', trigger: 'blur' }
                ],
                name: [
                    { required: true, whitespace: true, message: '物料名称不能为空', trigger: 'blur' }
                ],
                type: [
                    { required: true, whitespace: true, message: '物料类型不能为空', trigger: 'blur' }
                ],
                model: [
                    { required: true, whitespace: true, message: '规格/型号不能为空', trigger: 'blur' }
                ],
                standard: [
                    { required: true, whitespace: true, message: '标准/规范不能为空', trigger: 'blur' }
                ],
                material: [
                    { required: true, whitespace: true, message: '物料材质不能为空', trigger: 'blur' }
                ],
            },
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

            this.fetchMoreProductItems();
        },
        // 点击更多按钮加载下一页的物料
        fetchMoreProductItems() {
            this.loading = true;

            ProductDao.findProductItems(this.filter).then(items => {
                this.items.push(...items);

                this.more      = items.length >= this.filter.pageSize;
                this.loading   = false;
                this.reloading = false;
                this.filter.pageNumber++;
            });
        },
        // 编辑物料: item 为 undefined 表示创建，否则表示更新
        editItem(item) {
            // 1. 重置表单，避免上一次的验证信息影响到本次编辑
            // 2. 生成编辑对象的副本
            // 3. 显示编辑对话框

            this.$refs.form.resetFields();

            if (item) {
                // 更新
                this.itemClone = Utils.clone(item); // 重要: 克隆对象
            } else {
                // 创建
                this.itemClone = this.newItem();
            }

            this.modal = true;
        },
        // 保存编辑后的物料
        saveItem() {
            // 1. 表单验证不通过则返回
            // 2. 克隆被编辑对象
            // 3. 找到被编辑对象的下标
            // 4. 保存成功后如果是更新则替换已有对象，创建则添加到最前面
            // 5. 提示保存成功，隐藏编辑对话框

            this.$refs.form.validate(valid => {
                if (!valid) { return; }

                // [2] 克隆被编辑对象
                // [3] 找到被编辑对象的下标
                this.saving = true;
                const item  = Utils.clone(this.itemClone);  // 重要: 克隆被编辑的对象
                const index = this.items.findIndex(i => i.productItemId === item.productItemId); // 物料下标

                ProductDao.upsertProductItem(item).then((newItem) => {
                    // [4] 保存成功后如果是更新则替换已有对象，创建则添加到最前面
                    if (index >= 0) {
                        // 更新: 替换已有对象
                        this.items.replace(index, newItem);
                    } else {
                        // 创建: 添加到最前面
                        this.items.insert(0, newItem);
                    }

                    // [5] 提示保存成功，隐藏编辑对话框
                    this.saving = false;
                    this.modal  = false;
                    this.$Message.success('保存成功');
                }).catch(() => {
                    this.saving = false;
                });
            });
        },
        // 删除物料
        deleteItem(item) {
            // 1. 删除提示
            // 2. 从服务器删除成功后才从本地删除
            // 3. 提示删除成功

            this.$Modal.confirm({
                title: `确定删除 <font color="red">${item.name}</font> 吗?`,
                loading: true,
                onOk: () => {
                    ProductDao.deleteProductItem(item.productItemId).then(() => {
                        const index = this.items.findIndex(i => i.productItemId === item.productItemId); // 物料下标
                        this.items.remove(index); // [2] 从服务器删除成功后才从本地删除
                        this.$Modal.remove();
                        this.$Message.success('删除成功');
                    });
                }
            });
        },
        // 判断是否有编辑权限
        canEdit(item) {
            // Feature: 您帮我把物料管理和库存部分的编辑和删除权限开一下 (质量保证, 或者您给管理员开通也行)
            if (this.hasPermission(['ROLE_PRODUCE_QUALITY', 'ROLE_ADMIN_SYSTEM'])) {
                return true;
            }

            return this.isCurrentUser(item.userId);
        },
        // 新物料
        newItem() {
            return {
                productItemId: '0',
                name: '',
                code: '',
                type: '',
                model: '',
                standard: '',
                material: '',
                desc: '',
                warnCount: 10,
            };
        },
    },
};
</script>

<style lang="scss">
.product-item-manage {
    .list-page-toolbar-top {
        .ivu-input-wrapper {
            width: 250px;
            margin-right: 10px;

            &:last-child {
                width: 300px;
            }
        }
    }
}

.edit-item-modal {
    form {
        display: grid;
        grid-template-columns: 1fr 1fr;
        grid-gap: 0 20px;
    }
}
</style>
