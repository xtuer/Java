<!--
说明: 上传大 SQL 文件，然后导入 SQL 文件到数据库。
-->

<template>
    <div class="sql-file-import">
        <BigFileUploader auto @on-success="fileUploaded"/>

        <br><br>
        <div>上传文件的 ID: {{ fileUid }}</div>
        <Input v-model="rollbackSql" placeholder="导入错误时执行的回滚 SQL 语句, 可为空"/>

        <br><br>
        <Button :disabled="!importButtonEnabled" :loading="importing" type="primary" @click="importSqlFile">导入上传的 SQL 文件</Button>

        <div>导入进度: {{ importProcess.percent }}%, {{ importProcess.committedBytes }} / {{ importProcess.totalBytes }} Bytes</div>

        <!-- 导入错误原因 -->
        <pre v-if="importProcess.error" class="import-error">{{ importProcess.error }}</pre>
    </div>
</template>

<script>
import BigFileUploader from '@/components/BigFileUploader.vue';
import ImportSqlFileApi from './SqlFileImportApi';

export default {
    components: { BigFileUploader },
    data() {
        return {
            fileUid      : '',                                                            // 要导入的 SQL 文件的 ID。
            importing    : false,                                                         // 是否导入中。
            rollbackSql  : 'truncate table test_performance',                             // 导入错误时执行的回滚 SQL 语句。
            importTaskId : '',                                                            // 导入任务 ID，需要用来查询导入状态。
            timerId      : '',                                                            // 轮询状态的定时器 ID。
            importProcess: { percent: 0, totalBytes: 0, committedBytes: 0, error: '' },   // 导入进度。
        };
    },
    methods: {
        // 文件上传成功的回调函数。
        fileUploaded(file) {
            this.$Message.success(`上传成功: ${file.fileName}`);
            this.fileUid = file.fileUid;
        },
        // 导入 SQL 文件。
        importSqlFile() {
            /*
             逻辑:
             1. 请求导入 SQL 文件。
             2. 轮询 SQL 导入进度直到导入完成 (成功或者失败)。
                导入状态 0 (初始化)、1 (成功)、2 (失败)、3 (导入中)。
             */
            this.importing = true;
            ImportSqlFileApi.importSqlFile(this.fileUid, this.rollbackSql).then(task => {
                this.importTaskId = task.taskId;
                this.pollingImportState(); // 请求导入成功后，开始轮询导入状态，更新导入进度。
            }).catch(err => {
                this.importing = false;
                this.$Message.error('请求导入错误');
            });
        },
        // 轮询导入状态。
        pollingImportState() {
            this.timer = setInterval(() => {
                ImportSqlFileApi.findImportTask(this.importTaskId, false).then(task => {
                    // 导入进度处理。
                    let percent = 0;
                    const totalBytes = task.totalBytes;
                    const committedBytes = task.committedBytes;

                    // 计算百分比。
                    if (totalBytes > 0) {
                        percent = committedBytes / totalBytes * 100;
                        percent = percent.toFixed(2);
                    }

                    this.importProcess.totalBytes = totalBytes;
                    this.importProcess.committedBytes = committedBytes;
                    this.importProcess.percent = percent;
                    this.importProcess.error = task.error;

                    if (task.state === 1) {
                        // 导入成功。
                        this.importing = false;
                        this.$Message.success('导入成功');
                        clearInterval(this.timer);
                    } else if (task.state === 2) {
                        // 导入失败。
                        this.importing = false;
                        this.$Message.error('导入失败');
                        clearInterval(this.timer);
                    }

                    // 其他状态继续轮询。
                }).catch(err => {
                    console.log(err);
                });
            }, 2000);
        },
    },
    // 组件销毁时清除定时器。
    beforeDestroy() {
        clearInterval(this.timer);
    },
    computed: {
        // 上传文件成功后导入按钮才可用。
        importButtonEnabled() {
            return this.fileUid;
        }
    }
};
</script>

<style lang="scss">
.sql-file-import {
    .import-error {
        color: red;
    }
}
</style>
