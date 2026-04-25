package cn.zdjc.wms.project.domain.dto.inbound;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 收货组盘
 */
@Data
public class PutAwayCombineDto {

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

    /**
     * 订单明细号
     */
    private String businessItemNo;

    /**
     * sku信息
     */
    private String skuCode;

    /**
     * 收货数量
     */
    private BigDecimal qty;

    /**
     * 来源类型
     * 1,工作站
     * 2,pda
     */
    private Integer sourceType;

    public void checkData() {
       /* if (StringUtils.isEmpty(workstationCode)) {
            throw new IllegalArgumentException("工作站编号不能为空!");
        }*/
        if (StringUtils.isEmpty(containerCode)) {
            throw new IllegalArgumentException("容器号不能为空!");
        }
        if (StringUtils.isEmpty(businessOrderNo)) {
            throw new IllegalArgumentException("订单号不能为空!");
        }
        if (StringUtils.isEmpty(businessItemNo)) {
            throw new IllegalArgumentException("订单明细号不能为空!");
        }
        if (StringUtils.isEmpty(skuCode)) {
            throw new IllegalArgumentException("物料编码不能为空!");
        }
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("收货数量不能为空且必须大于0!");
        }
    }
}
