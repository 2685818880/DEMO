# 模板调整 TODO List

1. 修改各个模块的文件名
2. 修改各个模块的POM文件
3. 调整以下配置文件的数据库连接字符串
    * src/main/resources/config-custom-db.properties.temp
    
4. 调整系统默认文件目录地址    
    * bm-XXX-service/src/test/resources/config.properties
    * bm-XXX-service/src/test/resources/log4j2-test.xml
        
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