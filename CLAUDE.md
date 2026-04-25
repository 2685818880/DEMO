# CLAUDE.md - WMS 项目管理指南

本文档为 Claude Code 提供项目开发指导方针，用于代码生成、审查和维护。这些指南也将被 `code-review` 插件用于自动化代码审查。

## 📋 项目概述
- **项目类型**: WMS (Warehouse Management System) - Java Web 应用
- **技术栈**: Spring Boot, MyBatis, Vue.js, Oracle Database
- **代码位置**: 878/ 目录包含多个子模块
- **审查工具**: 使用 `/code-review` 命令进行自动化 PR 审查

## 🎯 Claude 协作指南

### 代码生成原则
1. **优先理解现有模式**：在添加新功能前，先阅读同模块的现有代码
2. **保持一致性**：遵循项目现有的编码风格和架构模式
3. **渐进式修改**：对于大型重构，先创建原型或分阶段实施
4. **验证假设**：不确定时，先检查现有测试或运行部分代码

### 与 Claude 交互的最佳实践
- 提供具体的文件路径和行号引用
- 明确说明是修复 bug、添加功能还是重构
- 对于复杂任务，允许 Claude 先分析现有代码结构
- 在做出重大架构决策前请求确认

## 📝 编码规范

### Java 代码规范
```java
// ✅ 正确示例
@Service
public class InventoryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryService.class);
    private final InventoryRepository inventoryRepository;
    
    @Autowired
    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }
    
    public Inventory getInventoryById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("库存ID必须为正整数");
        }
        return inventoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("库存记录不存在: " + id));
    }
}

// ❌ 应避免
// - 使用魔法数字
// - 空的 catch 块
// - 过长的函数（>50行）
// - 循环嵌套超过3层
```

### 命名约定
- **类名**: 大驼峰，名词，如 `InventoryController`
- **方法名**: 小驼峰，动词开头，如 `calculateTotalStock`
- **变量名**: 小驼峰，有意义，如 `availableQuantity`
- **常量**: 全大写加下划线，如 `MAX_RETRY_COUNT`
- **包名**: 全小写，反向域名，如 `cn.zdjc.wms.inventory`

### 注释规范
1. **类注释**: 说明类的职责和主要功能
2. **复杂算法**: 解释逻辑思路和关键步骤
3. **公开API**: 方法参数、返回值、异常说明
4. **TODO/FIXME**: 明确标记待办事项和已知问题

## ⚠️ 错误处理要求

### 必须处理的场景
- **数据库操作**: 所有 CRUD 操作必须处理异常
- **外部API调用**: 超时、网络异常、响应错误
- **文件操作**: 文件不存在、权限不足、IO异常
- **参数验证**: 方法入参的 null 检查、范围验证

### 异常处理模式
```java
// ✅ 正确的异常处理
try {
    return someRiskyOperation();
} catch (SpecificException e) {
    LOGGER.error("操作失败: {}", e.getMessage(), e);
    throw new BusinessException("业务操作失败", e);
} finally {
    // 必须清理资源
    cleanupResources();
}

// ❌ 禁止的做法
try {
    // 空 catch 块
} catch (Exception e) {
    // 仅打印堆栈
    e.printStackTrace();
}
```

### 日志记录标准
- **ERROR**: 业务失败、系统异常、数据不一致
- **WARN**: 可恢复的异常、降级操作、配置问题
- **INFO**: 重要业务操作、流程节点、状态变更
- **DEBUG**: 调试信息、详细参数、执行路径

## 🔒 安全要求

### 输入验证（必须执行）
```java
// ✅ 正确的输入验证
public void updateUser(String username, String password) {
    if (username == null || username.trim().isEmpty()) {
        throw new ValidationException("用户名不能为空");
    }
    if (username.length() < 3 || username.length() > 50) {
        throw new ValidationException("用户名长度必须在3-50字符之间");
    }
    if (!Pattern.matches("^[a-zA-Z0-9_]+$", username)) {
        throw new ValidationException("用户名只能包含字母、数字和下划线");
    }
    // SQL 注入防护
    if (password != null && password.contains("'") || password.contains(";")) {
        throw new ValidationException("密码包含非法字符");
    }
}
```

### 安全防护措施
1. **SQL 注入**: 必须使用参数化查询或 MyBatis 参数绑定
2. **XSS 防护**: 前端输出必须转义，使用 Vue.js 的文本绑定
3. **CSRF 防护**: 重要操作需要令牌验证
4. **权限验证**: 业务操作前检查用户权限
5. **敏感数据**: 密码、密钥必须加密存储

## 🧪 测试规范

### 单元测试要求
```java
// ✅ 良好的单元测试
@Test
public void shouldCalculateInventoryTotal() {
    // Given
    Inventory inventory = new Inventory();
    inventory.setQuantity(10);
    inventory.setUnitPrice(100.0);
    
    // When
    double total = inventory.calculateTotalValue();
    
    // Then
    assertEquals(1000.0, total, 0.001);
    assertNotNull(inventory.getLastUpdated());
}

// ❌ 应避免的测试
// - 测试私有方法（通过公共方法间接测试）
// - 依赖外部服务（使用 Mock）
// - 测试实现细节而非行为
```

### 测试覆盖率目标
- **业务逻辑层**: 80%+ 行覆盖率
- **控制器层**: 主要路径覆盖
- **工具类**: 100% 分支覆盖
- **集成测试**: 关键业务流程

## 🚀 性能优化指南

### 数据库优化
1. **查询优化**: 避免 N+1 查询，使用 JOIN 或批量查询
2. **索引策略**: 为频繁查询的字段添加索引
3. **连接池**: 合理配置连接池参数
4. **事务管理**: 使用合适的事务隔离级别

### 代码性能
```java
// ✅ 性能友好的代码
// 使用 StringBuilder 进行字符串拼接
StringBuilder sql = new StringBuilder();
sql.append("SELECT * FROM inventory WHERE ");
sql.append("status = ?");

// 使用批量操作
List<Inventory> items = inventoryRepository.findAll();
for (Inventory item : items) {
    processItem(item);  // 避免在循环中执行查询
}

// ❌ 性能问题
// 在循环中执行数据库查询
for (Long id : idList) {
    inventoryRepository.findById(id);  // 每次循环都查询数据库
}
```

## 📁 项目结构规范

### 模块划分
```
wms.pj878/
├── src/main/java/cn/zdjc/wms/
│   ├── controller/     # 控制器层
│   ├── service/        # 业务逻辑层
│   ├── repository/     # 数据访问层
│   ├── model/          # 数据模型
│   └── config/         # 配置类
├── src/main/resources/
│   ├── mapper/         # MyBatis XML
│   └── application.yml # 配置文件
└── src/test/java/      # 测试代码
```

### 包依赖规则
1. **controller** → **service** → **repository**
2. 不允许反向依赖或循环依赖
3. 通用工具类放在 `common` 包中
4. 领域模型保持独立，不依赖其他包

## 🔍 Code-Review 插件专用指南

### 审查优先级
1. **安全漏洞** (优先级: 高危) - 立即阻止合并
2. **数据一致性风险** (优先级: 高) - 必须修复
3. **性能问题** (优先级: 中) - 建议修复
4. **代码规范** (优先级: 低) - 可选择性修复

### 必须检查的项目
- [ ] 所有数据库查询都使用参数化查询
- [ ] 用户输入都经过验证
- [ ] 异常都被适当处理
- [ ] 事务边界正确设置
- [ ] 日志级别适当
- [ ] 测试覆盖关键路径

### 自动审查阈值
- **置信度 ≥80**: 必须修复的问题
- **置信度 60-79**: 建议修复的问题
- **置信度 <60**: 可忽略的微小问题

## 📊 质量指标

### 代码质量红线
- **圈复杂度**: 单个方法 ≤15
- **代码重复率**: ≤5%
- **测试覆盖率**: ≥70%
- **编译警告**: 零容忍
- **TODO/FIXME**: 必须有创建日期和负责人

### 审查通过标准
1. 无安全漏洞
2. 通过所有自动化测试
3. 符合编码规范
4. 有适当的文档更新
5. 性能影响可接受

## 🔄 更新和维护

### 指南更新流程
1. 发现新的最佳实践或问题模式
2. 在团队内讨论并达成共识
3. 更新 CLAUDE.md 文件
4. 通知团队成员
5. 应用于后续的所有审查

### 版本记录
- **v1.0.0** (2026-04-03): 初始版本，建立基本规范

---

*本文档是动态更新的，随着项目发展不断完善。所有贡献者都应熟悉并遵循这些指南。*