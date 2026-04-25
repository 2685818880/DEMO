
package cn.zdjc.wms.project.domain.enums.sort;

import cn.zdjc.wms.project.common.enums.FrontendEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StationBindInfoStatusEnums implements FrontendEnum {
    BIND("bind", "已绑定"),
    RESERVATION_BIND("reservation_bind", "预约绑定");

    private String code;
    private String name;

}