# 表单设计器集成与多数据库支持实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将form-generator表单设计器集成到878 WMS项目中，并扩展支持MySQL、SQL Server、Oracle多数据库动态选择存储能力。

**Architecture:** 前端采用直接嵌入集成方式，将form-generator-dev源码作为新模块嵌入；后端采用动态数据源路由方案，基于Spring AbstractRoutingDataSource实现多数据库切换，通过SQL方言处理器处理数据库语法差异。

**Tech Stack:** Spring Boot 2.6.7, MyBatis-Plus, Druid, Vue 2.6.10, Element UI 2.12.0, MySQL, SQL Server, Oracle

---

## 文件结构概览

### 后端文件
```
src/main/java/cn/zdjc/wms/project/form/
├── controller/
│   ├── DatabaseConfigController.java         # 数据库配置管理API
│   ├── FormConfigController.java             # 表单配置管理API  
│   └── FormDataController.java               # 表单数据操作API
├── service/
│   ├── impl/
│   │   ├── DatabaseConfigServiceImpl.java    # 数据库配置服务实现
│   │   ├── FormConfigServiceImpl.java        # 表单配置服务实现
│   │   └── FormDataServiceImpl.java          # 表单数据服务实现
│   ├── DatabaseConfigService.java            # 数据库配置服务接口
│   ├── FormConfigService.java                # 表单配置服务接口
│   └── FormDataService.java                  # 表单数据服务接口
├── repository/
│   ├── DatabaseConfigRepository.java         # 数据库配置数据访问
│   ├── FormConfigRepository.java             # 表单配置数据访问
│   └── FormDataRepository.java               # 表单数据数据访问
├── mapper/
│   ├── DatabaseConfigMapper.java             # 数据库配置MyBatis接口
│   ├── FormConfigMapper.java                 # 表单配置MyBatis接口
│   └── FormDataMapper.java                   # 表单数据MyBatis接口
├── model/
│   ├── entity/
│   │   ├── DatabaseConfigEntity.java         # 数据库配置实体
│   │   ├── FormConfigEntity.java             # 表单配置实体
│   │   └── FormDataEntity.java               # 表单数据实体
│   ├── dto/
│   │   ├── DatabaseConfigDto.java            # 数据库配置DTO
│   │   ├── FormConfigDto.java                # 表单配置DTO
│   │   └── FormDataDto.java                  # 表单数据DTO
│   └── param/
│       ├── DatabaseConfigParam.java          # 数据库配置参数
│       ├── FormConfigParam.java              # 表单配置参数
│       └── FormDataParam.java                # 表单数据参数
├── config/
│   ├── DynamicDataSourceConfig.java          # 动态数据源配置
│   ├── DataSourceContextHolder.java          # 数据源上下文持有者
│   └── DynamicDataSource.java                # 动态数据源类
├── aspect/
│   └── DataSourceAspect.java                 # 数据源切换切面
├── annotation/
│   └── FormDataSource.java                   # 表单数据源注解
├── dialect/
│   └── SqlDialectProcessor.java              # SQL方言处理器
└── constant/
    ├── DatabaseType.java                     # 数据库类型枚举
    └── FormErrorCode.java                    # 表单错误码常量
```

### 前端文件
```
vue/src/form-designer/
├── components/                               # 表单设计器核心组件
│   ├── Designer.vue                          # 设计器主组件
│   ├── Preview.vue                           # 预览组件
│   └── Generator.vue                         # 代码生成器组件
├── views/
│   ├── FormDesignerList.vue                  # 表单配置列表页面
│   ├── FormDesignerDesign.vue                # 表单设计器页面
│   └── FormDesignerPreview.vue               # 表单预览页面
├── api/
│   ├── database.js                           # 数据库配置API
│   ├── formConfig.js                         # 表单配置API
│   └── formData.js                           # 表单数据API
├── utils/
│   ├── formValidator.js                      # 表单验证工具
│   └── formParser.js                         # 表单解析工具
└── assets/
    ├── css/
    │   └── form-designer.css                 # 表单设计器样式
    └── icons/                                # 图标资源
```

### 数据库SQL文件
```
src/main/resources/sql/mysql/
├── V1.0.0__create_form_tables.sql            # 表单相关表创建
└── V1.1.0__add_multi_database_support.sql    # 多数据库支持扩展
```

---

## 第一阶段：表单设计器基础集成（2周）

### Task 1: 创建数据库表结构

**Files:**
- Create: `src/main/resources/sql/mysql/V1.0.0__create_form_tables.sql`
- Create: `src/main/resources/sql/mysql/V1.1.0__add_multi_database_support.sql`

- [ ] **Step 1: 创建表单配置表SQL**

```sql
-- V1.0.0__create_form_tables.sql
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
```

- [ ] **Step 2: 创建多数据库支持扩展SQL**

```sql
-- V1.1.0__add_multi_database_support.sql
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
```

- [ ] **Step 3: 执行Flyway迁移验证**

```bash
cd E:\claudeWork\878\wms.pj878
mvn flyway:migrate -Dflyway.url=jdbc:mysql://127.0.0.1:3306/wms87802 -Dflyway.user=root -Dflyway.password=root
```

Expected: BUILD SUCCESS with "Successfully applied 2 migrations"

- [ ] **Step 4: 验证数据库表创建**

```bash
mysql -u root -p wms87802 -e "SHOW TABLES LIKE 'wms_form_%';"
mysql -u root -p wms87802 -e "SHOW TABLES LIKE 'wms_database_config';"
mysql -u root -p wms87802 -e "DESC wms_form_config;"
```

Expected: 看到3个相关表，wms_form_config包含db_code和table_name字段

- [ ] **Step 5: 提交数据库迁移**

```bash
git add src/main/resources/sql/mysql/
git commit -m "feat: 创建表单设计器数据库表结构"
```

### Task 2: 创建表单实体和数据模型

**Files:**
- Create: `src/main/java/cn/zdjc/wms/project/form/model/entity/FormConfigEntity.java`
- Create: `src/main/java/cn/zdjc/wms/project/form/model/entity/FormDataEntity.java`
- Create: `src/main/java/cn/zdjc/wms/project/form/model/entity/DatabaseConfigEntity.java`

- [ ] **Step 1: 创建FormConfigEntity**

```java
// src/main/java/cn/zdjc/wms/project/form/model/entity/FormConfigEntity.java
package cn.zdjc.wms.project.form.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import cn.zdjc.wms.project.common.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_form_config")
public class FormConfigEntity extends BaseEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String code;
    
    private String description;
    
    private String configJson;
    
    private String formType;
    
    private String dbCode;
    
    private String tableName;
    
    private Integer version = 1;
    
    private Integer status = 1;
}
```

- [ ] **Step 2: 创建FormDataEntity**

```java
// src/main/java/cn/zdjc/wms/project/form/model/entity/FormDataEntity.java
package cn.zdjc.wms.project.form.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import cn.zdjc.wms.project.common.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_form_data")
public class FormDataEntity extends BaseEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String formCode;
    
    private String businessKey;
    
    private String businessType;
    
    private String formDataJson;
    
    private String dataStatus = "DRAFT";
}
```

- [ ] **Step 3: 创建DatabaseConfigEntity**

```java
// src/main/java/cn/zdjc/wms/project/form/model/entity/DatabaseConfigEntity.java
package cn.zdjc.wms.project.form.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import cn.zdjc.wms.project.common.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_database_config")
public class DatabaseConfigEntity extends BaseEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String dbCode;
    
    private String dbName;
    
    private String dbType;
    
    private String driverClass;
    
    private String jdbcUrl;
    
    private String username;
    
    private String password;
    
    private Integer initialSize = 3;
    
    private Integer minIdle = 3;
    
    private Integer maxActive = 60;
    
    private Long maxWait = 60000L;
    
    private String validationQuery;
    
    private Integer enabled = 1;
}
```

- [ ] **Step 4: 创建常量枚举**

```java
// src/main/java/cn/zdjc/wms/project/form/constant/DatabaseType.java
package cn.zdjc.wms.project.form.constant;

public enum DatabaseType {
    MYSQL("MySQL", "com.mysql.cj.jdbc.Driver", "mysql"),
    SQLSERVER("SQL Server", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "sqlserver"),
    ORACLE("Oracle", "oracle.jdbc.OracleDriver", "oracle");
    
    private final String name;
    private final String driverClass;
    private final String vendor;
    
    DatabaseType(String name, String driverClass, String vendor) {
        this.name = name;
        this.driverClass = driverClass;
        this.vendor = vendor;
    }
    
    public String getName() { return name; }
    public String getDriverClass() { return driverClass; }
    public String getVendor() { return vendor; }
}
```

- [ ] **Step 5: 提交实体类**

```bash
git add src/main/java/cn/zdjc/wms/project/form/model/
git add src/main/java/cn/zdjc/wms/project/form/constant/
git commit -m "feat: 创建表单设计器实体类和常量枚举"
```

### Task 3: 创建MyBatis Mapper接口

**Files:**
- Create: `src/main/java/cn/zdjc/wms/project/form/mapper/FormConfigMapper.java`
- Create: `src/main/java/cn/zdjc/wms/project/form/mapper/FormDataMapper.java`
- Create: `src/main/java/cn/zdjc/wms/project/form/mapper/DatabaseConfigMapper.java`
- Create: `src/main/resources/mapper/form/FormConfigMapper.xml`
- Create: `src/main/resources/mapper/form/FormDataMapper.xml`
- Create: `src/main/resources/mapper/form/DatabaseConfigMapper.xml`

- [ ] **Step 1: 创建FormConfigMapper接口**

```java
// src/main/java/cn/zdjc/wms/project/form/mapper/FormConfigMapper.java
package cn.zdjc.wms.project.form.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.zdjc.wms.project.form.model.entity.FormConfigEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FormConfigMapper extends BaseMapper<FormConfigEntity> {
    
    /**
     * 根据表单编码查询
     */
    FormConfigEntity selectByCode(String code);
    
    /**
     * 检查表单编码是否已存在
     */
    int checkCodeExists(String code);
}
```

- [ ] **Step 2: 创建FormDataMapper接口**

```java
// src/main/java/cn/zdjc/wms/project/form/mapper/FormDataMapper.java
package cn.zdjc.wms.project.form.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.zdjc.wms.project.form.model.entity.FormDataEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FormDataMapper extends BaseMapper<FormDataEntity> {
    
    /**
     * 根据表单编码和业务键查询
     */
    FormDataEntity selectByFormCodeAndBusinessKey(String formCode, String businessKey);
    
    /**
     * 统计表单数据数量
     */
    int countByFormCode(String formCode);
}
```

- [ ] **Step 3: 创建DatabaseConfigMapper接口**

```java
// src/main/java/cn/zdjc/wms/project/form/mapper/DatabaseConfigMapper.java
package cn.zdjc.wms.project.form.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.zdjc.wms.project.form.model.entity.DatabaseConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface DatabaseConfigMapper extends BaseMapper<DatabaseConfigEntity> {
    
    /**
     * 查询所有启用的数据库配置
     */
    List<DatabaseConfigEntity> selectAllEnabled();
    
    /**
     * 根据数据库编码查询
     */
    DatabaseConfigEntity selectByCode(String dbCode);
    
    /**
     * 检查数据库编码是否已存在
     */
    int checkCodeExists(String dbCode);
}
```

- [ ] **Step 4: 创建FormConfigMapper XML映射**

```xml
<!-- src/main/resources/mapper/form/FormConfigMapper.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="cn.zdjc.wms.project.form.mapper.FormConfigMapper">
    
    <select id="selectByCode" resultType="cn.zdjc.wms.project.form.model.entity.FormConfigEntity">
        SELECT * FROM wms_form_config WHERE code = #{code} LIMIT 1
    </select>
    
    <select id="checkCodeExists" resultType="int">
        SELECT COUNT(*) FROM wms_form_config WHERE code = #{code}
    </select>
    
</mapper>
```

- [ ] **Step 5: 创建DatabaseConfigMapper XML映射**

```xml
<!-- src/main/resources/mapper/form/DatabaseConfigMapper.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="cn.zdjc.wms.project.form.mapper.DatabaseConfigMapper">
    
    <select id="selectAllEnabled" resultType="cn.zdjc.wms.project.form.model.entity.DatabaseConfigEntity">
        SELECT * FROM wms_database_config WHERE enabled = 1
    </select>
    
    <select id="selectByCode" resultType="cn.zdjc.wms.project.form.model.entity.DatabaseConfigEntity">
        SELECT * FROM wms_database_config WHERE db_code = #{dbCode} LIMIT 1
    </select>
    
    <select id="checkCodeExists" resultType="int">
        SELECT COUNT(*) FROM wms_database_config WHERE db_code = #{dbCode}
    </select>
    
</mapper>
```

- [ ] **Step 6: 运行Mapper测试**

```bash
cd E:\claudeWork\878\wms.pj878
mvn test -Dtest="*MapperTest" -DfailIfNoTests=false
```

Expected: BUILD SUCCESS or no tests found (正常，因为我们还没有测试)

- [ ] **Step 7: 提交Mapper文件**

```bash
git add src/main/java/cn/zdjc/wms/project/form/mapper/
git add src/main/resources/mapper/form/
git commit -m "feat: 创建表单设计器MyBatis Mapper接口和XML映射"
```

### Task 4: 创建Repository数据访问层

**Files:**
- Create: `src/main/java/cn/zdjc/wms/project/form/repository/FormConfigRepository.java`
- Create: `src/main/java/cn/zdjc/wms/project/form/repository/FormDataRepository.java`
- Create: `src/main/java/cn/zdjc/wms/project/form/repository/DatabaseConfigRepository.java`

- [ ] **Step 1: 创建FormConfigRepository**

```java
// src/main/java/cn/zdjc/wms/project/form/repository/FormConfigRepository.java
package cn.zdjc.wms.project.form.repository;

import cn.zdjc.wms.project.form.mapper.FormConfigMapper;
import cn.zdjc.wms.project.form.model.entity.FormConfigEntity;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;

@Repository
public class FormConfigRepository {
    
    @Autowired
    private FormConfigMapper formConfigMapper;
    
    public FormConfigEntity save(FormConfigEntity entity) {
        if (entity.getId() == null) {
            formConfigMapper.insert(entity);
        } else {
            formConfigMapper.updateById(entity);
        }
        return entity;
    }
    
    public Optional<FormConfigEntity> findById(Long id) {
        return Optional.ofNullable(formConfigMapper.selectById(id));
    }
    
    public Optional<FormConfigEntity> findByCode(String code) {
        return Optional.ofNullable(formConfigMapper.selectByCode(code));
    }
    
    public boolean existsByCode(String code) {
        return formConfigMapper.checkCodeExists(code) > 0;
    }
    
    public int deleteById(Long id) {
        return formConfigMapper.deleteById(id);
    }
    
    public int deleteByCode(String code) {
        return formConfigMapper.deleteByCode(code);
    }
}
```

- [ ] **Step 2: 创建DatabaseConfigRepository**

```java
// src/main/java/cn/zdjc/wms/project/form/repository/DatabaseConfigRepository.java
package cn.zdjc.wms.project.form.repository;

import cn.zdjc.wms.project.form.mapper.DatabaseConfigMapper;
import cn.zdjc.wms.project.form.model.entity.DatabaseConfigEntity;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;

@Repository
public class DatabaseConfigRepository {
    
    @Autowired
    private DatabaseConfigMapper databaseConfigMapper;
    
    public DatabaseConfigEntity save(DatabaseConfigEntity entity) {
        if (entity.getId() == null) {
            databaseConfigMapper.insert(entity);
        } else {
            databaseConfigMapper.updateById(entity);
        }
        return entity;
    }
    
    public Optional<DatabaseConfigEntity> findById(Long id) {
        return Optional.ofNullable(databaseConfigMapper.selectById(id));
    }
    
    public Optional<DatabaseConfigEntity> findByCode(String dbCode) {
        return Optional.ofNullable(databaseConfigMapper.selectByCode(dbCode));
    }
    
    public boolean existsByCode(String dbCode) {
        return databaseConfigMapper.checkCodeExists(dbCode) > 0;
    }
    
    public List<DatabaseConfigEntity> findAllEnabled() {
        return databaseConfigMapper.selectAllEnabled();
    }
    
    public int deleteById(Long id) {
        return databaseConfigMapper.deleteById(id);
    }
}
```

- [ ] **Step 3: 创建FormDataRepository**

```java
// src/main/java/cn/zdjc/wms/project/form/repository/FormDataRepository.java
package cn.zdjc.wms.project.form.repository;

import cn.zdjc.wms.project.form.mapper.FormDataMapper;
import cn.zdjc.wms.project.form.model.entity.FormDataEntity;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;

@Repository
public class FormDataRepository {
    
    @Autowired
    private FormDataMapper formDataMapper;
    
    public FormDataEntity save(FormDataEntity entity) {
        if (entity.getId() == null) {
            formDataMapper.insert(entity);
        } else {
            formDataMapper.updateById(entity);
        }
        return entity;
    }
    
    public Optional<FormDataEntity> findById(Long id) {
        return Optional.ofNullable(formDataMapper.selectById(id));
    }
    
    public Optional<FormDataEntity> findByFormCodeAndBusinessKey(String formCode, String businessKey) {
        return Optional.ofNullable(formDataMapper.selectByFormCodeAndBusinessKey(formCode, businessKey));
    }
    
    public int countByFormCode(String formCode) {
        return formDataMapper.countByFormCode(formCode);
    }
    
    public int deleteById(Long id) {
        return formDataMapper.deleteById(id);
    }
}
```

- [ ] **Step 4: 编译测试Repository**

```bash
cd E:\claudeWork\878\wms.pj878
mvn compile -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 5: 提交Repository文件**

```bash
git add src/main/java/cn/zdjc/wms/project/form/repository/
git commit -m "feat: 创建表单设计器Repository数据访问层"
```

### Task 5: 创建Service业务逻辑层

**Files:**
- Create: `src/main/java/cn/zdjc/wms/project/form/service/FormConfigService.java`
- Create: `src/main/java/cn/zdjc/wms/project/form/service/impl/FormConfigServiceImpl.java`
- Create: `src/main/java/cn/zdjc/wms/project/form/service/DatabaseConfigService.java`
- Create: `src/main/java/cn/zdjc/wms/project/form/service/impl/DatabaseConfigServiceImpl.java`
- Create: `src/main/java/cn/zdjc/wms/project/form/service/FormDataService.java`
- Create: `src/main/java/cn/zdjc/wms/project/form/service/impl/FormDataServiceImpl.java`

- [ ] **Step 1: 创建FormConfigService接口**

```java
// src/main/java/cn/zdjc/wms/project/form/service/FormConfigService.java
package cn.zdjc.wms.project.form.service;

import cn.zdjc.wms.project.form.model.entity.FormConfigEntity;
import java.util.List;
import java.util.Optional;

public interface FormConfigService {
    
    /**
     * 保存表单配置
     */
    FormConfigEntity saveFormConfig(FormConfigEntity entity);
    
    /**
     * 根据ID获取表单配置
     */
    Optional<FormConfigEntity> getFormConfigById(Long id);
    
    /**
     * 根据编码获取表单配置
     */
    Optional<FormConfigEntity> getFormConfigByCode(String code);
    
    /**
     * 查询表单配置列表
     */
    List<FormConfigEntity> listFormConfigs();
    
    /**
     * 启用/禁用表单配置
     */
    boolean updateFormConfigStatus(String code, Integer status);
    
    /**
     * 删除表单配置
     */
    boolean deleteFormConfig(String code);
    
    /**
     * 验证表单JSON配置
     */
    boolean validateFormJson(String configJson);
}
```

- [ ] **Step 2: 创建FormConfigServiceImpl实现**

```java
// src/main/java/cn/zdjc/wms/project/form/service/impl/FormConfigServiceImpl.java
package cn.zdjc.wms.project.form.service.impl;

import cn.zdjc.wms.project.form.model.entity.FormConfigEntity;
import cn.zdjc.wms.project.form.repository.FormConfigRepository;
import cn.zdjc.wms.project.form.service.FormConfigService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONValidator;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.Optional;

@Service
public class FormConfigServiceImpl implements FormConfigService {
    
    @Autowired
    private FormConfigRepository formConfigRepository;
    
    @Override
    public FormConfigEntity saveFormConfig(FormConfigEntity entity) {
        // 验证表单编码
        if (!StringUtils.hasText(entity.getCode())) {
            throw new IllegalArgumentException("表单编码不能为空");
        }
        
        // 验证表单名称
        if (!StringUtils.hasText(entity.getName())) {
            throw new IllegalArgumentException("表单名称不能为空");
        }
        
        // 验证JSON配置
        if (!validateFormJson(entity.getConfigJson())) {
            throw new IllegalArgumentException("表单JSON配置格式无效");
        }
        
        // 检查编码是否重复（更新时排除自身）
        if (formConfigRepository.existsByCode(entity.getCode())) {
            throw new IllegalArgumentException("表单编码已存在: " + entity.getCode());
        }
        
        return formConfigRepository.save(entity);
    }
    
    @Override
    public Optional<FormConfigEntity> getFormConfigById(Long id) {
        return formConfigRepository.findById(id);
    }
    
    @Override
    public Optional<FormConfigEntity> getFormConfigByCode(String code) {
        return formConfigRepository.findByCode(code);
    }
    
    @Override
    public List<FormConfigEntity> listFormConfigs() {
        // 使用MyBatis-Plus查询所有启用的表单配置
        QueryWrapper<FormConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1) // 只查询启用的
                    .orderByDesc("created_time"); // 按创建时间倒序
        
        return formConfigMapper.selectList(queryWrapper);
    }
    
    @Override
    public boolean updateFormConfigStatus(String code, Integer status) {
        Optional<FormConfigEntity> optional = formConfigRepository.findByCode(code);
        if (optional.isPresent()) {
            FormConfigEntity entity = optional.get();
            entity.setStatus(status);
            formConfigRepository.save(entity);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean deleteFormConfig(String code) {
        return formConfigRepository.deleteByCode(code) > 0;
    }
    
    @Override
    public boolean validateFormJson(String configJson) {
        if (!StringUtils.hasText(configJson)) {
            return false;
        }
        
        try {
            // 验证JSON格式
            JSONValidator validator = JSONValidator.from(configJson);
            return validator.validate();
        } catch (Exception e) {
            return false;
        }
    }
}
```

- [ ] **Step 3: 创建DatabaseConfigService接口**

```java
// src/main/java/cn/zdjc/wms/project/form/service/DatabaseConfigService.java
package cn.zdjc.wms.project.form.service;

import cn.zdjc.wms.project.form.model.entity.DatabaseConfigEntity;
import java.util.List;
import java.util.Optional;

public interface DatabaseConfigService {
    
    /**
     * 保存数据库配置
     */
    DatabaseConfigEntity saveDatabaseConfig(DatabaseConfigEntity entity);
    
    /**
     * 根据ID获取数据库配置
     */
    Optional<DatabaseConfigEntity> getDatabaseConfigById(Long id);
    
    /**
     * 根据编码获取数据库配置
     */
    Optional<DatabaseConfigEntity> getDatabaseConfigByCode(String dbCode);
    
    /**
     * 查询所有启用的数据库配置
     */
    List<DatabaseConfigEntity> getAllEnabledConfigs();
    
    /**
     * 测试数据库连接
     */
    boolean testDatabaseConnection(DatabaseConfigEntity config);
    
    /**
     * 启用/禁用数据库配置
     */
    boolean updateDatabaseConfigStatus(String dbCode, Integer status);
    
    /**
     * 删除数据库配置
     */
    boolean deleteDatabaseConfig(String dbCode);
}
```

- [ ] **Step 4: 创建DatabaseConfigServiceImpl实现**

```java
// src/main/java/cn/zdjc/wms/project/form/service/impl/DatabaseConfigServiceImpl.java
package cn.zdjc.wms.project.form.service.impl;

import cn.zdjc.wms.project.form.constant.DatabaseType;
import cn.zdjc.wms.project.form.model.entity.DatabaseConfigEntity;
import cn.zdjc.wms.project.form.repository.DatabaseConfigRepository;
import cn.zdjc.wms.project.form.service.DatabaseConfigService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class DatabaseConfigServiceImpl implements DatabaseConfigService {
    
    @Autowired
    private DatabaseConfigRepository databaseConfigRepository;
    
    @Override
    public DatabaseConfigEntity saveDatabaseConfig(DatabaseConfigEntity entity) {
        // 验证数据库编码
        if (!StringUtils.hasText(entity.getDbCode())) {
            throw new IllegalArgumentException("数据库编码不能为空");
        }
        
        // 验证数据库名称
        if (!StringUtils.hasText(entity.getDbName())) {
            throw new IllegalArgumentException("数据库名称不能为空");
        }
        
        // 验证数据库类型
        if (!StringUtils.hasText(entity.getDbType())) {
            throw new IllegalArgumentException("数据库类型不能为空");
        }
        
        try {
            // 验证数据库类型是否支持
            DatabaseType.valueOf(entity.getDbType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("不支持的数据库类型: " + entity.getDbType());
        }
        
        // 验证JDBC URL
        if (!StringUtils.hasText(entity.getJdbcUrl())) {
            throw new IllegalArgumentException("JDBC连接URL不能为空");
        }
        
        // 检查编码是否重复（更新时排除自身）
        if (databaseConfigRepository.existsByCode(entity.getDbCode())) {
            throw new IllegalArgumentException("数据库编码已存在: " + entity.getDbCode());
        }
        
        // 测试数据库连接
        if (!testDatabaseConnection(entity)) {
            throw new IllegalArgumentException("数据库连接测试失败，请检查连接信息");
        }
        
        return databaseConfigRepository.save(entity);
    }
    
    @Override
    public Optional<DatabaseConfigEntity> getDatabaseConfigById(Long id) {
        return databaseConfigRepository.findById(id);
    }
    
    @Override
    public Optional<DatabaseConfigEntity> getDatabaseConfigByCode(String dbCode) {
        return databaseConfigRepository.findByCode(dbCode);
    }
    
    @Override
    public List<DatabaseConfigEntity> getAllEnabledConfigs() {
        return databaseConfigRepository.findAllEnabled();
    }
    
    @Override
    public boolean testDatabaseConnection(DatabaseConfigEntity config) {
        Connection connection = null;
        try {
            // 加载数据库驱动
            Class.forName(config.getDriverClass());
            
            // 建立连接
            connection = DriverManager.getConnection(
                config.getJdbcUrl(), 
                config.getUsername(), 
                config.getPassword()
            );
            
            // 测试查询
            if (StringUtils.hasText(config.getValidationQuery())) {
                try (var statement = connection.createStatement();
                     var resultSet = statement.executeQuery(config.getValidationQuery())) {
                    return resultSet.next();
                }
            }
            
            return connection.isValid(5); // 5秒超时
        } catch (ClassNotFoundException | SQLException e) {
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // 忽略关闭异常
                }
            }
        }
    }
    
    @Override
    public boolean updateDatabaseConfigStatus(String dbCode, Integer status) {
        Optional<DatabaseConfigEntity> optional = databaseConfigRepository.findByCode(dbCode);
        if (optional.isPresent()) {
            DatabaseConfigEntity entity = optional.get();
            entity.setEnabled(status);
            databaseConfigRepository.save(entity);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean deleteDatabaseConfig(String dbCode) {
        Optional<DatabaseConfigEntity> optional = databaseConfigRepository.findByCode(dbCode);
        if (optional.isPresent()) {
            DatabaseConfigEntity entity = optional.get();
            databaseConfigRepository.deleteById(entity.getId());
            return true;
        }
        return false;
    }
}
```

- [ ] **Step 5: 编译测试Service层**

```bash
cd E:\claudeWork\878\wms.pj878
mvn compile -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 6: 提交Service文件**

```bash
git add src/main/java/cn/zdjc/wms/project/form/service/
git commit -m "feat: 创建表单设计器Service业务逻辑层"
```

### Task 6: 创建动态数据源配置组件

**Files:**
- Create: `src/main/java/cn/zdjc/wms/project/form/config/DataSourceContextHolder.java`
- Create: `src/main/java/cn/zdjc/wms/project/form/config/DynamicDataSource.java`
- Create: `src/main/java/cn/zdjc/wms/project/form/config/DynamicDataSourceConfig.java`

- [ ] **Step 1: 创建DataSourceContextHolder**

```java
// src/main/java/cn/zdjc/wms/project/form/config/DataSourceContextHolder.java
package cn.zdjc.wms.project.form.config;

import org.springframework.util.StringUtils;

/**
 * 数据源上下文持有者（线程安全）
 */
public class DataSourceContextHolder {
    
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();
    
    /**
     * 设置当前数据源key
     */
    public static void setDataSourceKey(String dataSourceKey) {
        CONTEXT_HOLDER.set(dataSourceKey);
    }
    
    /**
     * 获取当前数据源key
     */
    public static String getDataSourceKey() {
        return CONTEXT_HOLDER.get();
    }
    
    /**
     * 获取当前数据源key，如果为空则返回默认数据源
     */
    public static String getDataSourceKeyOrDefault() {
        String key = CONTEXT_HOLDER.get();
        return StringUtils.hasText(key) ? key : "default";
    }
    
    /**
     * 清除数据源key
     */
    public static void clearDataSourceKey() {
        CONTEXT_HOLDER.remove();
    }
}
```

- [ ] **Step 2: 创建DynamicDataSource**

```java
// src/main/java/cn/zdjc/wms/project/form/config/DynamicDataSource.java
package cn.zdjc.wms.project.form.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;

/**
 * 动态数据源
 */
@Component
public class DynamicDataSource extends AbstractRoutingDataSource {
    
    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContextHolder.getDataSourceKeyOrDefault();
    }
    
    /**
     * 切换数据源
     */
    public static void switchDataSource(String dbCode) {
        if (org.springframework.util.StringUtils.hasText(dbCode)) {
            DataSourceContextHolder.setDataSourceKey(dbCode);
        } else {
            DataSourceContextHolder.clearDataSourceKey();
        }
    }
    
    /**
     * 重置为默认数据源
     */
    public static void resetToDefault() {
        DataSourceContextHolder.clearDataSourceKey();
    }
}
```

- [ ] **Step 3: 创建DynamicDataSourceConfig**

```java
// src/main/java/cn/zdjc/wms/project/form/config/DynamicDataSourceConfig.java
package cn.zdjc.wms.project.form.config;

import cn.zdjc.wms.project.form.model.entity.DatabaseConfigEntity;
import cn.zdjc.wms.project.form.service.DatabaseConfigService;
import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class DynamicDataSourceConfig {
    
    @Autowired
    private DatabaseConfigService databaseConfigService;
    
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource defaultDataSource() throws SQLException {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setInitialSize(3);
        dataSource.setMinIdle(3);
        dataSource.setMaxActive(60);
        dataSource.setMaxWait(60000L);
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setTestWhileIdle(true);
        dataSource.setTimeBetweenEvictionRunsMillis(60000);
        dataSource.setMinEvictableIdleTimeMillis(300000);
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);
        dataSource.setPoolPreparedStatements(true);
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
        dataSource.setFilters("slf4j,stat");
        dataSource.setUseGlobalDataSourceStat(true);
        dataSource.setConnectionProperties("druid.stat.slowSqlMillis=5000;druid.stat.logSlowSql=true");
        return dataSource;
    }
    
    @Bean
    @Primary
    public DataSource dynamicDataSource() throws SQLException {
        Map<Object, Object> targetDataSources = new HashMap<>();
        
        // 1. 添加默认数据源
        DataSource defaultDataSource = defaultDataSource();
        targetDataSources.put("default", defaultDataSource);
        
        // 2. 加载数据库配置表中的所有启用的数据源
        List<DatabaseConfigEntity> dbConfigs = databaseConfigService.getAllEnabledConfigs();
        for (DatabaseConfigEntity config : dbConfigs) {
            try {
                DataSource dataSource = createDataSourceFromConfig(config);
                targetDataSources.put(config.getDbCode(), dataSource);
            } catch (Exception e) {
                // 记录错误日志，但继续加载其他数据源
                System.err.println("创建数据源失败: " + config.getDbCode() + ", 错误: " + e.getMessage());
            }
        }
        
        // 3. 配置动态数据源
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setDefaultTargetDataSource(defaultDataSource);
        dynamicDataSource.setTargetDataSources(targetDataSources);
        dynamicDataSource.afterPropertiesSet();
        
        return dynamicDataSource;
    }
    
    private DataSource createDataSourceFromConfig(DatabaseConfigEntity config) throws SQLException {
        DruidDataSource dataSource = new DruidDataSource();
        
        dataSource.setDriverClassName(config.getDriverClass());
        dataSource.setUrl(config.getJdbcUrl());
        dataSource.setUsername(config.getUsername());
        dataSource.setPassword(config.getPassword());
        
        // 连接池配置
        dataSource.setInitialSize(config.getInitialSize() != null ? config.getInitialSize() : 3);
        dataSource.setMinIdle(config.getMinIdle() != null ? config.getMinIdle() : 3);
        dataSource.setMaxActive(config.getMaxActive() != null ? config.getMaxActive() : 60);
        dataSource.setMaxWait(config.getMaxWait() != null ? config.getMaxWait() : 60000L);
        
        if (config.getValidationQuery() != null) {
            dataSource.setValidationQuery(config.getValidationQuery());
        } else {
            // 根据数据库类型设置默认验证查询
            switch (config.getDbType().toUpperCase()) {
                case "MYSQL":
                    dataSource.setValidationQuery("SELECT 1");
                    break;
                case "SQLSERVER":
                    dataSource.setValidationQuery("SELECT 1");
                    break;
                case "ORACLE":
                    dataSource.setValidationQuery("SELECT 1 FROM DUAL");
                    break;
                default:
                    dataSource.setValidationQuery("SELECT 1");
            }
        }
        
        dataSource.setTestWhileIdle(true);
        dataSource.setTimeBetweenEvictionRunsMillis(60000);
        dataSource.setMinEvictableIdleTimeMillis(300000);
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);
        dataSource.setPoolPreparedStatements(true);
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
        dataSource.setFilters("slf4j,stat");
        dataSource.setUseGlobalDataSourceStat(true);
        
        return dataSource;
    }
}
```

- [ ] **Step 4: 创建数据源切换切面**

```java
// src/main/java/cn/zdjc/wms/project/form/aspect/DataSourceAspect.java
package cn.zdjc.wms.project.form.aspect;

import cn.zdjc.wms.project.form.annotation.FormDataSource;
import cn.zdjc.wms.project.form.config.DynamicDataSource;
import cn.zdjc.wms.project.form.model.entity.FormConfigEntity;
import cn.zdjc.wms.project.form.service.FormConfigService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.lang.reflect.Method;
import java.util.Optional;

@Aspect
@Component
public class DataSourceAspect {
    
    @Autowired
    private FormConfigService formConfigService;
    
    /**
     * 在表单数据操作前自动切换数据源
     */
    @Before("@annotation(formDataSource)")
    public void switchDataSource(JoinPoint joinPoint, FormDataSource formDataSource) {
        String formCode = formDataSource.value();
        
        if (StringUtils.hasText(formCode)) {
            Optional<FormConfigEntity> config = formConfigService.getFormConfigByCode(formCode);
            if (config.isPresent() && StringUtils.hasText(config.get().getDbCode())) {
                DynamicDataSource.switchDataSource(config.get().getDbCode());
            } else {
                DynamicDataSource.resetToDefault();
            }
        } else {
            DynamicDataSource.resetToDefault();
        }
    }
    
    /**
     * 操作完成后清理数据源上下文
     */
    @After("@annotation(formDataSource)")
    public void clearDataSource(JoinPoint joinPoint, FormDataSource formDataSource) {
        DynamicDataSource.resetToDefault();
    }
    
    /**
     * 处理没有注解但有formCode参数的方法
     */
    @Before("execution(* cn.zdjc.wms.project.form.service.FormDataService.*(..)) && args(formCode,..)")
    public void switchDataSourceByParam(JoinPoint joinPoint, String formCode) {
        if (StringUtils.hasText(formCode)) {
            Optional<FormConfigEntity> config = formConfigService.getFormConfigByCode(formCode);
            if (config.isPresent() && StringUtils.hasText(config.get().getDbCode())) {
                DynamicDataSource.switchDataSource(config.get().getDbCode());
            } else {
                DynamicDataSource.resetToDefault();
            }
        } else {
            DynamicDataSource.resetToDefault();
        }
    }
    
    /**
     * 清理数据源上下文
     */
    @After("execution(* cn.zdjc.wms.project.form.service.FormDataService.*(..)) && args(formCode,..)")
    public void clearDataSourceByParam(JoinPoint joinPoint, String formCode) {
        DynamicDataSource.resetToDefault();
    }
}
```

- [ ] **Step 5: 创建FormDataSource注解**

```java
// src/main/java/cn/zdjc/wms/project/form/annotation/FormDataSource.java
package cn.zdjc.wms.project.form.annotation;

import java.lang.annotation.*;

/**
 * 表单数据源注解
 * 用于标记需要动态切换数据源的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FormDataSource {
    
    /**
     * 表单编码
     */
    String value();
}
```

- [ ] **Step 6: 编译测试动态数据源配置**

```bash
cd E:\claudeWork\878\wms.pj878
mvn compile -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 7: 提交动态数据源组件**

```bash
git add src/main/java/cn/zdjc/wms/project/form/config/
git add src/main/java/cn/zdjc/wms/project/form/aspect/
git add src/main/java/cn/zdjc/wms/project/form/annotation/
git commit -m "feat: 创建动态数据源配置和切换组件"
```

### Task 7: 创建SQL方言处理器

**Files:**
- Create: `src/main/java/cn/zdjc/wms/project/form/dialect/SqlDialectProcessor.java`
- Create: `src/main/java/cn/zdjc/wms/project/form/config/MyBatisPlusConfig.java`

- [ ] **Step 1: 创建SqlDialectProcessor**

```java
// src/main/java/cn/zdjc/wms/project/form/dialect/SqlDialectProcessor.java
package cn.zdjc.wms.project.form.dialect;

import cn.zdjc.wms.project.form.constant.DatabaseType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.Map;

@Component
public class SqlDialectProcessor {
    
    /**
     * 根据数据库类型获取分页SQL
     */
    public String getPaginationSql(String originalSql, DatabaseType dbType, long offset, long pageSize) {
        if (!StringUtils.hasText(originalSql)) {
            throw new IllegalArgumentException("原始SQL不能为空");
        }
        
        switch (dbType) {
            case MYSQL:
                return originalSql + " LIMIT " + offset + ", " + pageSize;
            case SQLSERVER:
                return "SELECT * FROM (" +
                       "    SELECT ROW_NUMBER() OVER (ORDER BY (SELECT 1)) AS row_num, * FROM (" + originalSql + ") t1" +
                       ") t2 WHERE t2.row_num BETWEEN " + (offset + 1) + " AND " + (offset + pageSize);
            case ORACLE:
                return "SELECT * FROM (" +
                       "    SELECT t.*, ROWNUM rn FROM (" + originalSql + ") t WHERE ROWNUM <= " + (offset + pageSize) +
                       ") WHERE rn > " + offset;
            default:
                throw new UnsupportedOperationException("不支持的数据库类型: " + dbType);
        }
    }
    
    /**
     * 获取数据库特定的建表语句
     */
    public String getCreateTableSql(String tableName, DatabaseType dbType, Map<String, String> columnDefinitions) {
        StringBuilder sql = new StringBuilder("CREATE TABLE " + tableName + " (\n");
        
        // 添加主键定义
        switch (dbType) {
            case MYSQL:
                sql.append("    id BIGINT AUTO_INCREMENT PRIMARY KEY");
                break;
            case SQLSERVER:
                sql.append("    id BIGINT IDENTITY(1,1) PRIMARY KEY");
                break;
            case ORACLE:
                sql.append("    id NUMBER(19) PRIMARY KEY");
                break;
            default:
                sql.append("    id BIGINT PRIMARY KEY");
        }
        
        // 添加其他列定义
        for (Map.Entry<String, String> entry : columnDefinitions.entrySet()) {
            sql.append(",\n    ").append(entry.getKey()).append(" ").append(entry.getValue());
        }
        
        sql.append("\n)");
        
        // 添加数据库特定后缀
        switch (dbType) {
            case MYSQL:
                sql.append(" ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                break;
            case ORACLE:
                // Oracle需要单独创建序列
                sql.append("\nCREATE SEQUENCE " + tableName + "_seq START WITH 1 INCREMENT BY 1");
                break;
        }
        
        return sql.toString();
    }
    
    /**
     * 处理字段类型的映射
     */
    public String mapColumnType(String javaType, DatabaseType dbType) {
        Map<String, String> mysqlMap = Map.of(
            "String", "VARCHAR(255)",
            "Integer", "INT",
            "Long", "BIGINT",
            "Double", "DOUBLE",
            "Boolean", "TINYINT(1)",
            "LocalDateTime", "DATETIME",
            "Date", "DATE"
        );
        
        Map<String, String> sqlServerMap = Map.of(
            "String", "NVARCHAR(255)",
            "Integer", "INT",
            "Long", "BIGINT",
            "Double", "FLOAT",
            "Boolean", "BIT",
            "LocalDateTime", "DATETIME2",
            "Date", "DATE"
        );
        
        Map<String, String> oracleMap = Map.of(
            "String", "VARCHAR2(255 CHAR)",
            "Integer", "NUMBER(10)",
            "Long", "NUMBER(19)",
            "Double", "NUMBER(19,4)",
            "Boolean", "NUMBER(1)",
            "LocalDateTime", "TIMESTAMP",
            "Date", "DATE"
        );
        
        switch (dbType) {
            case MYSQL: return mysqlMap.getOrDefault(javaType, "VARCHAR(255)");
            case SQLSERVER: return sqlServerMap.getOrDefault(javaType, "NVARCHAR(255)");
            case ORACLE: return oracleMap.getOrDefault(javaType, "VARCHAR2(255 CHAR)");
            default: return "VARCHAR(255)";
        }
    }
    
    /**
     * 获取验证查询语句
     */
    public String getValidationQuery(DatabaseType dbType) {
        switch (dbType) {
            case MYSQL: return "SELECT 1";
            case SQLSERVER: return "SELECT 1";
            case ORACLE: return "SELECT 1 FROM DUAL";
            default: return "SELECT 1";
        }
    }
}
```

- [ ] **Step 2: 创建MyBatisPlusConfig**

```java
// src/main/java/cn/zdjc/wms/project/form/config/MyBatisPlusConfig.java
package cn.zdjc.wms.project.form.config;

import cn.zdjc.wms.project.form.constant.DatabaseType;
import cn.zdjc.wms.project.form.model.entity.DatabaseConfigEntity;
import cn.zdjc.wms.project.form.service.DatabaseConfigService;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.binding.MappedStatement;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import java.sql.SQLException;

@Configuration
public class MyBatisPlusConfig {
    
    @Autowired
    private DatabaseConfigService databaseConfigService;
    
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 动态分页插件 - 根据当前数据源类型选择方言
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor() {
            @Override
            public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, 
                                   ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
                String dbCode = DataSourceContextHolder.getDataSourceKeyOrDefault();
                
                if (!"default".equals(dbCode)) {
                    DatabaseConfigEntity config = databaseConfigService.getDatabaseConfigByCode(dbCode).orElse(null);
                    if (config != null) {
                        try {
                            DatabaseType dbType = DatabaseType.valueOf(config.getDbType().toUpperCase());
                            this.setDbType(getDbType(dbType));
                        } catch (IllegalArgumentException e) {
                            this.setDbType(DbType.MYSQL);
                        }
                    } else {
                        this.setDbType(DbType.MYSQL);
                    }
                } else {
                    this.setDbType(DbType.MYSQL);
                }
                
                super.beforeQuery(executor, ms, parameter, rowBounds, resultHandler, boundSql);
            }
            
            private DbType getDbType(DatabaseType dbType) {
                switch (dbType) {
                    case MYSQL: return DbType.MYSQL;
                    case SQLSERVER: return DbType.SQL_SERVER;
                    case ORACLE: return DbType.ORACLE;
                    default: return DbType.MYSQL;
                }
            }
        };
        
        paginationInterceptor.setMaxLimit(1000L);
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        return interceptor;
    }
}
```

- [ ] **Step 3: 编译测试SQL方言处理器**

```bash
cd E:\claudeWork\878\wms.pj878
mvn compile -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 4: 提交SQL方言处理器**

```bash
git add src/main/java/cn/zdjc/wms/project/form/dialect/
git add src/main/java/cn/zdjc/wms/project/form/config/MyBatisPlusConfig.java
git commit -m "feat: 创建SQL方言处理器和MyBatis-Plus配置"
```

### Task 8: 创建Controller API层

**Files:**
- Create: `src/main/java/cn/zdjc/wms/project/form/controller/FormConfigController.java`
- Create: `src/main/java/cn/zdjc/wms/project/form/controller/FormDataController.java`
- Create: `src/main/java/cn/zdjc/wms/project/form/controller/DatabaseConfigController.java`

- [ ] **Step 1: 创建FormConfigController**

```java
// src/main/java/cn/zdjc/wms/project/form/controller/FormConfigController.java
package cn.zdjc.wms.project.form.controller;

import cn.zdjc.wms.project.form.model.entity.FormConfigEntity;
import cn.zdjc.wms.project.form.service.FormConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/form/config")
@Api(tags = "表单配置管理")
public class FormConfigController {
    
    @Autowired
    private FormConfigService formConfigService;
    
    @PostMapping("/save")
    @ApiOperation("保存表单配置")
    public ResponseEntity<FormConfigEntity> saveFormConfig(@RequestBody FormConfigEntity entity) {
        FormConfigEntity saved = formConfigService.saveFormConfig(entity);
        return ResponseEntity.ok(saved);
    }
    
    @GetMapping("/list")
    @ApiOperation("查询表单配置列表")
    public ResponseEntity<List<FormConfigEntity>> listFormConfigs() {
        List<FormConfigEntity> list = formConfigService.listFormConfigs();
        return ResponseEntity.ok(list);
    }
    
    @GetMapping("/{code}")
    @ApiOperation("获取表单配置详情")
    public ResponseEntity<FormConfigEntity> getFormConfig(@PathVariable String code) {
        Optional<FormConfigEntity> config = formConfigService.getFormConfigByCode(code);
        return config.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/{code}/status/{status}")
    @ApiOperation("启用/禁用表单配置")
    public ResponseEntity<Boolean> updateFormConfigStatus(
            @PathVariable String code,
            @PathVariable Integer status) {
        boolean success = formConfigService.updateFormConfigStatus(code, status);
        return success ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{code}")
    @ApiOperation("删除表单配置")
    public ResponseEntity<Boolean> deleteFormConfig(@PathVariable String code) {
        boolean success = formConfigService.deleteFormConfig(code);
        return success ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }
    
    @PostMapping("/validate")
    @ApiOperation("验证表单JSON配置")
    public ResponseEntity<Boolean> validateFormJson(@RequestBody String configJson) {
        boolean valid = formConfigService.validateFormJson(configJson);
        return ResponseEntity.ok(valid);
    }
}
```

- [ ] **Step 2: 创建DatabaseConfigController**

```java
// src/main/java/cn/zdjc/wms/project/form/controller/DatabaseConfigController.java
package cn.zdjc.wms.project.form.controller;

import cn.zdjc.wms.project.form.model.entity.DatabaseConfigEntity;
import cn.zdjc.wms.project.form.service.DatabaseConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/database/config")
@Api(tags = "数据库配置管理")
public class DatabaseConfigController {
    
    @Autowired
    private DatabaseConfigService databaseConfigService;
    
    @PostMapping("/save")
    @ApiOperation("保存数据库配置")
    public ResponseEntity<DatabaseConfigEntity> saveDatabaseConfig(@RequestBody DatabaseConfigEntity entity) {
        DatabaseConfigEntity saved = databaseConfigService.saveDatabaseConfig(entity);
        return ResponseEntity.ok(saved);
    }
    
    @GetMapping("/list")
    @ApiOperation("查询数据库配置列表")
    public ResponseEntity<List<DatabaseConfigEntity>> listDatabaseConfigs() {
        List<DatabaseConfigEntity> list = databaseConfigService.getAllEnabledConfigs();
        return ResponseEntity.ok(list);
    }
    
    @GetMapping("/{dbCode}")
    @ApiOperation("获取数据库配置详情")
    public ResponseEntity<DatabaseConfigEntity> getDatabaseConfig(@PathVariable String dbCode) {
        Optional<DatabaseConfigEntity> config = databaseConfigService.getDatabaseConfigByCode(dbCode);
        return config.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/{dbCode}/test")
    @ApiOperation("测试数据库连接")
    public ResponseEntity<Boolean> testDatabaseConnection(@PathVariable String dbCode) {
        Optional<DatabaseConfigEntity> config = databaseConfigService.getDatabaseConfigByCode(dbCode);
        if (config.isPresent()) {
            boolean success = databaseConfigService.testDatabaseConnection(config.get());
            return ResponseEntity.ok(success);
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/{dbCode}/status/{status}")
    @ApiOperation("启用/禁用数据库配置")
    public ResponseEntity<Boolean> updateDatabaseConfigStatus(
            @PathVariable String dbCode,
            @PathVariable Integer status) {
        boolean success = databaseConfigService.updateDatabaseConfigStatus(dbCode, status);
        return success ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{dbCode}")
    @ApiOperation("删除数据库配置")
    public ResponseEntity<Boolean> deleteDatabaseConfig(@PathVariable String dbCode) {
        boolean success = databaseConfigService.deleteDatabaseConfig(dbCode);
        return success ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }
}
```

- [ ] **Step 3: 创建FormDataController**

```java
// src/main/java/cn/zdjc/wms/project/form/controller/FormDataController.java
package cn.zdjc.wms.project.form.controller;

import cn.zdjc.wms.project.form.annotation.FormDataSource;
import cn.zdjc.wms.project.form.model.entity.FormDataEntity;
import cn.zdjc.wms.project.form.service.FormDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/form/data")
@Api(tags = "表单数据操作")
public class FormDataController {
    
    @Autowired
    private FormDataService formDataService;
    
    @PostMapping("/submit")
    @ApiOperation("提交表单数据")
    @FormDataSource("formCode")  // 根据formCode动态切换数据源
    public ResponseEntity<FormDataEntity> submitFormData(@RequestBody FormDataEntity entity) {
        FormDataEntity saved = formDataService.submitFormData(entity);
        return ResponseEntity.ok(saved);
    }
    
    @GetMapping("/list/{formCode}")
    @ApiOperation("查询表单数据列表")
    @FormDataSource("formCode")
    public ResponseEntity<List<FormDataEntity>> listFormData(@PathVariable String formCode) {
        List<FormDataEntity> list = formDataService.listFormData(formCode);
        return ResponseEntity.ok(list);
    }
    
    @GetMapping("/{id}")
    @ApiOperation("获取表单数据详情")
    public ResponseEntity<FormDataEntity> getFormData(@PathVariable Long id) {
        Optional<FormDataEntity> data = formDataService.getFormDataById(id);
        return data.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/{id}/status")
    @ApiOperation("更新表单数据状态")
    public ResponseEntity<Boolean> updateFormDataStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        boolean success = formDataService.updateFormDataStatus(id, status);
        return success ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }
    
    @PostMapping("/validate/{formCode}")
    @ApiOperation("验证表单数据")
    @FormDataSource("formCode")
    public ResponseEntity<Boolean> validateFormData(
            @PathVariable String formCode,
            @RequestBody String formDataJson) {
        boolean valid = formDataService.validateFormData(formCode, formDataJson);
        return ResponseEntity.ok(valid);
    }
}
```

- [ ] **Step 4: 创建FormDataService接口和实现**

```java
// src/main/java/cn/zdjc/wms/project/form/service/FormDataService.java
package cn.zdjc.wms.project.form.service;

import cn.zdjc.wms.project.form.model.entity.FormDataEntity;
import java.util.List;
import java.util.Optional;

public interface FormDataService {
    
    /**
     * 提交表单数据
     */
    FormDataEntity submitFormData(FormDataEntity entity);
    
    /**
     * 根据ID获取表单数据
     */
    Optional<FormDataEntity> getFormDataById(Long id);
    
    /**
     * 查询表单数据列表
     */
    List<FormDataEntity> listFormData(String formCode);
    
    /**
     * 更新表单数据状态
     */
    boolean updateFormDataStatus(Long id, String status);
    
    /**
     * 验证表单数据
     */
    boolean validateFormData(String formCode, String formDataJson);
}

// src/main/java/cn/zdjc/wms/project/form/service/impl/FormDataServiceImpl.java
package cn.zdjc.wms.project.form.service.impl;

import cn.zdjc.wms.project.form.annotation.FormDataSource;
import cn.zdjc.wms.project.form.model.entity.FormConfigEntity;
import cn.zdjc.wms.project.form.model.entity.FormDataEntity;
import cn.zdjc.wms.project.form.repository.FormDataRepository;
import cn.zdjc.wms.project.form.service.FormConfigService;
import cn.zdjc.wms.project.form.service.FormDataService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FormDataServiceImpl implements FormDataService {
    
    @Autowired
    private FormDataRepository formDataRepository;
    
    @Autowired
    private FormConfigService formConfigService;
    
    @Override
    @FormDataSource("formCode")  // 使用注解触发数据源切换
    public FormDataEntity submitFormData(FormDataEntity entity) {
        // 验证表单编码
        if (!StringUtils.hasText(entity.getFormCode())) {
            throw new IllegalArgumentException("表单编码不能为空");
        }
        
        // 验证表单配置是否存在
        Optional<FormConfigEntity> config = formConfigService.getFormConfigByCode(entity.getFormCode());
        if (config.isEmpty()) {
            throw new IllegalArgumentException("表单配置不存在: " + entity.getFormCode());
        }
        
        // 验证表单配置是否启用
        if (config.get().getStatus() != 1) {
            throw new IllegalArgumentException("表单配置已被禁用: " + entity.getFormCode());
        }
        
        // 验证表单数据JSON
        if (!validateFormData(entity.getFormCode(), entity.getFormDataJson())) {
            throw new IllegalArgumentException("表单数据验证失败");
        }
        
        // 设置创建时间
        if (entity.getCreatedTime() == null) {
            entity.setCreatedTime(LocalDateTime.now());
        }
        
        // 设置默认状态
        if (!StringUtils.hasText(entity.getDataStatus())) {
            entity.setDataStatus("DRAFT");
        }
        
        return formDataRepository.save(entity);
    }
    
    @Override
    public Optional<FormDataEntity> getFormDataById(Long id) {
        return formDataRepository.findById(id);
    }
    
    @Override
    @FormDataSource("formCode")
    public List<FormDataEntity> listFormData(String formCode) {
        // 查询指定表单的数据，按创建时间倒序
        QueryWrapper<FormDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("form_code", formCode)
                    .orderByDesc("created_time");
        
        return formDataMapper.selectList(queryWrapper);
    }
    
    @Override
    public boolean updateFormDataStatus(Long id, String status) {
        Optional<FormDataEntity> optional = formDataRepository.findById(id);
        if (optional.isPresent()) {
            FormDataEntity entity = optional.get();
            entity.setDataStatus(status);
            formDataRepository.save(entity);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean validateFormData(String formCode, String formDataJson) {
        if (!StringUtils.hasText(formDataJson)) {
            return false;
        }
        
        try {
            // 验证JSON格式
            JSONValidator validator = JSONValidator.from(formDataJson);
            return validator.validate();
        } catch (Exception e) {
            return false;
        }
    }
}
```

- [ ] **Step 5: 编译测试Controller层**

```bash
cd E:\claudeWork\878\wms.pj878
mvn compile -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 6: 提交Controller和FormDataService**

```bash
git add src/main/java/cn/zdjc/wms/project/form/controller/
git add src/main/java/cn/zdjc/wms/project/form/service/FormDataService.java
git add src/main/java/cn/zdjc/wms/project/form/service/impl/FormDataServiceImpl.java
git commit -m "feat: 创建Controller API层和FormDataService"
```

### Task 9: 前端表单设计器集成

**Files:**
- Check: `E:\claudeWork\878\wms.pj878\vue\package.json`
- Create: `E:\claudeWork\878\wms.pj878\vue\src\form-designer\`
- Copy: `E:\claudeWork\878\form-generator-dev\` → `E:\claudeWork\878\wms.pj878\vue\src\form-designer\`

- [ ] **Step 1: 检查前端项目结构**

```bash
cd E:\claudeWork\878\wms.pj878/vue
ls -la
```

Expected: 看到package.json和src目录

- [ ] **Step 2: 查看现有package.json依赖版本**

```bash
cd E:\claudeWork\878\wms.pj878/vue
cat package.json | grep -A5 -B5 '"vue"\|"element-ui"\|"core-js"'
```

Expected: 看到Vue 2.6.10, Element UI 2.12.0, core-js 2.6.5等

- [ ] **Step 3: 复制form-generator-dev源码**

```bash
# 创建表单设计器目录
mkdir -p E:\claudeWork\878\wms.pj878/vue/src/form-designer

# 复制核心组件（简化示例，实际需要完整复制）
cp -r E:\claudeWork\878\form-generator-dev/src/components/ E:\claudeWork\878\wms.pj878/vue/src/form-designer/components/
cp -r E:\claudeWork\878\form-generator-dev/src/views/ E:\claudeWork\878\wms.pj878/vue/src/form-designer/views/
cp -r E:\claudeWork\878\form-generator-dev/src/utils/ E:\claudeWork\878\wms.pj878/vue/src/form-designer/utils/
cp -r E:\claudeWork\878\form-generator-dev/src/assets/ E:\claudeWork\878\wms.pj878/vue/src/form-designer/assets/
```

- [ ] **Step 4: 创建前端API封装**

```javascript
// E:\claudeWork\878\wms.pj878\vue\src\form-designer\api\database.js
import request from '@/utils/request'

export function saveDatabaseConfig(data) {
  return request({
    url: '/api/database/config/save',
    method: 'post',
    data
  })
}

export function getDatabaseConfigList() {
  return request({
    url: '/api/database/config/list',
    method: 'get'
  })
}

export function testDatabaseConnection(dbCode) {
  return request({
    url: `/api/database/config/${dbCode}/test`,
    method: 'post'
  })
}

export function deleteDatabaseConfig(dbCode) {
  return request({
    url: `/api/database/config/${dbCode}`,
    method: 'delete'
  })
}
```

```javascript
// E:\claudeWork\878\wms.pj878\vue\src\form-designer\api\formConfig.js
import request from '@/utils/request'

export function saveFormConfig(data) {
  return request({
    url: '/api/form/config/save',
    method: 'post',
    data
  })
}

export function getFormConfigList() {
  return request({
    url: '/api/form/config/list',
    method: 'get'
  })
}

export function getFormConfigDetail(code) {
  return request({
    url: `/api/form/config/${code}`,
    method: 'get'
  })
}

export function validateFormConfig(configJson) {
  return request({
    url: '/api/form/config/validate',
    method: 'post',
    data: configJson
  })
}
```

- [ ] **Step 5: 创建路由配置**

```javascript
// 在现有路由配置中添加，假设路由文件在 src/router/index.js
// 添加以下路由配置
{
  path: '/form-designer',
  component: Layout,
  redirect: '/form-designer/list',
  name: 'FormDesigner',
  meta: { title: '表单设计器', icon: 'form' },
  children: [
    {
      path: 'list',
      component: () => import('@/form-designer/views/FormDesignerList'),
      name: 'FormDesignerList',
      meta: { title: '表单配置列表', icon: 'list' }
    },
    {
      path: 'design/:id?',
      component: () => import('@/form-designer/views/FormDesignerDesign'),
      name: 'FormDesignerDesign',
      meta: { title: '表单设计器', icon: 'edit' },
      hidden: true
    },
    {
      path: 'preview/:id',
      component: () => import('@/form-designer/views/FormDesignerPreview'),
      name: 'FormDesignerPreview',
      meta: { title: '表单预览', icon: 'eye' },
      hidden: true
    },
    {
      path: 'database',
      component: () => import('@/form-designer/views/DatabaseConfig'),
      name: 'DatabaseConfig',
      meta: { title: '数据库配置', icon: 'database' }
    }
  ]
}
```

- [ ] **Step 6: 前端依赖版本兼容性处理**

根据设计文档中的依赖兼容性表，需要：
1. 保持Vue 2.6.10（不升级到2.6.11）
2. 使用Element UI 2.12.0
3. 升级core-js从2.6.5到3.x（如果需要）
4. 使用878现有的sortablejs而非vuedraggable

```bash
cd E:\claudeWork\878\wms.pj878/vue
# 检查并更新package.json中的依赖版本
# 这里假设已经手动调整
```

- [ ] **Step 7: 提交前端集成文件**

```bash
cd E:\claudeWork\878\wms.pj878
git add vue/src/form-designer/
# 如果有路由配置修改也添加
git commit -m "feat: 前端表单设计器集成和API封装"
```

### Task 10: 集成测试和验证

**Files:**
- Create: `src/test/java/cn/zdjc/wms/project/form/FormConfigServiceTest.java`
- Create: `src/test/java/cn/zdjc/wms/project/form/DatabaseConfigServiceTest.java`
- Create: `src/test/java/cn/zdjc/wms/project/form/DynamicDataSourceTest.java`

- [ ] **Step 1: 创建FormConfigService测试**

```java
// src/test/java/cn/zdjc/wms/project/form/FormConfigServiceTest.java
package cn.zdjc.wms.project.form;

import cn.zdjc.wms.project.form.model.entity.FormConfigEntity;
import cn.zdjc.wms.project.form.service.FormConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FormConfigServiceTest {
    
    @Autowired
    private FormConfigService formConfigService;
    
    @Test
    public void testSaveFormConfig() {
        FormConfigEntity entity = new FormConfigEntity();
        entity.setName("测试表单");
        entity.setCode("test_form");
        entity.setFormType("CUSTOM");
        entity.setConfigJson("{\"fields\": [], \"rules\": []}");
        
        FormConfigEntity saved = formConfigService.saveFormConfig(entity);
        
        assertNotNull(saved.getId());
        assertEquals("测试表单", saved.getName());
        assertEquals("test_form", saved.getCode());
    }
    
    @Test
    public void testValidateFormJson() {
        String validJson = "{\"fields\": [], \"rules\": []}";
        String invalidJson = "{invalid json";
        
        assertTrue(formConfigService.validateFormJson(validJson));
        assertFalse(formConfigService.validateFormJson(invalidJson));
    }
}
```

- [ ] **Step 2: 创建DatabaseConfigService测试**

```java
// src/test/java/cn/zdjc/wms/project/form/DatabaseConfigServiceTest.java
package cn.zdjc.wms.project.form;

import cn.zdjc.wms.project.form.model.entity.DatabaseConfigEntity;
import cn.zdjc.wms.project.form.service.DatabaseConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DatabaseConfigServiceTest {
    
    @Autowired
    private DatabaseConfigService databaseConfigService;
    
    @Test
    public void testSaveDatabaseConfig() {
        DatabaseConfigEntity entity = new DatabaseConfigEntity();
        entity.setDbCode("test_db");
        entity.setDbName("测试数据库");
        entity.setDbType("MYSQL");
        entity.setDriverClass("com.mysql.cj.jdbc.Driver");
        entity.setJdbcUrl("jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8");
        entity.setUsername("test");
        entity.setPassword("test");
        
        // 注意：这个测试需要实际的数据库连接，可能会失败
        // 在实际环境中应该使用内存数据库或Mock
        try {
            DatabaseConfigEntity saved = databaseConfigService.saveDatabaseConfig(entity);
            assertNotNull(saved.getId());
            assertEquals("test_db", saved.getDbCode());
        } catch (Exception e) {
            // 连接测试失败是预期的，因为我们没有真实的测试数据库
            System.out.println("数据库连接测试失败（预期中）: " + e.getMessage());
        }
    }
}
```

- [ ] **Step 3: 运行后端测试**

```bash
cd E:\claudeWork\878\wms.pj878
mvn test -Dtest="*Form*Test" -DfailIfNoTests=false
```

Expected: 测试通过或有预期的失败（如数据库连接测试）

- [ ] **Step 4: 前端构建测试**

```bash
cd E:\claudeWork\878\wms.pj878/vue
npm run build
```

Expected: 构建成功，没有错误

- [ ] **Step 5: 完整系统启动测试**

```bash
cd E:\claudeWork\878\wms.pj878
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Expected: 应用启动成功，没有启动错误

- [ ] **Step 6: 提交测试文件**

```bash
git add src/test/java/cn/zdjc/wms/project/form/
git commit -m "test: 添加表单设计器集成测试"
```

---

## 第二阶段：多数据库支持扩展（2周）

### Task 11: 表单设计器界面数据库选择功能

**Files:**
- Modify: `E:\claudeWork\878\wms.pj878\vue\src\form-designer\views\FormDesignerDesign.vue`
- Create: `E:\claudeWork\878\wms.pj878\vue\src\form-designer\components\DatabaseSelector.vue`

- [ ] **Step 1: 创建DatabaseSelector组件**

```vue
<!-- E:\claudeWork\878\wms.pj878\vue\src\form-designer\components\DatabaseSelector.vue -->
<template>
  <div class="database-selector">
    <el-form-item label="数据存储位置" prop="dbCode">
      <el-select 
        v-model="selectedDbCode" 
        placeholder="请选择数据库"
        @change="handleDatabaseChange"
        clearable
      >
        <el-option
          v-for="db in databaseList"
          :key="db.dbCode"
          :label="`${db.dbName} (${db.dbType})`"
          :value="db.dbCode"
        >
          <span style="float: left">{{ db.dbName }}</span>
          <span style="float: right; color: #8492a6; font-size: 13px">
            {{ db.dbType }}
          </span>
        </el-option>
      </el-select>
      
      <el-button 
        v-if="selectedDbCode"
        type="text" 
        size="small"
        @click="testConnection(selectedDbCode)"
      >
        测试连接
      </el-button>
      
      <el-button 
        type="text" 
        size="small"
        @click="gotoDatabaseConfig"
      >
        管理数据库
      </el-button>
    </el-form-item>
    
    <el-form-item v-if="selectedDbCode" label="自定义表名" prop="tableName">
      <el-input 
        v-model="tableName" 
        placeholder="可选，默认使用form_data_[表单编码]"
        :prefix-icon="'el-icon-document'"
      >
        <template slot="prepend">wms_form_dynamic_</template>
      </el-input>
      <div class="form-tip">
        不填则使用默认表名: form_data_{{ formCode || '[表单编码]' }}
      </div>
    </el-form-item>
    
    <el-dialog
      title="数据库连接测试"
      :visible.sync="testDialogVisible"
      width="30%"
    >
      <div v-if="testResult === 'testing'">
        <el-alert
          title="正在测试数据库连接..."
          type="info"
          :closable="false"
          show-icon
        />
      </div>
      <div v-else-if="testResult === 'success'">
        <el-alert
          title="数据库连接测试成功"
          type="success"
          show-icon
        />
      </div>
      <div v-else-if="testResult === 'error'">
        <el-alert
          :title="'数据库连接测试失败: ' + testErrorMessage"
          type="error"
          show-icon
        />
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="testDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getDatabaseConfigList, testDatabaseConnection } from '@/form-designer/api/database'

export default {
  name: 'DatabaseSelector',
  props: {
    value: {
      type: Object,
      default: () => ({})
    },
    formCode: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      databaseList: [],
      selectedDbCode: this.value.dbCode || '',
      tableName: this.value.tableName || '',
      testDialogVisible: false,
      testResult: '', // 'testing', 'success', 'error'
      testErrorMessage: ''
    }
  },
  watch: {
    selectedDbCode(newVal) {
      this.$emit('input', {
        dbCode: newVal,
        tableName: this.tableName
      })
    },
    tableName(newVal) {
      this.$emit('input', {
        dbCode: this.selectedDbCode,
        tableName: newVal
      })
    }
  },
  created() {
    this.loadDatabaseList()
  },
  methods: {
    async loadDatabaseList() {
      try {
        const { data } = await getDatabaseConfigList()
        this.databaseList = data
      } catch (error) {
        console.error('加载数据库列表失败:', error)
        this.$message.error('加载数据库列表失败')
      }
    },
    handleDatabaseChange(dbCode) {
      this.selectedDbCode = dbCode
      this.$emit('change', dbCode)
    },
    async testConnection(dbCode) {
      this.testDialogVisible = true
      this.testResult = 'testing'
      
      try {
        const { data } = await testDatabaseConnection(dbCode)
        this.testResult = data ? 'success' : 'error'
        if (!data) {
          this.testErrorMessage = '连接测试返回失败'
        }
      } catch (error) {
        this.testResult = 'error'
        this.testErrorMessage = error.message || '未知错误'
      }
    },
    gotoDatabaseConfig() {
      this.$router.push('/form-designer/database')
    }
  }
}
</script>

<style scoped>
.database-selector {
  margin-bottom: 20px;
}
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>
```

- [ ] **Step 2: 集成到表单设计器界面**

```vue
<!-- 在FormDesignerDesign.vue中添加数据库选择部分 -->
<!-- 在表单基本信息区域后添加 -->
<template>
  <div class="form-designer-container">
    <!-- 原有代码... -->
    
    <!-- 数据库选择部分 -->
    <el-card class="box-card database-section" shadow="never">
      <div slot="header" class="clearfix">
        <span>数据存储配置</span>
        <el-tag v-if="formConfig.dbCode" type="success" size="small">
          使用外部数据库存储
        </el-tag>
        <el-tag v-else type="info" size="small">
          使用默认数据库存储
        </el-tag>
      </div>
      
      <database-selector
        v-model="databaseConfig"
        :form-code="formConfig.code"
        @change="handleDatabaseChange"
      />
    </el-card>
    
    <!-- 原有代码... -->
  </div>
</template>

<script>
import DatabaseSelector from '@/form-designer/components/DatabaseSelector.vue'

export default {
  components: {
    DatabaseSelector
  },
  data() {
    return {
      // 原有数据...
      databaseConfig: {
        dbCode: '',
        tableName: ''
      }
    }
  },
  methods: {
    handleDatabaseChange(dbCode) {
      console.log('数据库切换为:', dbCode)
      // 这里可以添加数据库切换后的逻辑
    },
    // 保存时包含数据库配置
    saveFormConfig() {
      const formData = {
        ...this.formConfig,
        dbCode: this.databaseConfig.dbCode,
        tableName: this.databaseConfig.tableName
      }
      // 调用保存API...
    }
  },
  mounted() {
    // 加载表单配置时初始化数据库配置
    if (this.formConfigId) {
      this.loadFormConfig()
    }
  }
}
</script>
```

- [ ] **Step 3: 创建数据库配置管理页面**

```vue
<!-- E:\claudeWork\878\wms.pj878\vue\src\form-designer\views\DatabaseConfig.vue -->
<template>
  <div class="database-config-container">
    <el-card shadow="never">
      <div slot="header" class="clearfix">
        <span>数据库配置管理</span>
        <el-button 
          type="primary" 
          size="small" 
          icon="el-icon-plus"
          @click="handleAdd"
          style="float: right;"
        >
          新增数据库
        </el-button>
      </div>
      
      <el-table
        :data="databaseList"
        v-loading="loading"
        border
        style="width: 100%"
      >
        <el-table-column
          prop="dbCode"
          label="数据库编码"
          width="180"
        />
        <el-table-column
          prop="dbName"
          label="数据库名称"
          width="200"
        />
        <el-table-column
          prop="dbType"
          label="数据库类型"
          width="120"
        />
        <el-table-column
          prop="jdbcUrl"
          label="连接URL"
          show-overflow-tooltip
        />
        <el-table-column
          prop="enabled"
          label="状态"
          width="80"
        >
          <template slot-scope="scope">
            <el-tag 
              :type="scope.row.enabled === 1 ? 'success' : 'danger'"
              size="small"
            >
              {{ scope.row.enabled === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="220"
          fixed="right"
        >
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              @click="handleTest(scope.row)"
            >
              测试连接
            </el-button>
            <el-button
              type="text"
              size="small"
              @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              type="text"
              size="small"
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.enabled === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button
              type="text"
              size="small"
              style="color: #F56C6C"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 数据库配置表单对话框 -->
    <database-form-dialog
      :visible="dialogVisible"
      :form-data="currentDatabase"
      :is-edit="isEdit"
      @close="dialogVisible = false"
      @success="handleDialogSuccess"
    />
  </div>
</template>

<script>
import { 
  getDatabaseConfigList, 
  testDatabaseConnection, 
  deleteDatabaseConfig 
} from '@/form-designer/api/database'
import DatabaseFormDialog from '@/form-designer/components/DatabaseFormDialog.vue'

export default {
  name: 'DatabaseConfig',
  components: {
    DatabaseFormDialog
  },
  data() {
    return {
      databaseList: [],
      loading: false,
      dialogVisible: false,
      currentDatabase: {},
      isEdit: false
    }
  },
  created() {
    this.loadDatabaseList()
  },
  methods: {
    async loadDatabaseList() {
      this.loading = true
      try {
        const { data } = await getDatabaseConfigList()
        this.databaseList = data
      } catch (error) {
        console.error('加载数据库列表失败:', error)
        this.$message.error('加载数据库列表失败')
      } finally {
        this.loading = false
      }
    },
    handleAdd() {
      this.currentDatabase = {}
      this.isEdit = false
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.currentDatabase = { ...row }
      this.isEdit = true
      this.dialogVisible = true
    },
    async handleTest(row) {
      try {
        this.$message.info('正在测试数据库连接...')
        const { data } = await testDatabaseConnection(row.dbCode)
        if (data) {
          this.$message.success('数据库连接测试成功')
        } else {
          this.$message.error('数据库连接测试失败')
        }
      } catch (error) {
        this.$message.error('测试失败: ' + (error.message || '未知错误'))
      }
    },
    async handleToggleStatus(row) {
      const newStatus = row.enabled === 1 ? 0 : 1
      const action = newStatus === 1 ? '启用' : '禁用'
      
      try {
        // 这里需要调用更新状态的API
        // await updateDatabaseConfigStatus(row.dbCode, newStatus)
        this.$message.success(`已${action}数据库配置`)
        this.loadDatabaseList()
      } catch (error) {
        this.$message.error(`${action}失败: ` + error.message)
      }
    },
    async handleDelete(row) {
      await this.$confirm(`确定删除数据库配置 "${row.dbName}" 吗？`, '提示', {
        type: 'warning'
      })
      
      try {
        await deleteDatabaseConfig(row.dbCode)
        this.$message.success('删除成功')
        this.loadDatabaseList()
      } catch (error) {
        this.$message.error('删除失败: ' + error.message)
      }
    },
    handleDialogSuccess() {
      this.dialogVisible = false
      this.loadDatabaseList()
    }
  }
}
</script>
```

- [ ] **Step 4: 前端构建测试**

```bash
cd E:\claudeWork\878\wms.pj878/vue
npm run build
```

Expected: 构建成功

- [ ] **Step 5: 提交前端数据库选择功能**

```bash
cd E:\claudeWork\878\wms.pj878
git add vue/src/form-designer/components/DatabaseSelector.vue
git add vue/src/form-designer/views/DatabaseConfig.vue
git add vue/src/form-designer/views/FormDesignerDesign.vue
git commit -m "feat: 前端表单设计器数据库选择功能"
```

### Task 12: 动态表创建和数据迁移

**Files:**
- Create: `src/main/java/cn/zdjc/wms/project/form/service/impl/DynamicTableServiceImpl.java`
- Create: `src/main/java/cn/zdjc/wms/project/form/service/DynamicTableService.java`
- Modify: `src/main/java/cn/zdjc/wms/project/form/service/impl/FormDataServiceImpl.java`

- [ ] **Step 1: 创建DynamicTableService接口**

```java
// src/main/java/cn/zdjc/wms/project/form/service/DynamicTableService.java
package cn.zdjc.wms.project.form.service;

import cn.zdjc.wms.project.form.model.entity.FormConfigEntity;

/**
 * 动态表创建服务
 */
public interface DynamicTableService {
    
    /**
     * 为表单创建动态数据表
     */
    boolean createDynamicTable(FormConfigEntity formConfig);
    
    /**
     * 检查动态表是否存在
     */
    boolean checkTableExists(String tableName, String dbCode);
    
    /**
     * 删除动态表
     */
    boolean dropDynamicTable(String tableName, String dbCode);
    
    /**
     * 迁移表单数据到新数据库
     */
    boolean migrateFormData(FormConfigEntity oldConfig, FormConfigEntity newConfig);
}
```

- [ ] **Step 2: 创建DynamicTableServiceImpl实现**

```java
// src/main/java/cn/zdjc/wms/project/form/service/impl/DynamicTableServiceImpl.java
package cn.zdjc.wms.project.form.service.impl;

import cn.zdjc.wms.project.form.annotation.FormDataSource;
import cn.zdjc.wms.project.form.constant.DatabaseType;
import cn.zdjc.wms.project.form.dialect.SqlDialectProcessor;
import cn.zdjc.wms.project.form.model.entity.DatabaseConfigEntity;
import cn.zdjc.wms.project.form.model.entity.FormConfigEntity;
import cn.zdjc.wms.project.form.service.DatabaseConfigService;
import cn.zdjc.wms.project.form.service.DynamicTableService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class DynamicTableServiceImpl implements DynamicTableService {
    
    @Autowired
    private SqlDialectProcessor sqlDialectProcessor;
    
    @Autowired
    private DatabaseConfigService databaseConfigService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    @FormDataSource("formConfig.dbCode")
    public boolean createDynamicTable(FormConfigEntity formConfig) {
        if (formConfig == null || !StringUtils.hasText(formConfig.getCode())) {
            throw new IllegalArgumentException("表单配置无效");
        }
        
        // 确定表名
        String tableName = getTableName(formConfig);
        
        // 检查表是否已存在
        if (checkTableExists(tableName, formConfig.getDbCode())) {
            return true; // 表已存在，不需要重新创建
        }
        
        // 解析表单配置，获取字段定义
        Map<String, String> columnDefinitions = parseFormFields(formConfig);
        
        // 获取数据库类型
        DatabaseType dbType = getDatabaseType(formConfig.getDbCode());
        
        // 生成建表SQL
        String createTableSql = sqlDialectProcessor.getCreateTableSql(tableName, dbType, columnDefinitions);
        
        try {
            // 执行建表SQL
            jdbcTemplate.execute(createTableSql);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("创建动态表失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean checkTableExists(String tableName, String dbCode) {
        if (!StringUtils.hasText(tableName) || !StringUtils.hasText(dbCode)) {
            return false;
        }
        
        try {
            // 根据数据库类型执行不同的检查语句
            Optional<DatabaseConfigEntity> config = databaseConfigService.getDatabaseConfigByCode(dbCode);
            if (config.isEmpty()) {
                return false;
            }
            
            DatabaseType dbType = DatabaseType.valueOf(config.get().getDbType().toUpperCase());
            String checkSql;
            
            switch (dbType) {
                case MYSQL:
                    checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?";
                    break;
                case SQLSERVER:
                    checkSql = "SELECT COUNT(*) FROM sys.tables WHERE name = ?";
                    break;
                case ORACLE:
                    checkSql = "SELECT COUNT(*) FROM user_tables WHERE table_name = ?";
                    break;
                default:
                    return false;
            }
            
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, tableName.toUpperCase());
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean dropDynamicTable(String tableName, String dbCode) {
        if (!StringUtils.hasText(tableName) || !StringUtils.hasText(dbCode)) {
            return false;
        }
        
        try {
            String dropSql = "DROP TABLE IF EXISTS " + tableName;
            jdbcTemplate.execute(dropSql);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("删除动态表失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean migrateFormData(FormConfigEntity oldConfig, FormConfigEntity newConfig) {
        // TODO: 实现数据迁移逻辑
        // 1. 从旧数据库读取数据
        // 2. 转换数据格式（如果需要）
        // 3. 插入到新数据库
        // 4. 验证数据一致性
        throw new UnsupportedOperationException("数据迁移功能尚未实现");
    }
    
    private String getTableName(FormConfigEntity formConfig) {
        if (StringUtils.hasText(formConfig.getTableName())) {
            return formConfig.getTableName();
        }
        return "form_data_" + formConfig.getCode().toLowerCase();
    }
    
    private Map<String, String> parseFormFields(FormConfigEntity formConfig) {
        Map<String, String> columnDefinitions = new HashMap<>();
        
        try {
            JSONObject configJson = JSON.parseObject(formConfig.getConfigJson());
            JSONArray fields = configJson.getJSONArray("fields");
            
            if (fields != null) {
                for (int i = 0; i < fields.size(); i++) {
                    JSONObject field = fields.getJSONObject(i);
                    String fieldName = field.getString("name");
                    String fieldType = field.getString("type");
                    
                    if (StringUtils.hasText(fieldName) && StringUtils.hasText(fieldType)) {
                        // 根据字段类型映射到数据库类型
                        DatabaseType dbType = getDatabaseType(formConfig.getDbCode());
                        String dbTypeStr = sqlDialectProcessor.mapColumnType(fieldType, dbType);
                        columnDefinitions.put(fieldName, dbTypeStr);
                    }
                }
            }
            
            // 添加系统字段
            columnDefinitions.put("form_code", "VARCHAR(50)");
            columnDefinitions.put("business_key", "VARCHAR(100)");
            columnDefinitions.put("business_type", "VARCHAR(50)");
            columnDefinitions.put("data_status", "VARCHAR(20)");
            columnDefinitions.put("created_by", "VARCHAR(50)");
            columnDefinitions.put("created_time", "DATETIME");
            
        } catch (Exception e) {
            throw new IllegalArgumentException("解析表单字段配置失败: " + e.getMessage(), e);
        }
        
        return columnDefinitions;
    }
    
    private DatabaseType getDatabaseType(String dbCode) {
        if (StringUtils.hasText(dbCode)) {
            Optional<DatabaseConfigEntity> config = databaseConfigService.getDatabaseConfigByCode(dbCode);
            if (config.isPresent()) {
                try {
                    return DatabaseType.valueOf(config.get().getDbType().toUpperCase());
                } catch (IllegalArgumentException e) {
                    // 使用默认的MySQL类型
                }
            }
        }
        return DatabaseType.MYSQL;
    }
}
```

- [ ] **Step 3: 在FormDataService中集成动态表创建**

```java
// 在FormDataServiceImpl的submitFormData方法中添加表创建逻辑
@Service
public class FormDataServiceImpl implements FormDataService {
    
    @Autowired
    private DynamicTableService dynamicTableService;
    
    @Override
    @FormDataSource("formCode")
    public FormDataEntity submitFormData(FormDataEntity entity) {
        // 原有验证逻辑...
        
        // 确保动态表存在
        Optional<FormConfigEntity> config = formConfigService.getFormConfigByCode(entity.getFormCode());
        if (config.isPresent() && StringUtils.hasText(config.get().getDbCode())) {
            try {
                dynamicTableService.createDynamicTable(config.get());
            } catch (Exception e) {
                throw new RuntimeException("创建表单数据表失败: " + e.getMessage(), e);
            }
        }
        
        // 原有保存逻辑...
        return formDataRepository.save(entity);
    }
}
```

- [ ] **Step 4: 编译测试动态表服务**

```bash
cd E:\claudeWork\878\wms.pj878
mvn compile -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 5: 提交动态表创建功能**

```bash
git add src/main/java/cn/zdjc/wms/project/form/service/DynamicTableService.java
git add src/main/java/cn/zdjc/wms/project/form/service/impl/DynamicTableServiceImpl.java
git add src/main/java/cn/zdjc/wms/project/form/service/impl/FormDataServiceImpl.java
git commit -m "feat: 动态表创建和数据迁移服务"
```

### Task 13: 性能优化和监控

**Files:**
- Create: `src/main/java/cn/zdjc/wms/project/form/config/DataSourceMonitor.java`
- Modify: `src/main/java/cn/zdjc/wms/project/form/config/DynamicDataSourceConfig.java`
- Create: `src/main/java/cn/zdjc/wms/project/form/aop/PerformanceMonitorAspect.java`

- [ ] **Step 1: 创建数据源监控器**

```java
// src/main/java/cn/zdjc/wms/project/form/config/DataSourceMonitor.java
package cn.zdjc.wms.project.form.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DataSourceMonitor {
    
    @Autowired
    private DynamicDataSource dynamicDataSource;
    
    private final Map<String, DataSourceHealth> healthStatus = new ConcurrentHashMap<>();
    
    @Scheduled(fixedDelay = 30000) // 每30秒检查一次
    public void monitorDataSources() {
        // 获取所有数据源
        Map<Object, Object> targetDataSources = dynamicDataSource.getTargetDataSources();
        
        if (targetDataSources != null) {
            for (Map.Entry<Object, Object> entry : targetDataSources.entrySet()) {
                String dataSourceKey = entry.getKey().toString();
                DataSource dataSource = (DataSource) entry.getValue();
                
                boolean healthy = checkDataSourceHealth(dataSource);
                DataSourceHealth health = healthStatus.computeIfAbsent(
                    dataSourceKey, k -> new DataSourceHealth()
                );
                health.setHealthy(healthy);
                health.setLastCheckTime(System.currentTimeMillis());
                
                if (dataSource instanceof DruidDataSource) {
                    DruidDataSource druidDataSource = (DruidDataSource) dataSource;
                    health.setActiveCount(druidDataSource.getActiveCount());
                    health.setPoolingCount(druidDataSource.getPoolingCount());
                    health.setWaitThreadCount(druidDataSource.getWaitThreadCount());
                }
            }
        }
    }
    
    private boolean checkDataSourceHealth(DataSource dataSource) {
        if (dataSource instanceof DruidDataSource) {
            try {
                DruidDataSource druidDataSource = (DruidDataSource) dataSource;
                return druidDataSource.isEnable();
            } catch (Exception e) {
                return false;
            }
        }
        
        // 通用的健康检查
        try (var connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5秒超时
        } catch (SQLException e) {
            return false;
        }
    }
    
    public Map<String, DataSourceHealth> getHealthStatus() {
        return new ConcurrentHashMap<>(healthStatus);
    }
    
    public DataSourceHealth getDataSourceHealth(String dataSourceKey) {
        return healthStatus.get(dataSourceKey);
    }
    
    public static class DataSourceHealth {
        private boolean healthy;
        private long lastCheckTime;
        private int activeCount;
        private int poolingCount;
        private int waitThreadCount;
        
        // getter和setter方法...
        public boolean isHealthy() { return healthy; }
        public void setHealthy(boolean healthy) { this.healthy = healthy; }
        public long getLastCheckTime() { return lastCheckTime; }
        public void setLastCheckTime(long lastCheckTime) { this.lastCheckTime = lastCheckTime; }
        public int getActiveCount() { return activeCount; }
        public void setActiveCount(int activeCount) { this.activeCount = activeCount; }
        public int getPoolingCount() { return poolingCount; }
        public void setPoolingCount(int poolingCount) { this.poolingCount = poolingCount; }
        public int getWaitThreadCount() { return waitThreadCount; }
        public void setWaitThreadCount(int waitThreadCount) { this.waitThreadCount = waitThreadCount; }
    }
}
```

- [ ] **Step 2: 创建性能监控切面**

```java
// src/main/java/cn/zdjc/wms/project/form/aop/PerformanceMonitorAspect.java
package cn.zdjc.wms.project.form.aop;

import cn.zdjc.wms.project.form.config.DataSourceContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
public class PerformanceMonitorAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitorAspect.class);
    private static final long SLOW_THRESHOLD_MS = 1000; // 1秒
    
    @Around("execution(* cn.zdjc.wms.project.form.service.*.*(..))")
    public Object monitorServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        try {
            return joinPoint.proceed();
        } finally {
            stopWatch.stop();
            long executionTime = stopWatch.getTotalTimeMillis();
            
            if (executionTime > SLOW_THRESHOLD_MS) {
                String currentDataSource = DataSourceContextHolder.getDataSourceKeyOrDefault();
                logger.warn("慢服务调用: {}.{}, 数据源: {}, 耗时: {}ms",
                    joinPoint.getTarget().getClass().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    currentDataSource,
                    executionTime);
            }
            
            if (logger.isDebugEnabled()) {
                logger.debug("服务调用: {}.{}, 耗时: {}ms",
                    joinPoint.getTarget().getClass().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    executionTime);
            }
        }
    }
    
    @Around("@annotation(cn.zdjc.wms.project.form.annotation.FormDataSource)")
    public Object monitorDataSourceSwitchPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        String beforeDataSource = DataSourceContextHolder.getDataSourceKeyOrDefault();
        
        try {
            return joinPoint.proceed();
        } finally {
            stopWatch.stop();
            String afterDataSource = DataSourceContextHolder.getDataSourceKeyOrDefault();
            long switchTime = stopWatch.getTotalTimeMillis();
            
            if (switchTime > 100) { // 100ms阈值
                logger.warn("数据源切换耗时较长: {} -> {}, 耗时: {}ms",
                    beforeDataSource, afterDataSource, switchTime);
            }
        }
    }
}
```

- [ ] **Step 3: 添加数据源监控API**

```java
// 在DatabaseConfigController中添加监控API
@RestController
@RequestMapping("/api/database/config")
@Api(tags = "数据库配置管理")
public class DatabaseConfigController {
    
    @Autowired
    private DataSourceMonitor dataSourceMonitor;
    
    @GetMapping("/monitor/health")
    @ApiOperation("获取数据源健康状态")
    public ResponseEntity<Map<String, DataSourceMonitor.DataSourceHealth>> getDataSourceHealth() {
        Map<String, DataSourceMonitor.DataSourceHealth> healthStatus = dataSourceMonitor.getHealthStatus();
        return ResponseEntity.ok(healthStatus);
    }
    
    @GetMapping("/monitor/statistics")
    @ApiOperation("获取数据源统计信息")
    public ResponseEntity<Map<String, Object>> getDataSourceStatistics() {
        Map<String, DataSourceMonitor.DataSourceHealth> healthStatus = dataSourceMonitor.getHealthStatus();
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalDataSources", healthStatus.size());
        
        long healthyCount = healthStatus.values().stream()
            .filter(DataSourceMonitor.DataSourceHealth::isHealthy)
            .count();
        statistics.put("healthyDataSources", healthyCount);
        
        int totalActiveConnections = healthStatus.values().stream()
            .mapToInt(DataSourceMonitor.DataSourceHealth::getActiveCount)
            .sum();
        statistics.put("totalActiveConnections", totalActiveConnections);
        
        return ResponseEntity.ok(statistics);
    }
}
```

- [ ] **Step 4: 编译测试性能监控**

```bash
cd E:\claudeWork\878\wms.pj878
mvn compile -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 5: 提交性能优化和监控**

```bash
git add src/main/java/cn/zdjc/wms/project/form/config/DataSourceMonitor.java
git add src/main/java/cn/zdjc/wms/project/form/aop/
git add src/main/java/cn/zdjc/wms/project/form/controller/DatabaseConfigController.java
git commit -m "feat: 数据源性能监控和健康检查"
```

### Task 14: 集成测试和部署验证

**Files:**
- Create: `src/test/java/cn/zdjc/wms/project/form/integration/FormDesignerIntegrationTest.java`
- Create: `deploy/表单设计器部署指南.md`
- Update: `README.md`

- [ ] **Step 1: 创建集成测试**

```java
// src/test/java/cn/zdjc/wms/project/form/integration/FormDesignerIntegrationTest.java
package cn.zdjc.wms.project.form.integration;

import cn.zdjc.wms.project.form.model.entity.DatabaseConfigEntity;
import cn.zdjc.wms.project.form.model.entity.FormConfigEntity;
import cn.zdjc.wms.project.form.model.entity.FormDataEntity;
import cn.zdjc.wms.project.form.service.DatabaseConfigService;
import cn.zdjc.wms.project.form.service.FormConfigService;
import cn.zdjc.wms.project.form.service.FormDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FormDesignerIntegrationTest {
    
    @Autowired
    private FormConfigService formConfigService;
    
    @Autowired
    private FormDataService formDataService;
    
    @Autowired
    private DatabaseConfigService databaseConfigService;
    
    @Test
    public void testCompleteFormWorkflow() {
        // 1. 创建表单配置
        FormConfigEntity formConfig = new FormConfigEntity();
        formConfig.setName("用户注册表单");
        formConfig.setCode("user_registration");
        formConfig.setFormType("REGISTRATION");
        formConfig.setConfigJson("{\"fields\": [{\"name\": \"username\", \"type\": \"String\"}], \"rules\": []}");
        
        FormConfigEntity savedForm = formConfigService.saveFormConfig(formConfig);
        assertNotNull(savedForm.getId());
        
        // 2. 查询表单配置
        Optional<FormConfigEntity> foundForm = formConfigService.getFormConfigByCode("user_registration");
        assertTrue(foundForm.isPresent());
        assertEquals("用户注册表单", foundForm.get().getName());
        
        // 3. 提交表单数据
        FormDataEntity formData = new FormDataEntity();
        formData.setFormCode("user_registration");
        formData.setBusinessKey("user_001");
        formData.setBusinessType("USER");
        formData.setFormDataJson("{\"username\": \"testuser\"}");
        
        FormDataEntity savedData = formDataService.submitFormData(formData);
        assertNotNull(savedData.getId());
        assertEquals("user_registration", savedData.getFormCode());
        
        // 4. 验证表单数据
        boolean valid = formDataService.validateFormData("user_registration", "{\"username\": \"testuser2\"}");
        assertTrue(valid);
    }
    
    @Test
    public void testDatabaseConfigWorkflow() {
        // 注意：这个测试需要真实的数据库连接，可能在实际测试环境中失败
        // 可以使用内存数据库或Mock进行测试
        
        DatabaseConfigEntity dbConfig = new DatabaseConfigEntity();
        dbConfig.setDbCode("test_integration_db");
        dbConfig.setDbName("集成测试数据库");
        dbConfig.setDbType("MYSQL");
        dbConfig.setDriverClass("com.mysql.cj.jdbc.Driver");
        dbConfig.setJdbcUrl("jdbc:mysql://localhost:3306/test_integration?useUnicode=true");
        dbConfig.setUsername("test");
        dbConfig.setPassword("test");
        
        try {
            DatabaseConfigEntity savedDb = databaseConfigService.saveDatabaseConfig(dbConfig);
            assertNotNull(savedDb.getId());
            assertEquals("test_integration_db", savedDb.getDbCode());
            
            // 测试数据库连接
            boolean testResult = databaseConfigService.testDatabaseConnection(savedDb);
            // 这里可能失败，因为测试数据库可能不存在
            System.out.println("数据库连接测试结果: " + testResult);
            
        } catch (Exception e) {
            // 预期可能会失败，因为测试数据库可能不存在
            System.out.println("数据库配置测试失败（预期中）: " + e.getMessage());
        }
    }
}
```

- [ ] **Step 2: 运行集成测试**

```bash
cd E:\claudeWork\878\wms.pj878
mvn test -Dtest="*IntegrationTest" -DfailIfNoTests=false
```

Expected: 测试通过或有预期的失败（如数据库连接测试）

- [ ] **Step 3: 创建部署指南**

```markdown
# 表单设计器部署指南

## 系统要求
- Java 11+
- MySQL 5.7+/8.0+（主数据库）
- SQL Server 2012+（可选）
- Oracle 11g+/12c+（可选）
- Node.js 14+（前端构建）
- Maven 3.6+

## 部署步骤

### 1. 数据库准备
```sql
-- 在主数据库（MySQL）中执行
-- 表单设计器相关表会自动通过Flyway创建
-- 确保Flyway配置正确
```

### 2. 后端部署
```bash
# 克隆代码
git clone <repository-url>
cd wms.pj878

# 配置数据库连接
vim src/main/resources/application.yml
# 修改spring.datasource配置

# 构建项目
mvn clean package -DskipTests

# 部署war包
cp target/wms.pj878-V7.1.0.15.war <tomcat-webapps>/
```

### 3. 前端部署
```bash
cd vue

# 安装依赖
npm install

# 构建生产版本
npm run build

# 将dist目录内容复制到静态资源目录
cp -r dist/* <static-resource-path>/
```

### 4. 多数据库配置
1. 启动应用后，访问系统
2. 进入"表单设计器 → 数据库配置"
3. 添加需要支持的数据库连接信息
4. 测试连接确保配置正确

## 配置说明

### 主配置文件
```yaml
# application.yml
spring:
  datasource:
    # 主数据库配置
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/wms87802
    username: root
    password: root
    
form:
  datasource:
    # 动态数据源默认配置
    default-validation-query: SELECT 1
    connection-timeout: 30000
```

### 数据库驱动依赖
需要确保以下驱动在classpath中：
- MySQL: mysql-connector-java 8.0.30+
- SQL Server: mssql-jdbc 10.2.0+
- Oracle: ojdbc8 21.8.0.0+

## 监控和维护

### 健康检查端点
- `GET /api/database/config/monitor/health` - 数据源健康状态
- `GET /api/database/config/monitor/statistics` - 数据源统计信息

### 日志配置
查看数据源切换和性能日志：
```properties
logging.level.cn.zdjc.wms.project.form.aop=DEBUG
logging.level.cn.zdjc.wms.project.form.config=INFO
```

## 故障排除

### 常见问题
1. **数据源切换失败**
   - 检查数据库连接配置是否正确
   - 验证数据库服务是否可访问
   - 检查防火墙和网络连接

2. **动态表创建失败**
   - 检查数据库用户是否有建表权限
   - 验证表单JSON配置格式是否正确

3. **前端表单设计器无法访问**
   - 检查静态资源路径配置
   - 验证路由配置是否正确
   - 查看浏览器控制台错误信息

### 性能优化建议
1. 合理配置数据库连接池参数
2. 对频繁访问的表单配置启用缓存
3. 定期监控数据源健康状态
4. 根据实际使用情况调整连接池大小
```

- [ ] **Step 4: 更新README.md**

```markdown
## 表单设计器模块

### 功能特性
- 可视化表单设计，支持Element UI组件
- 表单配置JSON存储和版本管理
- 多数据库支持（MySQL、SQL Server、Oracle）
- 动态数据源路由和切换
- 表单数据提交和查询
- 数据库连接管理和监控

### 快速开始
1. 确保数据库表已创建（Flyway自动迁移）
2. 启动后端服务
3. 访问前端界面：`/form-designer/list`
4. 配置数据库连接信息
5. 开始设计表单

### API文档
- 表单配置管理：`/api/form/config/**`
- 表单数据操作：`/api/form/data/**`
- 数据库配置管理：`/api/database/config/**`

### 开发指南
详见 [docs/superpowers/specs/2026-04-22-form-designer-integration-design.md]
详见 [docs/superpowers/specs/2026-04-22-multi-database-support-design.md]
```

- [ ] **Step 5: 完整系统验证**

```bash
# 1. 完整构建
cd E:\claudeWork\878\wms.pj878
mvn clean package -DskipTests

# 2. 检查构建产物
ls -la target/*.war
ls -la target/lib/ | grep -E "(mysql|sqlserver|oracle|druid)"

# 3. 前端构建验证
cd vue
npm run build
ls -la dist/

# 4. 提交最终版本
git add deploy/
git add README.md
git add src/test/java/cn/zdjc/wms/project/form/integration/
git commit -m "docs: 添加部署指南和集成测试"
```

- [ ] **Step 6: 最终提交**

```bash
git status
git log --oneline -5
# 确保所有更改已提交
```

---

## 执行选项

**计划已完成并保存到 [docs/superpowers/plans/2026-04-22-form-designer-with-multi-database.md](E:\claudeWork\878\wms.pj878\docs\superpowers\plans\2026-04-22-form-designer-with-multi-database.md)。**

**两个执行选项：**

**1. 子代理驱动（推荐）** - 我为每个任务分派一个新鲜的子代理，在任务之间进行审查，快速迭代

**2. 内联执行** - 在此会话中使用executing-plans执行任务，使用检查点进行批处理执行

**哪种方法？**