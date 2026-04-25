package cn.zdjc.wms.project.domain.vo.pick;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拣选订单信息
 */
@Data
public class PickOrderInfoVo {

    /**
     * 拣选明细id
     */
    private String pickItemId;

    /**
     * 拣选单id
     */
    private String pickOrderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 订单明细号
     */
    private String orderItemNo;

    /**
     * 单据类型
     */
    private String orderType;

    /**
     * 单位
     */
    private String unit;

    /**
     * 物料号
     */
    private String skuCode;

    /**
     * 物料名称
     */
    private String skuName;

    /**
     * 拣选总数
     */
    private BigDecimal totalQty;

    /**
     * 已拣选数量
     */
    private BigDecimal pickedQty;

    /**
     * 库存数量
     */
    private BigDecimal inventoryQty;

    /**
     * 分配时间
     */
    private LocalDateTime allocateTime;

    /**
     * 波次时间
     */
    private LocalDateTime waveTime;

    /**
     * 物料id
     */
    private String materialId;

    /**
     * 库存id
     */
    private String cargoId;

    /**
     * 客户编码
     */
    private String customerCode;

    /**
     * 容器号
     */
    private String containerCode;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 数据类型 (1,拣选订单 2,库存数据)
     */
    private Integer dataType = 1;


    /**
     * 推荐拣选数量
     */
    private BigDecimal qty;

    /**
     * 是否紧急
     */
    private Integer isUrgent;

    /**
     * 单据明细id
     */
    private String businessItemId;
}
