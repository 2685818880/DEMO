package cn.zdjc.wms.project.domain.dto.palletize;


import lombok.Data;

@Data
public class CallContainerExtQuery {
    /**
     * 托盘类型
     */
    private String containerType;
    /**
     * 去向站点
     */
    private String toPos;
    /**
     * 数量
     */
    private int count;
    /**
     * 库区
     */
    private String houseCode;

    /**
     * 备注   changeBox  | callContainer
     */
    private String remark;
    /**
     * 订单箱号
     */
    private String orderContainer;
}
