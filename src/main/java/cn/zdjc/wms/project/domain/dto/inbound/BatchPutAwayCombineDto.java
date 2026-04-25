package cn.zdjc.wms.project.domain.dto.inbound;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.Data;

import java.util.List;


/**
 * 收货组盘
 */
@Data
public class BatchPutAwayCombineDto {

    /**
     * 工作站编号
     */
    private String workstationCode;

    /**
     * 容器号
     */
    private String containerCode;

    /**
     * 订单号
     */
    private String businessOrderNo;


    private String deviceCode;


    /**
     * 来源类型
     * 1,工作站
     * 2,pda
     */
    private Integer sourceType;

    /**
     * 订单明细
     */
    private List<PjInventoryReceiptItemDto> itemList;


    public void checkData() {
       /* if (StringUtils.isEmpty(workstationCode)) {
            throw new IllegalArgumentException("工作站编号不能为空!");
        }*/
        if (StringUtils.isEmpty(containerCode)) {
            throw new IllegalArgumentException("容器号不能为空!");
        }
//        if (StringUtils.isEmpty(businessOrderNo)) {
//            throw new IllegalArgumentException("订单号不能为空!");
//        }
        if (CollUtil.isEmpty(itemList)) {
            throw new IllegalArgumentException("订单明细集合不能为空!");
        }
        for (PjInventoryReceiptItemDto pjInventoryReceiptItemDto : itemList) {
            pjInventoryReceiptItemDto.checkData();
        }
    }


}
