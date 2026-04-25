CREATE TABLE IF NOT EXISTS `wms_ai_provider` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '提供商名称',
    `logo` VARCHAR(500) DEFAULT '' COMMENT 'Logo URL',
    `api_base_url` VARCHAR(500) NOT NULL COMMENT 'API地址',
    `api_key_encrypted` TEXT COMMENT 'API密钥（AES加密）',
    `api_key_status` VARCHAR(20) DEFAULT 'not_hosted' COMMENT '密钥状态: hosted/not_hosted/expired',
    `last_update_time` DATETIME COMMENT '最后更新时间',
    `is_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用 1=是 0=否',
    `remark` VARCHAR(500) DEFAULT '' COMMENT '备注说明',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '逻辑删除 1=正常 0=删除',
    `create_by` VARCHAR(50) DEFAULT '' COMMENT '创建人',
    `create_datetime` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `last_modify_by` VARCHAR(50) DEFAULT '' COMMENT '修改人',
    `last_modify_datetime` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    INDEX `idx_name` (`name`),
    INDEX `idx_key_status` (`api_key_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI提供商配置表';

CREATE TABLE IF NOT EXISTS `wms_ai_model` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `provider_id` BIGINT NOT NULL COMMENT '提供商ID',
    `name` VARCHAR(200) NOT NULL COMMENT '模型名称',
    `type` VARCHAR(50) DEFAULT '' COMMENT '模型类型: llm/embedding/vision/etc',
    `is_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用 1=是 0=否',
    `config_json` TEXT COMMENT '模型配置（JSON）',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '逻辑删除',
    `create_datetime` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_provider_id` (`provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型配置表';
