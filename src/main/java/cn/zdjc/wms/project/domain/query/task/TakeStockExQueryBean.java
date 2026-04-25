package cn.zdjc.wms.project.domain.query.task;

import cn.zdjc.warehouse.strategy.application.allotPolicy.ExQueryBean;
import cn.zdjc.wms.definition.infrastructure.enums.*;
import com.foeris.y.common.bean.BeanAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 盘点主单扩展实体（用于查询、展示或导出）
 * <p>
 * 用途：
 * - 盘点任务列表展示
 * - 盘点进度监控看板
 * - 盘点单导出（Excel/PDF）
 * - 与 ERP 系统对账/同步状态
 *
 * @version 1.1.0
 * @author xud
 * @since 2021-04-01
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TakeStockExQueryBean extends ExQueryBean {

    /**
     * 仓库号（对应数据库字段：house_code）
     */
    @BeanAlias("house_code")
    private String houseCode;

    /**
     * 工厂编码（建议补充，用于多工厂场景，若数据库存在则添加 @BeanAlias）
     */
    // @BeanAlias("factory_code") // 若数据库有该字段，请取消注释
    private String factoryCode;

    /**
     * 库存地点/库区（如 X01, X102，用于筛选盘点范围）
     */
    // @BeanAlias("inventory_area") // 若数据库有该字段，请取消注释
    private String inventoryArea;

    /**
     * 盘点单号（唯一标识，对应数据库字段：stock_form_no）
     */
    @BeanAlias("stock_form_no")
    private String stockFormNo;

    /**
     * 盘点单类型（如 FULL=全盘, CYCLE=循环盘点, SAMPLE=抽盘，对应数据库字段：take_stock_type）
     */
    @BeanAlias("take_stock_type")
    private TakeStockType takeStockType;

    /**
     * 盘点状态（如 CREATED=已创建, IN_PROGRESS=进行中, FINISHED=已完成，对应数据库字段：take_stock_status）
     */
    @BeanAlias("take_stock_status")
    private FormStatus takeStockStatus;

    /**
     * 盘点单位（计量单位，如 托/件/千克，对应数据库字段：stock_unit）
     */
    @BeanAlias("stock_unit")
    private String stockUnit;

    /**
     * 创建人/操作人（发起盘点任务的用户，对应数据库字段：operator）
     */
    @BeanAlias("operator")
    private String operator;

    /**
     * 创建时间（盘点任务创建时间，建议补充，若数据库存在）
     */
    // @BeanAlias("create_time") // 若数据库有该字段，请取消注释
    private LocalDateTime createTime;

    /**
     * 盘点开始时间（实际开始执行时间，对应数据库字段：start_time）
     */
    @BeanAlias("start_time")
    private LocalDateTime startTime;

    /**
     * 盘点结束时间（所有明细完成时间，对应数据库字段：finish_time）
     */
    @BeanAlias("finish_time")
    private LocalDateTime finishTime;

    /**
     * 过账人（提交至财务/ERP 的用户，对应数据库字段：submit_by）
     */
    @BeanAlias("submit_by")
    private String submitBy;

    /**
     * 过账时间（提交至 ERP 的时间，对应数据库字段：submit_datetime）
     */
    @BeanAlias("submit_datetime")
    private LocalDateTime submitDatetime;

    /**
     * 过账状态（NOT_SUBMITTED=未过账, SUBMITTED=已过账, FAILED=过账失败，对应数据库字段：submit_status）
     */
    @BeanAlias("submit_status")
    private SubmitStatus submitStatus;

    /**
     * 审核人（审批盘点结果的用户，对应数据库字段：audit_by）
     */
    @BeanAlias("audit_by")
    private String auditBy;

    /**
     * 审核时间（审核完成时间，对应数据库字段：audit_datetime）
     */
    @BeanAlias("audit_datetime")
    private LocalDateTime auditDatetime;

    /**
     * 审核状态（NOT_AUDITED=未审核, AUDITED=已审核, REJECTED=已驳回，对应数据库字段：audit_status）
     */
    @BeanAlias("audit_status")
    private AuditStatus auditStatus;

    /**
     * 单据系统级别（WMS=仅WMS内, ERP=需同步至ERP，对应数据库字段：form_level）
     */
    @BeanAlias("form_level")
    private FormLevel formLevel;

    /**
     * 异常编码（如 STOCK_MISMATCH, SYSTEM_ERROR，对应数据库字段：error_code）
     */
    @BeanAlias("error_code")
    private String errorCode;

    /**
     * 异常描述（详细错误信息，对应数据库字段：error_desc）
     */
    @BeanAlias("error_desc")
    private String errorDesc;

    /**
     * 备注/描述（用户填写的额外说明，对应数据库字段：remark）
     */
    @BeanAlias("remark")
    private String remark;

    // =============== 非数据库字段（用于前端展示）===============

    /**
     * 盘点状态中文描述（非持久化，用于前端直接显示）
     * 示例：FINISHED → "已完成"
     */
    private String takeStockStatusDesc;

    /**
     * 过账状态中文描述
     */
    private String submitStatusDesc;

    /**
     * 审核状态中文描述
     */
    private String auditStatusDesc;

    /**
     * 盘点耗时（小时，非持久化，用于效率分析）
     * 计算逻辑：finishTime - startTime（若两者均不为空）
     */
    private Double durationHours;
}