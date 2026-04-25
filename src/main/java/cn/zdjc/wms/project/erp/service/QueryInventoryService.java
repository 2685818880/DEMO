package cn.zdjc.wms.project.erp.service;

import cn.zdjc.wms.project.erp.dto.ResultWrapper;
import cn.zdjc.wms.project.erp.dto.storagematerial.ErpStorageMaterialDto;
import cn.zdjc.wms.project.erp.dto.storagematerial.InventoryInquiryRequestDto;

import java.util.List;


public interface QueryInventoryService {
    ResultWrapper<List<InventoryInquiryRequestDto>> handleQueryInventoryMaster(ErpStorageMaterialDto dto);
}