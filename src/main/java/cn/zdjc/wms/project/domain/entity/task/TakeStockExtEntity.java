package cn.zdjc.wms.project.domain.entity.task;

import cn.zdjc.wms.definition.infrastructure.enums.*;
import com.foeris.y.common.bean.BeanAlias;
import com.foeris.y.fairy.jdbc.ddd.Domain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 盘点主单扩展实体（用于查询、展示或导出）
 *
 * @version 1.0.0
 * @author liuyk
 * @since 2021-04-01
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TakeStockExtEntity extends Domain {

    /**
     * 仓库号（对应数据库字段：house_code）
     */
    @BeanAlias("house_code")
    private String houseCode;

    /**
     * 盘点单号（对应数据库字段：stock_form_no）
     */
    @BeanAlias("stock_form_no")
    private String stockFormNo;

    /**
     * 盘点单类型（如 FULL, CYCLE 等，对应数据库字段：take_stock_type）
     */
    @BeanAlias("take_stock_type")
    private TakeStockType takeStockType;

    /**
     * 盘点状态（如 CREATED, IN_PROGRESS, FINISHED 等，对应数据库字段：take_stock_status）
     */
    @BeanAlias("take_stock_status")
    private FormStatus takeStockStatus;

    /**
     * 盘点单位（计量单位，对应数据库字段：stock_unit）
     */
    @BeanAlias("stock_unit")
    private String stockUnit;

    /**
     * 过账人（对应数据库字段：submit_by）
     */
    @BeanAlias("submit_by")
    private String submitBy;

    /**
     * 过账时间（对应数据库字段：submit_datetime）
     */
    @BeanAlias("submit_datetime")
    private Date submitDatetime;

    /**
     * 过账状态（如 NOT_SUBMITTED, SUBMITTED, FAILED 等，对应数据库字段：submit_status）
     */
    @BeanAlias("submit_status")
    private SubmitStatus submitStatus;

    /**
     * 审核人（对应数据库字段：audit_by）
     */
    @BeanAlias("audit_by")
    private String auditBy;

    /**
     * 审核时间（对应数据库字段：audit_datetime）
     */
    @BeanAlias("audit_datetime")
    private Date auditDatetime;

    /**
     * 审核状态（如 NOT_AUDITED, AUDITED, REJECTED 等，对应数据库字段：audit_status）
     */
    @BeanAlias("audit_status")
    private AuditStatus auditStatus;

    /**
     * 创建人/操作人（对应数据库字段：operator）
     */
    @BeanAlias("operator")
    private String operator;

    /**
     * 盘点开始时间（对应数据库字段：start_time）
     */
    @BeanAlias("start_time")
    private Date startTime;

    /**
     * 盘点结束时间（对应数据库字段：finish_time）
     */
    @BeanAlias("finish_time")
    private Date finishTime;

    /**
     * 单据系统级别（如 WMS, ERP 等，对应数据库字段：form_level）
     */
    @BeanAlias("form_level")
    private FormLevel formLevel;

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
     * 备注/描述（对应数据库字段：remark）
     */
    @BeanAlias("remark")
    private String remark;
}