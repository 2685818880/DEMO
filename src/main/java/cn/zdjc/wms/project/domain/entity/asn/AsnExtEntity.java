package cn.zdjc.wms.project.domain.entity.asn;

import cn.zdjc.wms.definition.domain.entity.AsnEntity;
import cn.zdjc.wms.definition.infrastructure.enums.AsnStatus;
import com.foeris.y.common.bean.BeanAlias;
import com.foeris.y.common.bean.BeanIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * ASN 扩展实体（用于查询、展示、导出等场景）
 */
@Data
public class AsnExtEntity extends AsnEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @BeanAlias("id")
    private UUID id;

    /**
     * 仓库编码
     */
    @BeanAlias("house_code")
    private String houseCode;

    /**
     * 单据号（WMS生成的流水单号）
     */
    @BeanAlias("form_no")
    private String formNo;

    /**
     * 单据类型
     */
    @BeanAlias("form_type")
    private String formType;

    /**
     * 单据状态
     */
    @BeanAlias("asn_status")
    private AsnStatus asnStatus = AsnStatus.Created;

    /**
     * 业务单据号（SAP单号）
     */
    @BeanAlias("business_form_no")
    private String businessFormNo;

    /**
     * 业务单据类型
     */
    @BeanAlias("business_form_type")
    private String businessFormType;

    /**
     * 收货开始时间
     */
    @BeanAlias("start_time")
    private Date startTime;

    /**
     * 收货结束时间
     */
    @BeanAlias("finish_time")
    private Date finishTime;

    /**
     * 供应商代码
     */
    @BeanAlias("supplier_code")
    private String supplierCode;

    /**
     * 供应商名称
     */
    @BeanAlias("supplier_name")
    private String supplierName;

    /**
     * 货主编码
     */
    @BeanAlias("owner_code")
    private String ownerCode;

    /**
     * 货主名称
     */
    @BeanAlias("owner_name")
    private String ownerName;

    /**
     * 单据备注
     */
    @BeanAlias("remark")
    private String remark;

    /**
     * 额外字段信息
     */
    @BeanAlias("extra")
    private String extra;

    /**
     * 是否激活
     */
    @BeanAlias("is_active")
    private Boolean isActive = true;

    /**
     * 创建时间
     */
    @BeanAlias("create_datetime")
    private Date createDatetime;

    /**
     * 创建人
     */
    @BeanAlias("create_by")
    private String createBy;

    /**
     * 最后修改时间
     */
    @BeanAlias("last_modify_datetime")
    private Date lastModifyDatetime;

    /**
     * 最后修改人
     */
    @BeanAlias("last_modify_by")
    private String lastModifyBy;

    /**
     * 标记是否已修改（非持久化字段，不参与数据库操作）
     */
//    @BeanIgnore
//    private Boolean modified = false;
}