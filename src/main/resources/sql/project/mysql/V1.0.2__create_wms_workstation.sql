CREATE TABLE `wms_workstation`
(
    `id`                   VARCHAR(255) NOT NULL,
    `workstation_code`     VARCHAR(255) COMMENT '工作站编号',
    `workstation_name`     VARCHAR(255) COMMENT '工作站名称',
    `workstation_describe` VARCHAR(255) COMMENT '工作站描述',
    `is_open`              VARCHAR(255) COMMENT '是否开启',
    `workstation_ip`       VARCHAR(255) COMMENT 'IP地址',
    `workstation_status`   VARCHAR(255) COMMENT '工作站状态',
    `workstation_mode`     VARCHAR(255) COMMENT '工作站模式',
    `operator`             VARCHAR(255) COMMENT '操作人',
    `create_datetime`          DATETIME,
    `create_by`            VARCHAR(255),
    `last_modify_datetime`     DATETIME,
    `last_modify_by`       VARCHAR(255),
    `deleted`              TINYINT,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='工作站状态表';

alter table wms_workstation add is_active TINYINT DEFAULT 0;
alter table wms_workstation add modified TINYINT DEFAULT 0;



