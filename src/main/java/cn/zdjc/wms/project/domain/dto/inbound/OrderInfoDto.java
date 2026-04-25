package cn.zdjc.wms.project.domain.dto.inbound;

import lombok.Data;

import java.util.List;


@Data
public class OrderInfoDto {
    /**
     * 工作站编号
     */
    private String workstationCode;
    /**
     * 订单号
     */
    private List<OrderExtDto> businessOrderNoList;


}
