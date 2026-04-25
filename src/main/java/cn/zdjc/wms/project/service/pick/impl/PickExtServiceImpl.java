package cn.zdjc.wms.project.service.pick.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.zdjc.bm.transjob.core.DispatchManager;
import cn.zdjc.bm.transjob.core.dispatch.Dispatch;
import cn.zdjc.bm.transjob.core.dispatch.param.DispatchParam;
import cn.zdjc.bm.transjob.domain.dto.DispatchInfoDto;
import cn.zdjc.bm.transjob.domain.dto.DispatchJobDto;
import cn.zdjc.bm.transjob.domain.dto.DoDispatchDto;
import cn.zdjc.bm.transjob.domain.entity.DispatchJob;
import cn.zdjc.bm.transjob.domain.service.DispatchInfoService;
import cn.zdjc.bm.transjob.domain.service.DispatchJobService;
import cn.zdjc.warehouse.WmsConfig;
import cn.zdjc.warehouse.definition.infrastructure.enums.LocationType;
import cn.zdjc.warehouse.inventory.application.service.*;
import cn.zdjc.warehouse.inventory.domain.dto.LockedStorageMaterialDto;
import cn.zdjc.warehouse.inventory.domain.dto.RelieveBySnDto;
import cn.zdjc.warehouse.inventory.domain.dto.StorageLocationInventoryDto;
import cn.zdjc.warehouse.inventory.domain.dto.StorageMaterialDto;
import cn.zdjc.wms.PlatFormConfig;
import cn.zdjc.wms.api.service.impl.PublishJobTaskAppServiceImpl;
import cn.zdjc.wms.common.utils.ArithmeticUtils;
import cn.zdjc.wms.commons.StorageMaterialInfo;
import cn.zdjc.wms.core.callback.TransportCallback;
import cn.zdjc.wms.core.callback.param.TransportCallbackParam;
import cn.zdjc.wms.core.dispatch.TransportDispatch;
import cn.zdjc.wms.core.dispatch.param.TransportDispatchParam;
import cn.zdjc.wms.definition.infrastructure.enums.FormStatus;
import cn.zdjc.wms.outbound.pick.application.params.ContainerTakeOutParam;
import cn.zdjc.wms.outbound.pick.application.service.PickOperationAppService;
import cn.zdjc.wms.outbound.pick.domain.entity.PickItem;
import cn.zdjc.wms.outbound.pick.domain.repository.IPickItemRepositoryService;
import cn.zdjc.wms.outbound.pick.domain.repository.params.PickAblItemQueryParam;
import cn.zdjc.wms.outbound.pick.domain.repository.params.PickItemQueryParam;
import cn.zdjc.wms.outbound.pick.domain.server.PickOutAppService;
import cn.zdjc.wms.outbound.pick.domain.vo.PickItemKey;
import cn.zdjc.wms.outbound.pick.infrastructure.enums.PickStatus;
import cn.zdjc.wms.outbound.pick.infrastructure.persistence.jdbc.repository.PickItemRepository;
import cn.zdjc.wms.outbound.requisition.domain.entity.ReqItem;
import cn.zdjc.wms.outbound.requisition.domain.entity.ReqOrder;
import cn.zdjc.wms.outbound.requisition.domain.repository.IReqDetailRepositoryService;
import cn.zdjc.wms.outbound.requisition.domain.repository.IReqItemRepositoryService;
import cn.zdjc.wms.outbound.requisition.domain.repository.IReqOrderRepositoryService;
import cn.zdjc.wms.outbound.requisition.domain.repository.params.ReqItemQueryParam;
import cn.zdjc.wms.outbound.requisition.domain.share.BusinessInfo;
import cn.zdjc.wms.outbound.requisition.domain.share.SkuInfo;
import cn.zdjc.wms.outbound.requisition.infrastructure.persistence.jdbc.repository.RequisitionReqDetailRepository;
import cn.zdjc.wms.outbound.requisition.infrastructure.persistence.jdbc.repository.RequisitionReqItemRepository;
import cn.zdjc.wms.outbound.requisition.infrastructure.persistence.jdbc.repository.RequisitionReqOrderRepository;
import cn.zdjc.wms.project.ProjectConfig;
import cn.zdjc.wms.project.domain.dto.inbound.BatchPutAwayCombineDto;
import cn.zdjc.wms.project.domain.dto.inbound.PjInventoryReceiptItemDto;
import cn.zdjc.wms.project.domain.dto.pick.OrderContainerLeaveDto;
import cn.zdjc.wms.project.domain.dto.pick.WorkstationCompletePickDto;
import cn.zdjc.wms.project.domain.dto.pick.WorkstationCompletePickItemDto;
import cn.zdjc.wms.project.domain.dto.pick.WorkstationPickInfoDto;
import cn.zdjc.wms.project.domain.dto.requisition.RequisitionItemContainerCodeExtDto;
import cn.zdjc.wms.project.domain.dto.requisition.RequisitionItemContainerCodeListExtDto;
import cn.zdjc.wms.project.domain.dto.requisition.RequisitionItemExtDto;
import cn.zdjc.wms.project.domain.dto.ws.WorkstationExtDto;
import cn.zdjc.wms.project.domain.entity.pick.PickItemExtEntity;
import cn.zdjc.wms.project.domain.vo.pick.PickOrderFinishVo;
import cn.zdjc.wms.project.domain.vo.pick.PickOrderInfoVo;
import cn.zdjc.wms.project.domain.vo.pick.WorkstationPickInfoVo;
import cn.zdjc.wms.project.service.inbound.PjInboundService;
import cn.zdjc.wms.project.service.pick.PickExtService;
import cn.zdjc.wms.project.service.pick.PickItemExtService;
import cn.zdjc.wms.project.service.workstation.WorkstationService;
import cn.zdjc.wms.transport.out.application.service.OutboundAppService;
import cn.zdjc.wms.transport.out.domain.entity.Outbound;
import cn.zdjc.wms.transport.out.infrastructure.enums.OutBoundStatus;
import com.foeris.y.common.bean.BeanUtl;
import com.foeris.y.common.json.JsonUtl;
import com.foeris.y.common.result.PageResult;
import com.foeris.y.common.result.ResultFactory;
import com.foeris.y.common.string.StringFormatUtl;
import com.foreris.eris.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 拣选扩展服务实现类
 *
 * <p>提供拣选业务的核心服务实现，包括：
 * <ul>
 *   <li>工作站拣选信息查询</li>
 *   <li>拣选任务完成处理</li>
 *   <li>容器离开工作站业务处理</li>
 * </ul>
 *
 * @author chensor
 * @version 1.0.0
 * @since 2022-12-12
 */
@Slf4j
@Service
public class PickExtServiceImpl implements PickExtService {

    // ==================== 依赖注入 ====================

    @Resource
    private StorageMaterialAppService storageMaterialAppService;

    @Resource
    private PickOutAppService pickOutAppService;

    @Resource
    private PickItemExtService pickItemExtService;

    @Resource
    private ContainerMaterialAppService containerMaterialAppService;

    @Resource
    private IPickItemRepositoryService iPickItemRepositoryService;

    @Resource
    private OutboundAppService outboundAppService;

    @Resource
    private DecreaseInventoryAppService decreaseInventoryAppService;

    @Resource
    private PickItemRepository pickItemRepository;

    @Resource
    private DispatchJobService dispatchJobService;

    @Resource
    private StorageLocationInventoryAppService storageLocationInventoryAppService;

    @Resource
    private LockedStorageMaterialAppService lockedStorageMaterialAppService;

    @Resource(type = RequisitionReqDetailRepository.class)
    private IReqDetailRepositoryService reqDetailRepositoryService;

    @Resource(type = RequisitionReqOrderRepository.class)
    private IReqOrderRepositoryService iReqOrderRepositoryService;

    @Resource(type = RequisitionReqItemRepository.class)
    private IReqItemRepositoryService iReqItemRepositoryService;


    @Lazy
    @Resource
    private DispatchInfoService dispatchInfoService;
    // ==================== 拣选信息查询 ====================
    @Lazy
    @Resource
    private DispatchManager dispatchManager;
    @Resource
    private PublishJobTaskAppServiceImpl publishJobTaskAppService;

    @Resource
    private PjInboundService inboundService;


    @Resource
    private PickOperationAppService pickOperationAppService;

    @Resource
    private WorkstationService workstationService;
    /**
     * 查询工作站拣选信息
     *
     * <p>根据容器号和工作站信息，查询当前待处理的拣选任务列表。
     *
     * <p><strong>业务流程：</strong>
     * <ol>
     *   <li>根据容器号查询所有拣选项（状态：创建、执行中、拣选中、已完成）</li>
     *   <li>过滤出未完成的拣选项（主数量 > 已拣数量 且 物料ID非空）</li>
     *   <li>转换为前端展示的VO对象</li>
     *   <li>构建响应对象并返回</li>
     * </ol>
     *
     * @param dto 拣选信息查询参数，包含容器号和工作站编号
     * @return 拣选信息视图对象，包含订单列表和容器状态信息
     * @throws BusinessException 当容器不存在或状态异常时抛出
     * @see WorkstationPickInfoDto
     * @see WorkstationPickInfoVo
     */
    @Override
    public WorkstationPickInfoVo findPickInfo(WorkstationPickInfoDto dto) {
        // 记录方法开始时间，用于性能监控
        long startTime = System.currentTimeMillis();

        String containerCode = dto.getContainerCode();
        String workstationCode = dto.getWorkstationCode();

        log.info("【查询拣选信息】开始 | containerCode={} | workstationCode={}",
                containerCode, workstationCode);

        try {
            // ========== 1. 查询符合条件的拣选项 ==========
            List<PickStatus> allowedStatuses = Arrays.asList(
                    PickStatus.create,
                    PickStatus.executing,
                    PickStatus.picking,
                    PickStatus.finished
            );

            log.debug("【查询拣选信息】查询拣选项 | containerCode={} | statuses={}",
                    containerCode, allowedStatuses);

            List<PickItemExtEntity> allPickItems = pickItemExtService.queryByContainerCode(
                    containerCode,
                    allowedStatuses
            );

            // 无拣选项时返回空响应
            if (CollUtil.isEmpty(allPickItems)) {
                log.warn("【查询拣选信息】未找到拣选项 | containerCode={}", containerCode);
                return buildEmptyResponse(workstationCode, containerCode);
            }

            log.info("【查询拣选信息】查询到 {} 个拣选项 | containerCode={}",
                    allPickItems.size(), containerCode);

            // ========== 2. 过滤未完成的拣选项 ==========
            List<PickItemExtEntity> unfinishedPickItems = allPickItems.stream()
                    .filter(this::isUnfinishedPickItem)
                    .collect(Collectors.toList());

            log.debug("【查询拣选信息】过滤后剩余 {} 个未完成拣选项 | containerCode={}",
                    unfinishedPickItems.size(), containerCode);

            // ========== 3. 转换为 VO 列表 ==========
            List<PickOrderInfoVo> pickOrderInfoVoList = convertToPickOrderInfoVoList(unfinishedPickItems);

            // ========== 4. 构建响应 ==========
            WorkstationPickInfoVo response = new WorkstationPickInfoVo();
            response.setWorkstationCode(workstationCode);
            response.setContainerCode(containerCode);
            response.setPickOrderInfoVoList(pickOrderInfoVoList);
            response.setNoPickTrayQty(0);

            long costTime = System.currentTimeMillis() - startTime;
            log.info("【查询拣选信息】成功返回 | containerCode={} | 订单数={} | 耗时={}ms",
                    containerCode, pickOrderInfoVoList.size(), costTime);

            return response;

        } catch (Exception e) {
            log.error("【查询拣选信息】系统异常 | containerCode={} | workstationCode={}",
                    containerCode, workstationCode, e);
            throw new BusinessException("查询拣选信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 判断是否为未完成拣选项
     *
     * <p>判定规则：
     * <ul>
     *   <li>拣选项不为空</li>
     *   <li>物料ID不为空</li>
     *   <li>主数量 > 已拣数量</li>
     * </ul>
     *
     * @param item 拣选项实体
     * @return true-未完成，false-已完成或无效
     */
    private boolean isUnfinishedPickItem(PickItemExtEntity item) {
        if (item == null || item.getStorage_material_id() == null) {
            return false;
        }
        double pickedQty = Optional.ofNullable(item.getPicked_qty()).orElse(0.0);
        boolean unfinished = item.getPrimary_qty() > pickedQty;

        if (!unfinished) {
            log.debug("【拣选过滤】拣选项已完成 | pickItemId={} | primaryQty={} | pickedQty={}",
                    item.getId(), item.getPrimary_qty(), pickedQty);
        }

        return unfinished;
    }

    /**
     * 将拣选项实体列表转换为VO列表
     *
     * @param entities 拣选项实体列表
     * @return 拣选订单信息VO列表
     */
    private List<PickOrderInfoVo> convertToPickOrderInfoVoList(List<PickItemExtEntity> entities) {
        if (CollUtil.isEmpty(entities)) {
            return Collections.emptyList();
        }

        log.debug("【数据转换】开始转换拣选项 | 数量={}", entities.size());

        List<PickOrderInfoVo> voList = entities.stream().map(entity -> {
            PickOrderInfoVo vo = new PickOrderInfoVo();
            vo.setOrderNo(entity.getRelation_order_no());
            vo.setOrderItemNo(entity.getRelation_item_no());
            vo.setPickItemId(String.valueOf(entity.getId()));
            vo.setTotalQty(BigDecimal.valueOf(entity.getPrimary_qty()));
            vo.setQty(BigDecimal.ZERO);
            vo.setPickedQty(BigDecimal.valueOf(entity.getPicked_qty()));
            vo.setUnit(entity.getUnit());
            vo.setSkuCode(entity.getSku_code());
            vo.setSkuName(entity.getSku_name());
            vo.setBatchNo(entity.getBatch_no());
            return vo;
        }).collect(Collectors.toList());

        log.debug("【数据转换】转换完成 | 数量={}", voList.size());

        return voList;
    }

    /**
     * 构建空响应对象
     *
     * @param workstationCode 工作站编号
     * @param containerCode 容器号
     * @return 空的拣选信息响应对象
     */
    private WorkstationPickInfoVo buildEmptyResponse(String workstationCode, String containerCode) {
        WorkstationPickInfoVo vo = new WorkstationPickInfoVo();
        vo.setWorkstationCode(workstationCode);
        vo.setContainerCode(containerCode);
        vo.setPickOrderInfoVoList(Collections.emptyList());
        vo.setNoPickTrayQty(0);
        return vo;
    }

    // ==================== 拣选完成处理 ====================

    /**
     * 完成拣选任务
     *
     * <p><strong>业务流程：</strong>
     * <ol>
     *   <li>参数校验（容器号、拣选列表非空）</li>
     *   <li>校验容器是否存在未完成的调度任务</li>
     *   <li>批量预加载拣选项数据</li>
     *   <li>校验所有拣选项是否存在</li>
     *   <li>逐个处理拣选明细（释放物料、更新进度、尝试完成订单）</li>
     *   <li>返回处理结果</li>
     * </ol>
     *
     * <p><strong>事务说明：</strong>
     * <ul>
     *   <li>整个方法在事务中执行，任一拣选项处理失败将回滚所有变更</li>
     *   <li>禁止在事务内并行处理，确保数据一致性</li>
     * </ul>
     *
     * @param dto 拣选完成参数，包含容器号和拣选明细列表
     * @return 拣选完成结果视图
     * @throws BusinessException 参数校验失败、拣选项不存在、拣选超量等业务异常
     * @throws RuntimeException 系统异常
     * @see WorkstationCompletePickDto
     * @see PickOrderFinishVo
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PickOrderFinishVo completePick(WorkstationCompletePickDto dto) {
        // 记录方法开始时间
        long startTime = System.currentTimeMillis();

        String containerCode = dto.getContainerCode();
        String orderContainerCode = dto.getOrderContainerCode();
        log.info("【完成拣选】开始处理 | containerCode={} | 拣选明细数={}",
                containerCode,
                dto.getPickItemList() != null ? dto.getPickItemList().size() : 0);

        try {
            // ========== 1. 参数校验 ==========
            validateInput(dto);

            // ========== 2. 校验容器是否存在未完成的调度任务 ==========
            validateNoActiveJob(containerCode);

            List<WorkstationCompletePickItemDto> items = dto.getPickItemList();

            // 无拣选明细时返回未完成响应
            if (CollUtil.isEmpty(items)) {
                log.warn("【完成拣选】拣选明细列表为空 | containerCode={}", containerCode);
                return createIncompleteResponse();
            }

            // ========== 3. 批量预加载拣选项数据（性能优化） ==========
            log.debug("【完成拣选】预加载拣选项数据 | 数量={}", items.size());
            Map<String, PickItemExtEntity> pickItemMap = preloadPickItems(items);

            // ========== 4. 校验所有拣选项是否存在 ==========
            validatePickItemsExist(items, pickItemMap);

            // ========== 5. 顺序处理每个拣选明细 ==========
            log.info("【完成拣选】开始处理拣选明细 | 数量={}", items.size());

            for (WorkstationCompletePickItemDto itemDto : items) {
                PickItemExtEntity pickItem = pickItemMap.get(itemDto.getPickItemId());
                UUID outbound_id= pickItem.getOutbound_id();
                Outbound outbound = outboundAppService.findOutboundById(outbound_id);
                if (outbound == null) {
                    log.error("下架拣选单不存在 | outboundId={}, containerCode={}", outbound_id, containerCode);
                    throw new BusinessException(String.format("托盘[%s]未找到其下架拣选单", containerCode));
                }
                OutBoundStatus  outBoundStatus= outbound.getFormStatus();
                if(Arrays.asList(OutBoundStatus.Created,OutBoundStatus.Executing,OutBoundStatus.CreatedAndExecuting).contains(outBoundStatus)){
                    throw new BusinessException(String.format("托盘[%s]下架拣选单未完成", containerCode));
                }

                processSinglePickItem(itemDto, pickItem, containerCode,orderContainerCode);
            }

            // ========== 6. 构建响应 ==========
            PickOrderFinishVo result = createIncompleteResponse();

            long costTime = System.currentTimeMillis() - startTime;
            log.info("【完成拣选】处理成功 | containerCode={} | 处理明细数={} | 耗时={}ms",
                    containerCode, items.size(), costTime);

            BatchPutAwayCombineDto putAwayDto = buildBatchPutAwayDto(dto);
            inboundService.toolingBatchPutAway(putAwayDto);


            return result;

        } catch (BusinessException e) {
            log.error("【完成拣选】业务异常 | containerCode={} | error={}",
                    containerCode, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("【完成拣选】系统异常 | containerCode={}", containerCode, e);
            throw new BusinessException("拣选操作失败: " + e.getMessage(), e);
        }
    }



/**
 * 构建批量上架DTO（防御式编程 + 语义清晰）
 */
        private BatchPutAwayCombineDto buildBatchPutAwayDto(WorkstationCompletePickDto source) {
            BatchPutAwayCombineDto dto = new BatchPutAwayCombineDto();
            dto.setContainerCode(source.getOrderContainerCode());
            dto.setDeviceCode(source.getWorkstationCode());
            dto.setWorkstationCode(source.getWorkstationCode());

            // 防御性初始化，避免NPE
            List<PjInventoryReceiptItemDto> items = Optional.ofNullable(source.getPickItemList())
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(item -> {
                        PjInventoryReceiptItemDto target = new PjInventoryReceiptItemDto();
                        target.setQty(item.getQty());
                        target.setSkuCode(item.getSkuCode());
                        target.setSkuName(item.getSkuName());
                        target.setUnit(item.getUnit());
                        target.setRemark(item.getOrderNo()); // 用订单号作备注
                        return target;
                    })
                    .collect(Collectors.toList());

            dto.setItemList(items);
            return dto;
        }




    /**
     * 处理单个拣选明细
     *
     * <p><strong>处理流程：</strong>
     * <ol>
     *   <li>物料释放/解绑</li>
     *   <li>更新拣选进度</li>
     *   <li>尝试完成关联订单</li>
     * </ol>
     *
     * @param dto 拣选明细参数
     * @param pickItem 拣选项实体
     * @param containerCode 容器号
     * @throws BusinessException 业务校验失败
     */
    private void processSinglePickItem(WorkstationCompletePickItemDto dto,
                                       PickItemExtEntity pickItem,
                                       String containerCode,
                                       String orderContainerCode) {
        try {
            log.info(">>> 【处理拣选明细】开始 | pickItemId={} | containerCode={} | qty={}",
                    pickItem.getId(), containerCode, dto.getQty());

            // ========== 1. 物料释放/解绑 ==========
            handleMaterialRelease(pickItem, containerCode, dto.getQty().doubleValue());

            // ========== 2. 更新拣选进度 ==========
            updatePickProgress(pickItem, dto.getQty().doubleValue(),orderContainerCode);

            // ========== 3. 尝试完成关联订单（幂等安全） ==========
            tryCompleteAssociatedOrder(pickItem.getRelation_order_id());

            log.info("<<< 【处理拣选明细】成功 | pickItemId={} | containerCode={}",
                    pickItem.getId(), containerCode);

        } catch (BusinessException e) {
            log.error("【处理拣选明细】业务校验失败 | pickItemId={} | containerCode={} | reason={}",
                    pickItem.getId(), containerCode, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("【处理拣选明细】系统异常 | pickItemId={} | containerCode={}",
                    pickItem.getId(), containerCode, e);
            throw new BusinessException("拣选操作失败，请重试", e);
        }
    }

    /**
     * 处理物料释放/解绑
     *
     * <p><strong>处理逻辑：</strong>
     * <ol>
     *   <li>参数校验（拣选项、序列号、锁定ID）</li>
     *   <li>查询序列号对应的锁定物料</li>
     *   <li>查找匹配的锁定物料记录</li>
     *   <li>根据可用数量决定处理方式：
     *       <ul>
     *         <li>可用数量为0：解除物料绑定</li>
     *         <li>可用数量>0：释放锁定数量</li>
     *       </ul>
     *   </li>
     * </ol>
     *
     * @param pickItem 拣选项实体
     * @param containerCode 容器号
     * @param pickQty 拣选数量
     */
    private void handleMaterialRelease(PickItemExtEntity pickItem,
                                       String containerCode,
                                       double pickQty) {
        // ========== 1. 参数校验 ==========
        if (pickItem == null) {
            log.warn("【物料释放】拣选项为空，跳过处理 | containerCode={}", containerCode);
            return;
        }

        String serialNo = pickItem.getSerial_no();
        if (serialNo == null || serialNo.trim().isEmpty()) {
            log.warn("【物料释放】序列号无效，跳过处理 | pickItemId={} | containerCode={}",
                    pickItem.getId(), containerCode);
            return;
        }

        UUID storageLockedId = pickItem.getStorage_locked_id();
        if (storageLockedId == null) {
            log.debug("【物料释放】未绑定锁定记录，无需释放 | pickItemId={} | serialNo={}",
                    pickItem.getId(), serialNo);
            return; // 正常业务场景：未锁定则无需释放
        }

        // ========== 2. 查询锁定物料 ==========
        log.debug("【物料释放】查询锁定物料 | serialNo={} | pickItemId={}", serialNo, pickItem.getId());

        List<StorageMaterialDto> storageMaterials =
                storageMaterialAppService.getStorageMaterialAndLockBySerialNo(serialNo);

        if (CollectionUtils.isEmpty(storageMaterials)) {
            log.warn("【物料释放】未找到序列号对应的锁定物料 | serialNo={} | pickItemId={}",
                    serialNo, pickItem.getId());
            return;
        }

        log.debug("【物料释放】查询到 {} 个锁定物料 | serialNo={}", storageMaterials.size(), serialNo);

        // ========== 3. 查找匹配的锁定物料并处理 ==========
        boolean matched = false;
        for (StorageMaterialDto material : storageMaterials) {
            // 跳过无效物料
            if (material == null) {
                log.warn("【物料释放】物料数据为null，跳过处理 | serialNo={}", serialNo);
                continue;
            }

            UUID materialLockedId = material.getStorage_locked_id();
            if (materialLockedId == null) {
                log.debug("【物料释放】物料未绑定锁定ID，跳过 | materialId={} | serialNo={}",
                        material.getId(), serialNo);
                continue;
            }

            // 匹配锁定记录
            if (materialLockedId.equals(storageLockedId)) {
                matched = true;
                log.debug("【物料释放】找到匹配的锁定物料 | pickItemId={} | materialId={}",
                        pickItem.getId(), material.getId());

                // ========== 4. 执行释放逻辑 ==========
                try {
                    if (ArithmeticUtils.isZero(material.getAvailable_qty())) {
                        log.info("【物料释放】可用数量为0，解除物料绑定 | pickItemId={} | materialId={} | serialNo={}",
                                pickItem.getId(), material.getId(), serialNo);
                        relieveMaterialBinding(pickItem, containerCode, material);
                    } else {
                        log.info("【物料释放】释放锁定数量 | pickItemId={} | materialId={} | releaseQty={} | serialNo={}",
                                pickItem.getId(), material.getId(), pickQty, serialNo);
                        releaseLockedQuantity(pickItem, pickQty, material);
                    }
                } catch (Exception e) {
                    log.error("【物料释放】操作失败 | pickItemId={} | materialId={} | serialNo={} | errorMessage={}",
                            pickItem.getId(), material.getId(), serialNo, e.getMessage(), e);
                    throw new RuntimeException("物料释放失败，请检查库存状态", e);
                }

                // 假设一个拣选项只对应一个锁定记录，找到后终止循环
                break;
            }
        }

        // ========== 5. 无匹配物料的审计 ==========
        if (!matched) {
            log.warn("【物料释放】未找到匹配的锁定物料记录 | pickItemId={} | storageLockedId={} | serialNo={} | totalMaterials={}",
                    pickItem.getId(), storageLockedId, serialNo, storageMaterials.size());

            // 可选：根据业务需求决定是否需要告警或补偿处理
        }
    }

    /**
     * 解除物料绑定
     *
     * <p>当物料可用数量为0时，执行解绑操作：
     * <ul>
     *   <li>查询关联的下架单</li>
     *   <li>调用容器物料服务解除绑定</li>
     *   <li>更新容器存储状态</li>
     * </ul>
     *
     * @param pickItem 拣选项实体
     * @param containerCode 容器号
     * @param material 物料信息
     * @throws BusinessException 下架单不存在
     */
    private void relieveMaterialBinding(PickItemExtEntity pickItem,
                                        String containerCode,
                                        StorageMaterialDto material) {
        // 查询关联的下架单
        Outbound outbound = outboundAppService.findOutboundById(pickItem.getOutbound_id());
        if (outbound == null) {
            throw new BusinessException(String.format("托盘[%s]未找到关联下架单", containerCode));
        }

        // 构建解绑参数
        RelieveBySnDto relieveDto = RelieveBySnDto.create()
                .setHouseCode(outbound.gainHouseCode())
                .setContainerCode(containerCode)
                .setSerialNo(Collections.singletonList(material.getSerial_no()))
                .setIsDelLock(true)
                .setIsDelStorage(PlatFormConfig.DO_PICK_OUT_DELETE_MATERIAL)
                .build();

        // 执行解绑
        containerMaterialAppService.relieveBySerialNo(relieveDto);

        // 更新容器存储状态
        storageLocationInventoryAppService.updateContainerStorageStatus(
                outbound.gainHouseCode(),
                containerCode
        );

        log.info("【物料解绑】完成 | serialNo={} | containerCode={} | houseCode={}",
                material.getSerial_no(), containerCode, outbound.gainHouseCode());
    }

    /**
     * 释放锁定数量
     *
     * <p>当物料可用数量>0时，释放指定数量的锁定。
     *
     * @param pickItem 拣选项实体
     * @param pickQty 释放数量
     * @param material 物料信息
     */
    private void releaseLockedQuantity(PickItemExtEntity pickItem,
                                       double pickQty,
                                       StorageMaterialDto material) {
        // 查询锁定记录
        LockedStorageMaterialDto locked = lockedStorageMaterialAppService.getById(
                pickItem.getStorage_locked_id()
        );

        if (locked != null) {
            // 执行释放
            lockedStorageMaterialAppService.releaseLockedRecode(locked, pickQty);
            log.info("【锁定释放】成功 | lockId={} | qty={}", locked.getId(), pickQty);
        } else {
            log.warn("【锁定释放】锁定记录不存在，跳过释放 | storageLockedId={}",
                    pickItem.getStorage_locked_id());
        }
    }

    /**
     * 更新拣选进度
     *
     * <p>更新拣选项的已拣数量和状态。
     *
     * @param pickItem 拣选项实体
     * @param pickQty 本次拣选数量
     * @throws BusinessException 拣选超量
     */
    private void updatePickProgress(PickItemExtEntity pickItem, double pickQty,String orderContainerCode) {
        // 校验拣选数量
        validatePickQuantity(pickItem, pickQty);

        // 计算新的已拣数量和状态
        double currentPicked = Optional.ofNullable(pickItem.getPicked_qty()).orElse(0.0);
        double newPicked = ArithmeticUtils.add(currentPicked, pickQty);
        PickStatus newStatus = ArithmeticUtils.equaled(newPicked, pickItem.getPrimary_qty())
                ? PickStatus.finished
                : PickStatus.picking;

        // 更新数据库
        boolean updated = pickItemExtService.updatePickedQtyAndStatus(
                pickItem.getId().toString(),
                newPicked,
                newStatus,orderContainerCode
        );

        if (!updated) {
            throw new BusinessException("拣选项更新失败，请重试");
        }

        log.info("【拣选进度】更新成功 | pickItemId={} | picked: {} -> {} | status: {} -> {}",
                pickItem.getId(),
                currentPicked,
                newPicked,
                pickItem.getPick_status(),
                newStatus);
    }

    /**
     * 校验拣选数量合法性
     *
     * <p>校验规则：
     * <ul>
     *   <li>拣选数量必须大于0</li>
     *   <li>已拣数量 + 本次拣选数量 <= 需求数量</li>
     * </ul>
     *
     * @param pickItem 拣选项实体
     * @param pickQty 本次拣选数量
     * @throws BusinessException 拣选数量不合法
     */
    private void validatePickQuantity(PickItemExtEntity pickItem, double pickQty) {
        if (pickQty <= 0) {
            throw new BusinessException("拣选数量必须大于0");
        }

        double currentPicked = Optional.ofNullable(pickItem.getPicked_qty()).orElse(0.0);
        double totalPicked = ArithmeticUtils.add(currentPicked, pickQty);

        if (ArithmeticUtils.bigger(totalPicked, pickItem.getPrimary_qty())) {
            throw new BusinessException(String.format(
                    "拣选超量：已拣[%.2f] + 本次[%.2f] > 需求[%.2f] | pickItemId=%s",
                    currentPicked, pickQty, pickItem.getPrimary_qty(), pickItem.getId()
            ));
        }
    }

    /**
     * 尝试完成关联订单
     *
     * <p>当拣选项处理完成后，检查关联订单是否可以完成：
     * <ol>
     *   <li>校验订单是否已全部拣选</li>
     *   <li>校验是否存在未关闭的拣选项</li>
     *   <li>校验物料分配是否完成</li>
     *   <li>完成订单</li>
     * </ol>
     *
     * @param orderId 订单ID
     */
    private void tryCompleteAssociatedOrder(UUID orderId) {
        if (orderId == null) {
            log.debug("【订单完成】订单ID为空，跳过处理");
            return;
        }

        log.debug("【订单完成】开始检查 | orderId={}", orderId);

        // 校验订单是否已全部拣选
        if (!isOrderFullyPicked(orderId)) {
            log.debug("【订单完成】订单未完成全部拣选，跳过完成操作 | orderId={}", orderId);
            return;
        }

        // 校验是否存在未关闭的拣选项
        if (hasUnclosedPickItems(orderId)) {
            log.warn("【订单完成】订单存在未关闭拣选项，无法完成 | orderId={}", orderId);
            throw new BusinessException("订单存在未关闭拣选项，无法完成");
        }

        // 校验物料分配是否完成
        if (!isOrderFullyAllocated(orderId)) {
            log.warn("【订单完成】订单物料分配未完成，无法完成 | orderId={}", orderId);
            throw new BusinessException("订单物料分配未完成，无法完成");
        }

        // 完成订单
        completeOrderSafely(orderId);
    }

    /**
     * 检查订单是否已全部拣选
     *
     * @param orderId 订单ID
     * @return true-已全部拣选，false-未全部拣选
     */
    private boolean isOrderFullyPicked(UUID orderId) {
        PickAblItemQueryParam param = new PickAblItemQueryParam();
        param.setRelationOrderId(orderId);

        List<PickItem> items = iPickItemRepositoryService.findPickAbleByContainer(param);

        BigDecimal picked = items.stream()
                .map(i -> Optional.ofNullable(i.getPickedQty()).orElse(0.0))
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal required = items.stream()
                .map(i -> Optional.ofNullable(i.getPrimaryQty()).orElse(0.0))
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean finished = picked.compareTo(required) >= 0;

        log.debug("【订单检查】拣选完成检查 | orderId={} | picked={} | required={} | finished={}",
                orderId, picked, required, finished);

        return finished;
    }

    /**
     * 检查订单是否存在未关闭的拣选项
     *
     * @param orderId 订单ID
     * @return true-存在未关闭拣选项，false-不存在
     */
    private boolean hasUnclosedPickItems(UUID orderId) {
        List<PickItem> unClosed = iPickItemRepositoryService.findUnClosedPickItem(orderId);
        boolean hasUnclosed = CollUtil.isNotEmpty(unClosed);

        if (hasUnclosed) {
            log.debug("【订单检查】存在 {} 个未关闭拣选项 | orderId={}", unClosed.size(), orderId);
        }

        return hasUnclosed;
    }

    /**
     * 检查订单物料分配是否完成
     *
     * @param orderId 订单ID
     * @return true-分配完成，false-分配未完成
     */
    private boolean isOrderFullyAllocated(UUID orderId) {
        ReqItemQueryParam param = new ReqItemQueryParam();
        param.setRelation_order_id(orderId);

        List<ReqItem> items = iReqItemRepositoryService.findItemByParam(param);

        for (ReqItem item : items) {
            if (item.gainAllottingQty() > 0) {
                log.warn("【订单检查】物料分配不完整 | orderId={} | sku={} | required={} | allocated={}",
                        orderId, item.gainSkuCode(), item.getPrimaryQty(), item.getConfirmQty());
                return false;
            }
        }

        return true;
    }

    /**
     * 安全完成订单
     *
     * <p>完成订单并更新状态，避免重复完成。
     *
     * @param orderId 订单ID
     */
    private void completeOrderSafely(UUID orderId) {
        ReqOrder order = iReqOrderRepositoryService.findOrderById(orderId);

        if (order != null) {
            order.finishOrder();
            iReqOrderRepositoryService.updateOrderFormStatus(order);
            log.info("【订单完成】成功 | orderId={}", orderId);
        } else {
            log.debug("【订单完成】订单已存在或已完成，跳过操作 | orderId={}", orderId);
        }
    }

    /**
     * 预加载拣选项数据
     *
     * <p>批量查询拣选项，减少循环内重复查询，提升性能。
     *
     * @param items 拣选明细列表
     * @return 拣选项Map（key: 拣选项ID, value: 拣选项实体）
     */
    private Map<String, PickItemExtEntity> preloadPickItems(List<WorkstationCompletePickItemDto> items) {
        List<String> itemIds = items.stream()
                .map(WorkstationCompletePickItemDto::getPickItemId)
                .collect(Collectors.toList());

        log.debug("【数据预加载】批量查询拣选项 | 数量={}", itemIds.size());

        List<PickItemExtEntity> entities = pickItemExtService.batchGetByIds(itemIds);

        return entities.stream()
                .collect(Collectors.toMap(e -> e.getId().toString(), e -> e));
    }

    /**
     * 校验所有拣选项是否存在
     *
     * @param items 拣选明细列表
     * @param pickItemMap 拣选项Map
     * @throws BusinessException 拣选项不存在
     */
    private void validatePickItemsExist(List<WorkstationCompletePickItemDto> items,
                                        Map<String, PickItemExtEntity> pickItemMap) {
        for (WorkstationCompletePickItemDto item : items) {
            if (!pickItemMap.containsKey(item.getPickItemId())) {
                log.error("【参数校验】拣选项不存在 | pickItemId={}", item.getPickItemId());
                throw new BusinessException(String.format("拣选项不存在 | pickItemId=%s", item.getPickItemId()));
            }
        }
        log.debug("【参数校验】所有拣选项存在校验通过 | 数量={}", items.size());
    }

    /**
     * 参数校验
     *
     * @param dto 拣选完成参数
     * @throws BusinessException 参数无效
     */
    private void validateInput(WorkstationCompletePickDto dto) {
        if (dto == null || StrUtil.isBlank(dto.getContainerCode()) || CollUtil.isEmpty(dto.getPickItemList())) {
            log.error("【参数校验】参数无效 | dto={}", dto);
            throw new BusinessException("参数无效：containerCode和pickItemList不能为空");
        }
        log.debug("【参数校验】参数校验通过 | containerCode={} | pickItemListSize={}",
                dto.getContainerCode(), dto.getPickItemList().size());
    }

    /**
     * 校验容器是否存在未完成的调度任务
     *
     * @param containerCode 容器号
     * @throws BusinessException 存在未完成任务
     */
    private void validateNoActiveJob(String containerCode) {
        DispatchJobDto activeJob = dispatchJobService.queryActiveJob(
                WmsConfig.DefaultHouseCode,
                containerCode
        );

        if (activeJob != null) {
            log.error("【任务校验】容器存在未完成的出库任务 | containerCode={} | jobId={}",
                    containerCode, activeJob.getId());
            throw new BusinessException(String.format("托盘[%s]存在未完成的出库任务(jobId=%s)",
                    containerCode, activeJob.getTask_no()));
        }
        log.debug("【任务校验】容器无未完成任务 | containerCode={}", containerCode);
    }

    /**
     * 创建未完成响应
     *
     * @return 拣选完成结果视图
     */
    private PickOrderFinishVo createIncompleteResponse() {
        PickOrderFinishVo vo = new PickOrderFinishVo();
        vo.setOrderIsFinish(Boolean.FALSE);
        return vo;
    }

    // ==================== 容器离开工作站 ====================

    /**
     * 处理容器离开工作站
     *
     * <p><strong>业务流程：</strong>
     * <ol>
     *   <li>查询容器当前状态</li>
     *   <li>校验容器是否在指定工作站</li>
     *   <li>校验容器状态是否允许离开</li>
     *   <li>更新容器位置信息</li>
     *   <li>记录操作日志</li>
     *   <li>触发后续业务流程</li>
     * </ol>
     *
     * <p><strong>事务说明：</strong>
     * <ul>
     *   <li>整个方法在事务中执行，任一操作失败将回滚所有变更</li>
     * </ul>
     *
     * @param dto 容器离开请求参数
     * @return true-成功，false-失败
     * @throws BusinessException 业务异常（容器不存在、状态不允许等）
     * @throws RuntimeException 系统异常
     * @see OrderContainerLeaveDto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processContainerLeave(OrderContainerLeaveDto dto) throws BusinessException {
        // 记录方法开始时间
        long startTime = System.currentTimeMillis();

        String containerCode = dto.getContainerCode();
        String workstationCode = dto.getWorkstationCode();
        String toPos = dto.getToPos();

        String houseCode=WmsConfig.DefaultHouseCode;
        log.info("【容器离开】开始处理 | containerCode={} | workstationCode={} | toPos={}",
                containerCode, workstationCode, toPos);

        try {
            // ========== 1. 查询容器当前状态 ==========
            log.debug("【容器离开】查询容器状态 | containerCode={}", containerCode);

            // TODO: 调用服务查询容器信息
            // ContainerInfo containerInfo = containerService.getContainerInfo(containerCode);
            // if (containerInfo == null) {
            //     throw new BusinessException("容器不存在: " + containerCode);
            // }

            // ========== 2. 校验容器是否在当前工作站 ==========
            // if (!containerInfo.getWorkstationCode().equals(dto.getWorkstationCode())) {
            //     throw new BusinessException("容器不在指定工作站: " + dto.getWorkstationCode());
            // }

            // ========== 3. 校验容器状态是否允许离开 ==========
            // if (!canContainerLeave(containerInfo)) {
            //     throw new BusinessException("容器状态不允许离开: " + containerInfo.getStatus());
            // }

            // ========== 4. 更新容器位置信息 ==========
            log.debug("【容器离开】更新容器位置 | containerCode={} | toPos={}", containerCode, toPos);

            // TODO: 调用服务更新容器位置



            // ========== 5. 记录操作日志 ==========
            logOperationLog(dto);

            // ========== 6. 触发后续业务流程 ==========
            triggerNextProcess(dto);

            long costTime = System.currentTimeMillis() - startTime;
            log.info("【容器离开】处理成功 | containerCode={} | 耗时={}ms", containerCode, costTime);

            return true;

        } catch (BusinessException e) {
            log.error("【容器离开】业务异常 | containerCode={} | error={}", containerCode, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("【容器离开】系统异常 | containerCode={}", containerCode, e);
            throw new BusinessException("容器离开处理失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void transportExecute(String houseCode, String containerCode, String deviceCode, String toPos, Map<String, Object> paramMap) {
        DispatchJobDto taskJob = dispatchJobService.queryActiveJob(houseCode, containerCode);
        if (taskJob != null) {
            throw new BusinessException(StringFormatUtl.formatI18n("容器{}任务已存在!", containerCode));
        }
        String dispatchCode = TransportDispatch.class.getSimpleName();
        String callbackCode = TransportCallback.class.getSimpleName();
        TransportDispatchParam dispatchParam = new TransportDispatchParam();
        dispatchParam.setHouseCode(houseCode);
        dispatchParam.setContainerCode(containerCode);
        dispatchParam.setLocFrom(deviceCode);
        dispatchParam.setLocTo(toPos);
        dispatchParam.setAuto_send(true);
        TransportCallbackParam callbackParam = new TransportCallbackParam();

        StorageLocationInventoryDto storageLocationInventoryDto = storageLocationInventoryAppService
                .getStorageByContainerCode(houseCode, containerCode);
        if (Objects.nonNull(storageLocationInventoryDto)
                && LocationType.cubic == storageLocationInventoryDto.getLoc_type()) {
            ResultFactory.getError(
                    "输送移动任务,任务创建失败,容器:" + containerCode + "在已存在立库中" + storageLocationInventoryDto.getLocation_code());
            return;
        }
        DispatchInfoDto dto = dispatchInfoService.getActivityDispatchInfo(houseCode, containerCode);
        if (dto == null) {
            dto = dispatchInfoService.createDispatchInfoWithReturn(
                    houseCode != null ? houseCode : WmsConfig.DefaultHouseCode, containerCode, "", "", dispatchCode,
                    dispatchParam, callbackCode, callbackParam, "");
        }
        List<DispatchJob> dispatchJobs;
        DoDispatchDto doDispatchDto = new DoDispatchDto(dto.getId(), dto.getHouse_code(), dto.getContainer_code(),
                deviceCode, dto.getTarget_pos(), dto.getContainerShape(), dto.getParameters());

        try {
            Dispatch dispatch = dispatchManager.queryByCode(dto.getDispatch_code());
            String param = new String(dto.getDispatch_param());
            dispatchJobs = dispatch.doDispatch(doDispatchDto,
                    (DispatchParam) JsonUtl.format(param, dispatch.getActionParamClass()), true);

            if (ObjectUtils.isNotEmpty(dispatchJobs)) {
                for (DispatchJob dispatchJob : dispatchJobs) {
                    DispatchJobDto jobDto = BeanUtl.copyProperties(dispatchJob, DispatchJobDto.class);
                    jobDto.setParameters(paramMap);
//                    dispatchJobService.addOrUpdateJob(jobDto);
                    DispatchJobDto dispatchJobDto = dispatchJobService.queryActiveJob(houseCode, containerCode);
                    if (Objects.nonNull(dispatchJobDto)) {
                        publishJobTaskAppService.publishDispatchJobToWCS(jobDto);
                    }

                }
            }
        } catch (Exception e) {
            log.error("输送移动任务异常：{}", ExceptionUtils.getStackTrace(e));
            ResultFactory.getError("输送移动任务,任务创建失败,ID:" + containerCode);
            return;
        }

    }

    /**
     * 记录容器离开操作日志
     *
     * @param dto 容器离开请求参数
     */
    private void logOperationLog(OrderContainerLeaveDto dto) {
        // TODO: 记录操作日志到数据库
        // OperationLog log = new OperationLog();
        // log.setOperationType("CONTAINER_LEAVE");
        // log.setContainerCode(dto.getContainerCode());
        // log.setWorkstationCode(dto.getWorkstationCode());
        // log.setToPos(dto.getToPos());
        // log.setOperator(getCurrentOperator());
        // log.setCreateTime(new Date());
        // operationLogService.save(log);

        log.info("【操作日志】记录完成 | containerCode={} | workstationCode={} | toPos={}",
                dto.getContainerCode(), dto.getWorkstationCode(), dto.getToPos());
    }

    /**
     * 触发后续业务流程
     *
     * <p>根据目标位置触发不同的后续流程：
     * <ul>
     *   <li>warehouse: 触发入库流程</li>
     *   <li>workshops: 触发出库流程</li>
     * </ul>
     *
     * @param dto 容器离开请求参数
     */
    private void triggerNextProcess(OrderContainerLeaveDto dto) {
        // TODO: 根据业务需求触发后续流程
        // 例如：通知 WCS 系统、更新订单状态、触发质检等

        String toPos = dto.getToPos().trim().toLowerCase();
        if ("warehouse".equals(toPos)) {
            log.debug("【后续流程】目标位置为仓库，触发入库流程 | containerCode={}", dto.getContainerCode());
            // triggerWarehouseProcess(dto);
        } else if ("workshops".equals(toPos)) {
            log.debug("【后续流程】目标位置为车间，触发出库流程 | containerCode={}", dto.getContainerCode());
            // triggerWorkshopProcess(dto);
        } else {
            log.warn("【后续流程】未知目标位置，跳过后续处理 | toPos={} | containerCode={}",
                    toPos, dto.getContainerCode());
        }
    }

    /**
     * 依据托盘查询可拣选的明细
     * @param queryParam
     * @return
     */
    @Override
    public List<RequisitionItemContainerCodeExtDto> findPickAbleByContainerList(PickAblItemQueryParam queryParam){

        // 4. 查询数据
        List<PickItem> pickItems = pickItemRepository.findPickAbleByContainer(queryParam);

        // 5. 数据转换 (增加空指针防护)
        List<RequisitionItemContainerCodeExtDto> dtoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(pickItems)) {
            dtoList = pickItems.stream()
                    .filter(Objects::nonNull)
                    .map(this::convertToDto)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return dtoList;
    }

    @Override
    public PageResult<RequisitionItemContainerCodeExtDto> findPickAbleByContainerPage(PickAblItemQueryParam query){

        // 4. 查询数据
        List<PickItem> pickItems = pickItemRepository.findPickAbleByContainer(query);

        // 5. 数据转换 (增加空指针防护)
        List<RequisitionItemContainerCodeExtDto> dtoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(pickItems)) {
            dtoList = pickItems.stream()
                    .filter(Objects::nonNull)
                    .map(this::convertToDto)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        int total = (int) dtoList.stream()
                .filter(Objects::nonNull)
                .count(); // 去重前的数量
        return new PageResult<>(dtoList, query.getPage(), query.getRow(), total);
    }

    @Override
    public void pickItemContainerExitOut(RequisitionItemContainerCodeListExtDto query) {
        // 1. 顶层参数校验
        if (query == null) {
            log.error("【容器出库】请求参数为空，操作终止");
            return;
        }

        // 2. 获取并校验列表
        List<RequisitionItemContainerCodeExtDto> pickContainerList = query.getPickContainerList();
        if (pickContainerList == null || pickContainerList.isEmpty()) {
            return;
        }

        // 3. 获取工作站编码 (用于日志和参数)
        String workstationCode = query.getWorkstationCode();
        if (workstationCode == null || workstationCode.trim().isEmpty()) {
             return;
        }

        log.info("【容器出库】开始处理，工作站: {}, 待处理数量: {}", workstationCode, pickContainerList.size());

        // 4. 遍历处理 (支持"部分成功"模式)
        int successCount = 0;
        int skipCount = 0;

        for (RequisitionItemContainerCodeExtDto dto : pickContainerList) {
            try {
                // 4.1 元素非空校验
                if (dto == null) {
                    log.warn("【容器出库】列表中包含 null 元素，已跳过");
                    skipCount++;
                    continue;
                }

                // 4.2 关键字段校验 (提取为独立方法，见下方)
                if (!validateDtoFields(dto)) {
                    log.warn("【容器出库】数据校验失败，已跳过, dto: {}", dto);
                    skipCount++;
                    continue;
                }

                // 4.3 安全转换 UUID (防止非法字符串导致崩溃)
                UUID relationOrderId;
                UUID relationItemId;
                UUID pickItemId;
                String businessFormNo = dto.getBusinessFormNo();
                try {
                    relationOrderId = UUID.fromString(dto.getFormId());
                    relationItemId = UUID.fromString(dto.getItemId());
                    pickItemId = UUID.fromString(dto.getId());
                } catch (IllegalArgumentException e) {
                    log.error("【容器出库】UUID 格式错误，formId: {}, itemId: {}, id: {}",
                            dto.getFormId(), dto.getItemId(), dto.getId(), e);
                    skipCount++;
                    continue;
                }

                // 4.4 构建参数
                ContainerTakeOutParam takeOutParam = new ContainerTakeOutParam();
                takeOutParam.setRelationOrderId(relationOrderId);
                takeOutParam.setRelationItemId(relationItemId);
                takeOutParam.setPickItemId(pickItemId);
                takeOutParam.setContainerCode(dto.getContainerCode().trim()); // 去除空格
                takeOutParam.setPickArea(workstationCode);

                // 4.5 执行核心出库操作
                pickOperationAppService.takeContainerOut(takeOutParam);
                successCount++;

//                WorkstationExtDto workstationExtDto=new WorkstationExtDto();
//                workstationExtDto.setWorkstationCode(workstationCode);
//                workstationExtDto.setOrderId(dto.getFormId());
//                workstationExtDto.setOrderNo(businessFormNo);
//                workstationService.updateStatus(workstationExtDto);


            } catch (Exception e) {
                // 5. 兜底异常捕获：防止单个数据异常导致整个批量操作回滚
                log.error("【容器出库】处理单条记录时发生未知异常, dto: {}", dto, e);
                skipCount++;
                // 根据业务需求：是否遇到异常立即抛出中断？默认继续处理下一条
                // throw new BusinessException("容器出库失败: " + e.getMessage(), e);
            }
        }

        // 6. 处理结果汇总日志
        log.info("【容器出库】处理完成，成功: {}, 跳过: {}, 总计: {}",
                successCount, skipCount, pickContainerList.size());
    }
    /**
     * 校验 DTO 关键字段是否完整
     * @param dto 待校验对象
     * @return 校验通过返回 true，否则返回 false
     */
    private boolean validateDtoFields(RequisitionItemContainerCodeExtDto dto) {
        // 校验 UUID 字符串字段 (不能为空且格式需合法)
        if (isBlank(dto.getFormId())) {
            log.warn("校验失败: formId 为空");
            return false;
        }
        if (isBlank(dto.getItemId())) {
            log.warn("校验失败: itemId 为空");
            return false;
        }
        if (isBlank(dto.getId())) {
            log.warn("校验失败: pickItemId 为空");
            return false;
        }

        // 校验容器编码
        if (isBlank(dto.getContainerCode())) {
            log.warn("校验失败: containerCode 为空");
            return false;
        }

        return true;
    }

    /**
     * 辅助方法：判断字符串是否为空 (兼容 null 和空白字符)
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 实体转 DTO (提取方法以降低圈复杂度，增加可读性)
     *
     * @param pickItem 拣选项
     * @return 扩展 DTO，若关键信息缺失则返回 null
     */
    /**
     * 实体转 DTO (增强版 - 全字段非空防护)
     *
     * @param pickItem 拣选项
     * @return 扩展 DTO，若关键信息缺失则返回 null
     */
    private RequisitionItemContainerCodeExtDto convertToDto(PickItem pickItem) {
        // 1. 顶层对象校验
        if (pickItem == null) {
            return null;
        }

        // 2. 核心关联对象校验 - StorageMaterialInfo
        StorageMaterialInfo storageMaterialInfo = pickItem.getStorageMaterialInfo();
        if (storageMaterialInfo == null) {
            log.warn("【订单容器出库】数据异常，PickItem 缺少 StorageMaterialInfo, pickItemId: {}",
                    pickItem.getRelation_item_id());
            return null;
        }

        // 3. 核心关联对象校验 - SkuInfo
        SkuInfo skuInfo = storageMaterialInfo.getSkuInfo();
        if (skuInfo == null) {
            log.warn("【订单容器出库】数据异常，StorageMaterialInfo 缺少 SkuInfo, containerCode: {}",
                    storageMaterialInfo.getContainerCode());
            return null;
        }

        // 4. 可选关联对象校验 - BusinessInfo (根据业务决定是否强依赖)
        BusinessInfo businessInfo = pickItem.getBusinessInfo();
        if (businessInfo == null) {
            log.warn("【订单容器出库】数据异常，PickItem 缺少 BusinessInfo, pickItemId: {}",
                    pickItem.getRelation_item_id());
            // 策略选择：如果 businessFormNo 是关键字段，建议返回 null；否则可继续处理
            return null;
        }

        // 5. 开始构建 DTO
        RequisitionItemContainerCodeExtDto dto = new RequisitionItemContainerCodeExtDto();

        // --- SKU 信息 (安全取值) ---
        String skuCode = skuInfo.getSkuCode();
        String skuName = skuInfo.getSkuName();
        dto.setSkuCode(skuCode != null ? skuCode : "");
        dto.setSkuName(skuName != null ? skuName : "");

        // --- 容器信息 (安全取值) ---
        String containerCode = storageMaterialInfo.getContainerCode();
        dto.setContainerCode(containerCode != null ? containerCode : "");

        // --- 数量信息 (安全取值) ---
        // 建议：如果数据库使用 double，此处建议转换为 BigDecimal 或做精度处理
        Double qty = storageMaterialInfo.getQty();
        dto.setPrimaryQty(qty);

        // --- 单位信息 (安全取值) ---
        String unit = storageMaterialInfo.getUnit();
        dto.setPrimaryUnit(unit != null ? unit : "");

        PickItemKey itemKey = pickItem.getItemKey();
        UUID id = itemKey.getOrderKey().getId();
        if (id != null) {
            dto.setId(id.toString());
        }

        UUID relationOrderId = pickItem.getRelation_order_id();
        if (relationOrderId != null) {
            dto.setFormId(relationOrderId.toString());
        }

        // --- 关联业务项 ID (特殊处理) ---
        UUID relationItemId = pickItem.getRelation_item_id();
        if (relationItemId != null) {
            dto.setItemId(relationItemId.toString());
        } else {
            // 策略：ID 为空时，可记录警告或生成临时标识，避免前端解析异常
            log.warn("【订单容器出库】数据异常，PickItem 缺少 relation_item_id, pickItemId: {}",
                    relationItemId);
            dto.setItemId(""); // 或根据业务需求设为 "UNKNOWN"
        }

        // --- 业务单据号 (安全取值) ---
        String businessFormNo = businessInfo.getBusinessFormNo();
        dto.setBusinessFormNo(businessFormNo != null ? businessFormNo : "");

        // --- 创建时间 ---
        dto.setCreateDatetime(new Date());

        // 6. 最终数据完整性校验 (可选)
        if (!validateDto(dto)) {
            log.warn("【订单容器出库】生成的 DTO 关键字段为空，已丢弃, dto: {}", dto);
            return null;
        }

        return dto;
    }

    /**
     * DTO 数据完整性校验 (可选工具方法)
     * 用于确保返回给前端的数据包含必要的业务字段
     */
    private boolean validateDto(RequisitionItemContainerCodeExtDto dto) {
        if (dto == null) {
            return false;
        }
        // 根据业务定义"关键字段"，以下仅为示例
        if (dto.getSkuCode() == null || dto.getSkuCode().isEmpty()) {
            return false;
        }
        if (dto.getContainerCode() == null || dto.getContainerCode().isEmpty()) {
            return false;
        }
        if (dto.getBusinessFormNo() == null || dto.getBusinessFormNo().isEmpty()) {
            return false;
        }
        return true;
    }
}