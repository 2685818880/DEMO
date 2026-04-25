package cn.zdjc.wms.project.domain.entity.material;

import cn.zdjc.warehouse.inventory.domain.entity.StorageMaterial;
import cn.zdjc.warehouse.inventory.infrastructure.enums.*;
import cn.zdjc.wms.common.annotation.Comment;
import cn.zdjc.wms.common.utils.ArithmeticUtils;
import com.foeris.y.common.bean.BeanAlias;
import com.foreris.eris.common.exception.BusinessException;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * 库存物料扩展实体（用于查询/展示）
 */
@EqualsAndHashCode(callSuper = true)
@Data
@BeanAlias("wms_storage_location")
public class StorageMaterialExtEntity extends StorageMaterial {


    @BeanAlias("house_code")
    private String houseCode;

    @BeanAlias("category_id")
    private UUID categoryId;

    @BeanAlias("category_code")
    private String categoryCode;

    @BeanAlias("category_name")
    private String categoryName;

    @BeanAlias("sku_id")
    private UUID skuId;

    @BeanAlias("sku_code")
    private String skuCode;

    @BeanAlias("sku_name")
    private String skuName;

    @BeanAlias("batch_no")
    private String batchNo;

    @BeanAlias("iqc_no")
    private String iqcNo;

    @BeanAlias("material_id")
    private UUID materialId;

    @BeanAlias("serial_no")
    private String serialNo;

    @BeanAlias("product_date")
    private String productDate;

    @BeanAlias("availability_date")
    private String availabilityDate;

    @BeanAlias("expire_date")
    private String expireDate;

    @BeanAlias("warning_date")
    private String warningDate;

    @BeanAlias("re_inspection_date")
    private String reInspectionDate;

    @BeanAlias("primary_qty")
    private Double primaryQty;

    @BeanAlias("primary_unit")
    private String primaryUnit;

    @BeanAlias("auxiliary_qty")
    private Double auxiliaryQty;

    @BeanAlias("auxiliary_unit")
    private String auxiliaryUnit;

    @BeanAlias("available_qty")
    private Double availableQty;

    @BeanAlias("quality_status")
    private QualityStatus qualityStatus;

    @BeanAlias("batch_status")
    private BatchStatus batchStatus;

    @BeanAlias("inventory_status")
    private InventoryStatus inventoryStatus;

    @Comment("上一次库存状态")
    @BeanAlias("last_inventory_status")
    private InventoryStatus lastInventoryStatus;

    @BeanAlias("loc_type")
    private StorageLocType locType = StorageLocType.virtual;

    @BeanAlias("inventory_status_voucher")
    private String inventoryStatusVoucher;

    @BeanAlias("vendor_name")
    private String vendorName;

    @BeanAlias("vendor_batch")
    private String vendorBatch;

    @BeanAlias("owner_code")
    private String ownerCode;

    @BeanAlias("owner_name")
    private String ownerName;

    @BeanAlias("remark")
    private String remark;

    @BeanAlias("package_level")
    private PackageLevel packageLevel;

    @BeanAlias("package_no")
    private String packageNo;

    @BeanAlias("package_amount")
    private Integer packageAmount;

    @BeanAlias("kitting_flag")
    private FlagStatus kittingFlag = FlagStatus.Y;

    @BeanAlias("factory_code")
    private String factoryCode;

    @BeanAlias("inventory_location")
    private String inventoryLocation;

    @BeanAlias("inbound_voucher")
    private String inboundVoucher;

    @BeanAlias("outbound_voucher")
    private String outboundVoucher;

    @BeanAlias("container_type_code")
    private String containerTypeCode;

    @BeanAlias("business_form_no")
    private String businessFormNo;

    @BeanAlias("business_item_no")
    private String businessItemNo;

    /**
     * 扣除库存的锁定数量
     * 因为扣除的是锁定的数量，所以库存的可用数量不变
     */
    public void reduceLockedQty(double reduceQty) {
        if (reduceQty < 0) {
            throw new BusinessException("库存占用数量扣减异常:扣减数量[{}]小于0", reduceQty);
        }
        if (reduceQty == 0) {
            return;
        }
        double remainQty = ArithmeticUtils.sub(this.primaryQty, reduceQty);
        if (ArithmeticUtils.compare(remainQty, 0D) < 0) {
            throw new BusinessException("库存占用数量扣减异常:扣减数量[{}]大于库存数量[{}]", reduceQty, this.primaryQty);
        }
        // 注意：锁定数量减少 → 可用数量应增加，但此处逻辑需根据业务确认
        // 当前仅更新主数量，辅助数量不应设为 reduceQty（原逻辑错误）
        this.primaryQty = remainQty;
        // auxiliaryQty 应同步按比例计算，或保持不变（根据业务）
    }

    /**
     * 库存可用数量扣减
     */
    public void reduceAvailQty(double reduceQty) {
        if (reduceQty < 0) {
            throw new BusinessException("库存可用数量扣减异常:扣减数量[{}]小于0", reduceQty);
        }
        if (reduceQty == 0) {
            return;
        }
        double remainPrimary = ArithmeticUtils.sub(this.primaryQty, reduceQty);
        double remainAvailable = ArithmeticUtils.sub(this.availableQty, reduceQty);

        if (remainPrimary < 0) {
            throw new BusinessException("库存可用数量扣减异常:扣减数量[{}]大于总库存数量[{}]", reduceQty, this.primaryQty);
        }
        if (remainAvailable < 0) {
            throw new BusinessException("库存可用数量扣减异常:扣减数量[{}]大于可用库存数量[{}]", reduceQty, this.availableQty);
        }

        this.primaryQty = remainPrimary;
        this.availableQty = remainAvailable;
        // auxiliaryQty 应同步更新（按比例或业务规则）
    }
}