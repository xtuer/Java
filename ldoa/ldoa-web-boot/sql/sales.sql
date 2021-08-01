#--------------------------------------------------------------------------------------
# 表名: customer
# 作者: 黄彪
# 日期: 2021-04-24
# 版本: 1.0
# 描述: 客户
#--------------------------------------------------------------------------------------
DROP TABLE IF EXISTS customer;

CREATE TABLE customer (
    customer_id bigint(20)   NOT NULL COMMENT '客户 ID',
    customer_sn varchar(128) NOT NULL COMMENT '客户编码',
    name        varchar(256) NOT NULL COMMENT '客户名称',
    business    varchar(128)          COMMENT '行业',
    region      varchar(128)          COMMENT '区域',
    type        varchar(128)          COMMENT '分类',
    phone       varchar(128)          COMMENT '电话',
    level       varchar(128)          COMMENT '关系等级',
    importance  varchar(128)          COMMENT '重要程度',
    status      varchar(128)          COMMENT '状态',

    address     varchar(999)          COMMENT '地址',
    owner       varchar(256)          COMMENT '负责人',
    remark      text                  COMMENT '备注',
    contacts_json text                COMMENT '客户联系人 JSON',

    created_at datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (customer_id) COMMENT '机构 ID 作为主键',
    UNIQUE  KEY index_sn (customer_sn) COMMENT '机构域名唯一',
    KEY idx_created_at (created_at) COMMENT '创建时间建立索引'
) ENGINE=InnoDB;

#--------------------------------------------------------------------------------------
# 表名: sales_order
# 作者: 黄彪
# 日期: 2021-05-02
# 版本: 1.0
# 描述: 销售订单
#--------------------------------------------------------------------------------------
DROP TABLE IF EXISTS sales_order;

CREATE TABLE sales_order (
    sales_order_id   bigint(20)   NOT NULL COMMENT '销售订单 ID',
    produce_order_id bigint(20)   NOT NULL COMMENT '生产订单 ID',
    sales_order_sn   varchar(128) NOT NULL COMMENT '销售订单编号',
    topic            varchar(128) NOT NULL COMMENT '主题',
    agreement_date   datetime     NOT NULL COMMENT '签约日期',
    delivery_date    datetime     NOT NULL COMMENT '交货日期',
    owner_id         bigint(20)   NOT NULL COMMENT '负责人 ID',
    sales_person_id  bigint(20)  DEFAULT 0 COMMENT '销售员 ID',
    customer_id      bigint(20)   NOT NULL COMMENT '客户 ID',
    customer_contact varchar(256)          COMMENT '客户联系人',
    business         varchar(128)          COMMENT '行业',
    work_unit        varchar(128)          COMMENT '执行单位',
    remark           text                  COMMENT '备注',

    deal_amount       double DEFAULT 0 COMMENT '总成交金额',
    cost_deal_amount  double DEFAULT 0 COMMENT '净销售额',
    consultation_fee  double DEFAULT 0 COMMENT '订单咨询费',
    should_pay_amount double DEFAULT 0 COMMENT '应收金额',
    paid_amount       double DEFAULT 0 COMMENT '已收金额',
    paid_at           datetime         COMMENT '收款时间',
    state             int    DEFAULT 0 COMMENT '状态: 0 (初始化), 1 (待支付), 2 (已支付), 3 (完成)',
    paid_type         int    DEFAULT 0 COMMENT '收款类型: 0 (未付), 1 (预付), 2 (全款)',

    produce_order_temp text            COMMENT '暂存使用的临时生产订单',

    created_at datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (sales_order_id) COMMENT '销售订单 ID 作为主键',
    KEY idx_created_at (created_at) COMMENT '创建时间建立索引',
    KEY idx_agreement_date (agreement_date) COMMENT '签约日期建立索引'
) ENGINE=InnoDB;
