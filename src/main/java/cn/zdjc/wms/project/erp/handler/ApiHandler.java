package cn.zdjc.wms.project.erp.handler;

import cn.zdjc.warehouse.WmsConfig;
import cn.zdjc.wms.project.common.constant.ProjectBusinessLockKeys;
import cn.zdjc.wms.project.common.constant.ProjectDistributedLockService;
import cn.zdjc.wms.project.erp.dto.ResultWrapper;
import cn.zdjc.wms.project.erp.dto.asn.ErpAsnDto;
import cn.zdjc.wms.project.erp.dto.requisition.ErpRequisitionDto;
import cn.zdjc.wms.project.erp.dto.sku.SkuMainDataDto;
import cn.zdjc.wms.project.erp.dto.storagematerial.ErpStorageMaterialDto;
import cn.zdjc.wms.project.erp.dto.storagematerial.InventoryInquiryRequestDto;
import cn.zdjc.wms.project.erp.service.QueryInventoryService;
import cn.zdjc.wms.project.erp.service.ReceiptSyncService;
import cn.zdjc.wms.project.erp.service.RequisitionSyncService;
import cn.zdjc.wms.project.erp.service.SkuSyncService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;


@Slf4j
@Service
public class ApiHandler {

    @Autowired
    private ProjectDistributedLockService pjDistributedLockService;

    @Resource
    private SkuSyncService skuSyncService;

    @Resource
    private ReceiptSyncService receiptSyncService;

    @Resource
    private RequisitionSyncService requisitionSyncService;

    @Resource
    private QueryInventoryService queryInventoryService;


    @Resource
    private ObjectMapper objectMapper;

    public ResultWrapper<Void> skuMaster(SkuMainDataDto dto) {
        // 3. 业务数据校验
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            log.warn("主数据中 ITEMS 为空");
            return ResultWrapper.buildFailure("400", "ITEMS 列表不能为空");
        }

        // 4. 分布式锁控制
        String syncKey = ProjectBusinessLockKeys.SKU_LOCK;
        log.info("尝试获取物料主数据同步锁，key={}", syncKey);

        if (!pjDistributedLockService.acquireBlockedLock(syncKey)) {
            String errorMsg = "无法获取物料主数据同步锁，可能正在处理中，请稍后再试。锁key=" + syncKey;
            log.warn(errorMsg);
            return ResultWrapper.buildFailure("423", "系统繁忙，请稍后再试"+errorMsg);
        }

        // 5. 执行同步
        try {
            log.info("成功获取物料主数据同步锁，开始处理...");
            skuSyncService.handleSkuMaster(dto);
            log.info("物料主数据同步处理完成");
            return ResultWrapper.buildSuccess(null);
        } catch (Exception e) {
            log.error("处理物料主数据同步时发生异常", e);
            return ResultWrapper.buildFailure("500", "同步失败: " + e.getMessage());
        } finally {
            pjDistributedLockService.releaseLock(syncKey);
            log.info("已释放物料主数据同步锁，key={}", syncKey);
        }
    }

    public ResultWrapper<Void> receiptMaster(ErpAsnDto dto) {


        // 3. 业务参数校验
        try {
            dto.fieldCheck();
            log.debug("SAP 收货单参数校验通过，单号: {}, 仓库: {}", dto.getBusNo(), dto.getHouseCode());
        } catch (Exception e) {
            String errorMsg = "参数校验失败: " + e.getMessage();
            log.warn("SAP 收货单参数校验失败，单号: {}, 仓库: {} - {}",
                    dto.getBusNo(), dto.getHouseCode(), errorMsg);
            return ResultWrapper.buildFailure("INVALID_PARAM", errorMsg);
        }

        // 4. 构建分布式锁 key (避免空指针)
        String busNo = Objects.requireNonNull(dto.getBusNo(), "BUS_NO 不能为空");
        String houseCode = Objects.requireNonNull(dto.getHouseCode(), "HOUSE_CODE 不能为空");
        String syncKey = ProjectBusinessLockKeys.TO_LOCK + houseCode + "_" + busNo;
        log.info("为收货单生成分布式锁 key={}", syncKey);

        // 5. 尝试获取锁（阻塞式）
        if (!pjDistributedLockService.acquireBlockedLock(syncKey)) {
            String errorMsg = "收货单正在处理中，请勿重复提交。单号: " + busNo;
            log.warn("获取锁失败，{}", errorMsg);
            return ResultWrapper.buildFailure("LOCKED", errorMsg);
        }

        // 6. 执行业务逻辑
        try {
            ResultWrapper<Void> result = receiptSyncService.handleReceiptMaster(dto);
            if (!result.isSuccess()) {
                log.warn("SAP 收货单处理失败，单号: {}，原因: {}", busNo, result.getMessage());
                return result; // 直接返回子服务的错误码和消息
            }
            log.info("SAP 收货单处理成功，单号: {}", busNo);
            return ResultWrapper.buildSuccess(null);
        } catch (Exception e) {
            String errorMsg = "收货单同步异常: " + e.getMessage();
            log.error("处理 SAP 收货单时发生系统异常，单号: {}", busNo, e);
            return ResultWrapper.buildFailure("SYSTEM_ERROR", errorMsg);
        } finally {
            // 7. 确保锁被释放
            try {
                pjDistributedLockService.releaseLock(syncKey);
                log.debug("已释放收货单处理锁，key={}", syncKey);
            } catch (Exception ex) {
                log.warn("释放分布式锁时发生异常，key={}", syncKey, ex);
            }
        }
    }

    public ResultWrapper<Void> requisitionMaster(ErpRequisitionDto dto) {

        // 3. 参数校验
        try {
            dto.fieldCheck();
            log.debug("SAP 发货单参数校验通过，单号: {}, 仓库: {}", dto.getBusinessFormNo(), dto.getHouseCode());
        } catch (Exception e) {
            String errorMsg = "参数校验失败: " + e.getMessage();
            log.warn("SAP 发货单校验失败，单号: {} - {}", dto.getBusinessFormNo(), errorMsg);
            return ResultWrapper.buildFailure("INVALID_PARAM", errorMsg);
        }

        // 4. 构建分布式锁 key（防御 NPE）
        String formNo = Objects.requireNonNull(dto.getBusinessFormNo(), "BUSINESS_FORM_NO 不能为空");
//        String houseCode = Objects.requireNonNull(dto.getHouseCode(), "HOUSE_CODE 不能为空");
        String houseCode = null;
        if(dto.getHouseCode()==null){
            houseCode= WmsConfig.DefaultHouseCode;
        }
        String syncKey = ProjectBusinessLockKeys.TO_LOCK + houseCode + "_" + formNo;
        log.info("为发货单生成分布式锁 key={}", syncKey);

        // 5. 获取锁
        if (!pjDistributedLockService.acquireBlockedLock(syncKey)) {
            String errorMsg = "发货单正在处理中，请勿重复提交。单号: " + formNo;
            log.warn("获取锁失败: {}", errorMsg);
            return ResultWrapper.buildFailure("LOCKED", errorMsg);
        }

        // 6. 执行业务
        try {
            ResultWrapper<Void> result = requisitionSyncService.handleRequisitionMaster(dto);
            if (!result.isSuccess()) {
                log.warn("SAP 发货单处理失败，单号: {}，原因: {}", formNo, result.getMessage());
                return result; // 透传子服务的错误码和消息
            }
            log.info("SAP 发货单处理成功，单号: {}", formNo);
            return ResultWrapper.buildSuccess(null);
        } catch (Exception e) {
            String errorMsg = "发货单同步异常: " + e.getMessage();
            log.error("处理 SAP 发货单时发生系统异常，单号: {}", formNo, e);
            return ResultWrapper.buildFailure("SYSTEM_ERROR", errorMsg);
        } finally {
            // 7. 释放锁
            try {
                pjDistributedLockService.releaseLock(syncKey);
                log.debug("已释放发货单处理锁，key={}", syncKey);
            } catch (Exception ex) {
                log.warn("释放分布式锁异常，key={}", syncKey, ex);
            }
        }
    }
    public ResultWrapper<List<InventoryInquiryRequestDto>> storageMaterialMaster(ErpStorageMaterialDto dto) {
        String syncKey = ProjectBusinessLockKeys.QUERY_INVENTORY_LOCK;
        log.info("为库存查询生成分布式锁 key={}", syncKey);

        // 1. 获取分布式锁（带超时阻塞）
        if (!pjDistributedLockService.acquireBlockedLock(syncKey)) {
            String errorMsg = "获取库存查询锁失败，请稍后重试";
            log.warn("获取分布式锁失败, key={}", syncKey);
            return ResultWrapper.buildFailure("LOCKED", errorMsg);
        }

        // 2. 执行业务逻辑
        try {
            return queryInventoryService.handleQueryInventoryMaster(dto);
        } catch (Exception e) {
            // 记录详细异常（关键！）
            log.error("库存查询业务执行异常, dto={}", dto, e);
            String errorMsg = "库存查询服务内部错误: " + e.getMessage();
            return ResultWrapper.buildFailure("SYSTEM_ERROR", errorMsg);
        } finally {
            // 3. 释放锁（确保 always 执行）
            try {
                pjDistributedLockService.releaseLock(syncKey);
                log.debug("分布式锁已释放, key={}", syncKey);
            } catch (Exception ex) {
                log.error("释放分布式锁异常, key={}", syncKey, ex);
                // 释放锁失败通常不影响主流程，但需告警
            }
        }
    }

}