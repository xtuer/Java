#--------------------------------------------------------------------------------------
# 表名: order
# 作者: 黄彪
# 日期: 2020-09-20
# 版本: 1.0
# 描述: 订单
#--------------------------------------------------------------------------------------
DROP TABLE IF EXISTS `order`;

CREATE TABLE `order` (
    order_id bigint(20) NOT NULL              COMMENT '订单 ID',
    order_sn varchar(64) NOT NULL             COMMENT '订单 SN',
    type             int(11)       DEFAULT 0  COMMENT '订单类型: 0 (销售订单)、1 (样品订单)',
    customer_company varchar(256)  DEFAULT '' COMMENT '客户单位',
    customer_contact varchar(64)   DEFAULT '' COMMENT '客户联系人',
    customer_address varchar(512)  DEFAULT '' COMMENT '客户收件地址',
    customer_phone   varchar(64)   DEFAULT '' COMMENT '客户联系方式',
    work_unit        varchar(256)  DEFAULT '' COMMENT '执行单位',
    salesperson_id   bigint(20) NOT NULL      COMMENT '销售员',
    calibrated       tinyint(4)    DEFAULT 0  COMMENT '是否校准，0 为否，1 为是',
    calibration_info varchar(1024) DEFAULT '' COMMENT '校准信息',
    requirement      text                     COMMENT '要求',
    attachment_id    bigint(20)    DEFAULT 0  COMMENT '附件 ID',
    order_date       datetime NOT NULL        COMMENT '下订日期',
    delivery_date    datetime NOT NULL        COMMENT '交货日期',
    return_date      datetime                 COMMENT '归还日期',
    state            int(11)       DEFAULT 0  COMMENT '状态: 0 (初始化), 1 (待审批), 2 (审批拒绝), 3 (审批完成), 4 (完成)',
    product_codes    varchar(512)  DEFAULT '' COMMENT '产品编码，使用逗号分隔',
    product_names    text                     COMMENT '产品名称，使用逗号分隔',
    progress         varchar(2048)            COMMENT '进度情况',

    created_at datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (order_id) COMMENT '订单 ID 作为主键',
    UNIQUE KEY idx_sn (order_sn) COMMENT '订单号唯一主键'
) ENGINE=InnoDB;

#--------------------------------------------------------------------------------------
# 表名: order_item
# 作者: 黄彪
# 日期: 2020-09-20
# 版本: 1.0
# 描述: 订单项
#--------------------------------------------------------------------------------------
DROP TABLE IF EXISTS order_item;

CREATE TABLE order_item (
    order_item_id bigint(20) NOT NULL COMMENT '订单项 ID',
    order_id      bigint(20) NOT NULL COMMENT '订单 ID',
    product_id    bigint(20) NOT NULL COMMENT '产品 ID',
    count         int(11) DEFAULT 0   COMMENT '产品数量',
    comment       text                COMMENT '备注',
    price         double  DEFAULT 0   COMMENT '单价',
    cost_price    double  DEFAULT 0   COMMENT '成本价',
    consultation_fee double DEFAULT 0 COMMENT '咨询费',

    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (order_item_id) COMMENT '订单项 ID 作为主键',
    KEY idx_order (order_id) COMMENT '订单 ID 建立索引'
) ENGINE=InnoDB;

#--------------------------------------------------------------------------------------
# 表名: maintenance_order
# 作者: 黄彪
# 日期: 2021-01-09
# 版本: 1.0
# 描述: 维修保养订单
#--------------------------------------------------------------------------------------
DROP TABLE IF EXISTS maintenance_order;

CREATE TABLE maintenance_order (
    maintenance_order_id bigint(20)   NOT NULL   COMMENT '维保订单 ID',
    maintenance_order_sn varchar(64)  NOT NULL   COMMENT '维保订单 SN',
    service_person_id    bigint(20)   NOT NULL   COMMENT '售后服务人员 ID',
    customer_name        varchar(256) DEFAULT '' COMMENT '客户名字',
    maintainable         tinyint(4)   DEFAULT 0  COMMENT '是否保养，0 为否，1 为是',
    repairable           tinyint(4)   DEFAULT 0  COMMENT '是否维修，0 为否，1 为是',
    salesperson_name     varchar(64)  DEFAULT '' COMMENT '销售人员名字',
    received_date        datetime     NOT NULL   COMMENT '收货日期',
    product_code         mediumtext              COMMENT '产品编码',
    product_name         mediumtext              COMMENT '产品名称',
    product_model        mediumtext              COMMENT '规格/型号',
    product_count        int(11)      DEFAULT 0  COMMENT '产品数量',
    product_item_name    varchar(256) DEFAULT '' COMMENT '物料名称',
    product_item_batch   varchar(64)  DEFAULT '' COMMENT '批次',
    product_item_count   int(11)      DEFAULT 0  COMMENT '物料数量',
    accessories          varchar(512) DEFAULT '' COMMENT '配件',
    need_certificate     tinyint(4)   DEFAULT 0  COMMENT '是否需要证书，0 为否，1 为是',
    problem              text                    COMMENT '客户反应的问题',
    progress             varchar(2048)           COMMENT '进度情况',
    order_sn             varchar(64)  DEFAULT '' COMMENT '订单号',
    state                int(11)      DEFAULT 0  COMMENT '状态: 0 (初始化), 1 (待审批), 2 (审批拒绝), 3 (审批完成), 4 (完成)',
    committed            tinyint(4)   DEFAULT 0  COMMENT '是否提交了，0 为否，1 为是',

    created_at datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (maintenance_order_id) COMMENT '维保订单 ID 作为主键',
    KEY idx_received_date (received_date) COMMENT '收货时间建立索引',
    KEY idx_created_at (created_at) COMMENT '创建时间建立索引'
) ENGINE=InnoDB;

#--------------------------------------------------------------------------------------
# 表名: maintenance_order_item
# 作者: 黄彪
# 日期: 2021-03-09
# 版本: 1.0
# 描述: 维修保养订单项
#--------------------------------------------------------------------------------------
DROP TABLE IF EXISTS maintenance_order_item;

CREATE TABLE maintenance_order_item (
    maintenance_order_id       bigint(20)   NOT NULL   COMMENT '维保订单 ID',
    maintenance_order_item_id  bigint(20)   NOT NULL   COMMENT '维保订单项 ID',
    product_name               varchar(256) DEFAULT '' COMMENT '产品名称',
    product_code               varchar(64)  DEFAULT '' COMMENT '产品编码',
    product_model              varchar(64)  DEFAULT '' COMMENT '规格/型号',
    electric_quantity_before   varchar(256) DEFAULT '' COMMENT '维修前电量',
    software_version_before    varchar(256) DEFAULT '' COMMENT '维修前软件版本',
    hardware_version_before    varchar(256) DEFAULT '' COMMENT '维修前硬件版本',
    power_dissipation_before   varchar(64)  DEFAULT 0  COMMENT '维修前功耗',
    temperature_before         varchar(64)  DEFAULT 0  COMMENT '维修前高温次数',
    chip_code                  varchar(64)  DEFAULT '' COMMENT '芯片编号',
    check_details              text                    COMMENT '检测问题明细',
    maintenance_details        text                    COMMENT '检测问题明细',
    probe_detector_code_before varchar(64)  DEFAULT '' COMMENT '探头换前编号',

    electric_quantity_after    varchar(256) DEFAULT '' COMMENT '维修后电量',
    software_version_after     varchar(256) DEFAULT '' COMMENT '维修后软件版本',
    hardware_version_after     varchar(256) DEFAULT '' COMMENT '维修后硬件版本',
    power_dissipation_after    varchar(64)  DEFAULT 0  COMMENT '维修后功耗',
    temperature_after          varchar(64)  DEFAULT 0  COMMENT '维修后高温次数',
    probe_detector_code_after  varchar(64)  DEFAULT '' COMMENT '探头换后编号',
    production_date            varchar(256) DEFAULT '' COMMENT '出厂时间',

    created_at datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (maintenance_order_item_id) COMMENT '维保订单项 ID 作为主键',
    KEY idx_maintenance_order (maintenance_order_id) COMMENT '以维保订单建立索引',
    KEY idx_created_at (created_at) COMMENT '创建时间建立索引'
) ENGINE=InnoDB;
