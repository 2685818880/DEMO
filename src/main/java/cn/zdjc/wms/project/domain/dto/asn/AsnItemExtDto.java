package cn.zdjc.wms.project.domain.dto.asn;

import cn.zdjc.warehouse.inventory.infrastructure.enums.FlagStatus;
import cn.zdjc.warehouse.material.infrastructure.enums.PackageType;
import cn.zdjc.wms.common.annotation.Comment;
import com.foeris.y.common.bean.BeanAlias;
import com.foeris.y.common.bean.BeanIgnore;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * ASN 明细扩展实体（用于展示/查询）
 * AsnItemEntity
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AsnItemExtDto implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 工作站状态
     */
    @BeanAlias("workstation_status")
    @Comment("工作站状态")
    private String workstationStatus;

    /**
     * 行号
     */
    @BeanAlias("item_no")
    @Comment("行号")
    private String itemNo;

    /**
     * ASN ID
     */
    @BeanAlias("asn_id")
    @Comment("ASN ID")
    private String asnId;

    /**
     * ASN 单号
     */
    @BeanAlias("asn_no")
    @Comment("ASN 单号")
    private String asnNo;

    /**
     * 来源业务单号
     */
    @BeanAlias("business_form_no")
    @Comment("来源业务单号")
    private String businessFormNo;

    /**
     * 来源业务行号
     */
    @BeanAlias("business_item_no")
    @Comment("来源业务行号")
    private String businessItemNo;

    /**
     * 来源业务类型
     */
    @BeanAlias("business_form_type")
    @Comment("来源业务类型")
    private String businessFormType;

    /**
     * 存货编码
     */
    @BeanAlias("category_code")
    @Comment("存货编码")
    private String categoryCode;

    /**
     * 存货名称
     */
    @BeanAlias("category_name")
    @Comment("存货名称")
    private String categoryName;

    /**
     * SKU编码
     */
    @BeanAlias("sku_code")
    @Comment("SKU编码")
    private String skuCode;

    /**
     * SKU名称
     */
    @BeanAlias("sku_name")
    @Comment("SKU名称")
    private String skuName;

    /**
     * 批次
     */
    @BeanAlias("batch_no")
    @Comment("批次")
    private String batchNo;

    /**
     * 工厂
     */
    @BeanAlias("factory")
    @Comment("工厂")
    private String factory;

    /**
     * 库存地点
     */
    @BeanAlias("inventory_area")
    @Comment("库存地点")
    private String inventoryArea;

    /**
     * 需求数量
     */
    @BeanAlias("primary_qty")
    @Comment("需求数量")
    private Double primaryQty;

    /**
     * 计量单位
     */
    @BeanAlias("primary_unit")
    @Comment("计量单位")
    private String primaryUnit;

    /**
     * 辅助数量
     */
    @BeanAlias("auxiliary_qty")
    @Comment("辅助数量")
    private Double auxiliaryQty;

    /**
     * 辅助单位
     */
    @BeanAlias("auxiliary_unit")
    @Comment("辅助单位")
    private String auxiliaryUnit;

    /**
     * 收料数量
     */
    @BeanAlias("confirm_qty")
    @Comment("收料数量")
    private Double confirmQty;

    /**
     * 过账数量
     */
    @BeanAlias("submit_qty")
    @Comment("过账数量")
    private Double submitQty;

    /**
     * 计划到货日期
     */
    @BeanAlias("plan_delivery_date")
    @Comment("计划到货日期")
    private String planDeliveryDate;

    /**
     * 是否质检
     * 1 是：物料状态变更成待检
     * 2 否：物料状态变成成OK
     */
    @BeanAlias("quality_flag")
    @Comment("是否质检（1=是，2=否）")
    private FlagStatus qualityFlag;

    /**
     * 包装类型
     */
    @BeanAlias("package_type")
    @Comment("包装类型")
    private PackageType packageType;

    /**
     * 额外字段信息
     */
    private String extra;

//    @BeanIgnore
//    private Boolean modified = false;

    @BeanAlias("id")
    private String id;

    @BeanAlias("is_active")
    private Boolean isActive = true;

    @BeanAlias("create_datetime")
    private Date createDatetime;

    @BeanAlias("create_by")
    private String createBy;

    @BeanAlias("last_modify_datetime")
    private Date lastModifyDatetime;

    @BeanAlias("last_modify_by")
    private String lastModifyBy;
}
