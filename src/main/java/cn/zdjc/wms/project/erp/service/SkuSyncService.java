package cn.zdjc.wms.project.erp.service;

import cn.zdjc.wms.project.erp.dto.sku.SkuMainDataDto;


public interface SkuSyncService {
    void handleSkuMaster(SkuMainDataDto dto);
}