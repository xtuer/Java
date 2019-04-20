process.env.VUE_APP_VERSION = new Date().getTime();

module.exports = {
    devServer: {
        port: 8888,
        // proxy: 'http://localhost:8080'
    },

    // 多页的页面
    pages: {
        admin: 'src/pages/admin/main.js',
    },

    // yarn build 的输出目录
    outputDir: '../web-app/src/main/webapp/WEB-INF/page-vue',
    assetsDir: 'static',

    css: {
        loaderOptions: {
            sass: {
                data: `
                    @import "@/../public/static/css/variables.scss";
                `
            }
        }
    },

    productionSourceMap: false, // 不生成 map 文件
    // configureWebpack: config => {
    //     if (process.env.NODE_ENV === 'production') { // 生产环境启用 gzip 压缩
    //         return {
    //             plugins: [new CompressionWebpackPlugin({
    //                 test: /\.(js|css)(\?.*)?$/i, // 需要压缩的文件正则
    //                 threshold: 10240,            // 文件大小大于这个值时启用压缩
    //                 deleteOriginalAssets: false, // 压缩后保留原文件
    //             })]
    //         };
    //     }
    // },
};
