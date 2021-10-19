<!-- Header (根据业务需求进行修改) -->
<template>
    <div class="header">
        <div class="title"><slot></slot></div>

        <!-- 消息 -->
        <Dropdown>
            <a class="message-count-wrapper margin-right-10" :class="messageAnimate">
                <Icon type="ios-notifications-outline" size="30" style="color: #ddd" class="clickable"/>
                <div class="message-count">{{ messageCount + auditCount }}</div>
            </a>
            <DropdownMenu slot="list" class="menu-userx">
                <DropdownItem>
                    <router-link :to="{ name: 'messages' }">消息 ({{ messageCount }})</router-link>
                </DropdownItem>
                <DropdownItem>
                    <router-link :to="{ name: 'audit-received' }">审批 ({{ auditCount }})</router-link>
                </DropdownItem>
            </DropdownMenu>
        </Dropdown>


        <!-- 用户头像和菜单 -->
        <Dropdown>
            <a href="javascript:void(0)" class="user-info">
                <Avatar :src="user.avatar || '/static-p/img/avatar.jpg'" :size="30" :bordered="false"/>
                <span class="user-name">{{ user.nickname }}</span>
                <Icon type="ios-arrow-down" style="margin-left: 8px"/>
            </a>
            <DropdownMenu slot="list">
                <DropdownItem><router-link to="/user-info">个人中心</router-link></DropdownItem>
                <DropdownItem><a href="/logout">退出账号</a></DropdownItem>
            </DropdownMenu>
        </Dropdown>
    </div>
</template>

<script>
export default {
    props: {},
    data() {
        return {};
    },
    methods: {},
    computed: {
        user() {
            return this.$store.state.user;
        },
        messageCount() {
            return this.$store.state.messageCount;
        },
        auditCount() {
            return this.$store.state.auditCount;
        },
        showMessageDropdown() {
            return this.messageCount > 0 && this.auditCount > 0;
        },
        messageAnimate() {
            return {
                'message-animate': this.messageCount + this.auditCount > 0,
            };
        }
    }
};
</script>

<style lang="scss">
.header {
    display: grid;
    grid-template-columns: 1fr repeat(3, max-content);
    align-items: center;
    grid-column-gap: $gap;
    padding-left: $gap;
    height: 60px;

    background-repeat  : no-repeat;
    background-position: center;
    background-size    : 100% 100%;
    background-image   : url("/static-p/img/admin-org-header/bg.png");

    .title {
        color: #eee;
        font-size: 18px;

    }

    .ivu-avatar {
        box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);
    }

    .user-name {
        color: #ccc;
        margin-left: 10px;
    }

    .ivu-dropdown-item {
        padding: 0;

        a {
            display: block;
            padding: 7px 16px;
        }
    }

    .message-count-wrapper {
        display: inline-block;
        position: relative;

        .message-count {
            position: absolute;
            top: 0;
            right: -3px;
            font-size: 10px;
            color: #eee;
            background: $errorColor;
            min-width: 16px;
            height: 16px;
            line-height: 16px;
            text-align: center;
            border-radius: 100%;
            padding: 0 1px;
        }
    }

    .message-animate {
        transform-origin: top center;
        animation: messageRotate 1s infinite linear alternate;
    }

    @keyframes messageRotate {
        0% {
            transform: rotate(45deg);
        }
        100% {
            transform: rotate(-45deg);
        }
    }
}
</style>
