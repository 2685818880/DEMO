-- 自定义报表配置表
-- 数据库: MySQL
-- 连接: 127.0.0.1:3306/wms878

CREATE TABLE IF NOT EXISTS wms_report_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    report_name VARCHAR(200) NOT NULL COMMENT '报表名称',
    report_code VARCHAR(100) NOT NULL UNIQUE COMMENT '报表编码',
    report_type VARCHAR(50) DEFAULT 'TABLE' COMMENT '报表类型: TABLE/CHART/MIXED',
    category VARCHAR(50) COMMENT '分组分类',
    config_json LONGTEXT NOT NULL COMMENT 'JSON配置内容',
    db_code VARCHAR(100) COMMENT '数据源编码',
    version INT DEFAULT 1 COMMENT '版本号',
    status TINYINT DEFAULT 0 COMMENT '状态: 0草稿 1发布',
    remark VARCHAR(500) COMMENT '备注',
    created_by VARCHAR(50) COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    last_modified_by VARCHAR(50) COMMENT '最后修改人',
    last_modified_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自定义报表配置表';
