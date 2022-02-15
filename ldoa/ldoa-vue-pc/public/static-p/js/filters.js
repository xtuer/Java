/**
 * 在此定义 Vue 全局的过滤器
 *
 * 1. 在 main.js 中安装全局过滤器
 *    import filters from '@/../public/static-p/js/filters';
 *
 *    Object.keys(filters).forEach(key => {
 *        Vue.filter(key, filters[key]);
 *    });
 * 2. 使用过滤器: {{ date | formatDate }}
 */

/**
 * 格式化时间的过滤器，时间无效返回 ----
 *
 * @param  {Date}   date 时间对象
 * @param  {String} pattern 时间格式
 * @return {String} 返回日期格式化得到的字符串
 */
const formatDate = function(date, pattern = 'YYYY-MM-DD HH:mm') {
    if (!date) {
        return '----';
    }

    const temp = dayjs(date); // 注意: date 为 undefined 时返回的是当前时间

    if (temp.isValid()) {
        return temp.format(pattern);
    } else {
        return '----';
    }
};

/**
 * 格式化时间，只有年月日
 *
 * @param {String}} date 时间对象
 * @return {String} 返回日期的字符串
 */
const formatDateSimple = function(date) {
    return formatDate(date, 'YYYY-MM-DD');
};

/**
 * 格式化时间，有年月日时分秒
 *
 * @param {String}} date 时间对象
 * @return {String} 返回日期的字符串
 */
const formatDateFull = function(date) {
    return formatDate(date, 'YYYY-MM-DD HH:mm:ss');
};

/**
 * 返回角色对应的名字
 *
 * @param  {String} role 角色
 * @return {String} 返回角色的名字
 */
const roleName = function(role) {
    return ROLES.filter(r => r.value === role).map(r => r.name).join('') || '未知';
};

/**
 * 根据性别的值获取对应的名字
 *
 * @param  {Integer} gender 性别的值，查看 constants.js 中 GENDERS 的定义
 * @return {String} 返回性别的名字
 */
const genderName = function(gender) {
    return GENDERS.filter(t => t.value === gender).map(t => t.name).join('');
};

/**
 * 根据身份信息类型值获取对应的名字
 *
 * @param  {Integer} value 身份信息的类型值
 * @return {String} 返回身份类型名字
 */
const idCardName = function(value) {
    return ID_CARD_TYPES.filter(t => t.value === value).map(t => t.name).join('');
};

/**
 * 把文件仓库的 URL '/file/repo/2018-06-19/293591971581788160.docx'
 * 转换为下载的 URL '/file/download/2018-06-19/293591971581788160.docx'
 *
 * @param  {String} repoUrl 文件仓库的 URL
 * @return {String} 返回下载使用的 URL
 */
const repoUrlToDownloadUrl = function(repoUrl) {
    if (repoUrl) {
        return repoUrl.replace('/file/repo', '/file/download');
    }

    return '';
};

/**
 * 去掉字符串 str 两边的空白，如果 str 为空白字符串，则返回 defaultValue
 *
 * @param {String} str 要 trim 的字符串
 * @param {String} defaultValue trim 为空白字符串时的默认值
 */
const trim = function(str, defaultValue = '') {
    const text = str && str.trim();
    return text || defaultValue;
};

/**
 * 获取审批类型的名字
 *
 * @param {String} typeValue 类型
 */
const auditTypeName = function(typeValue) {
    return AUDIT_TYPES.filter(t => t.value === typeValue).map(t => t.name).join('') || '未知';
};

/**
 * 获取 value 对应的 label
 *
 * @param {Int} value 值
 * @param {Array} pairs value/label 对的数组
 * @return {String} 返回 label 的字符串
 */
const labelForValue = function(value, pairs) {
    return pairs.filter(p => p.value === value).map(p => p.label).join('') || '未知';
};

/**
 * 获取 value 对应的 color
 *
 * @param {Int} value 值
 * @param {Array} pairs value/color 对的数组
 * @return {String} 返回 color 的字符串
 */
const colorForValue = function(value, pairs) {
    return pairs.filter(p => p.value === value).map(p => p.color).join('') || 'default';
};

/**
 * 出库类型的 Int 对应的字符串 Label
 *
 * @param {Int} type 出库类型
 * @returns 返回出库类型的 Label
 */
const stockOutRequestType = function(type) {
    switch (type) {
    case window.STOCK_OUT_TYPE.PRODUCT_ITEM: return '物料';
    case window.STOCK_OUT_TYPE.PRODUCT: return '产品';
    case window.STOCK_OUT_TYPE.ORDER: return '订单';
    default: return '未知';
    }
};

export default {
    formatDate,
    formatDateSimple,
    formatDateFull,
    roleName,
    genderName,
    idCardName,
    repoUrlToDownloadUrl,
    trim,
    auditTypeName,
    labelForValue,
    colorForValue,
    stockOutRequestType,
};
