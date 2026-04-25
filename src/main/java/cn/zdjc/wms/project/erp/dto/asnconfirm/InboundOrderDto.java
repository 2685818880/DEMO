package cn.zdjc.wms.project.erp.dto.asnconfirm;

import cn.zdjc.wms.customize.domain.dto.ExtraDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InboundOrderDto extends ExtraDto {
    private String houseCode;
    private String busNo;
    private String busType;
    private String inTime;
    private String supplierCode;
    private String supplierName;
    private String createTime;
    private String status;
    private String extended1;
    private String extended2;
    private String extended3;
    private String extended4;
    private String extended5;

    private List<InboundOrderItemDto> items;
}