<!--
功能: 大文件上传组件，支持并发、分片、断点续传、上传时取消。

属性:
auto: 是否选择文件后自动上传。

事件:
on-success: 上传成功时触发，参数为 { fileName, fileUid }

案例:
<BigFileUploader/>
<BigFileUploader auto @on-success="showMessage"/>
-->
<template>
    <div class="big-file-uploader">
        <div class="file-chooser">
            <!-- MD5 计算中、上传中不可点击 -->
            <input ref="file" type="file" :disabled="fileInputDisabled">

            <!-- 进度条: style="width: 30%" 设置进度 -->
            <div :class="{ active: state === 3 || state === 4 }" class="progress">
                <div :style="{ width: progress+'%' }" :class="progressBarClass" class="progress-bar progress-bar-striped"></div>
            </div>

            <!-- 文件名 -->
            <div class="file-name">{{ fileName }}</div>
        </div>
        <Icon :type="actionButton.icon" :color="actionButton.color" size="16" class="action-button" @click="doAction"/>
    </div>
</template>

<script>
import BigFileUploader from './BigFileUploader';

export default {
    mixins: [BigFileUploader]
};
</script>

<style lang="scss">
.big-file-uploader {
    display: inline-flex;
    overflow: hidden;
    height: 30px;
    border: 1px solid rgb(220, 222, 226);
    border-radius: 4px;
    align-items: center;
    padding: 0;

    &:hover {
        border-color: #57a3f3;
        transition: border-color .6s;
    }

    .file-chooser {
        position: relative;
        flex: 1;
        height: 100%;
        min-width: 100px;
        display: flex;
        align-items: center;
        border-right: 1px solid #eee;

        input, .progress {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
        }

        input {
            background: orange;
            opacity: 0.0; /* 设置为 0.6 就明白原理了 */
            z-index: 3;
        }

        .progress {
            z-index: 1;
        }

        .file-name {
            color: rgb(81, 90, 110);
            padding: 2px 6px;
            z-index: 2;
        }
    }

    .action-button {
        margin: 0 4px;
        cursor: pointer;

        &:hover {
            color: #2d8cf0;
        }
    }
}

/* 动态进度条 */
.big-file-uploader {
    .progress {
        background-color: #fff;
        box-shadow: none;
        overflow: hidden;
    }

    .progress-bar {
        background-color: #2196F3;
        box-shadow: none;
        text-align: left;
        display: flex;
        align-items: center;
        height: 100%;

        span {
            margin-left: 10px;
            white-space: nowrap;
        }

        /* 进度条颜色 */
        &.progress-bar-default {
            background-color: #B0BEC5;
        }
        &.progress-bar-primary {
            background-color: #2196F3;
        }
        &.progress-bar-secondary {
            background-color: #323a45;
        }
        &.progress-bar-success {
            background-color: #64DD17;
        }
        &.progress-bar-warning {
            background-color: #FFD600;
        }
        &.progress-bar-info {
            background-color: #29B6F6;
        }
        &.progress-bar-danger {
            background-color: #ef1c1c;
        }

        /* 条形样式 */
        &.progress-bar-striped {
            background-size: 40px 40px;
            background-image: linear-gradient(45deg,
                rgba(255,255,255,.15) 25%,
                transparent 25%,
                transparent 50%,
                rgba(255,255,255,.15) 50%,
                rgba(255,255,255,.15) 75%,
                transparent 75%, transparent);
        }
    }

    /* 产生 stripe 动画 */
    .progress.active .progress-bar {
        animation: progress-bar-stripes 2s linear infinite;
    }
    @keyframes progress-bar-stripes {
        from { background-position: 40px 0; }
        to   { background-position: 0 0; }
    }
}
</style>
