package cn.zdjc.wms.project.scheduler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.zdjc.platform.system.manage.log.lucene.BusinessDomain;
import cn.zdjc.platform.system.manage.log.lucene.BusinessLogger;
import cn.zdjc.platform.system.manage.log.lucene.BusinessLoggerFactory;
import cn.zdjc.platform.system.manage.scheduler.AbstractScheduler;
import cn.zdjc.warehouse.inventory.infrastructure.enums.FlagStatus;
import cn.zdjc.wms.PlatFormConfig;
import cn.zdjc.wms.definition.application.dto.CreateTransferDto;
import cn.zdjc.wms.definition.application.service.TransferAppService;
import cn.zdjc.wms.definition.domain.dto.TransferDto;
import cn.zdjc.wms.definition.infrastructure.enums.OutType;
import cn.zdjc.wms.outbound.pick.application.params.ContainerTakeOutParam;
import cn.zdjc.wms.outbound.requisition.domain.server.ReqTakeOutServer;
import cn.zdjc.wms.project.domain.entity.pick.PickItemExtEntity;
import com.foeris.y.fairy.jdbc.connection.DatabaseExecuter;
import com.foreris.eris.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 订单箱容器出库调度器
 * <p>
 * 功能：
 * 1. 定时扫描处于'有货'状态的立方体库位，将容器移库至默认拣货区（正常库存出库）
 * 2. 支持需求单整托的出库（可用数量为0的出库）
 * 注意：需确保数据库层面或业务层面有防重机制，避免重复创建移库单。
 * </p>
 *
 * @author WMS Team
 * @date 2023-10-27
 */
@Slf4j
@Component
public class OrderContainerStockOutScheduler extends AbstractScheduler {

    private static final String SCHEDULER_CODE = "OrderContainerStockOutScheduler";
    private static final String SCHEDULER_NAME = "订单箱容器出库-项目定制";
    private static final String LOG_PREFIX = "[OrderContainerStockOutScheduler] ";

    // 业务审计日志
    private final BusinessLogger businessLogger = BusinessLoggerFactory.getBusinessLogger(BusinessDomain.default_business.name());

    // 单次调度最大处理条数，防止数据量过大影响性能
    private static final int BATCH_LIMIT = 500;

    // 日志采样率（百分比）- 用于控制高频日志输出量
    private static final int LOG_SAMPLE_RATE = 10;

    // 分布式锁过期时间（分钟）
    private static final int LOCK_EXPIRE_MINUTES = 2;

    // SQL 常量定义 - 优化版（添加ORDER BY保证稳定性 + 条件下推）
    private static final String NORMAL_STOCK_SQL =
            "SELECT " +
                    "  sl.house_code AS houseCode, " +
                    "  sl.loc_no AS locationCode, " +
                    "  cm.container_code AS containerCode " +
                    "FROM " +
                    "  wms_storage_location sl " +
                    "  INNER JOIN wms_storage_location_inventory sli " +
                    "    ON sl.id = sli.location_id " +
                    "  INNER JOIN wms_container_material cm " +
                    "    ON sli.id = cm.storage_location_inventory_id AND cm.container_code IS NOT NULL " +
                    "  INNER JOIN wms_storage_material sm " +
                    "    ON cm.storage_material_id = sm.id AND sm.available_qty > 0 AND sm.remark IS NOT NULL " +
                    "  INNER JOIN wms_requisition_order oo " +
                    "    ON oo.form_no = sm.remark AND oo.remark = 'PROCESS' " +
                    "WHERE " +
                    "  sl.loc_type = 'cubic' " +
                    "  AND sl.storage_status = 'haveGoods' " +
                    "ORDER BY sl.id, cm.id " +
                    "LIMIT 500";

    private static final String FULL_PALLET_SQL =
            "SELECT " +
                    "  item.* " +
                    "FROM " +
                    "  wms_pick_item item " +
                    "  INNER JOIN wms_requisition_order ord " +
                    "    ON item.relation_order_id = ord.id " +
                    "    AND ord.form_status IN ('Created', 'Executing', 'Picked', 'CreatedAndExecuting') " +
                    "    AND ord.remark = 'PROCESS' " +
                    "  INNER JOIN wms_storage_material sm " +
                    "    ON item.storage_material_id = sm.id AND sm.available_qty = 0 " +
                    "  INNER JOIN wms_container_material wcm " +
                    "    ON sm.id = wcm.storage_material_id AND wcm.container_code IS NOT NULL " +
                    "  INNER JOIN wms_storage_location_inventory wsli " +
                    "    ON wsli.container_code = wcm.container_code " +
                    "  INNER JOIN wms_storage_location wsl " +
                    "    ON wsl.id = wsli.location_id " +
                    "    AND wsl.loc_type = 'cubic' " +
                    "    AND wsl.storage_status = 'haveGoods' " +
                    "WHERE " +
                    "  item.pick_status IN ('create', 'picking', 'executing') " +
                    "ORDER BY item.id " +
                    "LIMIT 500";

    @Resource
    private TransferAppService transferAppService;

    @Resource
    private ReqTakeOutServer reqTakeOutServer;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        long startTime = System.currentTimeMillis();

        // 尝试获取分布式锁
        String lockKey = "lock:scheduler:" + SCHEDULER_CODE;
//        Boolean locked = redisTemplate.opsForValue()
//                .setIfAbsent(lockKey, UUID.randomUUID().toString(), LOCK_EXPIRE_MINUTES, TimeUnit.MINUTES);

//        if (Boolean.FALSE.equals(locked)) {
//            log.info("{}获取分布式锁失败，可能已有其他实例在运行，跳过本次执行", LOG_PREFIX);
//            return;
//        }

        try {
            log.info("========== 任务开始：{} [{}] ==========", SCHEDULER_NAME, SCHEDULER_CODE);

            // ✅ 增强：任务开始业务日志
            businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                    .log("调度任务启动 | 批次限制:[{}] | 锁超时:[{}]分钟 | 采样率:[{}]%",
                            BATCH_LIMIT, LOCK_EXPIRE_MINUTES, LOG_SAMPLE_RATE);

            // 1. 处理正常库存出库（创建移库单）
            int transferResult = processNormalStockOut();

            // 2. 处理需求单整托出库（可用数量为0）
            int pickResult = processFullPalletStockOut();

            long costTime = System.currentTimeMillis() - startTime;

            // ✅ 增强：任务结束汇总业务日志
            businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                    .log("调度任务完成 | 移库处理:[{}]条 | 整托出库:[{}]条 | 总耗时:[{}]ms",
                            transferResult, pickResult, costTime);

            log.info("========== 任务结束：{} [{}] | 移库处理：{} | 整托出库：{} | 耗时：{} ms ==========",
                    SCHEDULER_NAME, SCHEDULER_CODE, transferResult, pickResult, costTime);

        } catch (Exception e) {
            // ✅ 增强：全局异常业务日志
            businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                    .log("调度任务执行异常 | 异常类型:[{}] | 消息:[{}]",
                            e.getClass().getSimpleName(), e.getMessage());

            log.error("调度任务执行过程中发生严重异常", e);
            throw new JobExecutionException("Scheduler execution failed", e);
        } finally {
            // 释放分布式锁（使用Lua脚本保证原子性）
            releaseLock(lockKey);

            // ✅ 增强：锁释放日志（采样）
            if (shouldLogSample()) {
                businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                        .log("分布式锁释放 | 锁Key:[{}]", lockKey);
            }
        }
    }

    /**
     * 处理正常库存出库（创建移库单）
     * @return 处理的记录数
     */
    private int processNormalStockOut() {
        long startTime = System.currentTimeMillis();
        log.info("{}开始处理正常库存出库...", LOG_PREFIX);

        int total = 0;
        int successCount = 0;
        int failCount = 0;

        try {
            // 1. 获取待处理库位列表（正常库存，available_qty > 0）
            List<CreateTransferDto> createTransferDtoList = getNormalStockLocationList();
            total = CollUtil.size(createTransferDtoList);

            if (CollUtil.isEmpty(createTransferDtoList)) {
                log.info("{}正常库存出库：未查询到待处理数据。耗时：{} ms",
                        LOG_PREFIX, (System.currentTimeMillis() - startTime));
                return 0;
            }

            log.info("{}正常库存出库：查询到待处理数据 {} 条", LOG_PREFIX, total);

            // ✅ 增强：采样记录查询明细（避免日志爆炸）
            if (shouldLogSample()) {
                String sampleInfo = createTransferDtoList.stream()
                        .limit(5)
                        .map(dto -> String.format("库位[%s]→容器[%s]",
                                dto.getLocationCode(), dto.getContainerCode()))
                        .collect(Collectors.joining(", "));
                businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                        .log("正常库存出库-待处理数据采样(前5条) | 总数:[{}] | 明细:{}",
                                total, sampleInfo + (total > 5 ? "..." : ""));
            }

            // 2. 获取目标库位配置（提前校验）
            String defaultPickArea = getDefaultPickArea();

            // 3. 批量处理 - 按目标库位分组
            Map<String, List<CreateTransferDto>> groupedByTarget = createTransferDtoList.stream()
                    .filter(dto -> dto != null && StrUtil.isNotBlank(dto.getLocationCode()))
                    .collect(Collectors.groupingBy(dto -> defaultPickArea));

            // 4. 分批处理
            for (Map.Entry<String, List<CreateTransferDto>> entry : groupedByTarget.entrySet()) {
                List<CreateTransferDto> batch = entry.getValue();
                String targetPos = entry.getKey();

                try {
                    // 准备批量数据
                    List<CreateTransferDto> preparedBatch = batch.stream().peek(dto -> {
                        dto.setOutType(OutType.Out);
                        dto.setToPos(targetPos);
                        dto.setIsDel("true");
                    }).collect(Collectors.toList());

                    // 批量创建移库单
                    List<TransferDto> results = createTransferBatch(preparedBatch);

                    // 记录成功
                    successCount += results.size();

                    // ✅ 增强：每条成功记录业务日志（采样控制）
                    for (TransferDto result : results) {
                        if (shouldLogSample()) {
                            businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                                    .setContainer(result.getContainer_code())
                                    .log("正常库存出库 - 移库单创建成功 | 源库位:[{}] → 目标库位:[{}] | 容器:[{}] | 移库单号:[{}]",
                                            result.getFrom_pos() != null ? result.getFrom_pos() : "N/A",
                                            targetPos,
                                            result.getContainer_code(),
                                            result.getTransfer_no());
                        }
                    }

                    // ✅ 增强：批量结果汇总日志（采样）
                    if (shouldLogSample() && CollUtil.isNotEmpty(results)) {
                        String sampleNos = results.stream()
                                .map(TransferDto::getTransfer_no)
                                .limit(3)
                                .collect(Collectors.joining(", "));
                        businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                                .log("批量创建移库单成功 | 目标库位:[{}] | 成功数:[{}] | 示例单号:[{}]",
                                        targetPos, results.size(),
                                        results.size() > 3 ? sampleNos + "..." : sampleNos);
                    }

                    // 记录业务日志（异步/批量）
                    logBatchTransferSuccess(results, targetPos);

                } catch (Exception e) {
                    log.warn("{}批量创建失败，降级为单条处理，目标库位: {}",
                            LOG_PREFIX, targetPos, e);

                    // ✅ 增强：记录批量异常降级日志
                    businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                            .log("批量创建移库单异常，降级单条处理 | 目标库位:[{}] | 批次大小:[{}] | 异常:{}",
                                    targetPos, batch.size(), e.getMessage());

                    // 降级：单条重试
                    int singleSuccess = fallbackProcessSingle(batch, targetPos);
                    successCount += singleSuccess;
                    failCount += batch.size() - singleSuccess;
                }
            }

        } catch (Exception e) {
            log.error("{}处理正常库存出库过程中发生异常", LOG_PREFIX, e);
            // ✅ 增强：记录处理过程异常
            businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                    .log("正常库存出库处理过程异常 | 异常类型:[{}] | 消息:[{}]",
                            e.getClass().getSimpleName(), e.getMessage());
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            log.info("{}正常库存出库处理完成 | 总数：{} | 成功：{} | 失败：{} | 耗时：{} ms",
                    LOG_PREFIX, total, successCount, failCount, costTime);
        }

        return successCount;
    }

    /**
     * 处理需求单整托出库（可用数量为0）
     * @return 处理的记录数
     */
    private int processFullPalletStockOut() {
        long startTime = System.currentTimeMillis();
        log.info("{}开始处理需求单整托出库...", LOG_PREFIX);

        int total = 0;
        int successCount = 0;
        int failCount = 0;

        try {
            // 1. 获取待处理的整托出库拣货项
            List<PickItemExtEntity> pickItemExtEntityList = getFullPalletPickItems();
            total = CollUtil.size(pickItemExtEntityList);

            if (CollUtil.isEmpty(pickItemExtEntityList)) {
                log.info("{}需求单整托出库：未查询到待处理数据。耗时：{} ms",
                        LOG_PREFIX, (System.currentTimeMillis() - startTime));
                return 0;
            }

            log.info("{}需求单整托出库：查询到待处理数据 {} 条", LOG_PREFIX, total);

            // ✅ 增强：采样记录查询明细
            if (shouldLogSample()) {
                String sampleInfo = pickItemExtEntityList.stream()
                        .limit(5)
                        .map(item -> String.format("拣货项ID[%d]→容器[%s]→需求单[%s]",
                                item.getId(), item.getContainer_code(), item.getRelation_order_id()))
                        .collect(Collectors.joining(", "));
                businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                        .log("整托出库-待处理数据采样(前5条) | 总数:[{}] | 明细:{}",
                                total, sampleInfo + (total > 5 ? "..." : ""));
            }

            // 2. 批量处理
            for (PickItemExtEntity pickItem : pickItemExtEntityList) {
                if (pickItem == null) {
                    log.warn("{}跳过无效拣货项", LOG_PREFIX);
                    continue;
                }
                pickItem.setPick_station(PlatFormConfig.DefaultPickArea);

                try {
                    // 参数校验
                    if (StrUtil.isBlank(pickItem.getContainer_code()) ||
                            pickItem.getId() == null) {
                        // ✅ 增强：参数校验失败日志（必记）
                        businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                                .setContainer(pickItem.getContainer_code())
                                .log("整托出库 - 参数校验失败 | 拣货项ID:[{}] | 容器:[{}] | 原因:参数不全",
                                        pickItem.getId(), pickItem.getContainer_code());

                        log.warn("{}跳过参数不全的拣货项 ID: {}",
                                LOG_PREFIX, pickItem.getId());
                        failCount++;
                        continue;
                    }

                    // 构建参数
                    ContainerTakeOutParam param = new ContainerTakeOutParam();
                    param.setPickItemId(pickItem.getId());
                    param.setPickArea(pickItem.getPick_station());
                    param.setRelationOrderId(pickItem.getRelation_order_id());
                    param.setRelationItemId(pickItem.getRelation_item_id());
                    param.setDeviceCode(StrUtil.isNotBlank(pickItem.getPick_station())
                            ? pickItem.getPick_station() : "DEFAULT_DEVICE");
                    param.setContainerCode(pickItem.getContainer_code());

                    // ✅ 增强：出库请求前日志（采样）
                    if (shouldLogSample()) {
                        businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                                .setContainer(pickItem.getContainer_code())
                                .log("整托出库 - 执行出库请求 | 拣货项ID:[{}] | 容器:[{}] | 需求单:[{}] | 目标拣货区:[{}]",
                                        pickItem.getId(), pickItem.getContainer_code(),
                                        pickItem.getRelation_order_id(), pickItem.getPick_station());
                    }

                    // 执行出库
                    reqTakeOutServer.doTakeOut(param);
                    successCount++;

                    // ✅ 增强：出库成功日志（必记）
                    businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                            .setContainer(pickItem.getContainer_code())
                            .log("整托出库 - 执行成功 | 拣货项ID:[{}] | 容器:[{}] | 需求单:[{}] → 拣货区:[{}]",
                                    pickItem.getId(), pickItem.getContainer_code(),
                                    pickItem.getRelation_order_id(), pickItem.getPick_station());

                } catch (Exception e) {
                    failCount++;
                    // ✅ 增强：出库异常日志（必记）
                    businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                            .setContainer(pickItem.getContainer_code())
                            .log("整托出库 - 执行失败 | 拣货项ID:[{}] | 容器:[{}] | 需求单:[{}] | 异常:{}",
                                    pickItem.getId(), pickItem.getContainer_code(),
                                    pickItem.getRelation_order_id(), e.getMessage());

                    log.error("{}拣货项出库异常，拣货项ID：{}, 容器：{}",
                            LOG_PREFIX, pickItem.getId(), pickItem.getContainer_code(), e);
                }
            }

        } catch (Exception e) {
            log.error("{}处理需求单整托出库过程中发生异常", LOG_PREFIX, e);
            // ✅ 增强：记录处理过程异常
            businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                    .log("整托出库处理过程异常 | 异常类型:[{}] | 消息:[{}]",
                            e.getClass().getSimpleName(), e.getMessage());
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            log.info("{}需求单整托出库处理完成 | 总数：{} | 成功：{} | 失败：{} | 耗时：{} ms",
                    LOG_PREFIX, total, successCount, failCount, costTime);
        }

        return successCount;
    }

    /**
     * 查询正常库存的待移库库位及容器信息（available_qty > 0）
     * @return 待处理数据传输对象列表
     */
    private List<CreateTransferDto> getNormalStockLocationList() {
        try {
            return DatabaseExecuter.queryBeanList(NORMAL_STOCK_SQL,
                    CreateTransferDto.class);
        } catch (Exception e) {
            log.error("{}查询正常库存待移库库位列表失败", LOG_PREFIX, e);
            // ✅ 增强：记录查询异常
            businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                    .log("查询正常库存待移库库位失败 | SQL:[NORMAL_STOCK_SQL] | 异常:{}",
                            e.getMessage());
            return CollUtil.newArrayList();
        }
    }

    /**
     * 查询需求单整托出库的拣货项（available_qty = 0）
     * @return 待处理的拣货项列表
     */
    private List<PickItemExtEntity> getFullPalletPickItems() {
        try {
            return DatabaseExecuter.queryBeanList(FULL_PALLET_SQL,
                    PickItemExtEntity.class);
        } catch (Exception e) {
            log.error("{}查询需求单整托出库拣货项失败", LOG_PREFIX, e);
            // ✅ 增强：记录查询异常
            businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                    .log("查询需求单整托出库拣货项失败 | SQL:[FULL_PALLET_SQL] | 异常:{}",
                            e.getMessage());
            return CollUtil.newArrayList();
        }
    }

    /**
     * 批量创建移库单
     */
    private List<TransferDto> createTransferBatch(List<CreateTransferDto> batch) {
        // TODO: 如果TransferAppService支持批量接口，优先使用
        // return transferAppService.createTransferBatch(batch);

        // 临时实现：循环调用（后续优化为真正批量）
        return batch.stream()
                .map(dto -> {
                    try {
                        TransferDto result = transferAppService.createTransfer(dto);

                        // ✅ 增强：单条创建结果日志（采样）
                        if (shouldLogSample()) {
                            if (result != null && StrUtil.isNotBlank(result.getTransfer_no())) {
                                businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                                        .setContainer(dto.getContainerCode())
                                        .log("批量创建移库单 - 单条成功 | 库位:[{}] → [{}] | 容器:[{}] | 单号:[{}]",
                                                dto.getLocationCode(), dto.getToPos(),
                                                dto.getContainerCode(), result.getTransfer_no());
                            } else {
                                businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                                        .setContainer(dto.getContainerCode())
                                        .log("批量创建移库单 - 单条返回空 | 库位:[{}] → [{}] | 容器:[{}]",
                                                dto.getLocationCode(), dto.getToPos(),
                                                dto.getContainerCode());
                            }
                        }
                        return result;

                    } catch (Exception e) {
                        // ✅ 增强：创建异常日志（采样+关键信息）
                        businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                                .setContainer(dto.getContainerCode())
                                .log("批量创建移库单 - 单条异常 | 库位:[{}] → [{}] | 容器:[{}] | 异常:{}",
                                        dto.getLocationCode(), dto.getToPos(),
                                        dto.getContainerCode(), e.getMessage());

                        log.error("{}单条创建移库单失败，库位: {}, 容器: {}",
                                LOG_PREFIX, dto.getLocationCode(), dto.getContainerCode(), e);
                        return null;
                    }
                })
                .filter(dto -> dto != null && StrUtil.isNotBlank(dto.getTransfer_no()))
                .collect(Collectors.toList());
    }

    /**
     * 降级处理：单条重试
     */
    private int fallbackProcessSingle(List<CreateTransferDto> batch, String targetPos) {
        int success = 0;
        for (CreateTransferDto dto : batch) {
            try {
                dto.setOutType(OutType.Out);
                dto.setToPos(targetPos);

                TransferDto result = transferAppService.createTransfer(dto);

                if (result != null && StrUtil.isNotBlank(result.getTransfer_no())) {
                    success++;

                    // ✅ 增强：单条成功日志（必记，不采样）
                    businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                            .setContainer(dto.getContainerCode())
                            .log("正常库存出库[降级单条] - 移库单创建成功 | 源库位:[{}] → 目标库位:[{}] | 容器:[{}] | 移库单号:[{}]",
                                    dto.getLocationCode(), targetPos,
                                    dto.getContainerCode(), result.getTransfer_no());

                } else {
                    // ✅ 增强：创建成功但返回异常
                    businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                            .setContainer(dto.getContainerCode())
                            .log("正常库存出库[降级单条] - 移库单创建返回空 | 源库位:[{}] → 目标库位:[{}] | 容器:[{}]",
                                    dto.getLocationCode(), targetPos, dto.getContainerCode());
                }

            } catch (Exception e) {
                // ✅ 增强：单条失败日志（必记）
                businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                        .setContainer(dto.getContainerCode())
                        .log("正常库存出库[降级单条] - 移库单创建失败 | 源库位:[{}] → 目标库位:[{}] | 容器:[{}] | 异常:{}",
                                dto.getLocationCode(), targetPos,
                                dto.getContainerCode(), e.getMessage());

                log.error("{}降级单条创建失败，库位: {}, 容器: {}",
                        LOG_PREFIX, dto.getLocationCode(), dto.getContainerCode(), e);
            }
        }
        return success;
    }

    /**
     * 批量记录移库成功日志
     */
    private void logBatchTransferSuccess(List<TransferDto> results, String targetPos) {
        if (CollUtil.isEmpty(results)) return;

        // ✅ 增强：合并相同目标库位的日志，减少条目数
        String summary = results.stream()
                .map(TransferDto::getTransfer_no)
                .limit(3)
                .collect(Collectors.joining(", "));

        businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                .log("批量移库创建成功 | 目标库位:[{}] | 总数:[{}] | 示例单号:[{}]",
                        targetPos, results.size(),
                        results.size() > 3 ? summary + "..." : summary);
    }

    /**
     * 获取默认拣货区配置
     */
    private String getDefaultPickArea() {
        String toPos = PlatFormConfig.DefaultPickArea;
        if (StrUtil.isBlank(toPos)) {
            // ✅ 增强：记录配置错误
            businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                    .log("配置错误: DefaultPickArea 未配置，请检查 application.yml");
            throw new BusinessException("配置错误: DefaultPickArea 未配置，请检查 application.yml");
        }
        return toPos.trim();
    }

    /**
     * 是否记录采样日志
     */
    private boolean shouldLogSample() {
        return (int)(Math.random() * 100) < LOG_SAMPLE_RATE;
    }

    /**
     * 释放分布式锁
     */
    private void releaseLock(String lockKey) {
        try {
            String script =
                    "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                            "   return redis.call('del', KEYS[1]) " +
                            "else " +
                            "   return 0 " +
                            "end";

            // 获取锁值
            String lockValue = redisTemplate.opsForValue().get(lockKey);
            if (lockValue == null) {
                log.debug("{}锁已过期，无需释放: {}", LOG_PREFIX, lockKey);
                return;
            }

            // 执行 Lua 脚本
            RedisScript<Long> redisScript = RedisScript.of(script, Long.class);
            Long result = redisTemplate.execute(
                    redisScript,
                    Collections.singletonList(lockKey),
                    lockValue
            );

            if (result != null && result == 1L) {
                log.info("{}成功释放分布式锁", LOG_PREFIX);
                // ✅ 增强：记录锁释放成功
                if (shouldLogSample()) {
                    businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                            .log("分布式锁成功释放 | 锁Key:[{}]", lockKey);
                }
            } else {
                log.warn("{}释放锁失败或锁已被其他线程持有", LOG_PREFIX);
                // ✅ 增强：记录锁释放失败
                businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                        .log("分布式锁释放失败或已被抢占 | 锁Key:[{}] | 返回值:[{}]", lockKey, result);
            }

        } catch (Exception e) {
            log.error("{}释放分布式锁异常: {}", LOG_PREFIX, lockKey, e);
            // ✅ 增强：记录锁释放异常
            businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
                    .log("释放分布式锁异常 | 锁Key:[{}] | 异常:{}", lockKey, e.getMessage());
        }
    }

    @Override
    public String getCode() {
        return SCHEDULER_CODE;
    }

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }
}