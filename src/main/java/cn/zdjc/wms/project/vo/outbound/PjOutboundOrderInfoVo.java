package cn.zdjc.wms.project.vo.outbound;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class PjOutboundOrderInfoVo {

    /**
     * 波次时间
     */
    private LocalDateTime waveLocalTime;

    /**
     * 波次时间
     */
    private Date waveTime;


    /**
     * 订单号
     */
    private String orderNo;
}
