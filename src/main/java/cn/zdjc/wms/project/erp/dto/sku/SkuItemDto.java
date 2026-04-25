package cn.zdjc.wms.project.erp.dto.sku;

import cn.zdjc.wms.customize.domain.dto.ExtraDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 物料主数据DTO（对应 JSON 中的单个 ITEM）
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkuItemDto extends ExtraDto {

    /** 品类编码 */
    @JsonProperty("CATEGORY_CODE")
    private String categoryCode;

    /** 品类名称 */
    @JsonProperty("CATEGORY_NAME")
    private String categoryName;

    /** SKU编码 */
    @JsonProperty("SKU_CODE")
    private String skuCode;

    /** SKU名称（物料名称） */
    @JsonProperty("SKU_NAME")
    private String skuName;

    /** 序列号标志（如：箱、托等） */
    @JsonProperty("SKU_SERIAL_FLAG")
    private String skuSerialFlag;

    /** 辅助单位（如：KG） */
    @JsonProperty("AUXILIARY_UNIT")
    private String auxiliaryUnit;

    /** 规格（如：10KG/箱） */
    @JsonProperty("SPECS")
    private String specs;

    /** 换算率（主单位与辅助单位的换算比例） */
    @JsonProperty("CONVERSION_RATE")
    private String conversionRate;

    /** 有效期时长（如：1） */
    @JsonProperty("DURATION_OF_VALIDITY")
    private String durationOfValidity;

    /** 有效期单位（如：年、月、日） */
    @JsonProperty("VALIDITY_DATE_UNIT")
    private String validityDateUnit;

    /** 批次管理标志（1=启用批次，0=不启用） */
    @JsonProperty("SKU_BATCH_FLAG")
    private String skuBatchFlag;

    /** 冻结状态（0=未冻结，1=冻结） */
    @JsonProperty("BLOCK_STATE")
    private String blockState;

    /** 满箱数量（如：32） */
    @JsonProperty("FULL_QUANTITY")
    private String fullQuantity;

    // 扩展字段
    @JsonProperty("Extended1")
    private String extended1;

    @JsonProperty("Extended2")
    private String extended2;

    @JsonProperty("Extended3")
    private String extended3;

    @JsonProperty("Extended4")
    private String extended4;

    @JsonProperty("Extended5")
    private String extended5;

    @JsonProperty("Extended6")
    private String extended6;
}