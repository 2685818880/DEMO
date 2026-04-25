package cn.zdjc.wms.project.domain.dto.pick;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WorkstationCompletePickItemDto {

    /**
     * 出库单
     */
    private String orderNo;

    /**
     * 出库单明细
     */
    private String orderItemNo;

    /**
     * 拣选明细id
     */
    private String pickItemId;

    /**
     * 拣选数量
     */
    private BigDecimal qty;

    /**
     * 单据类型
     */
    private String orderType;

    /**
     * sku信息
     */
    private String skuCode;

    /**
     * sku信息
     */
    private String skuName;
    /**
     * 收货数量
     */
    private String unit;


    public void checkData() {
        if (StringUtils.isEmpty(orderNo)) {
            throw new IllegalArgumentException("出库单不能为空!");
        }
        if (StringUtils.isEmpty(orderItemNo)) {
            throw new IllegalArgumentException("出库单明细不能为空!");
        }
        if (StringUtils.isEmpty(pickItemId)) {
            throw new IllegalArgumentException("拣选明细id不能为空!");
        }
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("拣选数量不能为空且必须大于0!");
        }
    }


}
