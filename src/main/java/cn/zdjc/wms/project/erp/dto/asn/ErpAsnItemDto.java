package cn.zdjc.wms.project.erp.dto.asn;

import cn.zdjc.wms.customize.domain.dto.ExtraDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * ERP 收货通知单明细项 DTO
 *
 * @author lele
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErpAsnItemDto extends ExtraDto {

    @JsonProperty("ITEM_NO")
    private String itemNo;

    @JsonProperty("CATEGORY_CODE")
    private String categoryCode;

    @JsonProperty("CATEGORY_NAME")
    private String categoryName;

    @JsonProperty("SKU_CODE")
    private String skuCode;

    @JsonProperty("SKU_NAME")
    private String skuName;

    @JsonProperty("SOURCE_AREA")
    private String sourceArea;

    @JsonProperty("TARGET_AREA")
    private String targetArea;

    @JsonProperty("BATCH_NO")
    private String batchNo;

    @JsonProperty("PRIMARY_QTY")
    private BigDecimal primaryQty; // 更适合金额/数量，避免 double 精度问题

    @JsonProperty("PRIMARY_UNIT")
    private String primaryUnit;

    @JsonProperty("PRODUCE_DATE")
    private String produceDate;

    @JsonProperty("VALIDITY")
    private String validity;

    @JsonProperty("PLAN_DELIVERY_DATE")
    private String planDeliveryDate;

    @JsonProperty("QUALITY_FLAG")
    private String qualityFlag;

    @JsonProperty("QUALITY_STATUS")
    private String qualityStatus;

    @JsonProperty("PRODUCER_BATCH_NO")
    private String producerBatchNo;

    @JsonProperty("ORGANIZATION")
    private String organization;

    @JsonProperty("COMPANY")
    private String company;

    @JsonProperty("IS_DELETE")
    private String isDelete;
}