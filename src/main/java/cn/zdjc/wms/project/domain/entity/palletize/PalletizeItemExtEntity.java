package cn.zdjc.wms.project.domain.entity.palletize;

import cn.zdjc.warehouse.inventory.infrastructure.enums.QualityStatus;
import cn.zdjc.wms.common.annotation.Comment;
import cn.zdjc.wms.definition.domain.entity.PalletizeItem;
import com.foeris.y.common.bean.BeanAlias;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.UUID;

/**
 * 组盘明细扩展实体（用于查询、展示或导出）
 */
@EqualsAndHashCode(callSuper = true)
@Data
@BeanAlias("wms_palletize_item")
public class PalletizeItemExtEntity extends PalletizeItem {

//    /**
//     * 组盘明细项（对应数据库字段：item_form_no）
//     */
//    @Comment("组盘明细项")
//    @BeanAlias("item_form_no")
//    private String itemFormNo;
//
//    /**
//     * 组盘单ID（对应数据库字段：palletize_form_id）
//     */
//    @Comment("组盘单ID")
//    @BeanAlias("palletize_form_id")
//    private UUID palletizeFormId;
//
//    /**
//     * 组盘单号（对应数据库字段：palletize_form_no）
//     */
//    @Comment("组盘单号")
//    @BeanAlias("palletize_form_no")
//    private String palletizeFormNo;
//
//    /**
//     * 收料单号（对应数据库字段：asn_form_no）
//     */
//    @Comment("收料单号")
//    @BeanAlias("asn_form_no")
//    private String asnFormNo;
//
//    /**
//     * 收料明细（对应数据库字段：asn_item_no）
//     */
//    @Comment("收料明细")
//    @BeanAlias("asn_item_no")
//    private String asnItemNo;
//
//    /**
//     * 收料单详情（对应数据库字段：asn_detail_no）
//     */
//    @Comment("收料单详情")
//    @BeanAlias("asn_detail_no")
//    private String asnDetailNo;
//
//    /**
//     * 拣选详情ID（对应数据库字段：pick_detail_id）
//     */
//    @Comment("拣选详情")
//    @BeanAlias("pick_detail_id")
//    private UUID pickDetailId;
//
//    /**
//     * 需求订单号（对应数据库字段：order_form_no）
//     */
//    @Comment("需求订单号")
//    @BeanAlias("order_form_no")
//    private String orderFormNo;
//
//    /**
//     * 需求订单行（对应数据库字段：order_item_no）
//     */
//    @Comment("需求订单行")
//    @BeanAlias("order_item_no")
//    private String orderItemNo;
//
//    /**
//     * 托盘号（对应数据库字段：container_code）
//     */
//    @Comment("托盘号")
//    @BeanAlias("container_code")
//    private String containerCode;
//
//    /**
//     * 物料序列号（对应数据库字段：serial_no）
//     */
//    @Comment("物料序列号")
//    @BeanAlias("serial_no")
//    private String serialNo;
//
//    /**
//     * 品类编码（对应数据库字段：category_code）
//     */
//    @Comment("品类")
//    @BeanAlias("category_code")
//    private String categoryCode;
//
//    /**
//     * 品类名称（对应数据库字段：category_name）
//     */
//    @Comment("品类名称")
//    @BeanAlias("category_name")
//    private String categoryName;
//
//    /**
//     * 物料编码（SKU编码，对应数据库字段：sku_code）
//     */
//    @Comment("物料编码")
//    @BeanAlias("sku_code")
//    private String skuCode;
//
//    /**
//     * 物料名称（SKU名称，对应数据库字段：sku_name）
//     */
//    @Comment("物料名称")
//    @BeanAlias("sku_name")
//    private String skuName;
//
//    /**
//     * 批次号（对应数据库字段：batch_no）
//     */
//    @Comment("批次号")
//    @BeanAlias("batch_no")
//    private String batchNo;
//
//    /**
//     * 质量状态（对应数据库字段：quality_status）
//     */
//    @Comment("质量状态")
//    @BeanAlias("quality_status")
//    private QualityStatus qualityStatus;
//
//    /**
//     * 主计量单位库存数量（对应数据库字段：primary_qty）
//     */
//    @Comment("库存数量")
//    @BeanAlias("primary_qty")
//    private Double primaryQty;
//
//    /**
//     * 主计量单位（对应数据库字段：primary_unit）
//     */
//    @Comment("库存单位")
//    @BeanAlias("primary_unit")
//    private String primaryUnit;
//
//    /**
//     * 辅助计量数量（对应数据库字段：auxiliary_qty）
//     */
//    @Comment("辅助计量数量")
//    @BeanAlias("auxiliary_qty")
//    private Double auxiliaryQty;
//
//    /**
//     * 辅助计量单位（对应数据库字段：auxiliary_unit）
//     */
//    @Comment("辅助计量单位")
//    @BeanAlias("auxiliary_unit")
//    private String auxiliaryUnit;
//
//    /**
//     * 库存物料ID（对应数据库字段：storage_material_id）
//     */
//    @Comment("库存物料ID")
//    @BeanAlias("storage_material_id")
//    private UUID storageMaterialId;

}