package cn.zdjc.wms.project.vo.outbound;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PjOutboundOrderPickInfoVo {

    /**
     * 拣选id
     */
    private String pickItemId;

    /**
     * 拣选总数量
     */
    private BigDecimal qty;

    /**
     * 拣选数量
     */
    private BigDecimal pickedQty;

    /**
     * 仓库号
     */
    private String houseCode;

    /**
     * 容器位置
     */
    private String locationCode;

    /**
     * 物料id
     */
    private String materialId;

    /**
     * 容器号
     */
    private String containerCode;

    /**
     * 波次时间
     */
    private LocalDateTime waveTime;

    /**
     * 单据id
     */
    private String businessId;

    /**
     * 单据号
     */
    private String businessNo;

    /**
     * 单据明细id
     */
    private String businessItemId;

    /**
     * 库位深度
     */
    private String depthValue;

    /**
     * 是否紧急
     */
    private String businessType;

    /**
     * 客户编码
     */
    private String customerCode;
}
