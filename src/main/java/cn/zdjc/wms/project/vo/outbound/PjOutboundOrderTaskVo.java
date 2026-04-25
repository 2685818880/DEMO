package cn.zdjc.wms.project.vo.outbound;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class PjOutboundOrderTaskVo {

    /**
     * 拣选id
     */
    private String pickItemId;

    /**
     * 拣选单id
     */
    private String pickOrderId;

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
     * 下架单容器位置
     */
    private String outboundLocationCode;

    /**
     * 物料id
     */
    private String materialId;

    /**
     * 容器号
     */
    private String containerCode;

    /**
     * 出库口
     */
    private String exitLocation;

    /**
     * 波次时间
     */
    private LocalDateTime waveTime;

    /**
     * 波次时间(发送wcs时间)
     */
    private Date waveSendWcsTime;

    /**
     * 库位深度
     */
    private String depthValue;

    /**
     * 出库类型
     */
    private String outboundType;

    /**
     * 订单号
     */
    private String orderNo;
}
