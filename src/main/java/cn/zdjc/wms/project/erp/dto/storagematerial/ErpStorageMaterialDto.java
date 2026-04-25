package cn.zdjc.wms.project.erp.dto.storagematerial;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.foeris.y.common.tree.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErpStorageMaterialDto extends Dto {

    @JsonProperty("SKU_CODE")
    private String sku_code;

    @JsonProperty("BATCH_NO")
    private String batch_no;

    @JsonProperty("HOUSE_CODE")
    private String house_code;

    @JsonProperty("QUALITY_STATUS")
    private String quality_status;

    @JsonProperty("AREA")
    private String area;
}
