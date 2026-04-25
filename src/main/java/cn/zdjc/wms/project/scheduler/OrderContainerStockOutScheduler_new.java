//package cn.zdjc.wms.project.scheduler;
//
//import cn.hutool.core.collection.CollUtil;
//import cn.zdjc.platform.system.manage.log.lucene.BusinessDomain;
//import cn.zdjc.platform.system.manage.log.lucene.BusinessLogger;
//import cn.zdjc.platform.system.manage.log.lucene.BusinessLoggerFactory;
//import cn.zdjc.platform.system.manage.scheduler.AbstractScheduler;
//import cn.zdjc.wms.PlatFormConfig;
//import cn.zdjc.wms.definition.application.dto.CreateTransferDto;
//import cn.zdjc.wms.definition.application.service.TransferAppService;
//import cn.zdjc.wms.definition.domain.dto.TransferDto;
//import cn.zdjc.wms.definition.infrastructure.enums.OutType;
//import cn.zdjc.wms.outbound.pick.application.params.ContainerTakeOutParam;
//import cn.zdjc.wms.outbound.pick.domain.entity.PickItem;
//import cn.zdjc.wms.outbound.pick.domain.server.PickOutServerManage;
//import cn.zdjc.wms.outbound.pick.domain.server.PickingContainerOutServer;
//import cn.zdjc.wms.project.domain.entity.pick.PickItemExtEntity;
//import com.foeris.y.fairy.jdbc.connection.DatabaseExecuter;
//import lombok.extern.slf4j.Slf4j;
//import org.quartz.JobExecutionContext;
//import org.quartz.JobExecutionException;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.util.List;
//
///**
// * 订单箱容器出库调度器
// * <p>
// * 功能：
// * 1. 定时扫描处于'有货'状态的立方体库位，将容器移库至默认拣货区（正常库存出库）
// * 2. 支持需求单整托的出库（可用数量为0的出库）
// * 注意：需确保数据库层面或业务层面有防重机制，避免重复创建移库单。
// * </p>
// *
// * @author WMS Team
// * @date 2023-10-27
// */
//@Slf4j
//@Component
//public class OrderContainerStockOutScheduler_new extends AbstractScheduler {
//
//    private static final String SCHEDULER_CODE = "OrderContainerStockOutScheduler";
//    private static final String SCHEDULER_NAME = "订单箱容器出库-项目定制";
//    private static final String LOG_PREFIX = "[OrderContainerStockOutScheduler] ";
//
//    // 业务审计日志
//    private final BusinessLogger businessLogger = BusinessLoggerFactory.getBusinessLogger(BusinessDomain.default_business.name());
//
//    // 单次调度最大处理条数，防止数据量过大影响性能
//    private static final int BATCH_LIMIT = 500;
//
//    // SQL 常量定义
//    private static final String LOC_TYPE_CUBIC = "cubic";
//    private static final String STORAGE_STATUS_HAVE_GOODS = "haveGoods";
//    private static final String FLAG_TRUE = "true";
//
//    @Resource
//    private TransferAppService transferAppService;
//
//    @Resource
//    private PickOutServerManage pickOutServerManage;
//
//    @Override
//    public void execute(JobExecutionContext context) throws JobExecutionException {
//        long startTime = System.currentTimeMillis();
//        log.info("========== 任务开始：{} [{}] ==========", SCHEDULER_NAME, SCHEDULER_CODE);
//
//        try {
//            // 1. 处理正常库存出库（创建移库单）
//            int transferResult = processNormalStockOut();
//
//            // 2. 处理需求单整托出库（可用数量为0）
//            int pickResult = processFullPalletStockOut();
//
//            long costTime = System.currentTimeMillis() - startTime;
//            log.info("========== 任务结束：{} [{}] | 移库处理：{} | 整托出库：{} | 耗时：{} ms ==========",
//                    SCHEDULER_NAME, SCHEDULER_CODE, transferResult, pickResult, costTime);
//
//        } catch (Exception e) {
//            log.error("调度任务执行过程中发生严重异常", e);
//            throw new JobExecutionException("Scheduler execution failed", e);
//        }
//    }
//
//    /**
//     * 处理正常库存出库（创建移库单）
//     * @return 处理的记录数
//     */
//    private int processNormalStockOut() {
//        long startTime = System.currentTimeMillis();
//        log.info("{}开始处理正常库存出库...", LOG_PREFIX);
//
//        int total = 0;
//        int successCount = 0;
//        int failCount = 0;
//
//        try {
//            // 1. 获取待处理库位列表（正常库存，available_qty > 0）
//            List<CreateTransferDto> createTransferDtoList = getNormalStockLocationList();
//            total = CollUtil.size(createTransferDtoList);
//
//            if (CollUtil.isEmpty(createTransferDtoList)) {
//                log.info("{}正常库存出库：未查询到待处理数据。耗时：{} ms", LOG_PREFIX, (System.currentTimeMillis() - startTime));
//                return 0;
//            }
//
//            log.info("{}正常库存出库：查询到待处理数据 {} 条", LOG_PREFIX, total);
//
//            // 2. 遍历处理
//            for (CreateTransferDto createDto : createTransferDtoList) {
//                // 基础数据校验
//                if (createDto == null || createDto.getLocationCode() == null) {
//                    log.warn("{}跳过无效数据：{}", LOG_PREFIX, createDto);
//                    continue;
//                }
//
//                try {
//                    // 设置移库参数
//                    createDto.setOutType(OutType.Out);
//                    createDto.setIsDel(FLAG_TRUE);
//
//                    // 目标库位配置
//                    String toPos = PlatFormConfig.DefaultPickArea;
//                    if (toPos == null || toPos.trim().isEmpty()) {
//                        log.error("{}配置错误：DefaultPickArea 未配置，跳过库位 {}", LOG_PREFIX, createDto.getLocationCode());
//                        failCount++;
//                        continue;
//                    }
//                    createDto.setToPos(toPos);
//
//                    // 调用服务创建移库单
//                    TransferDto transferDto = transferAppService.createTransfer(createDto);
//
//                    // 记录业务成功日志
//                    if (transferDto != null && transferDto.getTransfer_no() != null) {
//                        businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
//                                .setContainer(createDto.getContainerCode())
//                                .log("正常库存出库 - 库位 [{}]-->[{}], 移库单 [{}] 创建成功",
//                                        createDto.getLocationCode(), toPos, transferDto.getTransfer_no());
//                        successCount++;
//                    } else {
//                        // 服务未抛异常但返回空，视为失败
//                        log.warn("{}移库单创建返回为空，库位：{}", LOG_PREFIX, createDto.getLocationCode());
//                        failCount++;
//                    }
//
//                } catch (Exception e) {
//                    failCount++;
//                    // 记录业务失败日志及系统异常堆栈
//                    businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
//                            .setContainer(createDto.getContainerCode())
//                            .log("正常库存出库 - 库位 [{}]-->[{}], 移库单创建失败，原因：{}",
//                                    createDto.getLocationCode(), createDto.getToPos(), e.getMessage());
//                    log.error("{}移库单创建异常，库位：{}, 容器：{}",
//                            LOG_PREFIX, createDto.getLocationCode(), createDto.getContainerCode(), e);
//                }
//            }
//
//        } catch (Exception e) {
//            log.error("{}处理正常库存出库过程中发生异常", LOG_PREFIX, e);
//        } finally {
//            long costTime = System.currentTimeMillis() - startTime;
//            log.info("{}正常库存出库处理完成 | 总数：{} | 成功：{} | 失败：{} | 耗时：{} ms",
//                    LOG_PREFIX, total, successCount, failCount, costTime);
//        }
//
//        return successCount;
//    }
//
//    /**
//     * 处理需求单整托出库（可用数量为0）
//     * @return 处理的记录数
//     */
//    private int processFullPalletStockOut() {
//        long startTime = System.currentTimeMillis();
//        log.info("{}开始处理需求单整托出库...", LOG_PREFIX);
//
//        int total = 0;
//        int successCount = 0;
//        int failCount = 0;
//
//        try {
//            // 1. 获取待处理的整托出库拣货项
//            List<PickItemExtEntity> pickItems = getFullPalletPickItems();
//            total = CollUtil.size(pickItems);
//
//            if (CollUtil.isEmpty(pickItems)) {
//                log.info("{}需求单整托出库：未查询到待处理数据。耗时：{} ms", LOG_PREFIX, (System.currentTimeMillis() - startTime));
//                return 0;
//            }
//
//            log.info("{}需求单整托出库：查询到待处理数据 {} 条", LOG_PREFIX, total);
//
//            // 2. 遍历处理
//            for (PickItemExtEntity pickItem : pickItems) {
//                if (pickItem == null) {
//                    log.warn("{}跳过无效拣货项", LOG_PREFIX);
//                    continue;
//                }
//
//                try {
//                    // 获取出库处理器
//                    PickingContainerOutServer server = pickOutServerManage.getPickOutServer(pickItem);
//
//                    if (server == null) {
//                        log.error("{}未找到出库处理器，拣货项ID：{}", LOG_PREFIX, pickItem.getItemKey().getOrderKey().getId());
//                        failCount++;
//
//                        // 记录业务失败日志
//                        businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
//                                .setContainer(pickItem.getContainerCode())
//                                .log("需求单整托出库 - 未找到出库处理器，拣货项ID：{}", pickItem.getId());
//                        continue;
//                    }
//
//                    // 构建出库参数
//                    ContainerTakeOutParam takeOutParam = new ContainerTakeOutParam();
//                    takeOutParam.setPickArea(PlatFormConfig.DefaultPickArea);
//
//                    // 执行出库
//                    long itemStartTime = System.currentTimeMillis();
//                    server.doTakeOut(List.of(pickItem), PlatFormConfig.create_outbound);
//                    long itemDuration = System.currentTimeMillis() - itemStartTime;
//
//                    successCount++;
//
//                    // 记录业务成功日志
//                    businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
//                            .setContainer(pickItem.getContainerCode())
//                            .log("需求单整托出库 - 拣货项 [{}] 出库成功，耗时：{}ms",
//                                    pickItem.getId(), itemDuration);
//
//                    log.debug("{}拣货项 [{}] 出库成功，耗时：{}ms", LOG_PREFIX, pickItem.getId(), itemDuration);
//
//                } catch (Exception e) {
//                    failCount++;
//                    // 记录业务失败日志及系统异常堆栈
//                    businessLogger.setResource(SCHEDULER_CODE, SCHEDULER_NAME)
//                            .setContainer(pickItem.getContainerCode())
//                            .log("需求单整托出库 - 拣货项 [{}] 出库失败，原因：{}",
//                                    pickItem.getId(), e.getMessage());
//                    log.error("{}拣货项出库异常，拣货项ID：{}", LOG_PREFIX, pickItem.getId(), e);
//                }
//            }
//
//        } catch (Exception e) {
//            log.error("{}处理需求单整托出库过程中发生异常", LOG_PREFIX, e);
//        } finally {
//            long costTime = System.currentTimeMillis() - startTime;
//            log.info("{}需求单整托出库处理完成 | 总数：{} | 成功：{} | 失败：{} | 耗时：{} ms",
//                    LOG_PREFIX, total, successCount, failCount, costTime);
//        }
//
//        return successCount;
//    }
//
//    /**
//     * 查询正常库存的待移库库位及容器信息（available_qty > 0）
//     * @return 待处理数据传输对象列表
//     */
//    private List<CreateTransferDto> getNormalStockLocationList() {
//        String sql = " SELECT " +
//                "  sl.house_code AS houseCode, " +
//                "  sl.loc_no AS locationCode, " +
//                "  cm.container_code AS containerCode " +
//                "FROM " +
//                "  wms_storage_location sl " +
//                "  JOIN wms_storage_location_inventory sli ON sl.id = sli.location_id " +
//                "  LEFT JOIN wms_container_material cm ON sli.id = cm.storage_location_inventory_id " +
//                "  LEFT JOIN wms_storage_material sm ON cm.storage_material_id = sm.id " +
//                "  LEFT JOIN wms_requisition_order oo ON oo.form_no = sm.remark " +
//                "  LEFT JOIN wms_storage_location_standard sls ON sl.standard_id = sls.id  " +
//                "WHERE " +
//                "  sl.loc_type = 'cubic'  " +
//                "  AND sl.storage_status = 'haveGoods'  " +
//                "  AND sm.available_qty > 0  " +  // 正常库存：可用数量大于0
//                "  AND cm.container_code IS NOT NULL " +
//                "  AND oo.remark = 'PROCESS' " +
//                "LIMIT " + BATCH_LIMIT;
//
//        try {
//            return DatabaseExecuter.queryBeanList(sql, CreateTransferDto.class);
//        } catch (Exception e) {
//            log.error("{}查询正常库存待移库库位列表失败", LOG_PREFIX, e);
//            return CollUtil.newArrayList();
//        }
//    }
//
//    /**
//     * 查询需求单整托出库的拣货项（available_qty = 0）
//     * @return 待处理的拣货项列表
//     */
//    private List<PickItemExtEntity> getFullPalletPickItems() {
//        String sql = " SELECT " +
//                "  item.* " +
//                "FROM " +
//                "  wms_pick_item item " +
//                "  LEFT JOIN wms_storage_material sm ON item.storage_material_id = sm.id " +
//                "  LEFT JOIN wms_container_material wcm ON sm.id = wcm.storage_material_id " +
//                "  LEFT JOIN wms_storage_location_inventory wsli ON wsli.container_code = wcm.container_code " +
//                "  LEFT JOIN wms_storage_location wsl ON wsl.id = wsli.location_id " +
//                "  INNER JOIN wms_requisition_order ord ON item.relation_order_id = ord.id " +
//                "WHERE " +
//                "  ord.form_status IN ('Created', 'Executing', 'Picked', 'CreatedAndExecuting') " +
//                "  AND item.pick_status IN ('create', 'picking', 'executing') " +
//                "  AND wsl.loc_type = 'cubic' " +
//                "  AND wsl.storage_status = 'haveGoods' " +
//                "  AND sm.available_qty = 0 " +  // 整托出库：可用数量为0
//                "  AND wcm.container_code IS NOT NULL " +
//                "  AND ord.remark = 'PROCESS' " +
//                "LIMIT " + BATCH_LIMIT;
//
//        try {
//            return DatabaseExecuter.queryBeanList(sql, PickItemExtEntity.class);
//        } catch (Exception e) {
//            log.error("{}查询需求单整托出库拣货项失败", LOG_PREFIX, e);
//            return CollUtil.newArrayList();
//        }
//    }
//
//    @Override
//    public String getCode() {
//        return SCHEDULER_CODE;
//    }
//
//    @Override
//    public String getName() {
//        return SCHEDULER_NAME;
//    }
//}