package cn.zdjc.wms.project.domain.dto.requisition;

import cn.zdjc.warehouse.inventory.infrastructure.enums.QualityStatus;
import cn.zdjc.warehouse.material.infrastructure.enums.PackageType;
import cn.zdjc.wms.project.domain.dto.pick.PickItemExtDto;
import com.foeris.y.common.bean.BeanAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequisitionItemContainerCodeExtDto implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 出库需求单ID
     */
    private String formId;

    /**
     * 关联详情ID
     */
    private String itemId;
    /**
     * 主键ID
     */
    @BeanAlias("id")
    private String id;
    /**
     * 其他系统业务单号
     */
    @BeanAlias("business_form_no")
    private String businessFormNo;

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
     * 订单需求数量(库存单位数量)
     */
    @BeanAlias("primary_qty")
    private Double primaryQty = 0D;

    /**
     * 计量单位(库存单位)
     */
    @BeanAlias("primary_unit")
    private String primaryUnit;




    /**
     * 创建时间
     */
    @BeanAlias("create_datetime")
    private Date createDatetime;


    /**
     * 容器编号
     */
    private  String containerCode;



}