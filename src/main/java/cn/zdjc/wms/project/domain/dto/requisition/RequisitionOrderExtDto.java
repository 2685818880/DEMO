package cn.zdjc.wms.project.domain.dto.requisition;

import cn.zdjc.wms.common.annotation.Comment;
import cn.zdjc.wms.definition.infrastructure.enums.AllotStatus;
import cn.zdjc.wms.definition.infrastructure.enums.FormStatus;
import cn.zdjc.wms.definition.infrastructure.enums.SubmitStatus;
import cn.zdjc.wms.outbound.wave.infrastructure.enums.WaveType;
import com.foeris.y.common.bean.BeanAlias;
import com.foeris.y.common.bean.BeanIgnore;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequisitionOrderExtDto implements Serializable {

    /**
     * WMS本地出库订单编号
     */
    private String formNo;

    /**
     * 单据类型
     * 暂定 是备货出库还是下架出库
     */
    private String formType;

    /**
     * 单据状态
     */
    private FormStatus formStatus;

    /**
     * 其他系统业务单据号
     */
    private String businessFormNo;

    /**
     * 其他系统业务类型
     */
    private String businessFormType;

    /**
     * 计划作废改字段
     */
    @Comment("分配状态")
    private AllotStatus allotStatus;

    /**
     * 接口过账状态
     */
    private SubmitStatus submitStatus;

    /**
     * 过账人
     */
    private String submitBy;

    /**
     * 过账时间
     */
    private Date submitTime;

    /**
     * 单据开始时间
     */
    private Date startTime;

    /**
     * 单据完成时间 单据被完成时间（单据物料出库完成时间）
     */
    private Date finishTime;

    /**
     * 单据关闭时间 指单据全部操作完成，也过账完成了
     */
    private Date closeTime;

    /**
     * 作废人
     */
    private String cancelBy;

    /**
     * 作废时间
     */
    private Date cancelTime;

    /**
     * 作废原因
     */
    private String cancelReason;

    /**
     * 单据备注
     */
    private String remark;

    private String ownerCode;

    private String ownerName;

    private String operator;

    /**
     * 客户号
     */
    private String customCode;

    /**
     * 客户名称
     */
    private String customName;

    /**
     * 承运商代码
     */
    private String carrierCode;

    /**
     * 承运商名称
     */
    private String carrierName;

    /**
     * 订单地址描述
     */
    private String addressDesc;

    /**
     * 订单地址ID
     */
    private String addressId;

    private WaveType waveType;

    private String relationOrderNo;

    private String auditBy;

    private Date auditDatetime;

    private FormStatus auditStatus;

    private Date deleteDatetime;

    private String deleteUser;

    private Boolean isEnable = true;

    private Integer version;

    private Integer priority;

    /**
     * 标记是否已修改（非持久化字段）
     */
//    @BeanIgnore
//    private Boolean modified = false;

    @BeanAlias("id")
    private String id;

    @BeanAlias("is_active")
    private Boolean isActive = true;

    @BeanAlias("create_datetime")
    private Date createDatetime;

    @BeanAlias("create_by")
    private String createBy;

    @BeanAlias("last_modify_datetime")
    private Date lastModifyDatetime;

    @BeanAlias("last_modify_by")
    private String lastModifyBy;


    /**
     * 订单行列表
     */
    private List<RequisitionItemExtDto> items;
}