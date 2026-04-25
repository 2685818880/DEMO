package cn.zdjc.wms.project.erp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.zdjc.warehouse.material.application.service.SkuAppService;
import cn.zdjc.warehouse.material.domain.dto.SkuDto;
import cn.zdjc.wms.definition.application.service.AsnAppService;
import cn.zdjc.wms.definition.application.vo.AsnRequireApiVo;
import cn.zdjc.wms.definition.domain.dto.AsnDto;
import cn.zdjc.wms.definition.domain.dto.AsnItemDto;
import cn.zdjc.wms.definition.infrastructure.PrimaryGenerator;
import cn.zdjc.wms.definition.infrastructure.enums.FormPrefix;
import cn.zdjc.wms.project.erp.dto.ResultWrapper;
import cn.zdjc.wms.project.erp.dto.asn.ErpAsnDto;
import cn.zdjc.wms.project.erp.dto.asn.ErpAsnItemDto;
import cn.zdjc.wms.project.erp.service.ReceiptSyncService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 *收货单同步服务实现
 */
@Slf4j
@Service
public class ReceiptSyncServiceImpl implements ReceiptSyncService {

    @Resource
    private SkuAppService skuAppService;

    @Resource
    private AsnAppService asnAppService;

    @Override
    public ResultWrapper<Void> handleReceiptMaster(ErpAsnDto request) {
        log.info("开始处理 SAP 收货单，外部单号：{}", request.getBusNo());

        validateRequest(request);
        String internalNo = generateInternalFormNo();
        AsnDto asnDto = AsnDto.builder( request.getHouseCode(), "Common", request.getBusNo()
                , request.getBusType(), request.getSupplierCode(), request.getSupplierName());
        List<AsnItemDto> itemList = buildAsnItems(request, asnDto);

        if (itemList.isEmpty()) {
            String msg = "收货单 " + request.getBusNo() + " 无有效明细";
            log.warn(msg);
            throw new IllegalArgumentException(msg);
        }

        try {
            AsnRequireApiVo vo = new AsnRequireApiVo();
            vo.setAsnDto(asnDto);
            vo.setItemList(itemList);
            asnAppService.createAsn(vo);

            log.info("SAP 收货单 {} 同步成功，WMS 单号：{}", request.getBusNo(), internalNo);
            return ResultWrapper.buildSuccess(null);
        } catch (Exception e) {
            log.error("创建 WMS 入库单失败，外部单号：{}", request.getBusNo(), e);
            throw new IllegalStateException("入库单创建失败: " + e.getMessage(), e);
        }
    }

    // ---------------- 私有方法：职责分离 ----------------

    private void validateRequest(ErpAsnDto request) {
        if (StringUtils.isBlank(request.getBusNo())) {
            throw new IllegalArgumentException("BUS_NO 不能为空");
        }
        if (StringUtils.isBlank(request.getHouseCode())) {
            throw new IllegalArgumentException("仓库代码（HOUSE_CODE）不能为空");
        }
        if (CollUtil.isEmpty(request.getItems())) {
            throw new IllegalArgumentException("收货明细（ITEMS）不能为空");
        }
    }

    private String generateInternalFormNo() {
        return PrimaryGenerator.getInstance().nextFormNo(FormPrefix.ASN.name());
    }


    private List<AsnItemDto> buildAsnItems(ErpAsnDto request, AsnDto asnDto) {
        return request.getItems().stream()
                .peek(this::validateItem)
                .map(item -> convertToAsnItem(item, asnDto))
                .collect(Collectors.toList());
    }

    private void validateItem(ErpAsnItemDto item) {
        if (StringUtils.isBlank(item.getSkuCode())) {
            throw new IllegalArgumentException("明细行 SKU_CODE 不能为空");
        }
        if (item.getPrimaryQty() == null || item.getPrimaryQty().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("收料数量必须大于 0");
        }
    }

    private AsnItemDto convertToAsnItem(ErpAsnItemDto item, AsnDto asnDto) {
        SkuDto skuDto = fetchSkuOrThrow(item.getSkuCode(), asnDto.getBusiness_form_no());

        double qty = item.getPrimaryQty().doubleValue();
        AsnItemDto asnItem = AsnItemDto.builder(asnDto,
                item.getItemNo(),
                skuDto.getCategory_code(),
                skuDto.getCategory_name(),
                skuDto.getSku_code(),
                skuDto.getSku_name(),
                item.getBatchNo(),
                "", "",
                qty,
                item.getPrimaryUnit(),
                qty,
                item.getPrimaryUnit(),
                "",
                false
        );


        // 扩展字段：采购订单号（假设 producerBatchNo 存储订单号）
        if (StringUtils.isNotBlank(item.getProducerBatchNo())) {
            Map<String, Object> extra = new HashMap<>();
            extra.put("FORDERBILLNO", item.getProducerBatchNo());
            asnItem.setExtra(extra);
        }

        return asnItem;
    }

    private SkuDto fetchSkuOrThrow(String skuCode, String busNo) {
        SkuDto sku = skuAppService.getSkuByCode(skuCode);
        if (sku == null) {
            String msg = String.format("物料 [%s] 未在 WMS 中注册，单号：%s", skuCode, busNo);
            log.warn(msg);
            throw new IllegalArgumentException(msg);
        }
        return sku;
    }
}