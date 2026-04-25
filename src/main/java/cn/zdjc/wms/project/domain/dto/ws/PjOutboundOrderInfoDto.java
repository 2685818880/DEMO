package cn.zdjc.wms.project.domain.dto.ws;

import lombok.Data;

import java.util.List;

@Data
public class PjOutboundOrderInfoDto {

    /**
     * 拣选单状态
     */
    private List<String> pickStates;

    /**
     * 出库单状态
     */
    private List<String> outStates;

    /**
     * 拣选明细id
     */
    private String pickItemId;

    /**
     * 容器号
     */
    private String containerCode;

    /**
     * 出库口
     */
    private String outStationCode;

    /**
     * 仓库号
     */
    private String houseCode;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 订单号
     */
    private List<String> orderNoList;


    /**
     * 出库口
     */
    private List<String> outStationCodeList;

    /**
     * 不包含出库口
     */
    private List<String> notOutStationCodeList;

}
