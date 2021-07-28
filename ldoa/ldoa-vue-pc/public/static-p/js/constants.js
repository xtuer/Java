/**
 * 定义常量为全局变量: window.VAR_NAME = xxx;
 */

/*
管理者：黄经理
销售部销售：薛诚、李征、宋岩、朱娟
销售部技术支持：姜萌
生产部生产维保：王嘉琦、贾琳、高金东、卢鑫
销售部综合保证：王宇
生产部质量保证：李玲琴
生产部计划调度：王嘉琦
生产部检验测试：赵文彬
技术部硬件技术：李宇良
技术部软件技术：刘建忠

ROLE_ADMIN_SYSTEM("系统管理员"),
ROLE_ADMIN("管理者"),
ROLE_FINANCE("财务"),

ROLE_SALE_SALESPERSON("销售部销售"),
ROLE_SALE_SUPPORT("销售部技术支持"),
ROLE_SALE_GUARANTEE("销售部综合保证"),

ROLE_PRODUCE_MAINTENANCE("生产部生产维保"),
ROLE_PRODUCE_QUALITY("生产部质量保证"),
ROLE_PRODUCE_SCHEDULE("生产部计划调度"),
ROLE_PRODUCE_TEST("生产部检验测试"),

ROLE_TECHNIQUE_HARDWARE("技术部硬件技术"),
ROLE_TECHNIQUE_SOFTWARE("技术部软件技术");
*/
// 角色
window.ROLES = [
    { value: 'ROLE_ADMIN_SYSTEM', name: '系统管理员' },
    { value: 'ROLE_ADMIN',        name: '管理者' },
    { value: 'ROLE_FINANCE',      name: '财务' },

    { value: 'ROLE_SALE_SALESPERSON', name: '销售部销售' },
    { value: 'ROLE_SALE_SUPPORT',     name: '销售部技术支持' },
    { value: 'ROLE_SALE_GUARANTEE',   name: '销售部综合保证' },

    { value: 'ROLE_PRODUCE_MAINTENANCE', name: '生产部生产维保' },
    { value: 'ROLE_PRODUCE_QUALITY',     name: '生产部质量保证' },
    { value: 'ROLE_PRODUCE_SCHEDULE',    name: '生产部计划调度' },
    { value: 'ROLE_PRODUCE_TEST',        name: '生产部检验测试' },

    { value: 'ROLE_TECHNIQUE_HARDWARE', name: '技术部硬件技术' },
    { value: 'ROLE_TECHNIQUE_SOFTWARE', name: '技术部软件技术' },
];

// 权限控制
window.PERMISSIONS = {
    // 超级管理员的权限
    superAdmin: ['ROLE_ADMIN_SYSTEM'],

    // 维保订单的权限
    maintenance: ['ROLE_PRODUCE_MAINTENANCE', 'ROLE_PRODUCE_SCHEDULE'],

    // 物料入库的权限
    stockIn: ['ROLE_PRODUCE_QUALITY', 'ROLE_PRODUCE_SCHEDULE'],

    // 销售订单的权限
    salesOrder: ['ROLE_SALE_SALESPERSON', 'ROLE_ADMIN_SYSTEM'], // TODO: delete admin

    // 财务的权限
    finance: ['ROLE_FINANCE', 'ROLE_ADMIN_SYSTEM'], // TODO: delete admin
};

// 审批类型
window.AUDIT_TYPES = [
    { value: 'ORDER', name: '生产订单' },
    { value: 'OUT_OF_STOCK', name: '物料出库' },
    { value: 'MAINTENANCE_ORDER', name: '维修 / 保养' },
];
window.AUDIT_TYPE = {
    ORDER: 'ORDER', // 订单
    OUT_OF_STOCK: 'OUT_OF_STOCK', // 出库申请
    MAINTENANCE_ORDER: 'MAINTENANCE_ORDER', // 维保订单
};

window.AUDIT_STATES = [
    { value: 1, label: '待审批' },
    { value: 2, label: '拒绝' },
    { value: 3, label: '通过' },
    { value: -1, label: '所有' },
];

// 审批阶段状态
window.AUDIT_ITEM_STATES = [
    { value: 1,  label: '待审批' },
    { value: 2,  label: '拒绝' },
    { value: 3,  label: '通过' },
    { value: -1, label: '所有' },
];

// 订单类型
window.ORDER_TYPES = [
    { value: 0, label: '生产订单' },
    { value: 1, label: '样品订单' },
];

// 销售订单类型
window.SALES_ORDER_STATES = [
    { value: 0, label: '初始化', color: 'default' },
    { value: 1, label: '待支付', color: 'cyan' },
    { value: 2, label: '已支付', color: 'primary' },
    { value: 3, label: '完成', color: 'success' },
];
window.SALES_ORDER_STATE = {
    STATE_INIT    : 0,
    STATE_WAIT_PAY: 1,
    STATE_PAID    : 2,
    STATE_COMPLETE: 3,
};
// 收款类型
window.SALES_PAID_TYPE = {
    NONE_PAY: 0,
    PREV_PAY: 1,
    FULL_PAY: 2,
};
window.SALES_PAID_TYPES = [
    { value: 0, label: '未付' },
    { value: 1, label: '预付' },
    { value: 2, label: '全款' },
];

// 性别
window.GENDERS = [
    { value: 0, name: '未选' },
    { value: 1, name: '男' },
    { value: 2, name: '女' },
];

// 题目类型
window.QUESTION_TYPE = {
    SINGLE_CHOICE  : 1, // 单选题
    MULTIPLE_CHOICE: 2, // 多选题
    TFNG           : 3, // 判断题: true(是), false(否), not given(未提及)
    FITB           : 4, // 填空题: fill in the blank
    ESSAY_QUESTION : 5, // 问答题
    COMPLEX        : 6, // 复合题
    DESCRIPTION    : 7, // 题型题 (大题分组、介绍)
};

// 题目类型 (value 在前，name 在后，好排版)
window.QUESTION_TYPES = [
    { value: 1, name: '单选题' },
    { value: 2, name: '多选题' },
    { value: 3, name: '判断题' },
    { value: 4, name: '填空题' },
    { value: 5, name: '问答题' },
    { value: 6, name: '复合题' },
];

// 教学阶段
window.PHASES = ['高中', '初中', '小学'];

// 学科
window.SUBJECTS = ['语文', '数学', '英语', '物理', '化学', '生物', '地理', '政治', '历史'];

// 省
window.PROVINCES = ['北京', '上海', '天津', '重庆', '河北', '辽宁', '黑龙江', '吉林', '山东', '山西', '安徽', '浙江',
    '江苏', '江西', '广东', '福建', '海南', '河南', '湖北', '湖南', '四川', '云南', '贵州', '陕西', '甘肃',
    '青海', '内蒙古', '广西', '西藏', '新疆', '香港', '澳门', '台湾',
];

// 身份证件类型
window.ID_CARD_TYPES = [
    { value: 1,  name: '护照' },
    { value: 2,  name: '户口簿' },
    { value: 3,  name: '其他' },
    { value: 4,  name: '居民身份证' },
    { value: 5,  name: '军官证' },
    { value: 6,  name: '士兵证' },
    { value: 7,  name: '文职干部证' },
    { value: 8,  name: '部队离退休证' },
    { value: 9,  name: '香港特区护照/身份证明' },
    { value: 10, name: '澳门特区护照/身份证明' },
    { value: 11, name: '台湾居民来往大陆通行证' },
    { value: 12, name: '境外永久居住证' },
    { value: 13, name: '涉密证件' },
    { value: 14, name: '手机号码涉密' },
];

// 字典类型
window.DICTS = ['专业领域', '委托合作单位性质', '办学性质', '招生类型', '培训对象类别', '新闻公告'];

// 表单项模板
// label     : Form 中的 label
// name      : Form 中的 input 的 name
// customized: 模板项为 false，用户自定义的项为 true，在表单编辑器中使用
// required  : Form 中此项是否必填
// span      : Form 中占据的列数
// options   : type 为 select 时的下拉选项
window.FORM_TEMPLATE_FIELDS = [
    { label: '姓名', name: 'nickname', type: 'string', customized: false, required: false, span: 1, options: [] },
    { label: '账号', name: 'username', type: 'string', customized: false, required: false, span: 1, options: [] },
    { label: '性别', name: 'gender',   type: 'select', customized: false, required: false, span: 1, options: ['未选', '女', '男'] },
    { label: '民族', name: 'nation',   type: 'select', customized: false, required: false, span: 1, options: [
        '汉族', '满族', '蒙古族', '回族', '藏族', '维吾尔族', '苗族', '彝族', '壮族', '布依族', '侗族', '瑶族', '白族', '土家族',
        '哈尼族', '哈萨克族', '傣族', '黎族', '傈僳族', '佤族', '畲族', '高山族', '拉祜族', '水族', '东乡族', '纳西族', '景颇族',
        '柯尔克孜族', '土族', '达斡尔族', '仫佬族', '羌族', '布朗族', '撒拉族', '毛南族', '仡佬族', '锡伯族', '阿昌族', '普米族',
        '朝鲜族', '塔吉克族', '怒族', '乌孜别克族', '俄罗斯族', '鄂温克族', '德昂族', '保安族', '裕固族', '京族',
        '塔塔尔族', '独龙族', '鄂伦春族', '赫哲族', '门巴族', '珞巴族', '基诺族'
    ] },
    { label: '邮箱', name: 'email',  type: 'email',  customized: false, required: false, span: 1, options: [] },
    { label: '手机', name: 'mobile', type: 'number', customized: false, required: false, span: 1, options: [] },
    { label: '职务', name: 'title',  type: 'string', customized: false, required: false, span: 1, options: [] },
    { label: 'QQ',  name: 'qq',     type: 'number', customized: false, required: false, span: 1, options: [] },
    { label: '单位性质', name: 'workUnitType', type: 'string', customized: false, required: false, span: 1, options: [] },
    { label: '工作单位', name: 'workUnit',     type: 'string', customized: false, required: false, span: 1, options: [] },

    { label: '证件类型', name: 'idCardType',   type: 'select', customized: false, required: false, span: 1, options: [
        '居民身份证', '军官证', '士兵证', '护照', '户口簿', '文职干部证', '部队离退休证', '香港特区护照/身份证明',
        '澳门特区护照/身份证明', '台湾居民来往大陆通行证', '境外永久居住证', '涉密证件', '手机号码涉密', '其他'
    ] },
    { label: '证件号码', name: 'idCardNumber', type: 'string', customized: false, required: false, span: 1, options: [] },
    { label: '最高学历', name: 'educationBg',  type: 'select', customized: false, required: false, span: 1, options: [
        '大专', '本科', '研究生', '高中及高中以下'
    ] },

    { label: '联系地址', name: 'address',  type: 'address', customized: false, required: false, span: 2, options: [] },
    { label: '出生年月', name: 'birthday', type: 'date',    customized: false, required: false, span: 1, options: [] },
];

// 表单属性的类型
window.FORM_FIELD_TYPES = [
    { value: 'string',  name: '字符串' },
    { value: 'number',  name: '数字' },
    { value: 'email',   name: '邮箱' },
    { value: 'date',    name: '日期' },
    { value: 'address', name: '地址' },
    { value: 'select',  name: '下拉框' },
];
