package cn.zdjc.wms.project.erp.dto.sku;

import cn.zdjc.wms.customize.domain.dto.ExtraDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkuMainDataDto extends ExtraDto {

    @JsonProperty("ITEMS")
    private List<SkuItemDto> items;
}