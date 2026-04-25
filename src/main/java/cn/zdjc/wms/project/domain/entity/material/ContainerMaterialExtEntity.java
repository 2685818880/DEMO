package cn.zdjc.wms.project.domain.entity.material;

import cn.zdjc.warehouse.inventory.domain.entity.ContainerMaterial;
import cn.zdjc.warehouse.inventory.infrastructure.enums.ContainerStatus;
import com.foeris.y.common.bean.BeanAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.UUID;

/**
 * 容器物料扩展实体（用于查询/展示）
 * ContainerMaterial
 */
@EqualsAndHashCode(callSuper = false)
@Data
@BeanAlias("wms_container_material")
public class ContainerMaterialExtEntity extends ContainerMaterial {

    private static final long serialVersionUID = 1L;

    /**
     * 仓库号（对应数据库字段：house_code）
     */
    @BeanAlias("house_code")
    private String houseCode;

    /**
     * 容器库存ID（对应数据库字段：storage_location_inventory_id）
     */
    @BeanAlias("storage_location_inventory_id")
    private UUID storageLocationInventoryId;

    /**
     * 托盘号（对应数据库字段：container_code）
     */
    @BeanAlias("container_code")
    private String containerCode;

    /**
     * 库存物料ID（对应数据库字段：storage_material_id）
     */
    @BeanAlias("storage_material_id")
    private UUID storageMaterialId;

    /**
     * 物料序列号（对应数据库字段：serial_no）
     */
    @BeanAlias("serial_no")
    private String serialNo;

    /**
     * 托盘状态（默认为 ready，对应数据库字段：container_status）
     */
    @BeanAlias("container_status")
    private ContainerStatus containerStatus = ContainerStatus.ready;

    /**
     * SKU 编码（对应数据库字段：sku_code）
     */
    @BeanAlias("sku_code")
    private String skuCode;

    /**
     * SKU 名称（对应数据库字段：sku_name）
     */
    @BeanAlias("sku_name")
    private String skuName;

    /**
     * 批号（对应数据库字段：batch_no）
     */
    @BeanAlias("batch_no")
    private String batchNo;

    /**
     * 主计量单位库存数量（对应数据库字段：primary_qty）
     */
    @BeanAlias("primary_qty")
    private Double primaryQty;

    /**
     * 主计量单位（对应数据库字段：primary_unit）
     */
    @BeanAlias("primary_unit")
    private String primaryUnit;

    /**
     * 辅助单位库存数量（对应数据库字段：auxiliary_qty）
     */
    @BeanAlias("auxiliary_qty")
    private Double auxiliaryQty;

    /**
     * 辅助单位（对应数据库字段：auxiliary_unit）
     */
    @BeanAlias("auxiliary_unit")
    private String auxiliaryUnit;

    /**
     * 可用库存数量（对应数据库字段：available_qty）
     */
    @BeanAlias("available_qty")
    private Double availableQty;

    /**
     * 容器类型编码（对应数据库字段：container_type_code）
     */
    @BeanAlias("container_type_code")
    private String containerTypeCode;
}