package cn.zdjc.wms.project.domain.dto.pick;

import cn.zdjc.warehouse.inventory.infrastructure.enums.FlagStatus;
import cn.zdjc.warehouse.inventory.infrastructure.enums.PackageLevel;
import cn.zdjc.warehouse.inventory.infrastructure.enums.QualityStatus;
import cn.zdjc.wms.outbound.pick.infrastructure.enums.PickStatus;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.foeris.y.common.bean.BeanAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PickItemExtDto {
    @BeanAlias("container_code")
    @ExcelProperty(value = "周转箱号")
    private String containerCode;

    @BeanAlias("pick_type")
    @ExcelProperty(value = "业务类型")
    private String pickType;

    @BeanAlias("order_type")
    @ExcelProperty(value = "出库类型")
    private String orderType;

    @BeanAlias("sku_code")
    @ExcelProperty(value = "物料号")
    private String skuCode;

    @ExcelProperty(value = "工厂号")
    private String factoryCode;

    @BeanAlias("business_form_no")
    @ExcelProperty(value = "被分配TO号")
    private String businessFormNo;

    @BeanAlias("business_item_no")
    @ExcelProperty(value = "被分配的TO行号")
    private String businessItemNo;

    @BeanAlias("wave_time")
    @ExcelProperty(value = "波次时间")
    private String waveTime;

    @BeanAlias("primary_qty")
    @ExcelProperty(value = "被分配的数量")
    private Double primaryQty;

    /**
     * 已拣数量
     */
    @BeanAlias("picked_qty")
    @ExcelIgnore
    private Double pickedQty;

    /**
     * 库存位置
     */
    @BeanAlias("source_location")
    @ExcelProperty(value = "分配库存库位")
    private String sourceLocation;

    @BeanAlias("workstation_code")
    @ExcelProperty(value = "被分配的工作站")
    private String workstationCode;

    @BeanAlias("create_datetime")
    @ExcelProperty(value = "被分配的时间")
    @DateTimeFormat(value = "yyyy-MM-dd HH:mm:ss")
    private Date createDatetime;

    /**
     * 源区域
     */
    @BeanAlias("source_area")
    @ExcelIgnore
    private String sourceArea;
    /**
     * 物料名称
     */
    @BeanAlias("sku_name")
    @ExcelIgnore
    private String skuName;
    /**
     * 批次号
     */
    @BeanAlias("batch_no")
    @ExcelIgnore
    private String batchNo;
    /**
     * 质量状态
     */
    @BeanAlias("quality_status")
    @ExcelIgnore
    private QualityStatus qualityStatus;
    /**
     * 物料SN号
     */
    @BeanAlias("serial_no")
    @ExcelIgnore
    private String serialNo;

    /**
     * 计量单位
     */
    @BeanAlias("unit")
    @ExcelIgnore
    private String unit;
    /**
     * 货主
     */
    @BeanAlias("owner_code")
    @ExcelIgnore
    private String ownerCode;
    /**
     * 包装等级
     */
    @BeanAlias("package_level")
    @ExcelIgnore
    private PackageLevel packageLevel;
    /**
     * 母包装条码
     */
    @BeanAlias("package_no")
    @ExcelIgnore
    private String packageNo;
    /**
     * 包装数量
     */
    @BeanAlias("package_amount")
    @ExcelIgnore
    private Integer packageAmount;
    /**
     * 当前物料齐套标识
     */
    @BeanAlias("kitting_flag")
    @ExcelIgnore
    private FlagStatus kittingFlag = FlagStatus.Y;

    @BeanAlias("pick_status")
    @ExcelIgnore
    private PickStatus pickStatus;
}
