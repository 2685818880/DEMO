package cn.zdjc.wms.project.paas.excel.data;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


/**
 * @author caoxianlei
 */
@Getter
@Setter
@EqualsAndHashCode
public class DemoData {

    @ExcelProperty("仓库编码")
    private String houseCode;

    @ExcelProperty("物料SN")
    private String serialNo;

    @ExcelProperty("销售订单号")
    private String saleOrderNo;

    @ExcelProperty("销售订单行号")
    private String saleOrderItem;

    @ExcelProperty("生产订单号")
    private String productionOrderNo;

    @ExcelProperty("工序号")
    private String processCode;

    @ExcelProperty("预留单号")
    private String reserveNo;

    @ExcelProperty("预留明细行号")
    private String reserveItem;

    @ExcelProperty("托盘号")
    private String containerCode;

    @ExcelProperty("子托盘号")
    private String subContainerCode;

    @ExcelProperty("入库业务类型")
    private String enterBusinessFormType;

    @ExcelProperty("入库业务单号")
    private String enterBusinessFormNo;

    @ExcelProperty("入库业务明细行号")
    private String enterBusinessItemNo;

    @ExcelProperty("出库业务类型")
    private String exitBusinessFormType;

    @ExcelProperty("出库业务单号")
    private String exitBusinessFormNo;

    @ExcelProperty("出库业务明细行号")
    private String exitBusinessItemNo;

    @ExcelProperty("存货分类编码")
    private String categoryCode;

    @ExcelProperty("存货分类名称")
    private String categoryName;

    @ExcelProperty("sku编码")
    private String skuCode;

    @ExcelProperty("sku名称")
    private String skuName;

    @ExcelProperty("工厂号")
    private String factoryCode;

    @ExcelProperty("批次")
    private String batchNo;

    @ExcelProperty("数量I")
    @NumberFormat("0.000_ ")
    private Double primaryQty;

    @ExcelProperty("单位I")
    private String primaryUnit;

    @ExcelProperty("数量II")
    @NumberFormat("0.000_ ")
    private Double auxiliaryQty;

    @ExcelProperty("单位II")
    private String auxiliaryUnit;

    @ExcelProperty("入库来源位置")
    private String enterPoint;

    @ExcelProperty("入库目标库位")
    private String enterLoc;

    @ExcelProperty("入库时间")
    private Date enterTime;

    @ExcelProperty("出库来源库位")
    private String exitLoc;

    @ExcelProperty("出库目标位置")
    private String exitPoint;

    @ExcelProperty("出库时间")
    private Date exitTime;
}
