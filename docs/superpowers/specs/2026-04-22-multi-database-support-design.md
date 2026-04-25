# 表单设计器多数据库支持设计方案

## 项目概述
**目标**: 在已设计的表单设计器基础上，扩展支持多种关系型数据库的动态选择能力。每个表单可以指定使用特定的数据库（MySQL、SQL Server、Oracle）存储表单数据，实现数据存储的灵活配置。

**设计决策**:
- **集成方案**: 动态数据源路由（Dynamic DataSource Routing）
- **支持数据库**: MySQL、SQL Server、Oracle（暂不包含MongoDB）
- **配置存储**: 表单配置统一存储在MySQL主库中管理
- **连接管理**: 动态配置表方式管理数据库连接信息
- **事务处理**: 单库事务（每个数据库内部保证ACID）

**技术栈**:
- **后端**: Spring Boot 2.6.7 + MyBatis-Plus + Druid连接池
- **前端**: Vue 2.6.10 + Element UI 2.12.0
- **数据库**: MySQL（主库）、SQL Server、Oracle（动态数据源）

## 设计目标

### 1. 核心需求
- 每个表单可独立配置使用的数据库类型
- 支持运行时动态切换数据源
- 统一的数据库配置管理界面
- 透明化的多数据库操作接口

### 2. 成功标准
- ✅ 在表单设计器中新增数据库选择功能
- ✅ 管理员可配置多个数据库连接信息
- ✅ 表单数据根据配置自动路由到指定数据库
- ✅ 支持MySQL、SQL Server、Oracle三种数据库
- ✅ 保持原有表单设计器的所有功能
- ✅ 性能影响小于10%（相比单数据库方案）

## 整体架构

### 架构概览
```
客户端请求 → 表单数据服务 → 动态数据源路由 → 具体数据源
                                          ├── MySQL数据源（默认）
                                          ├── SQL Server数据源  
                                          └── Oracle数据源
```

### 核心组件
1. **数据库配置管理**: 管理所有可用的数据库连接信息
2. **动态数据源路由**: 基于Spring AbstractRoutingDataSource实现
3. **SQL方言处理器**: 处理不同数据库的语法差异
4. **数据源切换切面**: 自动化的数据源切换机制

## 详细设计

### 第一章：数据库配置管理

#### 1.1 数据库配置表设计
```sql
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
```

#### 1.2 默认数据源配置
- **主库(Default)**: 878项目现有的MySQL数据库，存储表单配置和系统数据
- **动态数据源**: 根据`wms_form_config`表中配置的`db_code`动态切换

### 第二章：表单配置扩展

#### 2.1 扩展表单配置表
在原有`wms_form_config`表基础上增加数据库关联字段：

```sql
ALTER TABLE wms_form_config 
ADD COLUMN db_code VARCHAR(50) COMMENT '关联的数据库编码',
ADD COLUMN table_name VARCHAR(100) COMMENT '自定义表名（可选）',
ADD INDEX idx_form_config_db_code (db_code);

-- 添加外键约束（可选）
ALTER TABLE wms_form_config 
ADD FOREIGN KEY fk_form_config_db_code (db_code) 
    REFERENCES wms_database_config(db_code) ON DELETE SET NULL;
```

#### 2.2 表单数据表命名规则
- **默认规则**: `form_data_[form_code]`（如`form_data_user_registration`）
- **自定义规则**: 如果`table_name`字段有值，使用该值作为表名
- **表前缀**: 所有动态创建的表使用`wms_form_dynamic_`前缀

### 第三章：动态数据源路由实现

#### 3.1 核心组件设计

**数据源上下文持有者（线程安全）**:
```java
public class DataSourceContextHolder {
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();
    
    public static void setDataSourceKey(String dataSourceKey) {
        CONTEXT_HOLDER.set(dataSourceKey);
    }
    
    public static String getDataSourceKey() {
        return CONTEXT_HOLDER.get();
    }
    
    public static void clearDataSourceKey() {
        CONTEXT_HOLDER.remove();
    }
}
```

**动态数据源配置**:
```java
@Configuration
public class DynamicDataSourceConfig {
    
    @Bean
    @Primary
    public DataSource dynamicDataSource() {
        Map<Object, Object> targetDataSources = new HashMap<>();
        
        // 1. 添加默认数据源（主库）
        DataSource defaultDataSource = createDefaultDataSource();
        targetDataSources.put("default", defaultDataSource);
        
        // 2. 从数据库配置表加载所有启用的数据源
        List<DatabaseConfigEntity> dbConfigs = databaseConfigService.getAllEnabledConfigs();
        for (DatabaseConfigEntity config : dbConfigs) {
            DataSource dataSource = createDataSourceFromConfig(config);
            targetDataSources.put(config.getDbCode(), dataSource);
        }
        
        // 3. 配置动态数据源
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setDefaultTargetDataSource(defaultDataSource);
        dynamicDataSource.setTargetDataSources(targetDataSources);
        dynamicDataSource.afterPropertiesSet();
        
        return dynamicDataSource;
    }
}
```

#### 3.2 数据源切换切面

```java
@Aspect
@Component
public class DataSourceAspect {
    
    /**
     * 在表单数据操作前自动切换数据源
     */
    @Before("execution(* cn.zdjc.wms.project.form.service.FormDataService.*(..)) && @annotation(formDataSource)")
    public void switchDataSource(FormDataSource formDataSource) {
        String formCode = formDataSource.value();
        FormConfigEntity config = formConfigService.getFormConfigByCode(formCode);
        
        if (config != null && StringUtils.isNotBlank(config.getDbCode())) {
            DynamicDataSource.switchDataSource(config.getDbCode());
        } else {
            DynamicDataSource.switchDataSource("default");
        }
    }
    
    /**
     * 操作完成后清理数据源上下文
     */
    @After("execution(* cn.zdjc.wms.project.form.service.FormDataService.*(..)) && @annotation(formDataSource)")
    public void clearDataSource(FormDataSource formDataSource) {
        DataSourceContextHolder.clearDataSourceKey();
    }
}
```

**自定义注解**:
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FormDataSource {
    String value(); // 表单编码
}
```

### 第四章：数据库方言支持

#### 4.1 数据库类型枚举
```java
public enum DatabaseType {
    MYSQL("MySQL", "com.mysql.cj.jdbc.Driver", "mysql"),
    SQLSERVER("SQL Server", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "sqlserver"),
    ORACLE("Oracle", "oracle.jdbc.OracleDriver", "oracle");
    
    private final String name;
    private final String driverClass;
    private final String vendor;
    
    // getter方法...
}
```

#### 4.2 SQL方言处理器
主要功能：
1. **分页SQL生成**: 为不同数据库生成正确的分页语法
2. **建表语句生成**: 处理自增主键等语法差异
3. **字段类型映射**: 统一Java类型到数据库类型的映射

**分页SQL示例**:
- **MySQL**: `SELECT * FROM table LIMIT #{offset}, #{pageSize}`
- **SQL Server**: 使用`ROW_NUMBER()`窗口函数
- **Oracle**: 使用`ROWNUM`伪列

#### 4.3 MyBatis-Plus方言配置
```java
@Configuration
public class MyBatisPlusConfig {
    
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 动态分页插件 - 根据当前数据源类型选择方言
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor() {
            @Override
            public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, 
                                   ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
                String dbCode = DataSourceContextHolder.getDataSourceKey();
                DatabaseConfigEntity config = databaseConfigService.getConfigByCode(dbCode);
                if (config != null) {
                    DatabaseType dbType = DatabaseType.valueOf(config.getDbType());
                    this.setDialectType(getDialectType(dbType));
                } else {
                    this.setDialectType(DbType.MYSQL);
                }
                super.beforeQuery(executor, ms, parameter, rowBounds, resultHandler, boundSql);
            }
            
            private String getDialectType(DatabaseType dbType) {
                switch (dbType) {
                    case MYSQL: return "mysql";
                    case SQLSERVER: return "sqlserver";
                    case ORACLE: return "oracle";
                    default: return "mysql";
                }
            }
        };
        paginationInterceptor.setMaxLimit(1000L);
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        return interceptor;
    }
}
```

## 实施计划

### 第一阶段：基础框架搭建（2周）
1. **数据库表创建**:
   - 创建`wms_database_config`表
   - 扩展`wms_form_config`表（添加`db_code`, `table_name`字段）
   
2. **后端基础组件**:
   - 实现数据库配置管理服务（CRUD接口）
   - 实现动态数据源路由框架
   - 实现数据源上下文管理

### 第二阶段：动态数据源集成（2周）
1. **数据源动态切换**:
   - 集成Spring AbstractRoutingDataSource
   - 实现数据源切换切面
   - 添加数据源监控和健康检查
   
2. **SQL方言支持**:
   - 实现SQL方言处理器
   - 配置MyBatis-Plus多数据库支持
   - 编写数据库类型转换工具

### 第三阶段：前端界面开发（1周）
1. **数据库管理界面**:
   - 数据库配置管理页面（增删改查）
   - 数据库连接测试功能
   - 数据库状态监控面板
   
2. **表单设计器扩展**:
   - 在表单设计界面添加数据库选择器
   - 数据库连接信息展示
   - 表单数据表名自定义功能

### 第四阶段：测试和优化（1周）
1. **功能测试**:
   - 多数据库连接测试
   - 数据源切换正确性验证
   - 分页查询和事务测试
   
2. **性能测试**:
   - 数据源切换性能基准测试
   - 多数据库并发压力测试
   - 连接池配置优化

## 风险评估和缓解措施

| 风险点 | 影响程度 | 缓解措施 |
|--------|----------|----------|
| 数据库驱动兼容性问题 | 高 | 提前测试各数据库版本的驱动兼容性，准备备用驱动版本 |
| 连接池资源泄漏 | 高 | 实施连接池监控，设置合理的超时和回收策略 |
| 数据源切换性能问题 | 中 | 使用线程本地存储，避免频繁的数据源重建 |
| 跨数据库事务不一致 | 中 | 明确只支持单库事务，不承诺跨库事务一致性 |
| SQL语法兼容性问题 | 中 | 使用SQL方言处理器，隔离数据库特定语法 |
| 前端界面复杂度增加 | 低 | 保持界面简洁，提供合理的默认值和引导 |

## 后续扩展规划

### 短期扩展（1-3个月）
1. **数据库连接池优化**: 根据使用情况动态调整连接池参数
2. **数据库监控告警**: 集成数据库性能监控和异常告警
3. **数据库备份恢复**: 支持动态数据源的备份和恢复策略

### 中期扩展（3-6个月）
1. **读写分离支持**: 支持主从数据库读写分离
2. **分库分表扩展**: 基于表单数据量的自动分表策略
3. **数据库迁移工具**: 提供数据库间数据迁移功能

### 长期扩展（6个月以上）
1. **多租户数据隔离**: 支持多租户场景下的数据库隔离
2. **数据库智能推荐**: 基于表单特性推荐合适的数据库类型
3. **云数据库集成**: 支持阿里云RDS、腾讯云CDB等云数据库

## 技术决策记录

### 1. 动态数据源路由方案选择
**决策**: 采用Spring AbstractRoutingDataSource而非抽象数据访问层
**理由**:
- Spring原生支持，社区方案成熟稳定
- 与878现有技术栈集成度更高
- 对于关系型数据库切换简单直接
- 配置集中管理，维护成本较低

### 2. MongoDB支持暂缓
**决策**: 暂时不包含MongoDB支持
**理由**:
- MongoDB与关系型数据库架构差异较大
- 需要单独的JSON转换层和查询优化
- 当前需求以关系型数据库为主
- 可后续作为独立扩展模块添加

### 3. 单库事务策略
**决策**: 只保证单数据库内部事务，不支持跨数据库分布式事务
**理由**:
- 跨数据库分布式事务实现复杂
- 性能开销较大，影响系统响应时间
- 当前业务场景未要求强一致的跨库事务
- 可通过业务层补偿机制处理一致性需求

### 4. SQL方言处理策略
**决策**: 在应用层实现SQL方言处理器
**理由**:
- 避免数据库特定语法污染业务代码
- 统一的分页、排序等通用操作接口
- 便于后续添加新的数据库类型支持
- 与MyBatis-Plus良好集成

## 附录

### A. 数据库驱动版本要求
- **MySQL**: mysql-connector-java 8.0.30+
- **SQL Server**: mssql-jdbc 10.2.0+
- **Oracle**: ojdbc8 21.8.0.0+

### B. 配置示例
```yaml
# application-datasource.yml 示例
form:
  datasource:
    mysql:
      driver-class-name: com.mysql.cj.jdbc.Driver
      validation-query: SELECT 1
    sqlserver:
      driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
      validation-query: SELECT 1
    oracle:
      driver-class-name: oracle.jdbc.OracleDriver
      validation-query: SELECT 1 FROM DUAL
```

### C. API接口扩展
**新增API**:
- `POST /api/database/config/save` - 保存数据库配置
- `GET /api/database/config/list` - 查询数据库配置列表
- `POST /api/database/config/{code}/test` - 测试数据库连接
- `POST /api/database/config/{code}/enable/{status}` - 启用/禁用数据库配置

**修改API**:
- `POST /api/form/config/save` - 增加`dbCode`和`tableName`参数
- `GET /api/form/config/{code}` - 返回结果包含数据库配置信息

### D. 变更记录
| 版本 | 日期 | 修改内容 | 修改人 |
|------|------|----------|--------|
| 1.0 | 2026-04-22 | 初始版本，完整的多数据库支持设计方案 | AI助手 |

---

**设计批准**: □ 已批准 □ 待批准

**批准人**: ____________________

**批准日期**: __________________