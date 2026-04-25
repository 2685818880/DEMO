package cn.zdjc.wms.project.domain.enums.ws;

import cn.zdjc.wms.project.common.enums.FrontendEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 🏗️ 工作站状态枚举
 * <p>
 * 用于标识工作站的运行状态
 * </p>
 *
 * @author WMS Team
 * @date 2026-03-23
 */
@AllArgsConstructor
@Getter
public enum WorkstationStatusEnums  implements FrontendEnum {

    /**
     * 空闲 - 可接受新订单
     */
    IDLE("IDLE", "空闲"),

    /**
     * 忙碌 - 正在执行订单任务
     */
    BUSY("BUSY", "忙碌"),

    /**
     * 维护 - 工作站维护中，不可用
     */
    MAINTENANCE("MAINTENANCE", "维护"),

    /**
     * 异常 - 工作站异常，需要干预
     */
    ERROR("ERROR", "异常"),

    /**
     * 离线 - 工作站离线，无法通信
     */
    OFFLINE("OFFLINE", "离线");

    /**
     * 状态码
     */
    private  String code;

    /**
     * 状态名称
     */
    private  String name;

}