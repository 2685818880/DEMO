package cn.zdjc.wms.project.domain.vo.pick;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PickConfirmItemVo {

    /**
     * 发货单号
     */
    private String externalNo;

    /**
     * 客户号
     */
    private String customerCode;

    /**
     * 明细号
     */
    private String externalLineNo;

    /**
     * 明细id
     */
    private String businessItemId;

    /**
     * 物料编码
     */
    private String skuCode;

    /**
     * 物料名称
     */
    private String skuName;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 单位
     */
    private String unit;

    /**
     * 拣选数量
     */
    private BigDecimal qty;

    /**
     * 组盘单id
     */
    private String combineOrderId;
}
