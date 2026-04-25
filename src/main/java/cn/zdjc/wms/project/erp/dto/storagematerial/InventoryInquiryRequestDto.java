package cn.zdjc.wms.project.erp.dto.storagematerial;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 库存查询请求 DTO
 * 对应字段说明：
 * - 所有 JSON 字段名与接口定义严格一致（使用 @JsonProperty）
 * - 使用 JSR-380 验证注解（可选）
 * - 日期使用 LocalDate（自动支持 yyyy-MM-dd 格式）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryInquiryRequestDto {

    @NotBlank(message = "库房编码不能为空")
    @JsonProperty("HOUSE_CODE")
    private String houseCode;

    @NotBlank(message = "SKU编码不能为空")
    @JsonProperty("SKU_CODE")
    private String skuCode;

    @JsonProperty("AREA")
    private String area; // 可为 null 或空

    @JsonProperty("SKU_NAME")
    private String skuName;

    @JsonProperty("BATCH_NO")
    private String batchNo;

    @NotNull(message = "数量不能为空")
    @JsonProperty("QTY")
    private Integer qty;

    @JsonProperty("QUALITY_STATUS")
    private String qualityStatus;

    @JsonProperty("UNIT")
    private String unit;

    @JsonProperty("PRODUCE_DATE")
    private LocalDate produceDate;

    @JsonProperty("VALIDITY")
    private LocalDate validity;

    @JsonProperty("ORGANIZATION")
    private String organization;

    @JsonProperty("COMPANY")
    private String company;
}