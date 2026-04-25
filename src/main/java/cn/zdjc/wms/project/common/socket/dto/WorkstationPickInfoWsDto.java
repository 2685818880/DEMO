package cn.zdjc.wms.project.common.socket.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WorkstationPickInfoWsDto {

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 订单明细号
     */
    private String orderItemNo;

    /**
     * 物料号
     */
    private String skuCode;

    /**
     * 物料名称
     */
    private String skuName;

    /**
     * 拣选数量
     */
    private BigDecimal pickQty;
}
