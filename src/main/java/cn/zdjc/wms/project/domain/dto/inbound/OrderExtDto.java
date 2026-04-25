package cn.zdjc.wms.project.domain.dto.inbound;

import com.foeris.y.common.bean.BeanAlias;
import lombok.Data;


@Data
public class OrderExtDto {
    /**
     * 订单号
     */
    private String businessOrderNo;
    /**
     * 单据状态
     */
    private String formStatus;

    /**
     * SKU编码
     */
    @BeanAlias("sku_code")
    private String skuCode;

    /**
     * SKU名称
     */
    @BeanAlias("sku_name")
    private String skuName;

    /**
     * 批次
     */
    @BeanAlias("batch_no")
    private String batchNo;

    /**
     * 订单需求数量(库存单位数量)
     */
    @BeanAlias("primary_qty")
    private Double primaryQty = 0D;

}
