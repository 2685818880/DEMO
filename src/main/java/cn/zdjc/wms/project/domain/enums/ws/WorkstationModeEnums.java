package cn.zdjc.wms.project.domain.enums.ws;

import cn.zdjc.wms.project.common.enums.FrontendEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WorkstationModeEnums implements FrontendEnum {

    MATERIAL_RECEIVE("material_receive", "收货"),
    MATERIAL_PICK("material_pick", "拣选"),

    // ========== 工装器具作业 ==========
    TOOLING_REGISTER("tooling_register", "新到工装建帐"),
    TOOLING_INBOUND("tooling_inbound", "新到工装入库"),
    TOOLING_PICK("tooling_pick", "新到工装拣选"),

    NO_MODE("no_mode", "无模式");
    private String code;
    private String name;

}