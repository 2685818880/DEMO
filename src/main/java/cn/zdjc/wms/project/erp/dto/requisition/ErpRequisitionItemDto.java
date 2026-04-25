package cn.zdjc.wms.project.erp.dto.requisition;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.foeris.y.common.tree.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * ERP 要货单明细项 DTO
 *
 * @author lele
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErpRequisitionItemDto extends Dto {

    @JsonProperty("ITEM_NO")
    private String itemNo;

    @JsonProperty("HOUSE_CODE")
    private String houseCode;

    @JsonProperty("SOURCE_AREA")
    private String sourceArea;

    @JsonProperty("TARGET_AREA")
    private String targetArea;

    @JsonProperty("CATEGORY_CODE")
    private String categoryCode;

    @JsonProperty("CATEGORY_NAME")
    private String categoryName;

    @JsonProperty("SKU_CODE")
    private String skuCode;

    @JsonProperty("BATCH_NO")
    private String batchNo;

    @JsonProperty("QUALITY")
    private String quality;

    @JsonProperty("QTY")
    private BigDecimal qty;

    @JsonProperty("UNIT")
    private String unit;

    @JsonProperty("ORGANIZATION")
    private String organization;

    @JsonProperty("COMPANY")
    private String company;

    /**
     * 验证字段是否合法（所有必填字段非空）
     *
     * @return true 表示数据合法，false 表示存在空字段
     */
    public boolean isValid() {
        return !isBlank(itemNo)
                && !isBlank(houseCode)
                && !isBlank(categoryCode)
                && !isBlank(categoryName)
                && !isBlank(skuCode)
                && !isBlank(quality)
                && Objects.nonNull(qty)
                && !isBlank(unit);
    }

    /**
     * 抛异常式校验（用于快速失败）
     *
     * @throws IllegalStateException 当必填字段为空时
     */
    public void validate() {
        if (isBlank(itemNo)) throw new IllegalStateException("ITEM_NO 不能为空");
        if (isBlank(houseCode)) throw new IllegalStateException("HOUSE_CODE 不能为空");
        if (isBlank(categoryCode)) throw new IllegalStateException("CATEGORY_CODE 不能为空");
        if (isBlank(categoryName)) throw new IllegalStateException("CATEGORY_NAME 不能为空");
        if (isBlank(skuCode)) throw new IllegalStateException("SKU_CODE 不能为空");
        if (isBlank(quality)) throw new IllegalStateException("QUALITY 不能为空");
        if (qty == null) throw new IllegalStateException("QTY 不能为空");
        if (isBlank(unit)) throw new IllegalStateException("UNIT 不能为空");
    }
}