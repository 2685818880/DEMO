package cn.zdjc.wms.project.erp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.zdjc.warehouse.inventory.infrastructure.enums.QualityStatus;
import cn.zdjc.warehouse.material.application.service.SkuAppService;
import cn.zdjc.warehouse.material.domain.dto.SkuDto;
import cn.zdjc.wms.definition.infrastructure.PrimaryGenerator;
import cn.zdjc.wms.definition.infrastructure.enums.FormPrefix;
import cn.zdjc.wms.outbound.requisition.application.params.ReqItemRegisterParam;
import cn.zdjc.wms.outbound.requisition.application.params.ReqOrderRegisterParam;
import cn.zdjc.wms.outbound.requisition.application.service.ReqOperationAppService;
import cn.zdjc.wms.outbound.requisition.application.service.ReqQueryAppService;
import cn.zdjc.wms.outbound.requisition.interfaces.web.dto.ReqOrderDto;
import cn.zdjc.wms.project.erp.dto.ResultWrapper;
import cn.zdjc.wms.project.erp.dto.requisition.ErpRequisitionDto;
import cn.zdjc.wms.project.erp.dto.requisition.ErpRequisitionItemDto;
import cn.zdjc.wms.project.erp.service.RequisitionSyncService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


/**
 *  发货单（要货单）同步服务实现
 */
@Slf4j
@Service
public class RequisitionSyncServiceImpl implements RequisitionSyncService {

    @Resource
    private SkuAppService skuAppService;

    @Resource
    private ReqOperationAppService reqOperationAppService;

    @Resource
    private ReqQueryAppService reqQueryAppService;


    @Override
    public ResultWrapper<Void> handleRequisitionMaster(ErpRequisitionDto request) {
        String businessFormNo = Optional.ofNullable(request)
                .map(ErpRequisitionDto::getBusinessFormNo)
                .orElse("UNKNOWN");


        try {
            log.info("【发货单同步】开始处理，外部单号：{}", businessFormNo);

            ReqOrderDto orderByBizOrder = reqQueryAppService.findOrderByBizOrder(businessFormNo);
            if(orderByBizOrder!=null){
                throw new IllegalArgumentException("入库单创建失败: 订单号["+businessFormNo+"]," +
                        "订单类型["+orderByBizOrder.getBusiness_form_type()+"]收料单已存在,无法重复创建");
            }

            // 1. 请求非空校验
            if (request == null) {
                String errorMsg = "请求参数不能为空";
                log.warn("【发货单同步】{}", errorMsg);
                return ResultWrapper.buildFailure("INVALID_REQUEST", errorMsg);
            }

            // 2. 明细非空校验
            List<ErpRequisitionItemDto> items = request.getItems();
            if (CollUtil.isEmpty(items)) {
                String errorMsg = " 发货单明细为空";
                log.warn("【发货单同步】{}，外部单号：{}", errorMsg, businessFormNo);
                return ResultWrapper.buildFailure("EMPTY_ITEMS", errorMsg);
            }

            // 3. 构建 WMS 注册参数
            ReqOrderRegisterParam registerParam = buildRegisterParam(request);

            // 4. 调用 WMS 服务注册订单
            reqOperationAppService.registerOrder(registerParam);

            log.info("【发货单同步】处理成功，外部单号：{}", businessFormNo);
            return ResultWrapper.buildSuccess(null);

        } catch (Exception e) {
            String errorMsg = "处理  发货单失败: " + e.getMessage();
            log.error("【发货单同步】外部单号：{} - {}", businessFormNo, errorMsg, e);
            return ResultWrapper.buildFailure("SYSTEM_ERROR", errorMsg);
        }
    }

    // ---------------- 私有方法：构建注册参数 ----------------

    private ReqOrderRegisterParam buildRegisterParam(ErpRequisitionDto request) {
        // 构建主单
        ReqOrderRegisterParam param = new ReqOrderRegisterParam();
        param.setOrderNo(PrimaryGenerator.getInstance().nextFormNo(FormPrefix.RO.name()));
        param.setFormType("simpleOut");
        param.setBizFormNo(request.getBusinessFormNo());
        param.setBizFormType(request.getBusinessFormType());
        param.setOwnerCode(""); //  未提供货主，可设为空或默认值
        param.setOwnerName(request.getCustomName()); // 假设 customName 作为货主名
        param.setCustomCode(request.getCustomCode());
        param.setCustomName(request.getCustomName());
//        param.setRemark(request.getCreateDate());

        // 构建明细
        List<ReqItemRegisterParam> itemParams = request.getItems().stream()
                .map(item -> convertItem(item, request.getBusinessFormNo()))
                .collect(Collectors.toList());

        param.setItemParams(itemParams);
        return param;
    }

    private ReqItemRegisterParam convertItem(ErpRequisitionItemDto item, String externalOrderNo) {
        String skuCode = item.getSkuCode();
        if (StringUtils.isBlank(skuCode)) {
            throw new IllegalArgumentException("物料编码不能为空!");
        }

        SkuDto skuDto = skuAppService.getSkuByCode(skuCode);
        if (skuDto == null) {
            throw new IllegalArgumentException("sku[" + skuCode + "]不存在");
        }

        BigDecimal qty = item.getQty();
        // 在 convertItem 中
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("数量必须大于 0");
        }
        double doubleQty = qty.doubleValue();

        ReqItemRegisterParam param = new ReqItemRegisterParam();
        param.setSkuCode(item.getSkuCode());
        param.setSkuName(skuDto.getSku_name());
        param.setBatchNo(item.getBatchNo());
        param.setQualityStatus(QualityStatus.Q);
        param.setPrimaryQty(doubleQty);
        param.setPrimaryUnit(item.getUnit());
        param.setBizItemNo(item.getItemNo());
        param.setHouseCode(item.getHouseCode());
        param.setSourceArea(item.getSourceArea());
        param.setTargetArea(item.getTargetArea());
        param.setRemark("");

        // 扩展字段
//        String orderNo = item.getOrganization();
//        if (StringUtils.isNotBlank(orderNo)) {
//            param.setExtra(Map.of("FORDERNO", orderNo));
//        }

        return param;
    }

}