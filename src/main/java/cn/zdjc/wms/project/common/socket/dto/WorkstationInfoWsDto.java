package cn.zdjc.wms.project.common.socket.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WorkstationInfoWsDto {

    /**
     * 工作站编号
     */
    private String workstationCode;

    /**
     * 工作站模式
     * 枚举: WorkstationModeEnums
     */
    private String workstationMode;

    /**
     * 是否开启
     */
    private String isOpen;

    /**
     * 拣选容器
     */
    private String pickContainerCode;

    /**
     * WorkstationMoveEnums
     * arrive 到位
     * leave  离开
     */
    private String pickContainerCodeStatus;

    /**
     * 拣选位置
     */
    private String pickDeviceCode;

    /**
     * 下一拣选容器
     */
    private String pickNextContainerCode;

    /**
     * WorkstationMoveEnums
     * arrive 到位
     * leave  离开
     */
    private String pickNextContainerCodeStatus;

    /**
     * 下一拣选容器 拣选数量
     */
    private BigDecimal pickQty;

    /**
     * 下一拣选订单
     */
    private String nextOrderPickOrder;

    /**
     * 下一拣选订单 对应的未拣选托盘数
     */
    private Integer nextOrderPickTrayNum;

    /**
     * 理货来源容器
     */
    private String tallySourceContainerCode;

    /**
     * WorkstationMoveEnums
     * arrive 到位
     * leave  离开
     */
    private String tallySourceContainerCodeStatus;

    /**
     * 理货来源位置
     */
    private String tallySourceDeviceCode;

    /**
     * 订单容器
     */
    private String orderContainerCode;

    /**
     * WorkstationMoveEnums
     * arrive 到位
     * leave  离开
     */
    private String orderContainerCodeStatus;

    /**
     * 订单位置
     */
    private String orderDeviceCode;

    /**
     * 当前拣选信息
     */
    private WorkstationPickInfoWsDto workstationPickInfoWsDto;
}
