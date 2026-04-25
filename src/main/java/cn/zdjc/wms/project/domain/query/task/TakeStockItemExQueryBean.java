package cn.zdjc.wms.project.domain.query.task;

import cn.zdjc.bm.transjob.infrastructure.enums.TaskStatus;
import cn.zdjc.warehouse.inventory.infrastructure.enums.QualityStatus;
import cn.zdjc.warehouse.strategy.application.allotPolicy.ExQueryBean;
import cn.zdjc.wms.definition.infrastructure.enums.ProfitLossType;
import cn.zdjc.wms.definition.infrastructure.enums.TakeStockType;
import cn.zdjc.wms.definition.infrastructure.enums.TaskStockItemStatus;
import com.foeris.y.common.bean.BeanAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 盘点明细扩展实体（用于查询、展示或导出）
 * <p>
 * 用途：
 * - 盘点任务执行界面（PDA/Web）
 * - 盘点差异明细报表
 * - 出入库任务联动监控
 * - 异常处理追踪
 *
 * @version 1.1.0
 * @author xud
 * @since 2021-04-02
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TakeStockItemExQueryBean extends ExQueryBean {

    /**
     * 仓库号（对应数据库字段：house_code）
     */
    @BeanAlias("house_code")
    private String houseCode;

    /**
     * 盘点单ID（主表ID，对应数据库字段：stock_form_id）
     */
    @BeanAlias("stock_form_id")
    private UUID stockFormId;

    /**
     * 盘点单号（对应数据库字段：stock_form_no）
     */
    @BeanAlias("stock_form_no")
    private String stockFormNo;

    /**
     * 盘点单类型（如 FULL=全盘, CYCLE=循环盘点，对应数据库字段：stock_form_type）
     */
    @BeanAlias("stock_form_type")
    private TakeStockType stockFormType;

    /**
     * 盘点明细单号（唯一标识，对应数据库字段：stock_item_no）
     */
    @BeanAlias("stock_item_no")
    private String stockItemNo;

    /**
     * 盘点明细状态（如 CREATED=已创建, SCANNED=已扫描, FINISHED=已完成，对应数据库字段：stock_item_status）
     */
    @BeanAlias("stock_item_status")
    private TaskStockItemStatus stockItemStatus;

    /**
     * 托盘号 / 容器编码（对应数据库字段：container_code）
     */
    @BeanAlias("container_code")
    private String containerCode;

    /**
     * 当前库存库位（来源库位，对应数据库字段：location_code）
     */
    @BeanAlias("location_code")
    private String locationCode;

    /**
     * 存货编码（物料分类编码，对应数据库字段：category_code）
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
     * 质量状态（如 OK=合格, QC=待检, LOCKED=冻结，对应数据库字段：quality_status）
     */
    @BeanAlias("quality_status")
    private QualityStatus qualityStatus;

    /**
     * 系统记录库存数量（对应数据库字段：stock_qty）
     * <p>
     * ⚠️ 建议使用 BigDecimal 避免浮点精度误差（若数据库为 DECIMAL 类型）
     */
    @BeanAlias("stock_qty")
    private BigDecimal stockQty;

    /**
     * 实际盘点数量（用户输入值，对应数据库字段：actual_qty）
     */
    @BeanAlias("actual_qty")
    private BigDecimal actualQty;

    /**
     * 库存单位（计量单位，如 托/件/kg，对应数据库字段：stock_unit）
     */
    @BeanAlias("stock_unit")
    private String stockUnit;

    /**
     * 盈亏类型（PROFIT=盘盈, LOSS=盘亏，对应数据库字段：profit_loss_type）
     * <p>
     * 计算逻辑：profitLossQty = actualQty - stockQty
     */
    @BeanAlias("profit_loss_type")
    private ProfitLossType profitLossType;

    /**
     * 盈亏数量（对应数据库字段：profit_loss_qty）
     */
    @BeanAlias("profit_loss_qty")
    private BigDecimal profitLossQty;

    // =============== 出库任务信息（用于差异处理）===============

    /**
     * 出库任务号（生成的移库任务，对应数据库字段：out_task_no）
     */
    @BeanAlias("out_task_no")
    private String outboundTaskNo; // 更清晰命名

    /**
     * 出库目标库位（如暂存区，对应数据库字段：out_location_code）
     */
    @BeanAlias("out_location_code")
    private String outboundLocationCode;

    /**
     * 出库任务状态（如 PENDING=待执行, RUNNING=执行中, COMPLETED=完成，对应数据库字段：out_task_status）
     */
    @BeanAlias("out_task_status")
    private TaskStatus outboundTaskStatus;

    /**
     * 出库任务下发时间（对应数据库字段：out_start_datetime）
     */
    @BeanAlias("out_start_datetime")
    private LocalDateTime outboundStartDatetime;

    /**
     * 出库完成时间（对应数据库字段：out_finish_datetime）
     */
    @BeanAlias("out_finish_datetime")
    private LocalDateTime outboundFinishDatetime;

    // =============== 入库（回库）任务信息 ===============

    /**
     * 入库任务号（回库任务，对应数据库字段：in_task_no）
     */
    @BeanAlias("in_task_no")
    private String inboundTaskNo;

    /**
     * 回库目标库位（原库位或新库位，对应数据库字段：in_location_code）
     */
    @BeanAlias("in_location_code")
    private String inboundLocationCode;

    /**
     * 入库任务状态（对应数据库字段：in_task_status）
     */
    @BeanAlias("in_task_status")
    private TaskStatus inboundTaskStatus;

    /**
     * 入库任务下发时间（对应数据库字段：in_start_datetime）
     */
    @BeanAlias("in_start_datetime")
    private LocalDateTime inboundStartDatetime;

    /**
     * 入库完成时间（对应数据库字段：in_finish_datetime）
     */
    @BeanAlias("in_finish_datetime")
    private LocalDateTime inboundFinishDatetime;

    // =============== 异常与备注 ===============

    /**
     * 异常编码（如 SCAN_MISMATCH, TASK_FAILED，对应数据库字段：error_code）
     */
    @BeanAlias("error_code")
    private String errorCode;

    /**
     * 异常描述（详细错误信息，对应数据库字段：error_desc）
     */
    @BeanAlias("error_desc")
    private String errorDesc;

    /**
     * 备注/说明（用户填写，对应数据库字段：remark）
     */
    @BeanAlias("remark")
    private String remark;

    // =============== 非数据库字段（用于前端展示）===============

    /**
     * 盘点明细状态中文描述（如 "已完成"）
     */
    private String stockItemStatusDesc;

    /**
     * 差异率（|盈亏量| / 系统库存，用于高亮显示）
     * 示例：0.05 表示 5%
     */
    private BigDecimal discrepancyRate;

    /**
     * 是否存在差异（actualQty != stockQty）
     */
    private Boolean hasDiscrepancy;
}