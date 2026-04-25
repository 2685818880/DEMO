# WMS 项目代码分析报告

## 1. 项目主要功能

本项目是一个**仓库管理系统（WMS）**，基于 Spring Boot 构建，包名为 `cn.zdjc.wms`，主要功能模块如下：

- **入库管理**：ASN 收货单处理、托盘化、上架（PjInboundServiceImpl）
- **出库/拣货管理**：拣货任务分配、工作站拣货确认、调度派发（PickExtServiceImpl、PickExtController）
- **ERP 数据同步**：与 ERP 系统双向同步 SKU 主数据、入库单、出库单、库存（erp 包下各 SyncService）
- **工作站管理**：工作站状态、历史记录、分拣配置（workstation、sort 包）
- **SKU 管理**：商品信息查询与同步（SkuExtController、SkuSyncService）
- **WCS 协议对接**：通过 HTTP 协议与 WCS（仓库控制系统）通信（ProjectWcsProtocolHttpImplV3）
- **WebSocket 支持**：实时推送工作站消息
- **Excel 导出**：报表导出功能（ExcelExportController）
- **Redis 缓存**：分布式锁与缓存支持（RedisCache、ProjectDistributedLockService）
- **Flyway 数据库迁移**：版本化数据库变更管理（ProjectFlywayModule）

---

## 2. TODO / FIXME 注释汇总

共发现 **6 处** TODO 注释，无 FIXME。

| # | 文件 | 行号 | 内容 |
|---|------|------|------|
| 1 | `src/main/java/cn/zdjc/wms/project/ProjectConfigInit.java` | 7 | `* TODO`（类级别待办，内容未填写） |
| 2 | `src/main/java/cn/zdjc/wms/project/service/inbound/impl/PjInboundServiceImpl.java` | 760 | `//TODO 需要拷贝原数据` |
| 3 | `src/main/java/cn/zdjc/wms/project/service/pick/impl/PickExtServiceImpl.java` | 1005 | `// TODO: 调用服务查询容器信息` |
| 4 | `src/main/java/cn/zdjc/wms/project/service/pick/impl/PickExtServiceImpl.java` | 1024 | `// TODO: 调用服务更新容器位置` |
| 5 | `src/main/java/cn/zdjc/wms/project/service/pick/impl/PickExtServiceImpl.java` | 1114 | `// TODO: 记录操作日志到数据库` |
| 6 | `src/main/java/cn/zdjc/wms/project/service/pick/impl/PickExtServiceImpl.java` | 1140 | `// TODO: 根据业务需求触发后续流程` |

> `PickExtServiceImpl.java` 集中了 4 处未完成逻辑，涉及容器查询/更新、操作日志记录和后续流程触发，需重点跟进。

---

## 3. 潜在空指针风险

### 高风险

| 文件 | 行号 | 问题描述 |
|------|------|----------|
| `PjInboundServiceImpl.java` | 678–690 | `stream().collect()` 中调用 `getSerial_no().trim()`，若 `serial_no` 为 null 则 NPE |
| `PjInboundServiceImpl.java` | 709–714 | 同上，另一处 `getSerial_no().trim()` 无 null 保护 |
| `PjInboundServiceImpl.java` | 750–772 | `map.get(entity.getSerial_no())` 返回值未判 null，直接调用 `asnDetail.getAsn_no()` 等方法 |
| `PjInboundServiceImpl.java` | 236–248 | `itemDtoList.get(0)` 在过滤后未再次校验列表非空，存在 `IndexOutOfBoundsException` 风险 |
| `PjInboundServiceImpl.java` | 320–330 | `asnItems.get(0)` 同上，未校验列表长度 |
| `PickExtServiceImpl.java` | 1079–1080 | `createDispatchInfoWithReturn()` 若返回 null，后续 `dto.getId()` 等调用直接 NPE |

### 中风险

| 文件 | 行号 | 问题描述 |
|------|------|----------|
| `PickExtServiceImpl.java` | 383 | `pickItemMap.get(itemDto.getPickItemId())` 结果未做 null 检查即传入下游方法 |
| `PickExtServiceImpl.java` | 1083 | `dispatchManager.queryByCode()` 返回值未判 null，直接调用 `dispatch.doDispatch()` |
| `PickExtController.java` | 428–431 | `pickInfo.getPickOrderInfoVoList()` 可能返回 null，直接调用 `.isEmpty()` 会 NPE |
| `SkuExtController.java` | 40–41 | `skuExtService.queryBySkuCode()` 结果未判 null，直接包装进成功响应返回 |

---

## 4. 修复建议

1. **`getSerial_no().trim()` 系列**：改用 `StringUtils.trimToNull(asnDetail.getSerial_no())` 或先过滤 null：
   ```java
   .filter(d -> d.getSerial_no() != null)
   ```

2. **`map.get()` 后直接使用**：加 null 检查或使用 `Optional`：
   ```java
   AsnDetailDto asnDetail = serialNoToAsnDetailMap.get(entity.getSerial_no());
   if (asnDetail == null) { throw new BusinessException("..."); }
   ```

3. **`list.get(0)` 越界风险**：在调用前校验：
   ```java
   if (CollUtil.isEmpty(itemDtoList)) { throw new BusinessException("..."); }
   ```

4. **服务返回值 null 检查**：对 `createDispatchInfoWithReturn()`、`queryByCode()` 等返回值统一加 null 断言或抛出业务异常。

5. **`getPickOrderInfoVoList()` 返回值**：改为：
   ```java
   List<PickOrderInfoVoList> list = pickInfo.getPickOrderInfoVoList();
   if (CollUtil.isEmpty(list)) { ... }
   ```
