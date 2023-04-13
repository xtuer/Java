create table dsc_ufile(
    file_uid  varchar(256) comment '唯一 ID: <fileMd5>',
    file_name varchar(256) comment '文件名',
    state int default 0    comment '上传状态: 0 (初始化)、1 (合并成功)、2 (合并失败)、3 (合并中)，例如要统计上传中的文件数量',
    json text              comment '上传文件的 Json，不包含分片数据',
    create_time datetime   comment '',
    primary key (file_uid)
);

create table dsc_ufile_chunk(
	file_uid varchar(256) comment '唯一 ID: <fileMd5>',
    sn int                comment '分片序号',
    state int default 0   comment '分片上传状态: 0 (初始化)、1 (导入成功)、2 (导入失败)、3 (导入中)、4 (排队等待)',
    json text             comment '分片的 Json',

    primary key (file_uid, sn)
);

-- SQL 文件导入任务。
create table dsc_sql_file_import_task(
    task_id varchar(64)   comment '任务唯一 ID',
    file_uid varchar(256) comment '唯一 ID: <fileMd5>',
    state int default 0   comment '导入状态: 0 (初始化)、1 (成功)、2 (失败)、3 (导入中)',
    start_time datetime   comment '导入开始时间',
    end_time   datetime   comment '导入结束时间',
    total_bytes     bigint default 0 comment 'SQL 文件的大小',
    committed_bytes bigint default 0 comment '已导入大小 (计算进度)',
    rollback_sql varchar(5120)       comment '发生错误时回滚的 SQL 语句',
    error text                       comment '发生错误的错误信息',

    primary key (task_id)
);
