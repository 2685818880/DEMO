package cn.zdjc.wms.project.domain.dto.pick;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CargoAllocateDto {

    private String businessNo;

    private String businessItemNo;

    private String skuCode;

    private BigDecimal qty;

    private List<String> houseCode;
}
