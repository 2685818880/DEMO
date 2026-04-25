package cn.zdjc.wms.project.domain.vo.pick;

import cn.zdjc.wms.project.domain.entity.pick.PickItemExtEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 收货组盘
 */
@Data
public class WorkstationPickInfoVo {

    /**
     * 工作站编号
     */
    private String workstationCode;

    /**
     * 容器号
     */
    private String containerCode;

    /**
     * 订单拣选明细信息
     */
    private List<PickOrderInfoVo> pickOrderInfoVoList;


    /**
     * 下一个容器号
     */
    private String nextContainerCode;


    /**
     * 下一个容器拣选数量
     */
    private BigDecimal nextPickQty;


    /**
     * 当前绑定订单未拣选托盘数
     */
    private Integer noPickTrayQty;


}
