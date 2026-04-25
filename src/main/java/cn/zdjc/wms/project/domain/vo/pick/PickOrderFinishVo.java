package cn.zdjc.wms.project.domain.vo.pick;

import lombok.Data;

@Data
public class PickOrderFinishVo {

    /**
     * 订单是否已完成
     */
    private Boolean orderIsFinish;

    /**
     * 提示信息
     */
    private String finishTipInfo;
}
