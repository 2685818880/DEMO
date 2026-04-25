package cn.zdjc.wms.project.domain.dto.pick;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.Data;

import java.util.List;

@Data
public class WorkstationCompletePickDto {

    /**
     * 工作站编号
     */
    private String workstationCode;

    /**
     * 容器号
     */
    private String containerCode;

    /**
     * 订单容器号
     */
    private String orderContainerCode;

    /**
     * 拣选明细集合
     */
    private List<WorkstationCompletePickItemDto> pickItemList;


    public void checkData() {
        if (StringUtils.isEmpty(workstationCode)) {
            throw new IllegalArgumentException("工作站编号不能为空!");
        }
        if (StringUtils.isEmpty(containerCode)) {
            throw new IllegalArgumentException("容器号不能为空!");
        }
        if (StringUtils.isEmpty(orderContainerCode)) {
            throw new IllegalArgumentException("订单容器号不能为空!");
        }
        if (ObjectUtils.isEmpty(pickItemList)) {
            throw new IllegalArgumentException("拣选明细集合不能为空!");
        }
        for (WorkstationCompletePickItemDto workstationCompletePickItemDto : pickItemList) {
            workstationCompletePickItemDto.checkData();
        }
    }


}
