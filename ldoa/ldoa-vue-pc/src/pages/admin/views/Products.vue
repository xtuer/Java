<!-- eslint-disable vue/no-parsing-error -->
<!--
搜索产品、分页加载 (加载下一页的产品)
-->
<template>
    <div class="product-manage list-page">
        <!-- 顶部工具栏 -->
        <div class="list-page-toolbar-top">
            <div style="display: flex">
                <Input v-model="filter.name" placeholder="请输入产品名称" @on-enter="searchProducts">
                    <span slot="prepend">产品名称</span>
                </Input>
                <Input v-model="filter.code" placeholder="请输入产品编码" @on-enter="searchProducts">
                    <span slot="prepend">产品编码</span>
                </Input>
                <Input v-model="filter.model" placeholder="请输入产品规格/型号" search enter-button @on-search="searchProducts">
                    <span slot="prepend">规格/型号</span>
                </Input>
            </div>

            <div>
                <Button type="primary" icon="md-add" class="margin-right-10" @click="editProduct()">添加产品</Button>
                <Button type="info" icon="md-arrow-down" :loading="exporting" @click="exportProducts()">导出产品</Button>
            </div>
        </div>

        <!-- 产品列表 -->
        <Table :data="products" :columns="productColumns" :loading="reloading" :max-height="tableMaxHeight" border
            @on-column-width-resize="saveTableColumnWidths(arguments)"
        >
            <!-- 操作按钮 -->
            <template slot-scope="{ row: product }" slot="action">
                <Button type="primary" size="small" :disabled="!canEdit(product)" @click="editProduct(product)">编辑</Button>
                <Button type="error" size="small" :disabled="!canEdit(product)" @click="deleteProduct(product)">删除</Button>
            </template>
        </Table>

        <!-- 底部工具栏 -->
        <div class="list-page-toolbar-bottom">
            <Button v-show="more" :loading="loading" shape="circle" icon="md-boat" @click="fetchMoreProducts">更多...</Button>
        </div>

        <!-- 编辑产品对话框 -->
        <Modal v-model="modal" :mask-closable="false" title="产品编辑" width="650" class="edit-product-modal" :styles="{ top: '60px' }">
            <Form ref="form" :model="productClone" :rules="productRules" :key="productClone.productId" :label-width="90">
                <FormItem label="产品编码:" prop="code">
                    <Input v-model="productClone.code" placeholder="请输入产品编码"/>
                </FormItem>
                <FormItem label="产品名称:" prop="name">
                    <Input v-model="productClone.name" placeholder="请输入产品名称"/>
                </FormItem>
                <FormItem label="规格/型号:" prop="model">
                    <Input v-model="productClone.model" placeholder="请输入规格/型号"/>
                </FormItem>
               <FormItem label="产品描述:">
                    <Input v-model="productClone.desc" type="textarea" :rows="4" placeholder="请输入产品描述"/>
                </FormItem>
                <FormItem label="子项物料:">
                    <Table :data="productClone.items" :columns="itemColumns" border>
                        <!-- 操作按钮 -->
                        <template slot-scope="{ index }" slot="count">
                            <InputNumber :min="1" :step="1" v-model="productClone.items[index].count" style="width: 60px"></InputNumber>
                        </template>
                        <!-- 操作按钮 -->
                        <template slot-scope="{ index }" slot="action">
                            <div class="column-buttons">
                                <!-- 删除子项物料 -->
                                <Icon type="ios-close-circle" size="18" class="clickable" @click="productClone.items.remove(index)"/>
                            </div>
                        </template>
                    </Table>
                </FormItem>
            </Form>

            <div slot="footer">
                <Button type="dashed" icon="md-add" style="float: left" @click="itemSelect = true">添加子项物料</Button>
                <Button type="text" @click="modal = false">取消</Button>
                <Button type="primary" :loading="saving" @click="saveProduct">保存</Button>
            </div>
        </Modal>

        <!-- 物料选择弹窗 -->
        <ProductItemSelect v-model="itemSelect" @on-ok="addProductItem"/>
    </div>
</template>

<script>
import ProductDao from '@/../public/static-p/js/dao/ProductDao';
import ProductItemSelect from '@/components/ProductItemSelect.vue';
import ProductItemExpand from '@/components/ProductItemExpand.vue';

export default {
    // eslint-disable-next-line vue/no-unused-components
    components: { ProductItemSelect, ProductItemExpand },
    data() {
        return {
            products: [],
            productClone: this.newProduct(),
            filter: { // 搜索条件
                name      : '',
                code      : '',
                model     : '',
                pageSize  : 50,
                pageNumber: 1,
            },
            more      : false, // 是否还有更多产品
            loading   : false, // 加载中
            reloading : false,
            modal     : false, // 是否显示编辑对话框
            saving    : false, // 保存中
            exporting : false, // 导出中
            itemSelect: false, // 物料选择弹窗是否可见
            tableName : 'products-table', // 表名
            tableMaxHeight : 200,
            // 产品的列
            productColumns: [
                {
                    type: 'expand',
                    width: 50,
                    render: (h, params) => {
                        return h(ProductItemExpand, {
                            props: {
                                row: params.row
                            }
                        });
                    }
                },
                { key : 'name',   title: '产品名称', width: 200, resizable: true },
                { key : 'code',   title: '产品编码', width: 130, resizable: true },
                { key : 'model',  title: '规格/型号', width: 130, resizable: true },
                { key : 'desc',   title: '产品描述', minWidth: 150 },
                { slot: 'action', title: '操作', width: 150, align: 'center', className: 'table-action', resizable: true },
            ],
            // 产品校验规则
            productRules: {
                code: [
                    { required: true, whitespace: true, message: '产品编码不能为空', trigger: 'blur' }
                ],
                name: [
                    { required: true, whitespace: true, message: '产品名称不能为空', trigger: 'blur' }
                ],
                model: [
                    { required: true, whitespace: true, message: '规格/型号不能为空', trigger: 'blur' }
                ],
            },
            // 子项物料的列
            itemColumns: [
                { key : 'name',   title: '物料名称' },
                { key : 'code',   title: '物料编码', width: 130 },
                { slot: 'count',  title: '数量', width: 100, align: 'center' },
                { slot: 'action', title: '操作', width: 70, align: 'center' },
            ],
        };
    },
    mounted() {
        this.restoreTableColumnWidths(this.productColumns);
        this.searchProducts();
        this.tableMaxHeight = this.calculateTableMaxHeight();
    },
    methods: {
        // 搜索产品
        searchProducts() {
            this.products          = [];
            this.more              = false;
            this.reloading         = true;
            this.filter.pageNumber = 1;

            this.fetchMoreProducts();
        },
        // 点击更多按钮加载下一页的产品
        fetchMoreProducts() {
            this.loading = true;

            ProductDao.findProducts(this.filter).then(products => {
                this.products.push(...products);

                this.more      = products.length >= this.filter.pageSize;
                this.loading   = false;
                this.reloading = false;
                this.filter.pageNumber++;
            });
        },
        // 编辑产品: product 为 undefined 表示创建，否则表示更新
        editProduct(product) {
            // 1. 重置表单，避免上一次的验证信息影响到本次编辑
            // 2. 生成编辑对象的副本
            // 3. 显示编辑对话框

            this.$refs.form.resetFields();

            if (product) {
                // 更新
                this.productClone = Utils.clone(product); // 重要: 克隆对象
            } else {
                // 创建
                this.productClone = this.newProduct();
            }

            this.modal = true;
        },
        // 保存编辑后的产品
        saveProduct() {
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
                const product = Utils.clone(this.productClone);  // 重要: 克隆被编辑的对象
                const index = this.products.findIndex(p => p.productId === product.productId); // 产品下标

                ProductDao.upsertProduct(product).then((newProduct) => {
                    // [4] 保存成功后如果是更新则替换已有对象，创建则添加到最前面
                    if (index >= 0) {
                        // 更新: 替换已有对象
                        this.products.replace(index, newProduct);
                    } else {
                        // 创建: 添加到最前面
                        this.products.insert(0, newProduct);
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
        // 删除产品
        deleteProduct(product) {
            // 1. 删除提示
            // 2. 从服务器删除成功后才从本地删除
            // 3. 提示删除成功

            this.$Modal.confirm({
                title: `确定删除 <font color="red">${product.name}</font> 吗?`,
                loading: true,
                onOk: () => {
                    ProductDao.deleteProduct(product.productId).then(() => {
                        const index = this.products.findIndex(p => p.productId === product.productId); // 产品下标
                        this.products.remove(index); // [2] 从服务器删除成功后才从本地删除
                        this.$Modal.remove();
                        this.$Message.success('删除成功');
                    });
                }
            });
        },
        // 添加产品项
        addProductItem(productItem) {
            // 如果物料已经存在，不需要重复添加
            const found = this.productClone.items.find(item => item.productItemId === productItem.productItemId);

            if (found) {
                this.$Message.warning('子项物料已经存在');
            } else {
                this.productClone.items.push(productItem);
            }
        },
        // 导出产品
        exportProducts() {
            this.exporting = true;
            this.exportFile(ProductDao.exportProducts(this.filter)).then(() => {
                this.exporting = false;
            });
        },
        // 判断是否有编辑权限
        canEdit(product) {
            return this.isCurrentUser(product.userId);
        },
        // 新产品
        newProduct() {
            return {
                productId: '0',
                name : '',
                code : '',
                model: '',
                desc : '',
                items: [],
            };
        }
    }
};
</script>

<style lang="scss">
.product-manage {
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

.edit-product-modal {
    .ivu-table-header thead tr th {
        padding: 0 !important;
    }
}
</style>
