package cn.zdjc.wms.project.scheduler;

import cn.zdjc.platform.system.manage.log.lucene.BusinessDomain;
import cn.zdjc.platform.system.manage.log.lucene.BusinessLogger;
import cn.zdjc.platform.system.manage.log.lucene.BusinessLoggerFactory;
import cn.zdjc.platform.system.manage.scheduler.AbstractScheduler;
import cn.zdjc.warehouse.inventory.domain.dto.StorageMaterialDto;
import cn.zdjc.warehouse.inventory.domain.service.StorageMaterialService;
import cn.zdjc.wms.PlatFormConfig;
import cn.zdjc.wms.commons.StorageMaterialInfo;
import cn.zdjc.wms.definition.infrastructure.enums.FormStatus;
import cn.zdjc.wms.outbound.pick.application.params.ContainerTakeOutParam;
import cn.zdjc.wms.outbound.pick.domain.entity.PickItem;
import cn.zdjc.wms.outbound.pick.domain.repository.IPickItemRepositoryService;
import cn.zdjc.wms.outbound.pick.domain.repository.params.PickItemQueryParam;
import cn.zdjc.wms.outbound.pick.domain.server.PickOutServerManage;
import cn.zdjc.wms.outbound.pick.domain.server.PickingContainerOutServer;
import cn.zdjc.wms.outbound.pick.infrastructure.enums.PickStatus;
import cn.zdjc.wms.outbound.requisition.application.service.ReqQueryAppService;
import cn.zdjc.wms.outbound.requisition.domain.repository.IReqOrderRepositoryService;
import cn.zdjc.wms.outbound.requisition.domain.repository.params.ReqOrderQueryParam;
import cn.zdjc.wms.outbound.requisition.domain.share.SkuInfo;
import cn.zdjc.wms.outbound.requisition.infrastructure.pojo.ReqOrderPojo;
import cn.zdjc.wms.outbound.requisition.interfaces.web.dto.ReqOrderDto;
import cn.zdjc.wms.project.common.utils.TraceIdGenerator;
import cn.zdjc.wms.project.domain.entity.ws.WorkstationExtEntity;
import cn.zdjc.wms.project.domain.enums.ws.WorkstationModeEnums;
import cn.zdjc.wms.project.domain.enums.ws.WorkstationStatusEnums;
import cn.zdjc.wms.project.domain.query.ws.WorkstationExtQuery;
import cn.zdjc.wms.project.service.workstation.WorkstationService;
import com.foeris.y.common.json.JsonUtl;
import com.foreris.eris.common.exception.BusinessException;
import com.google.common.collect.ArrayListMultimap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 📦 出库自动拣选调度器
 * <p>
 * 核心规则：
 * <ul>
 *   <li>库存 &gt; 0 → ✅ 直接放行下发</li>
 *   <li>库存 = 0 + 订单备注="PROCESS" → ✅ 允许下发</li>
 *   <li>库存 = 0 + 订单备注≠"PROCESS" → ⏭️ 跳过不下发</li>
 * </ul>
 * <p>
 * 工作站调度规则（核心口诀：查模式，不查状态；改订单，不改模式）：
 * <ul>
 *   <li>订单未完成 → 继续执行当前订单（保持忙碌）</li>
 *   <li>订单完成 → 释放绑定，工作模式保持 MATERIAL_PICK，状态变空闲</li>
 *   <li>无新订单 → 工作站保持空闲，工作模式保持 MATERIAL_PICK</li>
 *   <li>订单已绑定其他工作站 → ❌ 不允许重复绑定</li>
 * </ul>
 */
@Component
@Slf4j
public class PickScheduler extends AbstractScheduler {

    // ─────────────────────────────────────────────────────────────
    // ⚙️ 常量定义
    // ─────────────────────────────────────────────────────────────
    private static final String SCHEDULER_CODE = "PickScheduler";
    private static final String SCHEDULER_NAME = "出库自动拣选-项目定制(用于生成下架单)";
    private static final String LOG_PREFIX = "[PickScheduler] ";
    private static final String ORDER_REMARK_PROCESS = "PROCESS";
    private static final String SKU_CODE_UNKNOWN = "UNKNOWN";
    private static final String GROUP_KEY_SEPARATOR = "#|:";
    private static final String OPERATOR_SYSTEM = "system";

    // 活跃拣货状态常量（用于判断订单是否可释放）
    private static final List<PickStatus> ACTIVE_PICK_STATUSES =
            Collections.unmodifiableList(Arrays.asList(PickStatus.create, PickStatus.picking,PickStatus.executing));

    // 业务审计日志
    private final BusinessLogger businessLogger = BusinessLoggerFactory.getBusinessLogger(BusinessDomain.default_business.name());

    // ─────────────────────────────────────────────────────────────
    // 🔧 依赖注入
    // ─────────────────────────────────────────────────────────────
    @Resource
    private IPickItemRepositoryService pickItemRepository;
    @Resource
    private PickOutServerManage pickOutServerManage;
    @Resource
    private WorkstationService workstationService;
    @Resource
    private StorageMaterialService storageMaterialService;
    @Resource
    private IReqOrderRepositoryService reqOrderRepository;
    @Resource
    private ReqQueryAppService reqQueryAppService;
    @Resource
    private TraceIdGenerator traceIdGenerator;
    // ─────────────────────────────────────────────────────────────
    // 📋 Quartz 接口实现
    // ─────────────────────────────────────────────────────────────
    @Override
    public String getCode() {
        return SCHEDULER_CODE;
    }

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(JobExecutionContext context) throws JobExecutionException {
        long startTime = System.currentTimeMillis();
        String traceId = traceIdGenerator.generate();  // ✅ 注入使用

        try {
            log.info("{}▶ 开始执行自动拣选任务", LOG_PREFIX);
            // ✅ 审计日志：调度开始
            businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                    .setContainer(null)
                    .log("[trace={}] 调度任务开始 | triggerTime={}",
                            traceId, new Date());
            // 1. 获取所有拣选模式工作站（✅ 关键：只按模式过滤，不按状态过滤）
            //    这样既能查到空闲工作站（可绑定新单），也能查到忙碌工作站（可检查订单完成）
            List<WorkstationExtEntity> workstations = fetchWorkstationsByMode();
            if (CollectionUtils.isEmpty(workstations)) {
                log.debug("{}◀ 未找到拣选模式工作站，任务结束", LOG_PREFIX);
                return;
            }

            // 2. 顺序处理各工作站任务（保证事务一致性）
            int processedCount = 0;
            int errorCount = 0;

            for (WorkstationExtEntity ws : workstations) {
                try {
                    if (processWorkstation(ws,traceId)) {
                        processedCount++;
                    }
                } catch (Exception e) {
                    // ✅ 单个工作站处理失败不影响其他工作站
                    errorCount++;
                    log.error("{}❌ 处理工作站 [{}] 失败，继续处理下一个 | error:{}",
                            LOG_PREFIX, ws.getWorkstationCode(), e.getMessage(), e);
                    // ✅ 不抛出异常，确保事务不会因为单个工作站失败而回滚
                }
            }

            if (errorCount > 0) {
                log.warn("{}⚠ 任务完成但有 {} 个工作站处理失败", LOG_PREFIX, errorCount);
            }

            log.info("{}◀ 自动拣选任务完成 | 工作站总数:{} | 成功处理:{} | 失败:{}",
                    LOG_PREFIX, workstations.size(), processedCount, errorCount);
            long totalDuration = System.currentTimeMillis() - startTime;

            // ✅ 审计日志：调度结束（无论成功失败都记录）
            businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                    .setContainer(null)
                    .log("[trace={}] 调度任务结束 | totalWs={} success={} failed={} duration={}ms",
                            traceId, workstations.size(), processedCount, errorCount, totalDuration);
        } catch (BusinessException e) {
            log.error("{}❌ 业务异常：{}", LOG_PREFIX, e.getMessage());
            businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                    .setContainer(null)
                    .log("[trace={}] 调度业务异常 | error={}", traceId, e.getMessage());
            throw new JobExecutionException(e, false); // 业务异常不重试
        } catch (Exception e) {
            log.error("{}❌ 系统异常：{}", LOG_PREFIX, ExceptionUtils.getStackTrace(e));
            // ✅ 审计日志：系统异常
            businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                    .setContainer(null)
                    .log("[trace={}] 调度系统异常 | error={}", traceId, ExceptionUtils.getRootCauseMessage(e));
            throw new JobExecutionException(e, true);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 🏗️ 工作站处理主流程
    // ─────────────────────────────────────────────────────────────

    /**
     * 获取所有拣选模式工作站（✅ 核心：只按模式过滤，不按状态过滤）
     *
     * @return 所有模式为 MATERIAL_PICK 的工作站（不管空闲还是忙碌）
     */
    private List<WorkstationExtEntity> fetchWorkstationsByMode() {
        WorkstationExtQuery query = new WorkstationExtQuery();
        query.setWorkstationMode(WorkstationModeEnums.MATERIAL_PICK.getCode());
        // ❌ 不设置 workstationStatus 条件，确保能查到忙碌的工作站
        return workstationService.getWorkstationsByCode(query);
    }

    /**
     * 处理单个工作站的拣选任务
     *
     * @param ws 工作站实体
     * @return 是否成功处理
     */
    private boolean processWorkstation(WorkstationExtEntity ws,String traceId) {
        String wsCode = ws.getWorkstationCode();
        String boundOrderId = ws.getOrderId();

        String workstationStatus = ws.getWorkstationStatus();
        // 参数校验
        if (StringUtils.isBlank(wsCode)) {
            log.warn("{}⚠ 工作站编码为空，跳过 | workstation={}", LOG_PREFIX, ws);
            return false;
        }
        if("MAINTENANCE".equals(workstationStatus)||"ERROR".equals(workstationStatus)||"OFFLINE".equals(workstationStatus)){
            log.warn("{}⚠ 工作站状态{}不允许作业，跳过 | workstation={}", LOG_PREFIX,workstationStatus, ws);
            businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                    .setContainer(null)
                    .log("{}⚠ 工作站状态{}不允许作业，跳过 | workstation={}", LOG_PREFIX,workstationStatus, ws);
            return false;
        }

        try {
            // 场景 A: 工作站已绑定订单 → 检查订单是否完成，决定继续执行或释放
            if (StringUtils.isNotBlank(boundOrderId)&&"BUSY".equals(workstationStatus)) {
                return processBoundOrder(ws, boundOrderId,traceId);
            }

            // 场景 B: 工作站空闲（未绑定订单）→ 尝试绑定新订单
            return bindAndProcessNewOrder(ws,traceId);

        } catch (Exception e) {
            log.error("{}❌ 处理工作站 [{}] 异常：{}", LOG_PREFIX, wsCode, e.getMessage(), e);
            // 异常时尝试释放工作站，避免死锁
            try {
                releaseWorkstation(wsCode, "处理异常自动释放",traceId);
            } catch (Exception ex) {
                log.error("{}❌ 异常释放工作站 [{}] 失败：{}", LOG_PREFIX, wsCode, ex.getMessage(), ex);
            }
            return false;
        }
    }

    /**
     * 处理已绑定订单的工作站
     * <p>✅ 业务规则：订单未完成→继续执行 | 订单完成→释放绑定（保持模式）</p>
     *
     * @param ws      工作站实体
     * @param orderId 绑定的订单 ID
     * @return 是否处理成功
     */
    private boolean processBoundOrder(WorkstationExtEntity ws, String orderId,String traceId) {
        String wsCode = ws.getWorkstationCode();

        try {
            // 查询该订单的待执行拣货项
            List<PickItem> pendingItems = findPendingPickItems(orderId);

            if (CollectionUtils.isEmpty(pendingItems)) {
                // ✅ 订单已完成：释放工作站绑定，但保持工作模式为 MATERIAL_PICK
                log.info("{}ℹ 工作站 [{}] 绑定订单 [{}] 无待执行任务，释放工作站（模式保持）",
                        LOG_PREFIX, wsCode, orderId);
                boolean released = releaseWorkstation(wsCode, "订单任务完成",traceId);
                if (!released) {
                    log.warn("{}⚠ 释放工作站 [{}] 失败", LOG_PREFIX, wsCode);
                }
                return released;
            }

            // ✅ 订单未完成：继续执行当前订单的剩余任务（保持忙碌状态）
            return executePickTask(wsCode, pendingItems,traceId);

        } catch (Exception e) {
            log.error("{}❌ 处理已绑定订单工作站 [{}] 异常：{}", LOG_PREFIX, wsCode, e.getMessage(), e);
            // ✅ 发生异常时尝试释放工作站
            try {
                releaseWorkstation(wsCode, "处理异常自动释放：" + e.getMessage(),traceId);
            } catch (Exception ex) {
                log.error("{}❌ 异常释放工作站 [{}] 失败：{}", LOG_PREFIX, wsCode, ex.getMessage(), ex);
            }
            return false;
        }
    }

    /**
     * 空闲工作站绑定新订单
     * <p>✅ 业务规则：有新订单→绑定执行 | 无新订单→保持空闲（模式不变）</p>
     * <p>✅ 支持订单 extra 字段指定工作站：{"workstationCode":"WS001"}</p>
     * <p>✅ 新增：智能订单分配策略</p>
     *
     * @param ws 工作站实体（空闲状态）
     * @return 是否绑定并处理成功
     */
    private boolean bindAndProcessNewOrder(WorkstationExtEntity ws, String traceId) {
        String wsCode = ws.getWorkstationCode();
        if (StringUtils.isBlank(wsCode)) {
            log.warn("{}⚠ 工作站编码为空，无法绑定订单", LOG_PREFIX);
            return false;
        }

        // 1️⃣ 查询可分配的订单（按业务状态过滤）
        ReqOrderQueryParam queryParam = new ReqOrderQueryParam();
        queryParam.setForm_status(Arrays.asList(
                FormStatus.Created, FormStatus.Executing, FormStatus.Picked, FormStatus.CreatedAndExecuting));

        List<ReqOrderDto> candidateOrders = reqQueryAppService.findOrderByParam(queryParam);
        if (CollectionUtils.isEmpty(candidateOrders)) {
            log.debug("{}ℹ 工作站 [{}] 暂无可分配订单，保持空闲（模式保持）", LOG_PREFIX, wsCode);
            return false;
        }

        // 2️⃣ 遍历候选订单，找到第一个可绑定的进行绑定
        for (ReqOrderDto order : candidateOrders) {
            UUID orderId = order.getId();
            String businessFormNo = order.getBusiness_form_no();
            if (orderId == null) {
                continue;
            }

            // ✅ 审计日志：订单分配决策 - 跳过（工作站不匹配）
            if (!isOrderAssignableToWorkstation(order, wsCode)) {
                businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                        .setContainer(wsCode)
                        .log("[trace={}] 订单分配跳过 | orderId={}| orderNo={}",
                                traceId, orderId,businessFormNo);
                continue;
            }

            List<PickItem> pendingItems = findPendingPickItems(orderId.toString());

            // ✅ 审计日志：订单分配决策 - 跳过（无待执行任务）
            if (CollectionUtils.isEmpty(pendingItems)) {
                businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                        .setContainer(wsCode)
                        .log("[trace={}] 订单分配跳过 | orderId={} | orderNo={} reason=NO_PENDING_ITEMS",
                                traceId, orderId,businessFormNo);
                continue;
            }



            // ✅ 尝试绑定订单到工作站
            String orderNo = StringUtils.defaultString(order.getBusiness_form_no(), orderId.toString());
            boolean bindSuccess = bindOrderToWorkstation(wsCode, orderId.toString(), orderNo,traceId);
            if (!bindSuccess) {
                log.warn("{}⚠ 绑定订单 [{}] 到工作站 [{}] 失败，尝试下一个",
                        LOG_PREFIX, orderId, wsCode);
                continue;
            }
            businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                    .setContainer(null)
                    .log("{}✅ 工作站 [{}] 绑定订单 [{}] 成功 | orderNo={}",
                            LOG_PREFIX, wsCode, orderId, orderNo);


            log.info("{}✅ 工作站 [{}] 绑定订单 [{}] 成功 | orderNo={}",
                    LOG_PREFIX, wsCode, orderId, orderNo);



            // ✅ 绑定成功后立即执行任务
            return executePickTask(wsCode, pendingItems,traceId);
        }
        // ✅ 审计日志：订单分配决策 - 无可用订单
        businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                .setContainer(wsCode)
                .log("[trace={}] 订单分配完成 | ws={} result=NO_ASSIGNABLE_ORDER candidateCount={}",
                        traceId, wsCode, candidateOrders.size());
        log.debug("{}ℹ 工作站 [{}] 未找到可绑定的有效订单，保持空闲（模式保持）", LOG_PREFIX, wsCode);
        return false;
    }

    /**
     * 校验订单是否可分配到指定工作站
     * <p>支持订单 extra 字段指定工作站：{"workstationCode":"WS001"}</p>
     *
     * @param order                 订单 DTO
     * @param targetWorkstationCode 目标工作站编码
     * @return true: 可分配 / false: 不可分配（指定了其他工作站或解析失败）
     */
    private boolean isOrderAssignableToWorkstation(ReqOrderDto order, String targetWorkstationCode) {
        String extra = order.getExtra();

        // 无 extra 字段：默认所有工作站都可处理
        if (StringUtils.isBlank(extra)) {
            return true;
        }

        try {
            // ✅ 解析 extra 为 Map
            Map extraMap = JsonUtl.format(extra, Map.class);
            if (extraMap == null) {
                log.debug("{}⚠ 订单 [{}] extra 解析结果为 null，默认允许分配",
                        LOG_PREFIX, order.getId());
                return true;
            }

            // ✅ 获取指定的工作站编码
            Object specifiedWsObj = extraMap.get("workstationCode");
            if (specifiedWsObj == null) {
                return true;
            }

            // ✅ 转为字符串并比较（兼容数字/字符串类型）
            String specifiedWsCode = specifiedWsObj.toString().trim();
            boolean isMatch = StringUtils.equals(specifiedWsCode, targetWorkstationCode);

            if (!isMatch && log.isDebugEnabled()) {
                log.debug("{}⏭ 订单 [{}] 指定工作站 [{}] ≠ 当前工作站 [{}]，跳过",
                        LOG_PREFIX, order.getId(), specifiedWsCode, targetWorkstationCode);
            }
            return isMatch;

        } catch (Exception e) {
            // ✅ 解析失败时保守处理：记录日志 + 跳过该订单（避免错误分配）
            log.warn("{}⚠ 订单 [{}] extra 字段解析失败: {} | 跳过该订单",
                    LOG_PREFIX, order.getId(), extra, e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 🔗 订单绑定与释放（核心：改订单，不改模式）
    // ─────────────────────────────────────────────────────────────

    private List<PickItem> findPendingPickItems(String orderId) {
        if (StringUtils.isBlank(orderId)) {
            return Collections.emptyList();
        }

        UUID orderUuid;
        try {
            orderUuid = UUID.fromString(orderId);
        } catch (IllegalArgumentException e) {
            log.error("{}❌ 订单 ID 格式错误：{}", LOG_PREFIX, orderId, e);
            return Collections.emptyList();
        }

        PickItemQueryParam param = new PickItemQueryParam();
        param.setRelationOrderId(orderUuid);
        param.setPickStatuses(ACTIVE_PICK_STATUSES);
        param.setPickAble(Boolean.TRUE);

        List<PickItem> items = pickItemRepository.findPickItemByParam(param);
        if (CollectionUtils.isEmpty(items)) {
            return Collections.emptyList();
        }

        // 1️⃣ 提取非空的物料 ID 集合
        Set<UUID> materialIds = items.stream()
                .map(PickItem::getStorageMaterialInfo)
                .filter(Objects::nonNull)
                .map(StorageMaterialInfo::getStorageMaterialId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 2️⃣ 如果没有关联物料，直接返回原列表（或根据业务决定返回空）
        if (materialIds.isEmpty()) {
            return items;
        }

        try {
            // 3️⃣ 调用你唯一的接口：拿到物料列表
            List<StorageMaterialDto> storageMaterialDtos = storageMaterialService.listByIds(materialIds);

            // 4️⃣ ✅ 关键：转成 Map<UUID, StorageMaterialDto> 方便 O(1) 查找
            Map<UUID, StorageMaterialDto> materialMap = storageMaterialDtos.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(
                            StorageMaterialDto::getId,
                            Function.identity(),
                            (v1, v2) -> v1,  // 冲突时保留第一个
                            HashMap::new));

            // 5️⃣ ✅ 过滤：只保留「有可用库存」的拣货项
            return items.stream()
                    .filter(item -> {
                        StorageMaterialInfo materialInfo = item.getStorageMaterialInfo();
                        if (materialInfo == null || materialInfo.getStorageMaterialId() == null) {
                            return false;
                        }

                        StorageMaterialDto material = materialMap.get(materialInfo.getStorageMaterialId());
                        if (material == null) {
                            // 物料不存在于缓存/数据库中，根据业务决定：跳过 或 保留
                            return false;
                        }
                        Double availableStock = material.getAvailable_qty();
                        return availableStock != null && availableStock > 0;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("{}❌ 加载物料库存异常：{}", LOG_PREFIX, e.getMessage(), e);
            // 🔸 降级策略：根据业务决定是返回空列表，还是返回未过滤的 items
            return Collections.emptyList();
            // return items; // 如果允许「查不到库存时仍展示」，可改为返回原列表
        }
    }

    /**
     * 绑定订单到工作站（原子操作）
     * <p>✅ 核心：查询时已确认模式，绑定时无需重复设置 workstationMode</p>
     * <p>✅ 新增：检查订单是否已被其他工作站占用</p>
     * <p>✅ 修复：移除 @Transactional 注解，使用外层事务</p>
     *
     * @param workstationCode 工作站编码
     * @param orderId         订单 ID
     * @param orderNo         订单号
     * @return 是否绑定成功
     */

    public boolean bindOrderToWorkstation(String workstationCode, String orderId, String orderNo,String traceId) {
        if (StringUtils.isAnyBlank(workstationCode, orderId)) {
            businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                    .setContainer(workstationCode)
                    .log("绑定参数不完整 | orderId={}", orderId);
            return false;
        }

        try {
            WorkstationExtEntity ws = workstationService.findInfoByCodeAndMode(
                    workstationCode, WorkstationModeEnums.MATERIAL_PICK.getCode());
            if (ws == null) {
                businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                        .setContainer(workstationCode)
                        .log("绑定失败：工作站不存在或模式不匹配");
                return false;
            }

            if (StringUtils.isNotBlank(ws.getOrderId()) && !orderId.equals(ws.getOrderId())) {
                businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                        .setContainer(workstationCode)
                        .log("绑定失败：工作站已被订单[{}]占用", ws.getOrderId());
                return false;
            }

            if (isOrderBoundToOtherWorkstation(orderId, workstationCode)) {
                businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                        .setContainer(workstationCode)
                        .log("绑定失败：订单[{}]已被其他工作站占用", orderId);
                return false;
            }

            ws.setOrderId(orderId);
            ws.setOrderNo(orderNo);
            ws.setWorkstationStatus(WorkstationStatusEnums.BUSY.getCode());
            ws.setLastModifyDatetime(new Date());
            ws.setLastModifyBy(OPERATOR_SYSTEM);
            ws.setRemark("系统自动绑定订单：" + orderNo);

            boolean updated = workstationService.updateById(ws);
            if (updated) {
                log.info("{}✅ [trace={}] 工作站 [{}] 绑定订单 [{}] 成功",
                        LOG_PREFIX, traceId, workstationCode, orderId);

                // ✅ 审计日志：绑定成功（核心业务事件）
                businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                        .setContainer(workstationCode)
                        .log("[trace={}] 订单绑定成功 | orderId={} orderNo={} bindTime={}",
                                traceId, orderId, orderNo, new Date());
            } else {
                log.warn("{}⚠ [trace={}] 工作站 [{}] 绑定订单 [{}] 数据库更新失败",
                        LOG_PREFIX, traceId, workstationCode, orderId);

                // ✅ 审计日志：绑定失败
                businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                        .setContainer(workstationCode)
                        .log("[trace={}] 订单绑定失败 | orderId={} orderNo={} reason=DB_UPDATE_FAILED",
                                traceId, orderId, orderNo);
            }
            return updated;

        } catch (Exception e) {
            businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                    .setContainer(workstationCode)
                    .log("绑定订单异常 | orderId={}, error={}", orderId, e.getMessage());
            return false;
        }
    }

    /**
     * 释放工作站订单绑定
     * <p>✅ 核心原则：只清空订单信息，❌ 绝不修改 workstationMode</p>
     * <p>✅ 修复：移除 @Transactional 注解，使用外层事务</p>
     *
     * @param workstationCode 工作站编码
     * @param reason          释放原因
     * @return 是否释放成功
     */
    public boolean releaseWorkstation(String workstationCode, String reason,String traceId) {
        if (StringUtils.isBlank(workstationCode)) {
            return false;
        }

        try {
            WorkstationExtEntity ws = workstationService.findInfoByCodeAndMode(workstationCode, null);
            if (ws == null) {
                businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                        .setContainer(workstationCode)
                        .log("释放失败：工作站不存在");
                return false;
            }

            String originalOrderNo = ws.getOrderNo();
            String originalMode = ws.getWorkstationMode();

            // ✅ 使用 LambdaUpdateWrapper 显式设置 NULL 值
            boolean updated = workstationService.lambdaUpdate()
                    .eq(WorkstationExtEntity::getId, ws.getId())  // 主键条件
                    .set(WorkstationExtEntity::getOrderId, null)   // ✅ 显式更新为 NULL
                    .set(WorkstationExtEntity::getOrderNo, null)   // ✅ 显式更新为 NULL
                    .set(WorkstationExtEntity::getWorkstationStatus, WorkstationStatusEnums.IDLE.getCode())
                    .set(WorkstationExtEntity::getLastModifyDatetime, new Date())
                    .set(WorkstationExtEntity::getLastModifyBy, OPERATOR_SYSTEM)
                    .set(WorkstationExtEntity::getRemark, "【释放订单】" + reason + "; 原订单:" + originalOrderNo)
                    .update();

            if (updated) {
                log.info("{}✅ [trace={}] 工作站 [{}] 释放成功 | 原因={} 原订单={}",
                        LOG_PREFIX, traceId, workstationCode, reason, originalOrderNo);

                // ✅ 审计日志：释放成功（核心业务事件）
                businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                        .setContainer(workstationCode)
                        .log("[trace={}] 工作站释放 | reason={} originalOrder={} releaseTime={}",
                                traceId, reason, originalOrderNo, new Date());
            } else {
                log.warn("{}⚠ [trace={}] 工作站 [{}] 释放数据库更新失败", LOG_PREFIX, traceId, workstationCode);

                // ✅ 审计日志：释放失败
                businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                        .setContainer(workstationCode)
                        .log("[trace={}] 工作站释放失败 | reason={} originalOrder={} reason=DB_UPDATE_FAILED",
                                traceId, reason, originalOrderNo);
            }
            return updated;

        } catch (Exception e) {
            businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                    .setContainer(workstationCode)
                    .log("释放异常 | error={}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查订单是否已被其他工作站占用
     *
     * @param orderId                订单 ID
     * @param currentWorkstationCode 当前工作站编码
     * @return true: 已被其他工作站占用 / false: 未占用或占用的是当前工作站（幂等）
     */
    private boolean isOrderBoundToOtherWorkstation(String orderId, String currentWorkstationCode) {
        try {
            // 查询所有绑定了该订单的工作站
            WorkstationExtQuery query = new WorkstationExtQuery();
            query.setOrderId(orderId);
            List<WorkstationExtEntity> boundWorkstations = workstationService.getWorkstationsByCode(query);

            if (CollectionUtils.isEmpty(boundWorkstations)) {
                // 订单未被任何工作站绑定 ✅
                return false;
            }

            // 检查是否被当前工作站以外的其他工作站绑定
            for (WorkstationExtEntity ws : boundWorkstations) {
                if (!currentWorkstationCode.equals(ws.getWorkstationCode())) {
                    // 订单已被其他工作站占用 ❌
                    log.debug("{}⚠ 订单 [{}] 已被工作站 [{}] 占用", LOG_PREFIX, orderId, ws.getWorkstationCode());
                    return true;
                }
            }

            // 订单只被当前工作站绑定（幂等情况，允许）✅
            return false;

        } catch (Exception e) {
            log.error("{}❌ 检查订单绑定异常 | order={} | error:{}", LOG_PREFIX, orderId, e.getMessage(), e);
            return true; // 保守处理：异常时视为已被占用
        }
    }



    // ─────────────────────────────────────────────────────────────
    // 🎯 核心拣选逻辑
    // ─────────────────────────────────────────────────────────────

    /**
     * 执行拣选出库任务
     *
     * @param workstationCode
     * @param pickItems 拣货项列表
     * @return 是否执行成功
     */
    private boolean executePickTask(String workstationCode, List<PickItem> pickItems, String traceId) {

        if (CollectionUtils.isEmpty(pickItems)) {
            return true;
        }

        try {
            // 1. 构建出库参数

            ContainerTakeOutParam takeOutParam = new ContainerTakeOutParam();
            takeOutParam.setPickArea(workstationCode);

            // 2. 智能分组 & 库存/订单校验过滤
            ArrayListMultimap<String, PickItem> grouped = groupAndFilterPickItems(pickItems, takeOutParam,traceId);
            if (grouped.isEmpty()) {
                log.debug("{}ℹ 工作站 [{}] 所有拣货项均不满足下发条件", LOG_PREFIX, workstationCode);
                return true;  // ✅ 没有可执行的拣货项，返回成功
            }
            // 执行拣选任务前
            businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                    .setContainer(workstationCode)
                    .log("开始执行拣选任务 | 拣货项数量={}", pickItems.size());

            // 3. 执行出库
            executeGroupedPickOut(grouped,traceId);

            log.debug("{}✅ 工作站 [{}] 拣选任务执行完成 | 分组数:{} | 明细数:{}",
                    LOG_PREFIX, workstationCode, grouped.keySet().size(), pickItems.size());
            // 执行完成
            businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                    .setContainer(workstationCode)
                    .log("拣选任务执行完成 | 成功数量={}", grouped.keySet().size());

            return true;

        } catch (Exception e) {
            log.error("{}❌ 工作站 [{}] 执行拣选异常：{}", LOG_PREFIX, workstationCode, e.getMessage(), e);
            // ✅ 关键：不要在这里抛出异常，只记录日志并返回 false
            // 让外层决定是否重试或释放工作站
            return false;
        }
    }

    /**
     * 分组并过滤拣货项（核心业务规则）
     *
     * @param pickItems    拣货项列表
     * @param takeOutParam 出库参数
     * @return 分组后的拣货项
     */
    private ArrayListMultimap<String, PickItem> groupAndFilterPickItems(
            List<PickItem> pickItems, ContainerTakeOutParam takeOutParam, String traceId) {

        ArrayListMultimap<String, PickItem> result = ArrayListMultimap.create();
        if (CollectionUtils.isEmpty(pickItems)) {
            return result;
        }

        // 参数初始化
        ContainerTakeOutParam param = Objects.requireNonNullElse(takeOutParam, new ContainerTakeOutParam());
        String pickArea = StringUtils.defaultString(param.getPickArea(), "");
        String deviceCode = StringUtils.defaultString(param.getDeviceCode(), "");
        String defaultStation = StringUtils.isNotBlank(deviceCode) ? deviceCode : pickArea;

        // 缓存预加载（减少数据库查询）
        Map<UUID, StorageMaterialDto> materialCache = preloadMaterialCache(pickItems);
        Map<UUID, ReqOrderPojo> orderCache = preloadOrderCache(pickItems);

        int skippedCount = 0;
        int allowedCount = 0;

        for (PickItem item : pickItems) {
            if (item == null) continue;

            String skuCode = extractSkuCode(item);
            UUID materialId = extractMaterialId(item);
            String orderNo = Objects.requireNonNullElse(item.gainRelationOrderNo(), "UNKNOWN");

            //TODO SQL直接过滤呢 只有需要拣选的容器走工作站模式，整托的走订单箱出库模式出库
//            boolean hasStock = checkHasAvailableStock(materialId, materialCache);
//            boolean isProcessOrder = checkIsProcessOrder(item, orderCache);
//            boolean allowDispatch = hasStock /*|| isProcessOrder*/;

//            if (!allowDispatch) {
//                skippedCount++;
//
//                // ✅ 审计日志：拣货项决策 - 跳过（核心业务规则）
//                businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
//                        .setContainer(pickArea)
//                        .log("[trace={}] 拣货项跳过 | sku={} orderNo={} hasStock={} isProcess={} decision=SKIP",
//                                traceId, skuCode, orderNo, hasStock, isProcessOrder);
//                continue;
//            }

            allowedCount++;

            // ✅ 审计日志：拣货项决策 - 放行（核心业务规则）
//            businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
//                    .setContainer(pickArea)
//                    .log("[trace={}] 拣货项放行 | sku={} orderNo={} hasStock={} isProcess={} decision=ALLOW qty={}",
//                            traceId, skuCode, orderNo, hasStock, isProcessOrder, item.getPrimaryQty());

            updatePickItemExport(item, pickArea, defaultStation, skuCode);
            String groupKey = buildGroupKey(item);
            result.put(groupKey, item);
        }

        // ✅ 审计日志：批量决策统计
        businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                .setContainer(pickArea)
                .log("[trace={}] 拣货项决策统计 | total={} allowed={} skipped={} area={}",
                        traceId, pickItems.size(), allowedCount, skippedCount, pickArea);

        return result;

    }

    // ─────────────────────────────────────────────────────────────
    // 🔍 辅助方法：缓存与校验
    // ─────────────────────────────────────────────────────────────

    private Map<UUID, StorageMaterialDto> preloadMaterialCache(List<PickItem> items) {
        Set<UUID> ids = items.stream()
                .filter(Objects::nonNull)
                .map(PickItem::getStorageMaterialInfo)
                .filter(Objects::nonNull)
                .map(StorageMaterialInfo::getStorageMaterialId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            return storageMaterialService.listByIds(ids).stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(
                            StorageMaterialDto::getId,
                            Function.identity(),
                            (v1, v2) -> v1,
                            HashMap::new));
        } catch (Exception e) {
            log.error("{}❌ 加载物料缓存异常：{}", LOG_PREFIX, e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    private Map<UUID, ReqOrderPojo> preloadOrderCache(List<PickItem> items) {
        Set<UUID> ids = items.stream()
                .filter(Objects::nonNull)
                .map(PickItem::getRelation_order_id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            return reqOrderRepository.findAllById(ids).stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(
                            ReqOrderPojo::getId,
                            Function.identity(),
                            (v1, v2) -> v1,
                            HashMap::new));
        } catch (Exception e) {
            log.error("{}❌ 加载订单缓存异常：{}", LOG_PREFIX, e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    private String extractSkuCode(PickItem item) {
        return Optional.ofNullable(item.getStorageMaterialInfo())
                .map(StorageMaterialInfo::getSkuInfo)
                .map(SkuInfo::getSkuCode)
                .filter(StringUtils::isNotBlank)
                .orElse(SKU_CODE_UNKNOWN);
    }

    private UUID extractMaterialId(PickItem item) {
        return Optional.ofNullable(item.getStorageMaterialInfo())
                .map(StorageMaterialInfo::getStorageMaterialId)
                .orElse(null);
    }

    //必须是拣选托盘
    private boolean checkHasAvailableStock(UUID materialId, Map<UUID, StorageMaterialDto> cache) {
        if (materialId == null || cache == null) {
            return false;
        }
        StorageMaterialDto material = cache.get(materialId);
        if (material == null) {
            return false;
        }
        Double qty = material.getAvailable_qty();
        // ✅ 处理 null 值
        return qty != null && qty > 0;
    }

    private boolean checkIsProcessOrder(PickItem item, Map<UUID, ReqOrderPojo> cache) {
        if (item == null || cache == null) {
            return false;
        }
        UUID orderId = item.getRelation_order_id();
        if (orderId == null) {
            return false;
        }
        ReqOrderPojo order = cache.get(orderId);
        if (order == null) {
            return false;
        }
        // ✅ 安全获取 remark
        String remark = order.getRemark();
        return ORDER_REMARK_PROCESS.equals(remark);
    }

    private void updatePickItemExport(PickItem item, String area, String station, String skuCode) {
        try {
            boolean areaChanged = !StringUtils.equals(item.getPickArea(), area);
            boolean stationChanged = !StringUtils.equals(item.getPickStation(), station);

            if (areaChanged) {
                item.updatePickArea(area);
            }
            if (stationChanged) {
                item.updatePickStation(station);
            }

            if ((areaChanged || stationChanged) && log.isDebugEnabled()) {
                log.debug("{}🔄 更新出口：物料 [{}] area:{}→{} station:{}→{}",
                        LOG_PREFIX, skuCode,
                        item.getPickArea(), area,
                        item.getPickStation(), station);
            }
        } catch (Exception e) {
            log.error("{}❌ 更新物料 [{}] 出口信息失败：{}", LOG_PREFIX, skuCode, e.getMessage(), e);
        }
    }

    private String buildGroupKey(PickItem item) {
        try {
            String orderNo = Objects.requireNonNullElse(item.gainRelationOrderNo(), "");
            Object orderId = Objects.requireNonNullElse(item.gainRelationOrderId(), "");

            String orderNoStr = StringUtils.isBlank(orderNo) ? "NO_ORDER_NO" : orderNo;
            String orderIdStr = (orderId == null || StringUtils.isBlank(orderId.toString()))
                    ? "NO_ORDER_ID" : orderId.toString();

            return orderNoStr + GROUP_KEY_SEPARATOR + orderIdStr;
        } catch (Exception e) {
            log.error("{}❌ 构建分组 Key 失败", LOG_PREFIX, e);
            return "ERROR_GROUP_" + UUID.randomUUID();
        }
    }

    private ContainerTakeOutParam buildTakeOutParam(String pickArea) {
        ContainerTakeOutParam param = new ContainerTakeOutParam();
        param.setPickArea(pickArea);
        return param;
    }

    // ─────────────────────────────────────────────────────────────
    // 🚀 执行出库
    // ─────────────────────────────────────────────────────────────


    private void executeGroupedPickOut(ArrayListMultimap<String, PickItem> groupedItems, String traceId) {
        if (groupedItems.isEmpty()) return;

        for (String groupKey : groupedItems.keySet()) {
            List<PickItem> taskList = groupedItems.get(groupKey);
            if (CollectionUtils.isEmpty(taskList)) continue;

            try {
                PickItem sample = taskList.get(0);
                String skuCode = extractSkuCode(sample);
                String orderNo = Objects.requireNonNullElse(sample.gainRelationOrderNo(), "UNKNOWN");

                PickingContainerOutServer server = pickOutServerManage.getPickOutServer(sample);

                if (server == null) {
                    log.error("{}❌ [trace={}] 未找到出库处理器 | groupKey={} sku={}",
                            LOG_PREFIX, traceId, groupKey, skuCode);

                    // ✅ 审计日志：出库执行 - 处理器缺失
                    businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                            .setContainer(null)
                            .log("[trace={}] 出库执行失败 | groupKey={} sku={} orderNo={} reason=SERVER_NOT_FOUND",
                                    traceId, groupKey, skuCode, orderNo);
                    continue;
                }

                long startTime = System.currentTimeMillis();
                server.doTakeOut(taskList, PlatFormConfig.create_outbound);
                long duration = System.currentTimeMillis() - startTime;

                log.debug("{}✅ [trace={}] 出库成功 | groupKey={} count={} duration={}ms",
                        LOG_PREFIX, traceId, groupKey, taskList.size(), duration);

                // ✅ 审计日志：出库执行 - 成功
                businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                        .setContainer(null)
                        .log("[trace={}] 出库执行成功 | groupKey={} sku={} orderNo={} itemCount={} duration={}ms",
                                traceId, groupKey, skuCode, orderNo, taskList.size(), duration);

            } catch (Exception e) {
                log.error("{}❌ [trace={}] 出库失败 | groupKey={} error={}",
                        LOG_PREFIX, traceId, groupKey, e.getMessage(), e);

                // ✅ 审计日志：出库执行 - 异常
                businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                        .setContainer(null)
                        .log("[trace={}] 出库执行异常 | groupKey={} error={} rootCause={}",
                                traceId, groupKey, e.getMessage(), ExceptionUtils.getRootCauseMessage(e));
            }
        }
    }
}