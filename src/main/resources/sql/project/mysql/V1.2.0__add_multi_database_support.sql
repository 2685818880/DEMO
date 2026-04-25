CREATE TABLE wms_database_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    db_code VARCHAR(50) NOT NULL UNIQUE COMMENT '数据库编码',
    db_name VARCHAR(100) NOT NULL COMMENT '数据库名称',
    db_type VARCHAR(20) NOT NULL COMMENT '数据库类型: MYSQL, SQLSERVER, ORACLE',
    driver_class VARCHAR(200) NOT NULL COMMENT '驱动类',
    jdbc_url VARCHAR(500) NOT NULL COMMENT 'JDBC连接URL',
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码',
    initial_size INT DEFAULT 3 COMMENT '初始连接数',
    min_idle INT DEFAULT 3 COMMENT '最小空闲连接',
    max_active INT DEFAULT 60 COMMENT '最大活动连接',
    max_wait BIGINT DEFAULT 60000 COMMENT '获取连接最大等待时间(ms)',
    validation_query VARCHAR(100) COMMENT '验证查询语句',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用: 1启用, 0禁用',
    created_by VARCHAR(50),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_time TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_db_config_code (db_code),
    INDEX idx_db_config_type (db_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE wms_form_config
ADD COLUMN db_code VARCHAR(50) COMMENT '关联的数据库编码',
ADD COLUMN table_name VARCHAR(100) COMMENT '自定义表名（可选）',
ADD INDEX idx_form_config_db_code (db_code);