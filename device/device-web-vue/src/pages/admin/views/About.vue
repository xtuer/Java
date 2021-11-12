<template>
    <div class="about">
        <Button @click="findActiveGateways">连接到服务器的设备网关</Button> {{ gateways }}

        <Input class="message" v-model="message" type="textarea" :autosize="{minRows: 4,maxRows: 5}" placeholder="请输 JSON 格式的消息..." />
        <div class="message-buttons">
            <Button @click="createMessage('STATUS_DOWN')">下行状态请求消息</Button>
            <Button @click="createMessage('GATEWAY_RESET_DOWN')">下行复位网关消息</Button>
            <Button @click="createMessage('GATEWAY_VERSION_DOWN')">下行获取网关版本消息</Button>
            <Button @click="createMessage('DEVICE_SEARCH_DOWN')">下行设备入网搜索请求消息</Button>
            <Button @click="createMessage('DEVICE_RESET_DOWN')">下行复位设备消息</Button>
            <Button @click="createMessage('HEARTBEAT_DOWN')">下行心跳消息</Button>

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
        // 创建消息
        createMessage(type) {
            if ('STATUS_DOWN' === type) { // 创建设备状态请求消息
                this.message = '{"gatewayId": "gw-1", "deviceId": "", "type": "STATUS_DOWN", "address": 123}';
            } else if ('GATEWAY_RESET_DOWN' === type) {
                this.message = '{"gatewayId": "gw-1", "deviceId": "", "type": "GATEWAY_RESET_DOWN"}';
            } else if ('GATEWAY_VERSION_DOWN' === type) {
                this.message = '{"gatewayId": "gw-1", "deviceId": "", "type": "GATEWAY_VERSION_DOWN"}';
            } else if ('DEVICE_SEARCH_DOWN' === type) {
                this.message = '{"gatewayId": "gw-1", "deviceId": "", "type": "DEVICE_SEARCH_DOWN"}';
            } else if ('DEVICE_RESET_DOWN' === type) {
                this.message = '{"gatewayId": "gw-1", "deviceId": "", "type": "DEVICE_RESET_DOWN"}';
            } else if ('HEARTBEAT_DOWN' === type) {
                this.message = '{"gatewayId": "gw-1", "deviceId": "", "type": "HEARTBEAT_DOWN", "address": 456}';
            }
        },
        // 给网关发送消息
        sendMessage() {
            try {
                const msg = JSON.parse(this.message);
                this.loading = true;
                GatewayDao.sendMessageToGateway(msg).then(() => {
                    this.$Message.success('发送成功');
                    this.loading = false;
                }).catch(() => {
                    this.loading = false;
                });
            } catch (err) {
                this.$Message.error('消息的 JSON 格式错误: ' + err.toString());
            }
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

        button:not(:first-child) {
            margin-left: 10px;
        }
    }
}
</style>
