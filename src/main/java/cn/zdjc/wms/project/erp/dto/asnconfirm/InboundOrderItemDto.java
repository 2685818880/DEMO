package cn.zdjc.wms.project.erp.dto.asnconfirm;

import cn.zdjc.wms.customize.domain.dto.ExtraDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InboundOrderItemDto  extends ExtraDto {
    private String itemNo;
    private String lineNo;
    private String categoryCode;
    private String categoryName;
    private String skuCode;
    private String skuName;
    private String actualQty;
    private String primaryUnit;
    private String batchNo;
    private String isDelete;
    private String extended1;
    private String extended2;
    private String extended3;
    private String extended4;
    private String extended5;

}