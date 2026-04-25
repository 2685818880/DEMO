package cn.zdjc.wms.project.domain.dto.inbound;

import lombok.Data;

import java.util.List;


@Data
public class ToolingOrderContainerBindDto {


    private String workstationCode;


    private String businessOrderNo;


    private List<ToolingContainerDto> containerCodeList;

}
