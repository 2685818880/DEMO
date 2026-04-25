package cn.zdjc.wms.project.erp.service;

import cn.zdjc.wms.project.erp.dto.ResultWrapper;
import cn.zdjc.wms.project.erp.dto.requisition.ErpRequisitionDto;

import java.util.UUID;

public interface RequisitionConfirmSyncService {
    ResultWrapper<Void> handleRequisitionConfirmSyncMaster(UUID id) ;

}