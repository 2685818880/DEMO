package cn.zdjc.wms.project.domain.enums.ws;

import cn.zdjc.wms.project.common.enums.FrontendEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WorkstationMoveEnums implements FrontendEnum {
    ARRIVE("arrive", "到位"),
    LEAVE("leave", "离开");

    private String code;
    private String name;

}