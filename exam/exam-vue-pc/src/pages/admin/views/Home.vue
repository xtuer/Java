<!-- 页面布局 -->
<template>
    <div class="home">
        <!-- Header -->
        <Header/>

        <!-- 左侧侧边栏 -->
        <div class="sidebar">
            <Menu :active-name="activeName" :open-names="['1']" width="auto" @on-select="navigateTo">
                <MenuItem v-for="item in menuItems" :key="item.name" :name="item.name">{{ item.label }}</MenuItem>
            </Menu>
        </div>

        <!-- 内容显示区 -->
        <div class="content">
            <PerfectScrollbar class="content-wrapper">
                <router-view/>
            </PerfectScrollbar>
        </div>
    </div>
</template>

<script>
import Header from '@/components/Header.vue';

export default {
    components: {
        Header,
    },
    data() {
        return {
            activeName: '',
            menuItems: [ // 所有菜单项，每个菜单项有不同的权限
                { label: '试卷管理', name: 'papers'            },
                { label: '考试管理', name: 'exams'             },
                { label: '整卷批改', name: 'correct-papers'    },
                { label: '逐题批改', name: 'correct-questions' },
            ],
        };
    },
    mounted() {
        this.activeName = this.$route.name;
    },
    methods: {
        navigateTo(name) {
            this.$router.push({ name });
        },
    },
    watch: {
        // 监听路由变化时高亮对应的菜单项
        $route(to, from) {
            if (this.menuItems.some(item => item.name === to.name)) {
                this.activeName = to.name;
            }
        }
    }
};
</script>

<style lang="scss">
.home {
    display: grid;
    grid-template-columns: 180px 1fr;
    grid-template-rows: max-content 1fr;
    min-height: 100%;

    > .header {
        grid-column: span 2;
        box-shadow: 0 0px 15px #ccc;
        z-index: 1000;
    }

    > .sidebar {
        // 隐藏 Menu 右边框
        .ivu-menu-vertical.ivu-menu-light:after {
            display: none;
        }

        .ivu-menu-item-active::after {
            background: #5cadff !important;
        }
    }

    > .content {
        background: #eceef8;
        padding: 24px;

        > .content-wrapper {
            padding: 18px;
            background: white;
            border-radius: 4px;
            min-height: 100%;
        }
    }
}
</style>
