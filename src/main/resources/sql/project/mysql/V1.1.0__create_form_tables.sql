CREATE TABLE wms_form_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '表单名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '表单编码',
    description VARCHAR(500) COMMENT '表单描述',
    config_json LONGTEXT NOT NULL COMMENT 'JSON配置内容',
    form_type VARCHAR(50) NOT NULL COMMENT '表单类型',
    version INT DEFAULT 1 COMMENT '版本号',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用，0禁用',
    created_by VARCHAR(50),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_time TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_form_config_code (code),
    INDEX idx_form_config_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE wms_form_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    form_code VARCHAR(50) NOT NULL COMMENT '表单编码',
    business_key VARCHAR(100) COMMENT '业务关联键',
    business_type VARCHAR(50) COMMENT '业务类型',
    form_data_json LONGTEXT NOT NULL COMMENT '表单数据JSON',
    data_status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '数据状态',
    created_by VARCHAR(50),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY fk_form_data_form_code (form_code)
        REFERENCES wms_form_config(code) ON DELETE CASCADE,
    INDEX idx_form_data_form_code (form_code),
    INDEX idx_form_data_business_key (business_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;