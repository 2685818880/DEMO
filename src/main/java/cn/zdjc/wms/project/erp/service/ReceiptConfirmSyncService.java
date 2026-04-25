package cn.zdjc.wms.project.erp.service;
import cn.zdjc.wms.project.erp.dto.ResultWrapper;
import cn.zdjc.wms.project.erp.dto.asn.ErpAsnDto;

import java.util.UUID;

public interface ReceiptConfirmSyncService {

    ResultWrapper<Void> handleReceiptConfirmSyncMaster(UUID id) ;

}