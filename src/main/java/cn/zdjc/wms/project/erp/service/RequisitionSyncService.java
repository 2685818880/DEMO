package cn.zdjc.wms.project.erp.service;

import cn.zdjc.wms.project.erp.dto.ResultWrapper;
import cn.zdjc.wms.project.erp.dto.requisition.ErpRequisitionDto;

public interface RequisitionSyncService {
    ResultWrapper<Void> handleRequisitionMaster(ErpRequisitionDto dto) ;

}