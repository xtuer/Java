<!-- eslint-disable vue/no-parsing-error -->

<!--
搜索消息、分页加载 (加载下一页的消息)
-->
<template>
    <div class="messages list-page">
        <!-- 顶部工具栏 -->
        <div class="list-page-toolbar-top">
        </div>

        <!-- 消息列表 -->
        <Table :data="messages" :columns="columns" :loading="reloading" border>
            <!-- 消息类型 -->
            <template slot-scope="{ row: message }" slot="type">
                {{ message.type | labelForValue(window.MESSAGE_TYPES) }}
            </template>

            <!-- 消息内容 -->
            <template slot-scope="{ row: message }" slot="content">
                <a @click="showMessageTargetDetails(message)">{{ message.content }}</a>
                <Button v-if="!message.read" type="error" size="small" class="margin-left-10" @click="markAsRead(message)">标记为已读</Button>
            </template>

            <!-- 创建时间 -->
            <template slot-scope="{ row: message }" slot="createdAt">
                {{ message.createdAt | formatDate }}
            </template>
        </Table>

        <!-- 底部工具栏 -->
        <div class="list-page-toolbar-bottom">
            <Button v-show="more" :loading="loading" shape="circle" icon="md-boat" @click="fetchMoreMessages">更多...</Button>
        </div>

        <!-- 订单详情弹窗 -->
        <OrderDetails v-model="orderModal" :order-id="orderId"/>
    </div>
</template>

<script>
import MessageDao from '@/../public/static-p/js/dao/MessageDao';
import OrderDetails from '@/components/OrderDetails.vue';

export default {
    components: { OrderDetails, },
    data() {
        return {
            messages  : [],
            filter    : this.newFilter(), // 搜索条件
            more      : false, // 是否还有更多消息
            loading   : false, // 加载中
            reloading : false,
            columns   : [
                // 设置 width, minWidth，当大小不够时 Table 会出现水平滚动条
                { slot: 'type', title: '消息类型', width: 150 },
                { slot: 'content', title: '消息内容' },
                { slot: 'createdAt', title: '创建时间', width: 150 },
            ],
            orderId: '0',
            orderModal: false,
        };
    },
    mounted() {
        this.searchMessages();
    },
    methods: {
        // 搜索消息
        searchMessages() {
            this.messages  = [];
            this.more      = false;
            this.reloading = true;
            this.filter    = { ...this.newFilter() };

            this.fetchMoreMessages();
        },
        // 点击更多按钮加载下一页的消息
        fetchMoreMessages() {
            this.loading = true;

            MessageDao.findMessagesByReceiverId(this.filter).then(messages => {
                this.messages.push(...messages);

                this.more      = messages.length >= this.filter.pageSize;
                this.loading   = false;
                this.reloading = false;
                this.filter.pageNumber++;
            });
        },
        // 标记消息为已读
        markAsRead(message) {
            this.$Modal.confirm({
                title: '确定标记消息为 <font color="red">已读</font> 吗?',
                loading: true,
                onOk: () => {
                    MessageDao.markMessageRead(message.messageId).then(() => {
                        message.read = true;
                        this.$Message.success('审批完成');
                        this.$Modal.remove();
                    });
                }
            });
        },
        // 显示消息对象详情
        showMessageTargetDetails(message) {
            switch (message.type) {
            // 显示订单详情
            case MESSAGE_TYPE.CREATE_ORDER:
            case MESSAGE_TYPE.UPDATE_ORDER:
                this.orderId = message.targetId;
                this.orderModal = true;
                break;
            default:
                break;
            }
        },
        // 新建搜索条件
        newFilter() {
            return { // 搜索条件
                receiverId: this.$store.getters.currentUserId,
                pageSize  : 50,
                pageNumber: 1,
            };
        },
    }
};
</script>

<style lang="scss">
</style>
