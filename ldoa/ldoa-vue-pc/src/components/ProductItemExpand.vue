<!--
功能: 表格中展开产品项的组件

属性:
product: 产品项
-->
<template>
    <div class="product-item-expand">
        <div><b>物料名称</b></div>
        <div><b>物料编码</b></div>
        <div><b>规格/型号</b></div>
        <div><b>物料材质</b></div>
        <div><b>标准/规范</b></div>
        <div><b>库存数量</b></div>

        <template v-for="item in product.items">
            <div :key="item.productItemId + '1'">{{ item.name }}</div>
            <div :key="item.productItemId + '2'">{{ item.code }}</div>
            <div :key="item.productItemId + '3'">{{ item.model }}</div>
            <div :key="item.productItemId + '4'">{{ item.material }}</div>
            <div :key="item.productItemId + '5'">{{ item.standard }}</div>
            <div :key="item.productItemId + '6'">{{ item.count }} {{ item.unit }}</div>
        </template>
    </div>
</template>

<script>
export default {
    props: {
        row: { type: Object, required: true }, // 产品
    },
    data() {
        return {
            product: this.row
        };
    }
};
</script>

<style lang="scss">
.product-item-expand {
    display: grid;
    grid-template-columns: repeat(6, minmax(100px, 300px));
    grid-gap: 10px;
}
</style>
