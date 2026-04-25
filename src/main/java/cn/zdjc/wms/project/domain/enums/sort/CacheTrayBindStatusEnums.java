package cn.zdjc.wms.project.domain.enums.sort;

import cn.zdjc.wms.project.common.enums.FrontendEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CacheTrayBindStatusEnums implements FrontendEnum {
    RESERVATION_INBOUND("reservation_inbound", "预约入库"),
    INBOUND("inbound", "已入库"),
    RESERVATION_OUTBOUND("reservation_outbound", " "),
    OUTBOUND("outbound", "已出库");

    private String code;
    private String name;

}