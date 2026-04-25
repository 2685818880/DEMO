package cn.zdjc.wms.project.control.pick;


import cn.zdjc.warehouse.WmsConfig;
import cn.zdjc.wms.api.dto.TaskApplyParam;
import cn.zdjc.wms.api.service.TransJobAppService;
import cn.zdjc.wms.commons.StorageMaterialInfo;
import cn.zdjc.wms.definition.application.query.AsnExQuery;
import cn.zdjc.wms.definition.domain.dto.AsnDto;
import cn.zdjc.wms.definition.domain.dto.PalletizeFormDto;
import cn.zdjc.wms.definition.domain.service.AsnService;
import cn.zdjc.wms.definition.domain.service.PalletizeFormService;
import cn.zdjc.wms.definition.infrastructure.enums.AllotStatus;
import cn.zdjc.wms.definition.infrastructure.enums.AsnStatus;
import cn.zdjc.wms.definition.infrastructure.enums.FormStatus;
import cn.zdjc.wms.definition.infrastructure.enums.PalletizeFormStatus;
import cn.zdjc.wms.outbound.pick.domain.entity.PickItem;
import cn.zdjc.wms.outbound.pick.domain.repository.IPickItemRepositoryService;
import cn.zdjc.wms.outbound.pick.domain.repository.params.PickAblItemQueryParam;
import cn.zdjc.wms.outbound.requisition.application.params.ReqAllotParam;
import cn.zdjc.wms.outbound.requisition.application.params.ReqPickOutParam;
import cn.zdjc.wms.outbound.requisition.application.service.ReqOperationAppService;
import cn.zdjc.wms.outbound.requisition.domain.entity.ReqOrder;
import cn.zdjc.wms.outbound.requisition.domain.share.CarrierInfo;
import cn.zdjc.wms.outbound.requisition.domain.share.SkuInfo;
import cn.zdjc.wms.outbound.requisition.infrastructure.persistence.jdbc.repository.RequisitionReqOrderRepository;
import cn.zdjc.wms.project.ProjectConfig;
import cn.zdjc.wms.project.common.constant.ProjectConstant;
import cn.zdjc.wms.project.domain.dto.inbound.BatchPutAwayCombineDto;
import cn.zdjc.wms.project.domain.dto.inbound.OrderExtDto;
import cn.zdjc.wms.project.domain.dto.inbound.PjInventoryReceiptItemDto;
import cn.zdjc.wms.project.domain.dto.pick.*;
import cn.zdjc.wms.project.domain.dto.requisition.RequisitionItemContainerCodeExtDto;
import cn.zdjc.wms.project.domain.dto.requisition.RequisitionItemContainerCodeListExtDto;
import cn.zdjc.wms.project.domain.dto.requisition.RequisitionItemExtDto;
import cn.zdjc.wms.project.domain.dto.requisition.RequisitionOrderWithPalletCountDto;
import cn.zdjc.wms.project.domain.entity.requisition.RequisitionOrderExtEntity;
import cn.zdjc.wms.project.domain.query.pick.PickItemExtQuery;
import cn.zdjc.wms.project.domain.query.requisition.RequisitionItemExtExQuery;
import cn.zdjc.wms.project.domain.query.requisition.RequisitionOrderExtExQuery;
import cn.zdjc.wms.project.domain.vo.pick.PickOrderFinishVo;
import cn.zdjc.wms.project.domain.vo.pick.PickOrderInfoVo;
import cn.zdjc.wms.project.domain.vo.pick.WorkstationPickInfoVo;
import cn.zdjc.wms.project.erp.dto.ResultWrapper;
import cn.zdjc.wms.project.service.inbound.PjInboundService;
import cn.zdjc.wms.project.service.pick.PickExtService;
import cn.zdjc.wms.project.service.requisition.RequisitionItemExtService;
import cn.zdjc.wms.project.service.requisition.RequisitionOrderExtService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.foeris.y.common.exception.BusinessException;
import com.foeris.y.common.json.JsonUtl;
import com.foeris.y.common.result.MessageResult;
import com.foeris.y.common.result.PageResult;
import com.foeris.y.common.result.ResultFactory;
import com.foeris.y.common.string.StringFormatUtl;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 拣选相关
 */
@RestController
@RequestMapping(ProjectConstant.PREFIX_CONTEXT_PATH + "/pick")
@Slf4j
public class PickExtController {
    @Resource
    private PickExtService pickExtService;

    @Resource
    private RequisitionItemExtService requisitionItemExtService;

    @Resource
    private RequisitionOrderExtService requisitionOrderExtService;

    @Resource
    private ReqOperationAppService reqOperationAppService;

    @Resource
    private PjInboundService inboundService;


    @Resource
    private TransJobAppService transJobAppService;

    @Resource
    private RequisitionReqOrderRepository requisitionReqOrderRepository;

    @Resource
    private PalletizeFormService palletizeFormService;

    // ========== 工具方法：安全获取字符串（避免 null.trim() NPE）==========
    private static String safeTrim(String str) {
        return Optional.ofNullable(str).map(String::trim).orElse("");
    }

    /**
     * 发货单列表（不分页）
     */
    @PostMapping("/find-orders-list")
    public ResultWrapper<List<OrderExtDto>> findOrderListInfo(@RequestBody RequisitionItemExtExQuery query) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("【查询发货单列表】参数: {}", query);
            String workstationCode = query != null ? query.getWorkstationCode() : null;
            if (StringUtils.isEmpty(workstationCode)) {
                return ResultWrapper.buildFailure("工作站编码不能为空");
            }
            // 设置状态（建议：后续可移至 Service 层）
//            query.setAllotStatus(String.valueOf(AllotStatus.Created));

            List<String> formStatusList = new ArrayList<>();
            formStatusList.add(String.valueOf(FormStatus.Created));
            formStatusList.add(String.valueOf(FormStatus.Executing));
            formStatusList.add(String.valueOf(FormStatus.Picked));
            formStatusList.add(String.valueOf(FormStatus.Waiting));

            query.setFormStatusList(formStatusList);


            // 调用服务层查询
            List<OrderExtDto> orderNos = requisitionItemExtService.orderNoListInfo(query);

            log.info("【查询发货单列表】返回 {} 个订单号，耗时: {}ms",
                    CollectionUtils.isNotEmpty(orderNos) ? orderNos.size() : 0,
                    System.currentTimeMillis() - startTime);

            return ResultWrapper.buildSuccess(orderNos);

        } catch (Exception e) {
            log.error("【查询发货单列表】系统异常，耗时: {}ms, 参数: {}",
                    System.currentTimeMillis() - startTime,
                    query,
                    e);
            return ResultWrapper.buildFailure("系统异常: " + e.getMessage());
        }
    }

    /**
     * 发货单列表分页
     */
    @GetMapping("/find-orders-page")
    public PageResult<OrderExtDto> findOrderPageInfo(@RequestBody RequisitionItemExtExQuery query) {
        long startTime = System.currentTimeMillis();
        String workstationCode = query != null ? query.getWorkstationCode() : null;
        if (StringUtils.isEmpty(workstationCode)) {
            return ResultFactory.getErrorPage("工作站编码不能为空");
        }
        try {
            log.info("【查询发货单分页】参数: {}", query);

            // 设置状态（建议：后续可移至 Service 层）
//            query.setAllotStatus(String.valueOf(AllotStatus.Created));
            List<String> formStatusList = new ArrayList<>();
            formStatusList.add(String.valueOf(FormStatus.Created));
            formStatusList.add(String.valueOf(FormStatus.Executing));
            formStatusList.add(String.valueOf(FormStatus.Picked));
            formStatusList.add(String.valueOf(FormStatus.Waiting));
            query.setFormStatusList(formStatusList);
            // 调用服务层查询
            PageResult<OrderExtDto> pageResult = requisitionItemExtService.orderNoPageInfo(query);

            log.info("【查询发货单分页】返回 {} 条数据，总计 {} 条，耗时: {}ms",
                    (pageResult != null && CollectionUtils.isNotEmpty(pageResult.getRows())) ? pageResult.getRows().size() : 0,
                    pageResult != null ? pageResult.getTotal() : 0,
                    System.currentTimeMillis() - startTime);

            return pageResult;

        } catch (Exception e) {
            log.error("【查询发货单分页】系统异常，耗时: {}ms, 参数: {}",
                    System.currentTimeMillis() - startTime,
                    query,
                    e);
            return ResultFactory.getErrorPage("系统异常: " + e.getMessage());
        }
    }

    /**
     * 执行订单出库操作
     * @param query 业务查询参数，必须包含 businessFormNo
     * @return 操作结果
     */
    @PostMapping("/execute-pick-out")
    public ResultWrapper<String> executePickOut(@Valid @RequestBody RequisitionOrderExtExQuery query) {
        long startTime = System.currentTimeMillis();


        try {
            // ========== 1. 参数校验 ==========
            if (query == null || StringUtils.isEmpty(query.getBusinessFormNo())) {
                log.warn("【订单出库】参数校验失败: businessFormNo 为空");
                return ResultWrapper.buildFailure("业务单据号不能为空");
            }
            String workstationCode = query.getWorkstationCode();
            // 🔒 修复：使用 StringUtils.isEmpty 替代 .isEmpty()，避免 NPE
            if (StringUtils.isEmpty(workstationCode)) {
                log.warn("【订单出库】参数校验失败: workstationCode 为空字符串");
                return ResultWrapper.buildFailure("工作站号不能为空");
            }

            String businessFormNo = query.getBusinessFormNo();
            // 🔒 修复：使用 StringUtils.isEmpty 替代 .isEmpty()，防御性编程
            if (StringUtils.isEmpty(businessFormNo)) {
                log.warn("【订单出库】参数校验失败: businessFormNo 为空字符串");
                return ResultWrapper.buildFailure("业务单据号不能为空");
            }

            log.info("【订单出库】开始处理，businessFormNo: {}", businessFormNo);

            // ========== 2. 查询订单信息 ==========
            RequisitionOrderExtEntity orderEntity = requisitionOrderExtService.findByBusinessFormNo(businessFormNo);
            if (orderEntity == null) {
                log.warn("【订单出库】订单不存在，businessFormNo: {}, 耗时: {}ms",
                        businessFormNo, System.currentTimeMillis() - startTime);
                return ResultWrapper.buildFailure("未找到业务单据号为 [" + businessFormNo + "] 的出库订单");
            }
            ReqAllotParam allotParam=new ReqAllotParam();
            allotParam.setOrderId(orderEntity.getId());
            //先执行分配
            reqOperationAppService.allotOrder(allotParam);

            allocateWorkStation(allotParam,workstationCode);

            // ========== 3. 订单状态校验 ==========
//            if (!canExecutePickOut(orderEntity)) {
//                String errorMsg = buildOrderStatusErrorMsg(orderEntity);
//                log.warn("【订单出库】订单状态不允许出库，businessFormNo: {}, formStatus: {}, 耗时: {}ms",
//                        businessFormNo, orderEntity.getForm_status(), System.currentTimeMillis() - startTime);
//                return ResultWrapper.buildFailure(errorMsg);
//            }

            // ========== 4. 执行出库操作 ==========
            //改为走定时器
//            ReqPickOutParam pickOutParam = new ReqPickOutParam();
//            pickOutParam.setOrderId(orderEntity.getId());
//            pickOutParam.setPickArea(workstationCode);
//            log.info("【订单出库】调用出库服务，orderId: {}, businessFormNo: {}",
//                    orderEntity.getId(), businessFormNo);
//            reqOperationAppService.doPickOut(pickOutParam);

            // ========== 5. 记录成功日志 ==========
            long costTime = System.currentTimeMillis() - startTime;
            log.info("【订单出库】执行成功，businessFormNo: {}, formNo: {}, orderId: {}, 耗时: {}ms",
                    businessFormNo,
                    Optional.ofNullable(orderEntity.getForm_no()).orElse("unknown"),
                    orderEntity.getId(), costTime);

            // 性能监控告警（可选）
            if (costTime > TimeUnit.SECONDS.toMillis(5)) {
                log.warn("【订单出库】执行耗时较长，businessFormNo: {}, 耗时: {}ms", businessFormNo, costTime);
            }

            return ResultWrapper.buildSuccess("订单出库执行成功");

        } catch (IllegalArgumentException e) {
            log.error("【订单出库】参数异常，businessFormNo: {}, 耗时: {}ms, 原因: {}",
                    query != null ? query.getBusinessFormNo() : "null",
                    System.currentTimeMillis() - startTime,
                    e.getMessage(), e);
            return ResultWrapper.buildFailure("参数错误: " + e.getMessage());

        } catch (BusinessException e) {
            log.error("【订单出库】业务异常，businessFormNo: {}, 耗时: {}ms, 原因: {}",
                    query != null ? query.getBusinessFormNo() : "null",
                    System.currentTimeMillis() - startTime,
                    e.getMessage(), e);
            return ResultWrapper.buildFailure(e.getMessage());

        } catch (Exception e) {
            log.error("【订单出库】系统异常，businessFormNo: {}, 耗时: {}ms",
                    query != null ? query.getBusinessFormNo() : "null",
                    System.currentTimeMillis() - startTime, e);
            return ResultWrapper.buildFailure("系统异常，请联系管理员"+e.getMessage());
        }
    }

    public void allocateWorkStation(ReqAllotParam allotParam,String workstationCode) {
        // 参数校验
        if (Objects.isNull(allotParam)) {
            throw new BusinessException("分配参数不能为空");
        }
        if (Objects.isNull(allotParam.getOrderId())) {
            throw new BusinessException("订单 ID 不能为空");
        }

        log.info("执行工作站分配，orderId: {}", allotParam.getOrderId());

        // 查询订单
        ReqOrder reqOrder = requisitionReqOrderRepository.findOrderById(allotParam.getOrderId());
        if (Objects.isNull(reqOrder)) {
            throw new BusinessException("未找到指定 ID[{}] 的单据信息", allotParam.getOrderId());
        }
        String originalExtraStr = reqOrder.getExtraStr();
        Map<String, Object> newParams = new HashMap<>();
        newParams.put("workstationCode",workstationCode);
        // 3. 合并逻辑：有值则解析合并，无值则直接使用新参数
        if (StringUtils.isNotBlank(originalExtraStr)) {
            try {
                // 解析原有 JSON 为 Map
                Map originalMap = JsonUtl.format(originalExtraStr, Map.class);

                // 合并新参数（新参数覆盖同名旧参数）
                if (originalMap == null) {
                    originalMap = new HashMap<>();
                }
                originalMap.putAll(newParams);

                // 转回 JSON 字符串
                reqOrder.setExtraStr(JsonUtl.parse(originalMap));
            } catch (Exception e) {
                // 解析失败时降级处理：记录日志 + 使用新参数
                log.warn("extraStr 解析失败，使用新参数覆盖，original: {}", originalExtraStr, e);
                reqOrder.setExtraStr(JsonUtl.parse(newParams));
            }
        } else {
            // 原值为空，直接设置新参数
            reqOrder.setExtraStr(JsonUtl.parse(newParams));
        }

        // 更新订单
        requisitionReqOrderRepository.updateReqOrderRemarkAndWorkStation(reqOrder);

        log.info("工作站分配完成，orderId: {}", allotParam.getOrderId());
    }

    /**
     * 拣选模式 才有订单箱  ,订单箱离开工作站接口
     * @param dto 箱离开请求参数
     * @return 操作结果包装器
     * @see OrderContainerLeaveDto
     */
    @PostMapping("/order-container-leave-station")
    @ResponseBody
    public ResultWrapper<Void> orderContainerLeaveStation(
            @Valid @RequestBody OrderContainerLeaveDto dto) {
        String workstationCode = dto != null ? dto.getWorkstationCode() : null;
        if (StringUtils.isEmpty(workstationCode)) {
            return ResultWrapper.buildFailure("工作站编码不能为空");
        }
        String toPos = dto != null ? dto.getToPos() : null;
        if (StringUtils.isEmpty(toPos)) {
            return ResultWrapper.buildFailure("目标位置不能为空");
        }
        String containerCode = dto != null ? dto.getContainerCode() : null;
        if (StringUtils.isEmpty(containerCode)) {
            return ResultWrapper.buildFailure("容器编号不能为空");
        }
        String houseCode=WmsConfig.DefaultHouseCode;
        // 记录方法开始时间，用于性能监控
        long startTime = System.currentTimeMillis();

        try {
            // ========== 1. 记录请求日志 ==========
            log.info("【箱离开工作站】收到请求 | containerCode={} | workstationCode={} | toPos={} | systemCode={} | deviceCode={}",
                    dto != null ? dto.getContainerCode() : "null",
                    dto != null ? dto.getWorkstationCode() : "null",
                    dto != null ? dto.getToPos() : "null",
                    dto != null ? dto.getSystemCode() : "null",
                    dto != null ? dto.getDeviceCode() : "null");

            //TODO 工作站进行拣选，选择任意一个需求单，扫入订单箱号，不点击拣选完成，点击去车间，页面提示成功，没有做防呆校验
            //订单箱没有发现组盘信息是不允许移动的

            PalletizeFormDto formDto = palletizeFormService.queryByContainerCode(dto.getContainerCode(), Lists.newArrayList(PalletizeFormStatus.Created,
                    PalletizeFormStatus.Executing,PalletizeFormStatus.CreatedAndExecuting));

            if (formDto==null) {
                return ResultWrapper.buildFailure("未获取当订单箱: " + dto.getContainerCode()+"的容器信息,请确认是否拣选完成");
            }

            //需要取消组盘 任务下发到车间
            if(Objects.equals("workshops",toPos)){

                BatchPutAwayCombineDto batchPutAwayCombineDto=new BatchPutAwayCombineDto();
                batchPutAwayCombineDto.setContainerCode(containerCode);
                batchPutAwayCombineDto.setWorkstationCode(workstationCode);
                log.info("【组盘解绑】收到请求，容器编码：{}", containerCode);
                inboundService.combineUnbind(batchPutAwayCombineDto);

                String targetPos =null;
                Map<String, String> sectionDeviceTargetPos = ProjectConfig.SectionDeviceTargetPos;
                if (!ObjectUtils.isEmpty(sectionDeviceTargetPos)) {
                    targetPos = sectionDeviceTargetPos.get(toPos);
                }
                if(StringUtils.isEmpty(targetPos)){
                    throw new com.foreris.eris.common.exception.BusinessException(StringFormatUtl.formatI18n("字典 [SectionDeviceTargetPos] 区域设备对应关系未配置:{}!", toPos));
                }
                pickExtService.transportExecute( houseCode,  containerCode,  workstationCode,  targetPos,null);
                //

                PickAblItemQueryParam queryParam = new PickAblItemQueryParam();
                queryParam.setContainerCode(containerCode);
                List<RequisitionItemContainerCodeExtDto> dtoList = pickExtService.findPickAbleByContainerList(queryParam);
                if(CollectionUtils.isNotEmpty(dtoList)){
                    RequisitionItemContainerCodeExtDto requisitionItemContainerCodeExtDto = dtoList.get(0);
                    reqOperationAppService.OrderStockOut(UUID.fromString(requisitionItemContainerCodeExtDto.getId()));
                }


            }
            //申请入库到立库
            else if(Objects.equals("warehouse",toPos)){
                TaskApplyParam applyParam = new TaskApplyParam.Builder()
                        .setSystemCode("01")
                        .setHouseCode(houseCode)
                        .setDeviceCode(workstationCode)
                        .setContainerCode(containerCode)
                        .setContainerShape(null)
                        .setParameters(null)
                        .build();
                MessageResult result = transJobAppService.applyTransJob(applyParam);
                if (!result.getSuccess()) {
                    return ResultWrapper.buildFailure("入库申请失败: " + result.getMessage());
                }
            }

            // ========== 3. 执行箱离开业务逻辑 ==========
            boolean success = pickExtService.processContainerLeave(dto);

            if (success) {
                // 记录成功日志
                log.info("【箱离开工作站】处理成功 | containerCode={} | 耗时={}ms",
                        dto != null ? dto.getContainerCode() : "null",
                        System.currentTimeMillis() - startTime);
                return ResultWrapper.buildSuccess();
            } else {
                // 记录业务失败日志
                log.warn("【箱离开工作站】处理失败 | containerCode={} | 耗时={}ms",
                        dto != null ? dto.getContainerCode() : "null",
                        System.currentTimeMillis() - startTime);
                return ResultWrapper.buildFailure("箱离开操作失败，请检查参数或联系管理员");
            }

        } catch (IllegalArgumentException e) {
            // 参数校验异常
            log.error("【箱离开工作站】参数异常 | containerCode={} | error={} | 耗时={}ms",
                    dto != null ? dto.getContainerCode() : "null",
                    e.getMessage(),
                    System.currentTimeMillis() - startTime,
                    e);
            return ResultWrapper.buildFailure("参数错误: " + e.getMessage());

        } catch (com.foreris.eris.common.exception.BusinessException e) {
            // 业务异常（如容器状态不允许离开）
            log.error("【箱离开工作站】业务异常 | containerCode={} | error={} | 耗时={}ms",
                    dto != null ? dto.getContainerCode() : "null",
                    e.getMessage(),
                    System.currentTimeMillis() - startTime,
                    e);
            return ResultWrapper.buildFailure(e.getMessage());

        } catch (Exception e) {
            // 系统异常
            log.error("【箱离开工作站】系统异常 | containerCode={} | 耗时={}ms",
                    dto != null ? dto.getContainerCode() : "null",
                    System.currentTimeMillis() - startTime,
                    e);
            return ResultWrapper.buildFailure("系统内部错误，请稍后重试"+e.getMessage());
        }
    }

    /**
     * 根据业务单据号查询订单及未拣选托盘数量
     *
     * @param query 业务查询参数（需包含 businessFormNo）
     * @return 订单基本信息 + 托盘统计（总托盘数、未拣选托盘数、已拣选托盘数）
     */
    @PostMapping("/order-pick-pallet-count")
    public ResultWrapper<RequisitionOrderWithPalletCountDto> orderWithPalletCount(
            @Valid @RequestBody RequisitionOrderExtExQuery query) {

        long startTime = System.currentTimeMillis();
        try {
            // 参数校验
            if (query == null || StringUtils.isEmpty(query.getBusinessFormNo())) {
                log.warn("【查询订单托盘数】参数校验失败: businessFormNo 为空，耗时: {}ms",
                        System.currentTimeMillis() - startTime);
                return ResultWrapper.buildFailure("业务单据号 (businessFormNo) 不能为空");
            }

            log.info("【查询订单托盘数】开始处理，businessFormNo: {}", query.getBusinessFormNo());

            // 核心业务逻辑：获取订单及托盘统计
            RequisitionOrderWithPalletCountDto result = requisitionOrderExtService.getOrderWithPalletCount(query);

            // 空值处理
            if (result == null) {
                log.warn("【查询订单托盘数】未找到订单，businessFormNo: {}, 耗时: {}ms",
                        query.getBusinessFormNo(), System.currentTimeMillis() - startTime);
                return ResultWrapper.buildFailure("未找到业务单据号为 [" + query.getBusinessFormNo() + "] 的出库订单");
            }

            // 记录成功日志（敏感信息脱敏）
            log.info("【查询订单托盘数】成功，订单号: {}, 托盘统计：总数={}, 未拣选={}, 已完成={}, 耗时: {}ms",
                    Optional.ofNullable(result.getFormNo()).orElse("unknown"),
                    result.getPalletCount(),
                    result.getActivePalletCount(),
                    result.getCompletedPalletCount(),
                    System.currentTimeMillis() - startTime);

            return ResultWrapper.buildSuccess(result);

        } catch (IllegalArgumentException e) {
            log.error("【查询订单托盘数】参数异常，businessFormNo: {}, 耗时: {}ms, 原因: {}",
                    query != null ? query.getBusinessFormNo() : "null",
                    System.currentTimeMillis() - startTime,
                    e.getMessage());
            return ResultWrapper.buildFailure("参数错误: " + e.getMessage());

        } catch (Exception e) {
            log.error("【查询订单托盘数】系统异常，businessFormNo: {}, 耗时: {}ms",
                    query != null ? query.getBusinessFormNo() : "null",
                    System.currentTimeMillis() - startTime,
                    e);
            return ResultWrapper.buildFailure("系统异常，请联系管理员"+e.getMessage());
        }
    }



    /**
     * 查询当前拣选信息
     */
    @PostMapping("/find-pick-info")
    public ResultWrapper<WorkstationPickInfoVo> findPickInfo(@RequestBody WorkstationPickInfoDto workstationPickInfoDto) {
        try {
            if (workstationPickInfoDto != null) {
                workstationPickInfoDto.checkData();
            }
            WorkstationPickInfoVo pickInfo = pickExtService.findPickInfo(workstationPickInfoDto);
            if (pickInfo != null) {
                // 🔒 修复：使用 CollectionUtils.isEmpty 替代 .isEmpty()，避免 List 为 null 时 NPE
                List<PickOrderInfoVo> pickOrderInfoVoList = pickInfo.getPickOrderInfoVoList();
                if (CollectionUtils.isEmpty(pickOrderInfoVoList)) {
                    String containerCode = workstationPickInfoDto != null ?
                            Optional.ofNullable(workstationPickInfoDto.getContainerCode()).orElse("unknown") : "unknown";
                    return ResultWrapper.buildFailure("容器:" + containerCode + "未发现拣选信息");
                }
            }
            return ResultWrapper.buildSuccess(pickInfo);
        } catch (Exception e) {
            return ResultWrapper.buildFailure("查询当前工作站拣选信息失败: " + e.getMessage());
        }
    }

    /**
     * 完成当前拣选信息
     */
    @PostMapping("/complete-pick")
    public ResultWrapper<PickOrderFinishVo> completePick(@RequestBody WorkstationCompletePickDto dto) {
        try {
            // 1. 参数校验
            if (dto != null) {
                dto.checkData();
            }

            // 2. 执行拣选完成（核心业务）
            PickOrderFinishVo resultVo = pickExtService.completePick(dto);
            log.info("拣选订单完成成功 | 容器: {}, 工作站: {}",
                    dto != null ? Optional.ofNullable(dto.getOrderContainerCode()).orElse("unknown") : "unknown",
                    dto != null ? Optional.ofNullable(dto.getWorkstationCode()).orElse("unknown") : "unknown");


            return ResultWrapper.buildSuccess(resultVo);
        } catch (IllegalArgumentException | BusinessException e) {
            // 明确业务校验异常
            log.warn("参数校验失败 | 原因: {}", e.getMessage());
            return ResultWrapper.buildFailure("操作失败: " + e.getMessage());
        } catch (Exception e) {
            // 系统级异常（含拣选服务异常）
            log.error("完成拣选流程异常 | 容器: {}, 异常: ",
                    dto != null ? Optional.ofNullable(dto.getOrderContainerCode()).orElse("unknown") : "unknown", e);
            return ResultWrapper.buildFailure("系统繁忙，请稍后重试"+e.getMessage());
        }
    }


    @PostMapping("/order-container-exit_out")
    @ResponseBody
    public ResultWrapper<Void> orderContainerExitOut(
            @RequestBody RequisitionOrderExtExQuery query) {
        String businessFormNo = query != null ? query.getBusinessFormNo() : null;
        // 🔒 修复：日志中使用 Optional 避免 businessFormNo 为 null 时输出异常
        log.debug("【订单容器出库】请求接收，businessFormNo: {}",
                Optional.ofNullable(businessFormNo).orElse("null"));

        return ResultWrapper.buildSuccess(null);
    }


    /**
     * 定检清单-发货单拣选容器列表
     */
    @PostMapping("/find-orders-pick-container-list")
    @ResponseBody
    public ResultWrapper<List<RequisitionItemContainerCodeExtDto>> pickItemContainerList(
            @RequestBody RequisitionOrderExtExQuery query) {

        String businessFormNo = query.getBusinessFormNo();
        if (StringUtils.isEmpty(businessFormNo)) {
            return ResultWrapper.buildFailure("单据编号不能为空");
        }
        String workstationCode = query.getWorkstationCode();
        if (StringUtils.isEmpty(workstationCode)) {
            return ResultWrapper.buildFailure("工作站编码不能为空");
        }
        // 2. 规范日志记录 (使用 info 级别以便生产环境追踪)
        log.info("【订单容器出库】请求接收，businessFormNo: {}", businessFormNo);

        // 3. 参数转换
        PickAblItemQueryParam queryParam = new PickAblItemQueryParam();
        queryParam.setBizFormNo(businessFormNo);

        List<RequisitionItemContainerCodeExtDto> dtoList = pickExtService.findPickAbleByContainerList(queryParam);

        log.info("【订单容器出库】查询完成，businessFormNo: {}, 结果数量：{}", businessFormNo, dtoList.size());

        // 6. 返回结果
        return ResultWrapper.buildSuccess(dtoList);
    }

    /**
     * 定检清单-发货单拣选容器分页
     */
    @GetMapping("/find-orders-pick-container-page")
    public PageResult<RequisitionItemContainerCodeExtDto> pickItemContainerPage(@RequestBody RequisitionItemExtExQuery query) {
        String businessFormNo = query.getBusinessFormNo();
        if (StringUtils.isEmpty(businessFormNo)) {
            return ResultFactory.getErrorPage("单据编号不能为空 ");
        }
        String workstationCode = query.getWorkstationCode();
        if (StringUtils.isEmpty(workstationCode)) {
            return ResultFactory.getErrorPage("工作站编码不能为空 ");
        }
        log.info("【订单容器出库】请求接收，businessFormNo: {}", businessFormNo);

        // 3. 参数转换
        PickAblItemQueryParam queryParam = new PickAblItemQueryParam();
        queryParam.setBizFormNo(businessFormNo);

        // 6. 返回结果
        return pickExtService.findPickAbleByContainerPage(queryParam);
    }

    /**
     * 定检清单-拣选容器出库
     */
    @PostMapping("/pick-item-container-exit_out")
    @ResponseBody
    public ResultWrapper<Void> pickItemContainerExitOut(
            @RequestBody RequisitionItemContainerCodeListExtDto query) {
        String workstationCode = query != null ? query.getWorkstationCode() : null;
        if (StringUtils.isEmpty(workstationCode)) {
            return ResultWrapper.buildFailure("工作站编码不能为空");
        }
        pickExtService.pickItemContainerExitOut(query);
        return ResultWrapper.buildSuccess(null);
    }

}
