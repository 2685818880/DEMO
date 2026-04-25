package cn.zdjc.wms.project.erp.control;

import cn.zdjc.wms.project.common.constant.ProjectHttpConstant;
import cn.zdjc.wms.project.erp.dto.ResultWrapper;
import cn.zdjc.wms.project.erp.dto.asn.ErpAsnDto;
import cn.zdjc.wms.project.erp.dto.requisition.ErpRequisitionDto;
import cn.zdjc.wms.project.erp.dto.sku.SkuMainDataDto;
import cn.zdjc.wms.project.erp.dto.storagematerial.ErpStorageMaterialDto;
import cn.zdjc.wms.project.erp.dto.storagematerial.InventoryInquiryRequestDto;
import cn.zdjc.wms.project.erp.handler.ApiHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(ProjectHttpConstant.PREFIX_CONTEXT_PATH)
@AllArgsConstructor
public class DataSyncController {

    @Resource
    private ApiHandler apiHandler;

    /**
     * 品类管理同步（物料主数据）
     */
    @PostMapping("/masterData")
    public ResultWrapper<?> skuSynchronous(@Valid @RequestBody SkuMainDataDto request) {
        log.info("物料主数据同步请求: {}", request);
        return apiHandler.skuMaster(request);
    }

    /**
     * 同步收货单
     */
    @PostMapping("/asn")
    public ResultWrapper<Void> receiptSync(@Valid @RequestBody ErpAsnDto request) {
        log.info("收货单同步请求: {}", request);
        return apiHandler.receiptMaster(request);
    }

    /**
     * 同步发货单
     */
    @PostMapping("/outbound")
    public ResultWrapper<Void> requisitionSync(@Valid @RequestBody ErpRequisitionDto request) {
        log.info("发货单同步请求: {}", request);
        return apiHandler.requisitionMaster(request);
    }

    /**
     * 库存查询
     */
    @PostMapping("/queryInventory")
    public ResultWrapper<List<InventoryInquiryRequestDto>> inventoryInquiry(@Valid @RequestBody ErpStorageMaterialDto request) {
        log.info("库存查询请求: {}", request);
        return apiHandler.storageMaterialMaster(request);
    }
}