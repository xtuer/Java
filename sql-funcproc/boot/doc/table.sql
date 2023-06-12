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
    state int default 0   comment '分片上传状态: 0 (初始化)、1 (上传成功)、2 (上传失败)、3 (上传中)',
    json text             comment '分片的 Json',

    primary key (file_uid, sn)
);
