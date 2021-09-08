import TableUtils from '@/../public/static-p/js/utils/TableUtils';

/**
 * 下载 url 指定的文件
 *
 * @param  {String} url 要下载的 URL
 * @return 无返回值
 */
const download = function(url) {
    // 1. 如果 url 是文件仓库的 url，则替换为下载使用的 url
    // 2. 创建 form 表单
    // 3. 提交表单下载文件

    if (!url) {
        return;
    }

    // [1] 如果 url 是文件仓库的 url，则替换为下载使用的 url
    const finalUrl = url.replace('/file/repo', '/file/download');

    // [2] 创建 form 表单
    const form = document.createElement('form');
    form.method = 'GET';
    form.action = finalUrl;
    document.body.appendChild(form);

    // [3] 提交表单下载文件
    form.submit();
};

/**
 * 导出文件并下载到本地，不管成功还是失败，返回的 Promise 都执行 resolve 函数。
 *
 * 示例:
 * this.exporting = true;
 * this.exportFile(ProductDao.exportProducts(this.filter)).then(() => {
 *     this.exporting = false;
 * }
 *
 * @param {Promise} 执行导出的 Promise 请求，其执行成功的参数为导出文件的 URL
 * @return 无返回值
 */
const exportFile = function(p) {
    return new Promise(function(resolve) {
        p.then(url => {
            download(url);
            resolve();
        }).catch(() => {
            resolve();
        });
    });
};

/**
 * 返回上一路由
 *
 * 案例: <Button @click="goBack()">返回</Button>
 *
 * @return 无返回值
 */
const goBack = function() {
    window.history.length > 1 ? this.$router.go(-1) : this.$router.push('/');
};

/**
 * 判断是否系统管理员
 *
 * @param {String} role 角色
 * @return 是系统管理员返回 true，否则返回 false
 */
const isSystemAdmin = function(user) {
    return user.roles.includes('ROLE_ADMIN_SYSTEM');
};

/**
 * 判断传入的 userId 是否当前登陆用户的 ID
 *
 * @param {Long} userId 用户 ID
 * @return {Bool} 是当前用户返回 true，否则返回 false
 */
const isCurrentUser = function(userId) {
    return userId === this.$store.state.user.userId;
};

/**
 * 获取当前登陆用户的 ID
 *
 * @return {Long} 返回登陆用户的 ID
 */
const currentUserId = function() {
    return this.$store.state.user.userId;
};


// 状态: { "初始化", "审批中", "审批拒绝", "审批通过", "完成" }
/**
 * 返回状态对应的颜色，给 Tag 使用，参考 https://iviewui.com/components/tag
 * <Tag color="red">red</Tag>
 *
 * @param {Int} state 状态值
 * @return {String} Tag 的颜色
 */
const stateColor = function(state) {
    switch (state) {
    case 0: return 'default'; // 初始化
    case 1: return 'cyan';    // 审批中
    case 2: return 'error';   // 审批拒绝
    case 3: return 'primary'; // 审批通过
    case 4: return 'success'; // 完成
    default: return 'default';
    }
};

/**
 * 保存表的列
 *
 * 调用示例:
 * 1. 在 data 中定义属性 tableName
 * 2. <Table :data="stockRecords" :columns="columns" :loading="reloading" border
 *        @on-column-width-resize="saveTableColumnWidths(arguments)"
 *    >
 *
 * @param {JSON} args 数组 [newWidth, oldWidth, column]
 */
const saveTableColumnWidths = function(args) {
    const newWidth = args[0];
    const oldWidth = args[1];
    const column   = args[2];

    TableUtils.saveTableColumnWidths(this.tableName, this.currentUserId(), newWidth, oldWidth, column);
};

/**
 * 恢复表格的列宽度
 *
 * 调用示例:
 * 1. 在 data 中定义属性 tableName
 * 2. 在 mounted 中调用 this.restoreTableColumnWidths(columns)
 *
 * @param {Array} columns 表格的列属性数组
 */
const restoreTableColumnWidths = function(columns) {
    TableUtils.restoreTableColumnWidths(this.tableName, this.currentUserId(), columns);
};

/**
 * 设置 obj 的 field 属性为传入的参数 n 的整数值
 * 使用案例: <InputNumber v-model="count" :min="0" @on-change="ensureInt(order, 'count', $event)"/>
 *
 * @param {Object} obj   要设置属性的对象
 * @param {String} field 属性名
 * @param {Number} n     数值
 */
const ensureInt = function(obj, field, n) {
    this.$nextTick(() => {
        n = parseInt(n) || 0;
        this.$set(obj, field, n);
    });
};

/**
 * 计算 .content 中 Table 的最大高度
 *
 * @returns 返回 Table 的最大高度
 */
const calculateTableMaxHeight = function() {
    const top = document.querySelector('.ivu-table-wrapper').getBoundingClientRect().top;
    const vh = Math.max(document.documentElement.clientHeight || 0, window.innerHeight || 0);
    const tableMaxHeight = vh - 98 - top;

    return tableMaxHeight;
};

export default {
    download,
    exportFile,
    goBack,
    isSystemAdmin,
    isCurrentUser,
    currentUserId,
    stateColor,
    restoreTableColumnWidths,
    saveTableColumnWidths,
    ensureInt,
    calculateTableMaxHeight,
};
