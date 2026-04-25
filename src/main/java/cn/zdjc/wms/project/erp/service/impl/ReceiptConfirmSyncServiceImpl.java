package cn.zdjc.wms.project.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.zdjc.wms.common.permission.infrastructure.JsonUtils;
import cn.zdjc.wms.common.utils.StringUtils;
import cn.zdjc.wms.definition.application.query.AsnItemExQuery;
import cn.zdjc.wms.definition.domain.dto.AsnDto;
import cn.zdjc.wms.definition.domain.dto.AsnItemDto;
import cn.zdjc.wms.definition.domain.service.AsnItemService;
import cn.zdjc.wms.definition.domain.service.AsnService;
import cn.zdjc.wms.outbound.requisition.application.service.ReqQueryAppService;
import cn.zdjc.wms.project.ProjectConfig;
import cn.zdjc.wms.project.erp.dto.ResultWrapper;
import cn.zdjc.wms.project.erp.dto.asnconfirm.InboundOrderDto;
import cn.zdjc.wms.project.erp.dto.asnconfirm.InboundOrderItemDto;
import cn.zdjc.wms.project.erp.service.ReceiptConfirmSyncService;
import com.foeris.y.common.exception.BusinessException;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReceiptConfirmSyncServiceImpl implements ReceiptConfirmSyncService {

    @Resource
    private AsnService asnService;

    @Autowired
    private AsnItemService asnItemService;


    @Override
    public ResultWrapper<Void> handleReceiptConfirmSyncMaster(UUID id) {
        log.info("开始同步收料确认至 ERP，ASN ID: {}", id);

        // 1. 查询 ASN 主单
        AsnDto asnDto = asnService.getAsnById(id);
        if (asnDto == null) {
            log.warn("ASN 主单不存在，ID: {}", id);
            throw new BusinessException("ASN 主单不存在");
        }
        log.debug("ASN 主单信息: 单号={}, 类型={}", asnDto.getBusiness_form_no(), asnDto.getBusiness_form_type());

        // 2. 查询 ASN 明细
        AsnItemExQuery asnItemExQuery = new AsnItemExQuery();
        asnItemExQuery.setAsn_id(asnDto.getId());
        List<AsnItemDto> asnItemDtoList = asnItemService.queryList(asnItemExQuery);
        if (CollectionUtils.isEmpty(asnItemDtoList)) {
            log.warn("ASN 明细为空，ASN ID: {}", id);
            throw new BusinessException("ASN 明细为空，无法同步");
        }

        // 3. 检查是否全部收料完成
        List<AsnItemDto> uncompletedItems = asnItemDtoList.stream()
                .filter(item -> !ObjectUtil.equal(item.getPrimary_qty(), item.getConfirm_qty()))
                .collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(uncompletedItems)) {
            String uncompletedSkus = uncompletedItems.stream()
                    .map(AsnItemDto::getSku_code)
                    .collect(Collectors.joining(", "));
            log.warn("存在未收料完成的明细，ASN ID: {}，未完成 SKU: {}", id, uncompletedSkus);
            throw new BusinessException("存在未收料完成的明细，过账失败！");
        }

        // 4. 构建 ERP 请求参数
        InboundOrderDto parameter = new InboundOrderDto();
        parameter.setBusNo(asnDto.getBusiness_form_no());
        parameter.setBusType(asnDto.getBusiness_form_type());

        List<InboundOrderItemDto> itemList = Lists.newArrayList();
        List<AsnItemDto> upSubmitQtyList = Lists.newArrayList();

        for (AsnItemDto item : asnItemDtoList) {
            Double confirmQty = item.getConfirm_qty();
            InboundOrderItemDto itemDto = new InboundOrderItemDto();
            itemDto.setItemNo(item.getItem_no());
            itemDto.setSkuCode(item.getSku_code());
            itemDto.setBatchNo(item.getBatch_no());
            itemDto.setActualQty(String.valueOf(confirmQty));
            itemList.add(itemDto);

            // 准备更新 submit_qty
            item.setSubmit_qty(item.getConfirm_qty());
            upSubmitQtyList.add(item);
        }
        parameter.setItems(itemList);

        // 5. 序列化请求体
        String data = JsonUtils.toJson(parameter);
        log.debug("ERP 同步请求参数: {}", data);



        // 7. 发送 HTTP 请求
        HttpResponse response;
        try {
            log.info("正在向 ERP 发送收料确认请求，URL: {}", ProjectConfig.ERP_Url);
            response = HttpRequest.post(ProjectConfig.ERP_Url)
                    .body(data)
                    .header("Content-Type", "application/json")
                    .timeout(10000) // 设置超时
                    .execute();
        } catch (Exception e) {
            log.error("调用 ERP 接口异常，ASN ID: {}", id, e);
            throw new BusinessException("同步 ERP 时网络异常：" + e.getMessage());
        }

        String responseBody = response.body();
        log.debug("ERP 响应状态: {}, 响应体: {}", response.getStatus(), responseBody);

        // 8. 解析响应
        if (StringUtils.isBlank(responseBody)) {
            log.error("ERP 返回空响应，ASN ID: {}", id);
            throw new BusinessException("ERP 返回空响应");
        }

        Map<String, Object> resultMap;
        try {
            resultMap = JsonUtils.toMap(responseBody);
        } catch (Exception e) {
            log.error("ERP 响应 JSON 解析失败，ASN ID: {}, 响应: {}", id, responseBody, e);
            throw new BusinessException("ERP 响应格式错误");
        }

        Boolean success = (Boolean) resultMap.get("success");
        if (Boolean.TRUE.equals(success)) {
            // 9. 更新 submit_qty
            asnItemService.addOrUpdate(upSubmitQtyList);
            log.info("ERP 同步成功，ASN ID: {}，单号: {}", id, asnDto.getBusiness_form_no());
            return ResultWrapper.buildSuccess(null);
        } else {
            Object errorMsg = resultMap.get("data");
            String message = errorMsg != null ? errorMsg.toString() : "未知错误";
            log.error("ERP 同步失败，ASN ID: {}，错误信息: {}", id, message);
            throw new BusinessException("ERP 同步失败：" + message);
        }
    }
}