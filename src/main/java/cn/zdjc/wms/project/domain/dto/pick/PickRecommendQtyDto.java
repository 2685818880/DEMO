package cn.zdjc.wms.project.domain.dto.pick;

import cn.zdjc.wms.project.domain.vo.pick.PickOrderInfoVo;
import lombok.Data;

@Data
public class PickRecommendQtyDto extends PickOrderInfoVo {
    /**
     * 容器号
     */
    private String containerCode;
}
