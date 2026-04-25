package cn.zdjc.wms.project.erp.service;
import cn.zdjc.wms.project.erp.dto.ResultWrapper;
import cn.zdjc.wms.project.erp.dto.asn.ErpAsnDto;

public interface ReceiptSyncService {

    ResultWrapper<Void> handleReceiptMaster(ErpAsnDto  dto) ;

}