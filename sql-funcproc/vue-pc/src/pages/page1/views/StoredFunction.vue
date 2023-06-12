<template>
    <div class="stored-function">
        <List header="存储函数" border>
            <ListItem>This is a piece of text.</ListItem>
            <ListItem>This is a piece of text.</ListItem>
            <ListItem>This is a piece of text.</ListItem>
        </List>

        <div>
            签名: xx
        </div>
    </div>
</template>

<script>
export default {
    data() {
        return {};
    },
    mounted() {},
    methods: {}
};
</script>

<style lang="scss">
.stored-function {
    display: grid;
    grid-template-columns: 200px 1fr;
    grid-column-gap: 10px;
}
</style>
