<template>
    <div class="about">
        <BigFileUploader auto @on-success="fileUploaded"/>
    </div>
</template>

<script>
import BigFileUploader from '@/components/BigFileUploader.vue';

export default {
    components: { BigFileUploader },
    methods: {
        fileUploaded(file) {
            this.$Message.success(`上传成功: ${file.fileName}`);
            console.log(file);
        }
    }
};
</script>

<style lang="scss">
</style>
