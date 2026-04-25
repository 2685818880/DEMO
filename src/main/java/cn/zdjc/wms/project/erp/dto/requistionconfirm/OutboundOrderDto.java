package cn.zdjc.wms.project.erp.dto.requistionconfirm;

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
public class OutboundOrderDto extends ExtraDto {

    private String businessFormNo;
    private String businessFormType;
    private String auditor;
    private String auditDate;
    private String customCode;
    private String customName;
    private String extended1;
    private String extended2;
    private String extended3;
    private String extended4;
    private String extended5;

    private List<OutboundOrderItemDto> datas;

}