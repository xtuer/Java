import Vue from 'vue';
import Router from 'vue-router';

Vue.use(Router);

export default new Router({
    routes: [
        {
            path: '/',
            name: 'home',
            component: () => import(/* webpackChunkName: "common" */ './views/Home.vue'),
            redirect: { name: 'orders' },

            children: [
                // 产品项管理
                {
                    path: '/product-items',
                    name: 'product-items',
                    component: () => import(/* webpackChunkName: "product" */ './views/ProductItems.vue'),
                },
                // 产品管理
                {
                    path: '/products',
                    name: 'products',
                    component: () => import(/* webpackChunkName: "product" */ './views/Products.vue'),
                },
                // =====================================================================================
                //                                         订单系统
                // =====================================================================================
                // 生产订单
                {
                    path: '/orders',
                    name: 'orders',
                    component: () => import(/* webpackChunkName: "product" */ './views/Orders.vue'),
                },
                // 维保订单
                {
                    path: '/maintenance-orders',
                    name: 'maintenance-orders',
                    component: () => import(/* webpackChunkName: "product" */ './views/MaintenanceOrders.vue'),
                },
                // =====================================================================================
                //                                         生产系统
                // =====================================================================================
                // 库存查询
                {
                    path: '/stocks',
                    name: 'stocks',
                    component: () => import(/* webpackChunkName: "product" */ './views/Stocks.vue'),
                },
                // 物料入库
                {
                    path: '/stock-in',
                    name: 'stock-in',
                    component: () => import(/* webpackChunkName: "product" */ './views/StockIn.vue'),
                },
                // 物料出库
                {
                    path: '/stock-out',
                    name: 'stock-out',
                    component: () => import(/* webpackChunkName: "product" */ './views/StockOut.vue'),
                },
                // =====================================================================================
                //                                         销售系统
                // =====================================================================================
                // 客户端倪
                {
                    path: '/customers',
                    name: 'customers',
                    component: () => import(/* webpackChunkName: "product" */ './views/Customers.vue'),
                },
                // 销售订单
                {
                    path: '/sales-orders',
                    name: 'sales-orders',
                    component: () => import(/* webpackChunkName: "product" */ './views/SalesOrders.vue'),
                },
                // 销售订单收款情况
                {
                    path: '/sales-order-payments',
                    name: 'sales-order-payments',
                    component: () => import(/* webpackChunkName: "product" */ './views/SalesOrderPayments.vue'),
                },
                // =====================================================================================
                //                                         共享文件
                // =====================================================================================
                // 所有文件
                {
                    path: '/disk-all',
                    name: 'disk-all',
                    component: () => import(/* webpackChunkName: "product" */ './views/DiskAll.vue'),
                },
                // 我的文件
                {
                    path: '/disk-my',
                    name: 'disk-my',
                    component: () => import(/* webpackChunkName: "product" */ './views/DiskMy.vue'),
                },
                // =====================================================================================
                //                                         系统管理
                // =====================================================================================
                // 用户管理
                {
                    path: '/users',
                    name: 'users',
                    component: () => import(/* webpackChunkName: "product" */ './views/Users.vue'),
                },
                // 审批配置
                {
                    path: '/audit-config',
                    name: 'audit-config',
                    component: () => import(/* webpackChunkName: "product" */ './views/AuditConfig.vue'),
                },
                // =====================================================================================
                //                                         个人中心
                // =====================================================================================
                // 个人信息
                {
                    path: '/user-info',
                    name: 'user-info',
                    component: () => import(/* webpackChunkName: "product" */ './views/UserInfo.vue'),
                },
                // 我的消息
                {
                    path: '/messages',
                    name: 'messages',
                    component: () => import(/* webpackChunkName: "product" */ './views/Messages.vue'),
                },
                // 我收到的审批
                {
                    path: '/audit-received',
                    name: 'audit-received',
                    component: () => import(/* webpackChunkName: "product" */ './views/AuditReceived.vue'),
                },
                // 我发起的审批
                {
                    path: '/audit-request',
                    name: 'audit-request',
                    component: () => import(/* webpackChunkName: "product" */ './views/AuditRequest.vue'),
                },
                {
                    path: '/about',
                    name: 'about',
                    component: () => import(/* webpackChunkName: "about" */ './views/About.vue'),
                },
                {
                    path: '/scroll',
                    name: 'scroll',
                    component: () => import(/* webpackChunkName: "about" */ './views/Scroll.vue'),
                },
            ]
        },
        {
            // 404 页面
            path: '*',
            component: () => import(/* webpackChunkName: "common" */ '../../components/404.vue'),
        }
    ],
});
