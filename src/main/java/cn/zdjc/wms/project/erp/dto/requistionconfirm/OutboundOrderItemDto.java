package cn.zdjc.wms.project.erp.dto.requistionconfirm;

import cn.zdjc.wms.customize.domain.dto.ExtraDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutboundOrderItemDto extends ExtraDto {

    private String businessItemNo;
    private String lineNo;
    private String houseCode;
    private String sourceArea;
    private String targetArea;
    private String categoryCode;
    private String skuCode;
    private String batchNo;
    private String quality;
    private String qty;
    private String unit;
    private String extended1;
    private String extended2;
    private String extended3;
    private String extended4;
    private String extended5;
}