#-------------------------------------------
# 表名: message
# 作者: 黄彪
# 日期: 2021-08-21
# 版本: 1.0
# 描述: 消息表
#------------------------------------------
DROP TABLE IF EXISTS message;

CREATE TABLE message (
    message_id  bigint(20) NOT NULL  COMMENT 'auto increment id',
    sender_id   bigint(20) DEFAULT 0 COMMENT '发送者 ID',
    receiver_id bigint(20) NOT NULL  COMMENT '接收者 ID',
    target_id   bigint(20) NOT NULL  COMMENT '目标 ID',
    type        varchar(64) NOT NULL COMMENT '类型',
    content     text                 COMMENT '内容',
    `read`      tinyint(4) DEFAULT 0 COMMENT '是否已读，0 为否，1 为是',

    created_at datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (message_id),
    KEY idx_receiver (receiver_id),
    KEY idx_created_at (created_at)
) COMMENT = 'DB WorkerID Assigner for UID Generator', ENGINE = INNODB;
