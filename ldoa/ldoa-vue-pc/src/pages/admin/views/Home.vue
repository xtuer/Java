<!-- 页面布局 -->
<template>
    <div class="home">
        <!-- Header -->
        <Header>LDOA</Header>

        <div class="main">
            <!-- 左侧侧边栏 -->
            <div class="sidebar">
                <Menu :active-name="activeMenuItemName" :open-names="openSubMenuIndexes" width="auto" @on-select="navigateTo">
                    <Submenu v-for="(sm, index) in mySubMenus" :key="index" :name="index">
                        <template slot="title"><Icon :type="sm.icon" /> {{ sm.label }}</template>
                        <MenuItem v-for="item in sm.menuItems" :key="item.name" :name="item.name">{{ item.label }}</MenuItem>
                    </Submenu>
                </Menu>
            </div>

            <!-- 内容显示区 -->
            <div class="content">
                <Scroll>
                    <div class="content-wrapper">
                        <router-view/>
                    </div>
                </Scroll>
            </div>
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
            activeMenuItemName: '', // 当前菜单项名字
            openSubMenuIndexes: [2, 3], // 展开的子菜单下标

            // 所有菜单项，每个菜单项有不同的权限
            subMenus: [
                { label: '产品系统', icon: 'md-construct', menuItems:
                    [
                        { label: '物料管理', name: 'product-items' },
                        { label: '产品管理', name: 'products'      },
                    ]
                },
                { label: '订单系统', icon: 'logo-usd', menuItems:
                    [
                        { label: '生产订单', name: 'orders' },
                        { label: '维保订单', name: 'maintenance-orders' },
                    ]
                },
                { label: '生产系统', icon: 'md-compass', menuItems:
                    [
                        { label: '库存查询', name: 'stocks' },
                        { label: '物料入库', name: 'stock-in' },
                        { label: '物料出库', name: 'stock-out' },
                    ]
                },
                { label: '销售系统', icon: 'logo-euro', menuItems:
                    [
                        { label: '客户中心', name: 'customers' },
                        { label: '销售订单', name: 'sales-orders' },
                        { label: '订单收款', name: 'sales-order-payments' },
                    ], roles: ['ROLE_SALE_SALESPERSON', 'ROLE_ADMIN_SYSTEM', 'ROLE_ADMIN', 'ROLE_FINANCE']
                },
                { label: '共享文件', icon: 'md-photos', menuItems:
                    [
                        { label: '所有文件', name: 'disk-all' },
                        { label: '我的文件', name: 'disk-my' },
                    ]
                },
                { label: '个人中心', icon: 'ios-contact', menuItems:
                    [
                        { label: '我的信息',   name: 'user-info' },
                        { label: '收到的审批', name: 'audit-received' },
                        { label: '发起的审批', name: 'audit-request'  },
                    ]
                },
                { label: '系统管理', icon: 'ios-color-fill', menuItems:
                    [
                        { label: '用户管理', name: 'users'        },
                        { label: '审批配置', name: 'audit-config' },
                    ]
                },
            ],
        };
    },
    mounted() {
        this.activeMenuItemName = this.$route.name;
    },
    methods: {
        // 路由跳转
        navigateTo(name) {
            this.$router.push({ name });
        },
        // 高亮菜单栏
        highlightMenu(menuItemName) {
            this.subMenus.forEach((sm, index) => {
                if (sm.menuItems.some(item => item.name === menuItemName)) {
                    this.activeMenuItemName = menuItemName;

                    if (!this.openSubMenuIndexes.includes(index)) {
                        this.openSubMenuIndexes.push(index);
                    }
                }
            });
        },
        // 权限判断逻辑:
        hasPermission(menu, role) {
            // 如果菜单项没有角色数组、或者角色数组为空、或者角色数组中包含了当前用户的角色，则有权访问
            return !menu.roles || menu.roles.length === 0 || menu.roles.includes(role);
        }
    },
    computed: {
        // 根据用户权限，过滤菜单
        mySubMenus() {
            const retSubMenus = [];
            const userRole = this.$store.state.user.roles[0] || '无';

            // 处理一级菜单
            for (let subMenu of this.subMenus) {
                if (this.hasPermission(subMenu, userRole)) {
                    let retSubMenu = Utils.clone(subMenu);
                    retSubMenu.menuItems = [];
                    retSubMenus.push(retSubMenu);

                    // 没用子菜单则继续下一个
                    if (!subMenu.menuItems) {
                        continue;
                    }

                    // 处理二级菜单
                    for (let menuItem of subMenu.menuItems) {
                        if (this.hasPermission(menuItem, userRole)) {
                            let retMenuItem = Utils.clone(menuItem);
                            retSubMenu.menuItems.push(retMenuItem);
                        }
                    }
                }
            }

            return retSubMenus;
        }
    },
    watch: {
        // 监听路由变化时高亮对应的菜单项
        $route: {
            immediate: true,
            handler(to, from) {
                this.highlightMenu(to.name);
            }
        }
    }
};
</script>

<style lang="scss">
.home {
    display: flex;
    flex-direction: column;
    width : 100%;
    height: 100vh;

    > .header {
        box-shadow: 0 0px 15px #ccc;
        z-index: 1000;
        height: 60px;
    }

    > .main {
        display: flex;
        flex: 1;

        > .sidebar {
            width: 180px;

            // 隐藏 Menu 右边框
            .ivu-menu-vertical.ivu-menu-light::after {
                display: none;
            }

            .ivu-menu-light.ivu-menu-vertical .ivu-menu-item-active:not(.ivu-menu-submenu):after {
                background: #5cadff;
            }

            .ivu-menu-item {
                padding-left: 50px !important;
            }
        }

        > .content {
            flex: 1;
            background: #eceef8;
            padding: 24px;

            > .content-wrapper, > .__vuescroll > .__panel > .__view > .content-wrapper {
                padding: 18px;
                background: white;
                border-radius: 4px;
                overflow: auto;
                min-height: calc(100vh - 60px - 50px);
            }
        }
    }
}
</style>
