package cn.zdjc.wms.project.ai.service;

import cn.zdjc.wms.project.ai.repository.inventory.InventoryRepository;
import cn.zdjc.wms.project.ai.repository.location.LocationRepository;
import cn.zdjc.wms.project.ai.repository.outbound.OutboundRepository;
import cn.zdjc.wms.project.ai.repository.sku.SkuRepository;
import cn.zdjc.wms.project.ai.repository.workorder.WorkOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * AI 数据服务 - 整合各 Repository 提供统一数据访问接口
 * 重构后：仅作为 Facade 层，实际查询逻辑已下沉到 Repository 层
 */
@Service
@Slf4j
public class AiDataService {

    @Resource
    private InventoryRepository inventoryRepository;

    @Resource
    private OutboundRepository outboundRepository;

    @Resource
    private LocationRepository locationRepository;

    @Resource
    private SkuRepository skuRepository;

    @Resource
    private WorkOrderRepository workOrderRepository;

    // ==================== 库存相关 ====================

    public List<Map<String, Object>> getCurrentStockSummary(String warehouseId) {
        return inventoryRepository.getCurrentStockSummary(warehouseId);
    }

    public List<Map<String, Object>> queryInventorySummary(String zonePattern) {
        return inventoryRepository.queryInventorySummary(zonePattern);
    }

    public List<Map<String, Object>> querySlowMovingProducts(String zonePattern, int dwellThresholdDays) {
        return inventoryRepository.querySlowMovingProducts(zonePattern, dwellThresholdDays);
    }

    public List<Map<String, Object>> queryExpiryWarningProducts(String zonePattern, int warningDays) {
        return inventoryRepository.queryExpiryWarningProducts(zonePattern, warningDays);
    }

    public List<Map<String, Object>> queryOverstockProducts(String zonePattern, BigDecimal qtyThreshold) {
        return inventoryRepository.queryOverstockProducts(zonePattern, qtyThreshold);
    }

    // ==================== 出库/入库相关 ====================

    public List<Map<String, Object>> getPickOutboundSummary(String warehouseId, int days) {
        return outboundRepository.getPickOutboundSummary(warehouseId, days);
    }

    public List<Map<String, Object>> getRecentPickAnomalies(int minutes) {
        return outboundRepository.getRecentPickAnomalies(minutes);
    }

    public List<Map<String, Object>> getPalletizeInboundSummary(String warehouseId, int days) {
        return outboundRepository.getPalletizeInboundSummary(warehouseId, days);
    }

    // ==================== 仓库/库位相关 ====================

    public List<Map<String, Object>> getWarehouseList() {
        return locationRepository.getWarehouseList();
    }

    public List<Map<String, Object>> getLocationList(String houseCode) {
        return locationRepository.getLocationList(houseCode);
    }

    public List<Map<String, Object>> getAllLocationCoordinates(String warehouseId) {
        return locationRepository.getAllLocationCoordinates(warehouseId);
    }

    public List<Map<String, Object>> queryLocationList(String keyword) {
        return locationRepository.queryLocationList(keyword);
    }

    public List<Map<String, Object>> queryContainerList(String keyword) {
        return locationRepository.queryContainerList(keyword);
    }

    // ==================== SKU 相关 ====================

    public List<Map<String, Object>> querySkuList(String keyword) {
        return skuRepository.querySkuList(keyword);
    }

    public List<Map<String, Object>> querySkuCategoryList(String keyword) {
        return skuRepository.querySkuCategoryList(keyword);
    }

    // ==================== 工单/调度相关 ====================

    public List<Map<String, Object>> queryDispatchInfoList(String keyword) {
        return workOrderRepository.queryDispatchInfoList(keyword);
    }

    public List<Map<String, Object>> queryDispatchJobList(String keyword) {
        return workOrderRepository.queryDispatchJobList(keyword);
    }

    public List<Map<String, Object>> querySchedulerManageList(String keyword) {
        return workOrderRepository.querySchedulerManageList(keyword);
    }
}
