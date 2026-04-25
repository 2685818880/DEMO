package cn.zdjc.wms.project.domain.entity.task;

import cn.zdjc.warehouse.inventory.infrastructure.enums.PackageLevel;
import cn.zdjc.warehouse.inventory.infrastructure.enums.QualityStatus;
import cn.zdjc.wms.definition.infrastructure.enums.ProfitLossType;
import cn.zdjc.wms.definition.infrastructure.enums.TakeStockType;
import com.foeris.y.common.bean.BeanAlias;
import com.foeris.y.fairy.jdbc.ddd.Domain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * 盘点物料明细扩展实体（用于查询、展示或导出）
 *
 * @version 1.0.0
 * @author liuyk
 * @since 2021-04-02
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TakeStockDetailExtEntity extends Domain {

    /**
     * 盘点物料明细单号（对应数据库字段：stock_detail_no）
     */
    @BeanAlias("stock_detail_no")
    private String stockDetailNo;

    /**
     * 盘点单ID（主表ID，对应数据库字段：stock_form_id）
     */
    @BeanAlias("stock_form_id")
    private UUID stockFormId;

    /**
     * 盘点单号（对应数据库字段：stock_form_no）
     */
    @BeanAlias("stock_form_no")
    private String stockFormNo;

    /**
     * 盘点单类型（如 FULL, CYCLE，对应数据库字段：stock_form_type）
     */
    @BeanAlias("stock_form_type")
    private TakeStockType stockFormType;

    /**
     * 盘点明细ID（对应数据库字段：stock_item_id）
     */
    @BeanAlias("stock_item_id")
    private UUID stockItemId;

    /**
     * 盘点明细单号（对应数据库字段：stock_item_no）
     */
    @BeanAlias("stock_item_no")
    private String stockItemNo;

    /**
     * 存货编码（物料分类编码，对应数据库字段：category_code）
     */
    @BeanAlias("category_code")
    private String categoryCode;

    /**
     * 存货名称（对应数据库字段：category_name）
     */
    @BeanAlias("category_name")
    private String categoryName;

    /**
     * 物料编码（SKU编码，对应数据库字段：sku_code）
     */
    @BeanAlias("sku_code")
    private String skuCode;

    /**
     * 物料名称（SKU名称，对应数据库字段：sku_name）
     */
    @BeanAlias("sku_name")
    private String skuName;

    /**
     * 物料序列号（对应数据库字段：serial_no）
     */
    @BeanAlias("serial_no")
    private String serialNo;

    /**
     * 库存物料ID（关联 wms_storage_material.id，对应数据库字段：storage_material_id）
     */
    @BeanAlias("storage_material_id")
    private UUID storageMaterialId;

    /**
     * 批次号（对应数据库字段：batch_no）
     */
    @BeanAlias("batch_no")
    private String batchNo;

    /**
     * 质量状态（如 OK, QC, LOCKED，对应数据库字段：quality_status）
     */
    @BeanAlias("quality_status")
    private QualityStatus qualityStatus;

    /**
     * 系统记录库存数量（对应数据库字段：stock_qty）
     */
    @BeanAlias("stock_qty")
    private Double stockQty;

    /**
     * 实际盘点数量（对应数据库字段：actual_qty）
     */
    @BeanAlias("actual_qty")
    private Double actualQty;

    /**
     * 库存单位（计量单位，对应数据库字段：stock_unit）
     */
    @BeanAlias("stock_unit")
    private String stockUnit;

    /**
     * 盈亏类型（盈/亏，对应数据库字段：profit_loss_type）
     */
    @BeanAlias("profit_loss_type")
    private ProfitLossType profitLossType;

    /**
     * 盈亏数量（对应数据库字段：profit_loss_qty）
     */
    @BeanAlias("profit_loss_qty")
    private Double profitLossQty;

    /**
     * 工厂编码（对应数据库字段：factory）
     */
    @BeanAlias("factory")
    private String factory;

    /**
     * 库存地点（对应数据库字段：inventory_area）
     */
    @BeanAlias("inventory_area")
    private String inventoryArea;

    /**
     * 包装等级（如 INNER, OUTER，对应数据库字段：package_level）
     */
    @BeanAlias("package_level")
    private PackageLevel packageLevel;

    /**
     * 包装码（分包/合包时使用，对应数据库字段：package_no）
     */
    @BeanAlias("package_no")
    private String packageNo;
}