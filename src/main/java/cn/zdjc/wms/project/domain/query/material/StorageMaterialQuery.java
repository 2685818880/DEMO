package cn.zdjc.wms.project.domain.query.material;

import cn.zdjc.warehouse.ExQuery;
import cn.zdjc.warehouse.inventory.infrastructure.enums.BatchStatus;
import cn.zdjc.warehouse.inventory.infrastructure.enums.InventoryStatus;
import cn.zdjc.warehouse.inventory.infrastructure.enums.QualityStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存物料查询条件实体
 * <p>
 * 用途：
 * - 库存明细列表高级搜索
 * - 批次/序列号追溯
 * - 质检状态监控
 * - 呆滞库存分析
 *
 * @version 1.1.0
 * @author xud
 * @since 2026-01-27
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StorageMaterialQuery extends ExQuery {

    /**
     * 库存物料ID（UUID 字符串，精确匹配）
     */
    private String storageMaterialId;

    /**
     * 仓库编码（如 X01, X02）
     */
    private String houseCode;

    /**
     * 仓库号（别名字段，若与 houseCode 不同则保留）
     */
    private String whse;

    /**
     * 容器编号 / 托盘号（支持模糊查询）
     */
    private String containerCode;

    /**
     * 物料序列号（唯一标识，精确匹配）
     */
    private String serialNo;

    /**
     * 库位编号（如 A01-02-03-04，支持模糊或精确）
     */
    private String locationCode;

    /**
     * 存货分类编码（如 RAW, FG, WIP）
     */
    private String categoryCode;

    /**
     * 存货分类名称（用于按名称搜索）
     */
    private String categoryName;

    /**
     * 存货分类ID（关联分类主数据）
     */
    private String categoryId;

    /**
     * 物料编码（SKU 编码，核心查询字段）
     */
    private String skuCode;

    /**
     * 物料名称（SKU 名称，支持模糊查询）
     */
    private String skuName;

    /**
     * 批次号（内部批次，精确匹配）
     */
    private String batchNo;

    /**
     * 供货单位名称（供应商名称）
     */
    private String vendorName;

    /**
     * 供应商批次号（原厂批次）
     */
    private String vendorBatch;

    /**
     * 检验批编号（IQC 单号，用于质检追溯）
     */
    private String iqcNo;

    /**
     * 质量状态（如 OK=合格, QC=待检, LOCKED=冻结）
     */
    private QualityStatus qualityStatus;

    /**
     * 质量状态列表（多选，用于 IN 查询）
     */
    private List<QualityStatus> qualityStatusList;

    /**
     * 批次状态（如 ACTIVE=有效, EXPIRED=过期）
     */
    private BatchStatus batchStatus;

    /**
     * 批次状态列表（多选）
     */
    private List<BatchStatus> batchStatusList;

    /**
     * 工厂编码（多工厂场景）
     */
    private String factoryCode;

    /**
     * 库存地点 / 库区（如 X102, BULK）
     */
    private String inventoryLocation;

    /**
     * 库存状态（如 NORMAL=正常, FROZEN=冻结, DAMAGED=损坏）
     */
    private InventoryStatus inventoryStatus;

    /**
     * 库存状态列表（多选）
     */
    private List<InventoryStatus> inventoryStatusList;

    // =============== 新增关键查询维度 ===============

    /**
     * 入库开始时间（用于分析库龄）
     */
    private LocalDateTime inboundStartTime;

    /**
     * 入库结束时间
     */
    private LocalDateTime inboundEndTime;

    /**
     * 最后出库时间之前（用于识别呆滞库存，如 >90天未动）
     */
    private LocalDateTime lastOutboundBefore;

    /**
     * 库存数量最小值（用于筛选低库存/高库存）
     */
    private BigDecimal minQty;

    /**
     * 库存数量最大值
     */
    private BigDecimal maxQty;

    /**
     * 是否包含已删除记录（默认 false）
     */
    private Boolean includeDeleted = false;
}