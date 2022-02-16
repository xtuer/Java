<!-- eslint-disable vue/no-parsing-error -->
<!--
功能: 产品选择弹窗

属性:
visible: 是否可见，可使用 v-model 双向绑定

事件:
on-ok: 点击确定时触发，参数为选择的产品
on-visible-change: 显示或隐藏时触发，显示时参数为 true，隐藏时为 false

案例:
<ProductSelect v-model="visible"/>
-->

<template>
    <Modal :value="visible" title="产品选择" :mask-closable="false" transfer width="700" class="product-select-modal"
           :styles="{ top: '40px', marginBottom: '80px' }" @on-visible-change="showEvent">
        <!-- 内容显示 -->
        <Scroll>
        <div class="list-page">
            <div class="list-page-toolbar-top">
                <Input v-model="filter.name" placeholder="请输入产品名称" @on-enter="searchProducts">
                    <span slot="prepend">产品名称</span>
                </Input>
                <Input v-model="filter.code" placeholder="请输入产品编码" search enter-button @on-search="searchProducts">
                    <span slot="prepend">产品编码</span>
                </Input>
            </div>

            <!-- 产品列表 -->
            <Table :data="products" :columns="columns" :loading="reloading" border>
                <!-- 操作按钮 -->
                <template slot-scope="{ row: product, index }" slot="action">
                    <Checkbox :value="product.productId === productSelected.productId" @on-change="selectProduct(index, $event)"></Checkbox>
                </template>
            </Table>
        </div>
        </Scroll>

        <!-- 底部工具栏 -->
        <div slot="footer">
            <Button v-show="more" :loading="loading" icon="md-boat" style="float: left" @click="fetchMoreProducts">更多...</Button>
            <Button type="text" @click="showEvent(false)">取消</Button>
            <Button type="primary" @click="ok">确定</Button>
        </div>
    </Modal>
</template>

<script>
import ProductDao from '@/../public/static-p/js/dao/ProductDao';
import ProductItemExpand from '@/components/ProductItemExpand.vue';

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
            products: [],
            productSelected: {}, // 选中的 item
            filter: { // 搜索条件
                name      : '',
                code      : '',
                pageSize  : 10,
                pageNumber: 1,
            },
            more     : false, // 是否还有更多物料
            loading  : false, // 加载中
            reloading: false, // 重新加载中
            columns  : [
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
                // 设置 width, minWidth，当大小不够时 Table 会出现水平滚动条
                { slot: 'action', title: '选择', width: 70, align: 'center' },
                { key : 'name',   title: '产品名称' },
                { key : 'code',   title: '产品编码', width: 130 },
                // { key : 'type',   title: '物料类型', width: 130 },
                { key : 'model',  title: '规格/型号', width: 130 },
            ],
        };
    },
    methods: {
        // 显示隐藏事件
        showEvent(visible) {
            this.$emit('on-visible-change', visible);

            // 例如 visible 时重新加载
            this.searchProducts();
            this.productSelected = {};
        },
        ok() {
            if (!this.productSelected.productId) {
                this.$Message.warning('请选择产品');
                return;
            }

            // 去掉 productSelected 中 productItemId 为 '0' 的项
            this.productSelected.items = this.productSelected.items
                .filter(i => i.productItemId !== '0')
                .map(i => {
                    i.stockOutCount = 1;
                    return i;
                });

            this.$emit('on-ok', this.productSelected);
            this.showEvent(false); // 关闭弹窗
        },
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
        // 选择产品
        selectProduct(index, selected) {
            if (selected) {
                this.productSelected = this.products[index];
            } else {
                this.productSelected = {};
            }
        }
    }
};
</script>

<style lang="scss">
.product-select-modal {
    .list-page-toolbar-top {
        grid-template-columns: 1fr 1fr;
        grid-gap: 10px;
    }

    .ivu-checkbox-wrapper {
        margin-right: 0;
    }
}
</style>
