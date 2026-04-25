package cn.zdjc.wms.project.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum IfEnums implements FrontendEnum {
    YES("yes", "是"),
    NO("no", "否");

    private String code;
    private String name;

}