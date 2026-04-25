package cn.zdjc.wms.project.domain.entity.task;

import cn.zdjc.bm.transjob.infrastructure.enums.TaskStatus;
import cn.zdjc.warehouse.inventory.infrastructure.enums.QualityStatus;
import cn.zdjc.wms.definition.infrastructure.enums.ProfitLossType;
import cn.zdjc.wms.definition.infrastructure.enums.TakeStockType;
import cn.zdjc.wms.definition.infrastructure.enums.TaskStockItemStatus;
import com.foeris.y.common.bean.BeanAlias;
import com.foeris.y.fairy.jdbc.ddd.Domain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.UUID;

/**
 * 盘点明细扩展实体（用于查询/展示）
 *
 * @version 1.0.0
 * @author liuyk
 * @since 2021-04-02
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TakeStockItemExtEntity extends Domain {

    /**
     * 仓库号（对应数据库字段：house_code）
     */
    @BeanAlias("house_code")
    private String houseCode;

    /**
     * 盘点单ID（对应数据库字段：stock_form_id）
     */
    @BeanAlias("stock_form_id")
    private UUID stockFormId;

    /**
     * 盘点单号（对应数据库字段：stock_form_no）
     */
    @BeanAlias("stock_form_no")
    private String stockFormNo;

    /**
     * 盘点单类型（对应数据库字段：stock_form_type）
     */
    @BeanAlias("stock_form_type")
    private TakeStockType stockFormType;

    /**
     * 盘点明细单号（对应数据库字段：stock_item_no）
     */
    @BeanAlias("stock_item_no")
    private String stockItemNo;

    /**
     * 盘点明细状态（对应数据库字段：stock_item_status）
     */
    @BeanAlias("stock_item_status")
    private TaskStockItemStatus stockItemStatus;

    /**
     * 托盘号（对应数据库字段：container_code）
     */
    @BeanAlias("container_code")
    private String containerCode;

    /**
     * 来源库位（对应数据库字段：location_code）
     */
    @BeanAlias("location_code")
    private String locationCode;

    /**
     * 存货编码（对应数据库字段：category_code）
     */
    @BeanAlias("category_code")
    private String categoryCode;

    /**
     * 存货名称（对应数据库字段：category_name）
     */
    @BeanAlias("category_name")
    private String categoryName;

    /**
     * 物料编码（SKU编码，对应数据库字段：sku_code）
     */
    @BeanAlias("sku_code")
    private String skuCode;

    /**
     * 物料名称（SKU名称，对应数据库字段：sku_name）
     */
    @BeanAlias("sku_name")
    private String skuName;

    /**
     * 批次号（对应数据库字段：batch_no）
     */
    @BeanAlias("batch_no")
    private String batchNo;

    /**
     * 质量状态（对应数据库字段：quality_status）
     */
    @BeanAlias("quality_status")
    private QualityStatus qualityStatus;

    /**
     * 库存数量（系统记录数量，对应数据库字段：stock_qty）
     */
    @BeanAlias("stock_qty")
    private Double stockQty;

    /**
     * 实际盘点数量（对应数据库字段：actual_qty）
     */
    @BeanAlias("actual_qty")
    private Double actualQty;

    /**
     * 库存单位（对应数据库字段：stock_unit）
     */
    @BeanAlias("stock_unit")
    private String stockUnit;

    /**
     * 盈亏类型（盈/亏，对应数据库字段：profit_loss_type）
     */
    @BeanAlias("profit_loss_type")
    private ProfitLossType profitLossType;

    /**
     * 盈亏数量（对应数据库字段：profit_loss_qty）
     */
    @BeanAlias("profit_loss_qty")
    private Double profitLossQty;

    /**
     * 出库任务号（对应数据库字段：out_task_no）
     */
    @BeanAlias("out_task_no")
    private String outTaskNo;

    /**
     * 出库口（目标库位，对应数据库字段：out_location_code）
     */
    @BeanAlias("out_location_code")
    private String outLocationCode;

    /**
     * 出库任务状态（对应数据库字段：out_task_status）
     */
    @BeanAlias("out_task_status")
    private TaskStatus outTaskStatus;

    /**
     * 出库任务下发时间（对应数据库字段：out_start_datetime）
     */
    @BeanAlias("out_start_datetime")
    private Date outStartDatetime;

    /**
     * 出库完成时间（对应数据库字段：out_finish_datetime）
     */
    @BeanAlias("out_finish_datetime")
    private Date outFinishDatetime;

    /**
     * 入库任务号（对应数据库字段：in_task_no）
     */
    @BeanAlias("in_task_no")
    private String inTaskNo;

    /**
     * 回库库位（对应数据库字段：in_location_code）
     */
    @BeanAlias("in_location_code")
    private String inLocationCode;

    /**
     * 回库任务状态（对应数据库字段：in_task_status）
     */
    @BeanAlias("in_task_status")
    private TaskStatus inTaskStatus;

    /**
     * 入库任务下发时间（对应数据库字段：in_start_datetime）
     */
    @BeanAlias("in_start_datetime")
    private Date inStartDatetime;

    /**
     * 入库完成时间（对应数据库字段：in_finish_datetime）
     */
    @BeanAlias("in_finish_datetime")
    private Date inFinishDatetime;

    /**
     * 异常编码（对应数据库字段：error_code）
     */
    @BeanAlias("error_code")
    private String errorCode;

    /**
     * 异常描述（对应数据库字段：error_desc）
     */
    @BeanAlias("error_desc")
    private String errorDesc;

    /**
     * 备注/说明（对应数据库字段：remark）
     */
    @BeanAlias("remark")
    private String remark;
}