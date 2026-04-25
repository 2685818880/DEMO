package cn.zdjc.wms.project.domain.enums.ws;

import cn.zdjc.wms.project.common.enums.FrontendEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WorkstationIsOpenEnums implements FrontendEnum {
    TRUE("true", "开启"),
    FALSE("false", "关闭");

    private String code;
    private String name;

}