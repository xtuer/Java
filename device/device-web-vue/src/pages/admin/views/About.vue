<template>
    <div class="about">
        <Button @click="findActiveGateways">连接到服务器的设备网关</Button> {{ gateways }}

        <Input class="message" v-model="message" type="textarea" :autosize="{minRows: 4,maxRows: 5}" placeholder="请输 JSON 格式的消息..." />
        <div class="message-buttons">
            <Button @click="createHeatbeatDownMessage">下行心跳消息</Button>
            <div class="stretch"></div>
            <Button type="primary" :loading="loading" @click="sendMessage">发送消息</Button>
        </div>
    </div>
</template>

<script>
import GatewayDao from '@/../public/static-p/js/dao/GatewayDao';

export default {
    data() {
        return {
            gateways: [], // 设备网关
            message: '',
            loading: false,
        };
    },
    mounted() {
        this.findActiveGateways();
    },
    methods: {
        // 获取连接到服务器的设备网关
        findActiveGateways() {
            GatewayDao.findActiveGateways().then(gateways => {
                this.gateways = gateways;
            });
        },
        // 创建下行心跳消息
        createHeatbeatDownMessage() {
            this.message = '{"gatewayId": "gw-1", "deviceId": "", "type": "HEARTBEAT_DOWN", "address": 123}';
        },
        // 给网关发送消息
        sendMessage() {
            this.loading = true;
            GatewayDao.sendMessageToGateway(this.message).then(() => {
                this.$Message.success('发送成功');
                this.loading = false;
            }).catch(() => {
                this.loading = false;
            });
        }
    }
};
</script>

<style lang="scss">
.about {
    .message {
        margin: 20px 0 10px 0;
    }

    .message-buttons {
        display: flex;
    }
}
</style>
