package cn.zdjc.wms.project.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.zdjc.wms.common.permission.infrastructure.JsonUtils;
import cn.zdjc.wms.common.utils.StringUtils;
import cn.zdjc.wms.outbound.requisition.application.service.ReqOperationAppService;
import cn.zdjc.wms.outbound.requisition.application.service.ReqQueryAppService;
import cn.zdjc.wms.outbound.requisition.domain.repository.params.ReqItemQueryParam;
import cn.zdjc.wms.outbound.requisition.interfaces.web.dto.ReqItemDto;
import cn.zdjc.wms.outbound.requisition.interfaces.web.dto.ReqOrderDto;
import cn.zdjc.wms.project.ProjectConfig;
import cn.zdjc.wms.project.erp.dto.ResultWrapper;
import cn.zdjc.wms.project.erp.dto.requistionconfirm.OutboundOrderDto;
import cn.zdjc.wms.project.erp.dto.requistionconfirm.OutboundOrderItemDto;
import cn.zdjc.wms.project.erp.service.RequisitionConfirmSyncService;
import com.foeris.y.common.exception.BusinessException;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 发货单（要货单）同步服务实现
 */
@Slf4j
@Service
public class RequisitionConfirmSyncServiceImpl implements RequisitionConfirmSyncService {

    @Resource
    private ReqQueryAppService reqQueryAppService;

    @Resource
    private ReqOperationAppService reqOperationAppService;

    @Override
    public ResultWrapper<Void> handleRequisitionConfirmSyncMaster(UUID id) {
        log.info("【ERP同步】开始同步要货单至 ERP，要货单 ID: {}", id);

        // 1. 查询要货单主单
        ReqOrderDto reqOrderDto = reqQueryAppService.findOrderById(id);
        if (reqOrderDto == null) {
            log.warn("【ERP同步】要货单不存在，ID: {}", id);
            throw new BusinessException("要货单不存在");
        }

        // 2. 状态校验：仅允许已确认/已完成的单据同步
//        if (!ReqOrderStatus.CONFIRMED.getCode().equals(reqOrderDto.getStatus())
//                && !ReqOrderStatus.COMPLETED.getCode().equals(reqOrderDto.getStatus())) {
//            log.warn("【ERP同步】要货单状态不可同步，ID: {}，当前状态: {}", id, reqOrderDto.getStatus());
//            throw new BusinessException("仅已确认或已完成的要货单可同步至 ERP");
//        }

        // 4. 校验主单关键字段
        if (StringUtils.isBlank(reqOrderDto.getBusiness_form_type())) {
            log.error("【ERP同步】要货单缺少业务单据类型，ID: {}", id);
            throw new BusinessException("要货单缺少业务类型，无法同步");
        }
        if (StringUtils.isBlank(reqOrderDto.getCustom_code()) || StringUtils.isBlank(reqOrderDto.getCustom_name())) {
            log.error("【ERP同步】要货单客户信息不完整，ID: {}", id);
            throw new BusinessException("客户信息缺失，无法同步");
        }

        // 5. 查询要货单明细
        ReqItemQueryParam queryParam = new ReqItemQueryParam();
        queryParam.setFormId(reqOrderDto.getId());
        List<ReqItemDto> reqItemDtos = reqQueryAppService.findItemByParam(queryParam);
        if (CollectionUtil.isEmpty(reqItemDtos)) {
            log.warn("【ERP同步】要货单明细为空，ID: {}", id);
            throw new BusinessException("要货单无明细，无法同步");
        }

        // 6. 校验明细关键字段 & 构建 ERP DTO
        OutboundOrderDto outboundOrderDto = buildOutboundOrderDto(reqOrderDto, reqItemDtos);

        // 7. 序列化请求体
        String data = JsonUtils.toJson(outboundOrderDto);
        log.debug("【ERP同步】请求参数: {}", data);

        // 8. 调用 ERP 接口
        String url = ProjectConfig.ERP_Url + "DwtSolution.webapi.IssueWebApiService.ExecuteDataSetTest.common.kdsvc";
        HttpResponse response;
        try {
            log.info("【ERP同步】调用 ERP 接口: {}", url);
            response = HttpRequest.post(url)
                    .body(data)
                    .header("Content-Type", "application/json")
                    .timeout(10000)
                    .execute();
        } catch (Exception e) {
            log.error("【ERP同步】HTTP 调用异常，要货单 ID: {}", id, e);
            throw new BusinessException("同步 ERP 失败：" + e.getMessage());
        }

        String responseBody = response.body();
        log.debug("【ERP同步】ERP 响应 (状态码={}): {}", response.getStatus(), responseBody);

        if (StrUtil.isBlank(responseBody)) {
            log.error("【ERP同步】ERP 返回空响应，要货单 ID: {}", id);
            throw new BusinessException("ERP 返回空响应");
        }

        // 9. 解析响应
        Map<String, Object> result;
        try {
            result = JsonUtils.toMap(responseBody);
        } catch (Exception e) {
            log.error("【ERP同步】ERP 响应 JSON 解析失败，ID: {}，响应: {}", id, responseBody, e);
            throw new BusinessException("ERP 响应格式错误");
        }

        Boolean success = (Boolean) result.get("success");
        if (Boolean.TRUE.equals(success)) {
            // 10. 标记为已提交（或更新状态）
            reqOperationAppService.submitOrder(id);
            log.info("【ERP同步】成功！要货单 ID: {}，ERP单号: {}", id, reqOrderDto.getBusiness_form_no());
            return ResultWrapper.buildSuccess(null);
        } else {
            String errorMsg = Optional.ofNullable(result.get("data"))
                    .map(Object::toString)
                    .orElse("未知错误");
            log.error("【ERP同步】ERP 业务失败，ID: {}，错误: {}", id, errorMsg);
            throw new BusinessException("ERP 同步失败：" + errorMsg);
        }
    }

    // --- 私有方法：构建 DTO，提升可读性 ---
    private OutboundOrderDto buildOutboundOrderDto(ReqOrderDto order, List<ReqItemDto> items) {
        OutboundOrderDto dto = new OutboundOrderDto();
        dto.setBusinessFormNo(order.getBusiness_form_no()); // 注意：此处应为 WMS 生成的单号（若为空需提前生成）
        dto.setBusinessFormType(order.getBusiness_form_type());
        dto.setCustomCode(order.getCustom_code());
        dto.setCustomName(order.getCustom_name());
        dto.setAuditor("system");

        List<OutboundOrderItemDto> itemDtos = Lists.newArrayList();
        for (ReqItemDto item : items) {
            // 关键字段校验
            if (StringUtils.isBlank(item.getSku_code())) {
                throw new BusinessException("明细 SKU 编码缺失，行项目编号: " + item.getItem_no());
            }
            Double confirmQty = item.getConfirm_qty();
            if (confirmQty == null || confirmQty <= 0) {
                throw new BusinessException("明细确认数量无效，SKU: " + item.getSku_code());
            }

            OutboundOrderItemDto itemDto = new OutboundOrderItemDto();
            itemDto.setBusinessItemNo(item.getItem_no());
            itemDto.setSkuCode(item.getSku_code());
            itemDto.setBatchNo(item.getBatch_no());
            itemDto.setQty(String.valueOf(item.getConfirm_qty())); // 确保转为字符串
            itemDto.setUnit(item.getPrimary_unit());
            itemDto.setHouseCode(item.getHouse_code());
            // 扩展字段可按需补充
            itemDtos.add(itemDto);
        }
        dto.setDatas(itemDtos);
        return dto;
    }
}