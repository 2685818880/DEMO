package cn.zdjc.wms.project.erp.service.impl;

import cn.zdjc.warehouse.inventory.application.query.SkuSummaryExQuery;
import cn.zdjc.warehouse.inventory.application.service.SkuSummaryAppService;
import cn.zdjc.warehouse.inventory.domain.dto.StorageMaterialDto;
import cn.zdjc.wms.project.erp.dto.ResultWrapper;
import cn.zdjc.wms.project.erp.dto.storagematerial.ErpStorageMaterialDto;
import cn.zdjc.wms.project.erp.dto.storagematerial.InventoryInquiryRequestDto;
import cn.zdjc.wms.project.erp.service.QueryInventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QueryInventoryServiceImpl implements QueryInventoryService {

    @Resource
    private SkuSummaryAppService skuSummaryAppService;

    @Override
    public ResultWrapper<List<InventoryInquiryRequestDto>> handleQueryInventoryMaster(ErpStorageMaterialDto dto) {
        // 构建查询条件
        SkuSummaryExQuery query = new SkuSummaryExQuery();
        query.setSku_code(dto.getSku_code());
        query.setHouse_code(dto.getHouse_code()); // 建议加上库房查询条件
        // 注意：不要用 batch_no 去 setSku_name！
        // 如果需要按批号查询，应使用专门的批号字段（假设 StorageMaterialDto 支持）
        // query.setBatch_no(dto.getBatch_no());

        // 执行查询
        List<StorageMaterialDto> storageMaterialDtos = skuSummaryAppService.querySku(query);

        // 转换为响应 DTO 列表
        List<InventoryInquiryRequestDto> result = storageMaterialDtos.stream()
                .map(this::convertToInventoryInquiryDto)
                .collect(Collectors.toList());

        return ResultWrapper.buildSuccess(result);
    }

    /**
     * 将 StorageMaterialDto 转换为 InventoryInquiryRequestDto
     */
    private InventoryInquiryRequestDto convertToInventoryInquiryDto(StorageMaterialDto source) {
        InventoryInquiryRequestDto target = new InventoryInquiryRequestDto();

        target.setHouseCode(source.getHouse_code());
        target.setSkuCode(source.getSku_code());
        target.setSkuName(source.getSku_name());
        target.setBatchNo(source.getBatch_no());
        target.setQty(source.getPrimary_qty() != null ? source.getPrimary_qty().intValue() : 0);
//        target.setQualityStatus(source.getQuality_status() != null ?
//                source.getQuality_status().getCode() : "W"); // 假设 QualityStatus 有 getCode()
        target.setUnit(source.getPrimary_unit());

        // 日期转换：StorageMaterialDto 中是 String，需转为 LocalDate
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (source.getProduct_date() != null && !source.getProduct_date().isEmpty()) {
            try {
                target.setProduceDate(LocalDate.parse(source.getProduct_date().split(" ")[0], formatter));
            } catch (Exception e) {
                log.warn("生产日期格式异常: {}", source.getProduct_date());
            }
        }
        if (source.getExpire_date() != null && !source.getExpire_date().isEmpty()) {
            try {
                target.setValidity(LocalDate.parse(source.getExpire_date().split(" ")[0], formatter));
            } catch (Exception e) {
                log.warn("有效期格式异常: {}", source.getExpire_date());
            }
        }

        target.setOrganization(source.getFactory_code()); // 或根据业务映射
        target.setCompany(source.getOwner_name());        // 或根据业务映射
        target.setArea(source.getInventory_location());   // 库存地点作为 AREA

        return target;
    }
}