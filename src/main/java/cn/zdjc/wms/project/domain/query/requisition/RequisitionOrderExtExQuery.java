package cn.zdjc.wms.project.domain.query.requisition;

import cn.zdjc.warehouse.strategy.application.allotPolicy.ExQueryBean;
import cn.zdjc.wms.definition.infrastructure.enums.FormStatus;
import cn.zdjc.wms.definition.infrastructure.enums.SubmitStatus;
import com.foeris.y.common.jdbc.query.QueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 领料单扩展查询条件实体（用于前端筛选、报表查询）
 * <p>
 * 用途：
 * - 领料单列表高级搜索
 * - SAP/WMS 单据对账查询
 * - 过账状态监控看板
 *
 * @version 1.1.0
 * @author xud
 * @since 2026-01-27
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RequisitionOrderExtExQuery extends ExQueryBean {
    /**
     * 工作站编号
     */
    private String workstationCode;
    /**
     * 单据状态（精确匹配，如 CREATED, APPROVED, FINISHED）
     */
    private FormStatus formStatus;

    /**
     * 单据状态列表（多选，用于 IN 查询）
     */
    private List<FormStatus> formStatusList;

    /**
     * 单据创建时间范围 - 开始（格式：yyyy-MM-dd HH:mm:ss）
     * <p>
     * 建议后端接收为 LocalDateTime，前端传 ISO 字符串
     */
    private LocalDateTime formStartTime;

    /**
     * 单据创建时间范围 - 结束
     */
    private LocalDateTime formEndTime;

    /**
     * 过账状态（精确匹配，如 NOT_SUBMITTED, SUBMITTED, FAILED）
     */
    private SubmitStatus submitStatus;

    /**
     * 过账状态列表（多选）
     */
    private List<SubmitStatus> submitStatusList;

    /**
     * 过账时间范围 - 开始
     */
    private LocalDateTime submitStartTime;

    /**
     * 过账时间范围 - 结束
     */
    private LocalDateTime submitEndTime;

    /**
     * SAP 创建时间范围 - 开始（WMS 接收 SAP 单据的时间）
     */
    private LocalDateTime sapStartTime;

    /**
     * SAP 创建时间范围 - 结束
     */
    private LocalDateTime sapEndTime;

    /**
     * 仓库号（模糊匹配或精确匹配，如 X01）
     */
    private String whse;

    /**
     * 业务单据号（支持模糊查询，如 %REQ2026%）
     */
    private String businessFormNo;

    /**
     * 单据类型（如 MATERIAL_REQ, RETURN_REQ）
     */
    private String businessFormType;

    /**
     * 创建人/申请人（用于按操作人筛选）
     */
    private String creator;

    /**
     * 单据来源系统（如 SAP, WMS, MANUAL）
     */
    private String sourceSystem;

    /**
     * 是否包含已删除单据（默认 false）
     */
    private Boolean includeDeleted = false;
}