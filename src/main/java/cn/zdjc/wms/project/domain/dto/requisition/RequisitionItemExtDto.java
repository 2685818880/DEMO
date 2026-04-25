package cn.zdjc.wms.project.domain.dto.requisition;

import cn.zdjc.warehouse.inventory.infrastructure.enums.QualityStatus;
import cn.zdjc.warehouse.material.infrastructure.enums.PackageType;
import cn.zdjc.wms.project.domain.dto.pick.PickItemExtDto;
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
public class RequisitionItemExtDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 出库单项目
     */
    @BeanAlias("item_no")
    private String itemNo;

    /**
     * 其他系统业务行号
     */
    @BeanAlias("business_item_no")
    private String businessItemNo;

    /**
     * 出库需求单ID
     */
    @BeanAlias("form_id")
    private String formId;

    /**
     * 出库需求单号
     */
    @BeanAlias("form_no")
    private String formNo;

    /**
     * 其他系统业务单号
     */
    @BeanAlias("business_form_no")
    private String businessFormNo;

    /**
     * 单据状态
     */
    @BeanAlias("formStatus")
    private String formStatus;

    /**
     * 工厂编号
     */
    @BeanAlias("factory_code")
    private String factoryCode;

    /**
     * 仓库号
     */
    @BeanAlias("house_code")
    private String houseCode;

    /**
     * 源区域
     */
    @BeanAlias("source_area")
    private String sourceArea;

    /**
     * 目标区域
     */
    @BeanAlias("target_area")
    private String targetArea;

    /**
     * 存货分类编码
     */
    @BeanAlias("category_code")
    private String categoryCode;

    /**
     * 存货分类名称
     */
    @BeanAlias("category_name")
    private String categoryName;

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
     * 批次
     */
    @BeanAlias("batch_no")
    private String batchNo;

    /**
     * 质量状态
     */
    @BeanAlias("quality_status")
    private QualityStatus qualityStatus;

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
     * 辅助计量
     */
    @BeanAlias("auxiliary_qty")
    private Double auxiliaryQty = 0D;

    /**
     * 辅助计量数量
     */
    @BeanAlias("auxiliary_unit")
    private String auxiliaryUnit;

    /**
     * 已分配数量
     */
    @BeanAlias("allotted_qty")
    private Double allottedQty = 0D;

    /**
     * 波次正在执行的数量
     */
    @BeanAlias("wave_qty")
    private Double waveQty = 0D;

    /**
     * 已确认数量(实际单据发出数量)
     */
    @BeanAlias("confirm_qty")
    private Double confirmQty = 0D;

    /**
     * 包装等级
     */
    @BeanAlias("package_type")
    private PackageType packageType;

    /**
     * 是否超过sku设置的保质期
     */
    @BeanAlias("is_overdue")
    private Boolean isOverdue;

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
     * 客户号
     */
    @BeanAlias("custom_code")
    private String customCode;

    /**
     * 客户名称
     */
    @BeanAlias("custom_name")
    private String customName;

    /**
     * 承运商编码
     */
    @BeanAlias("carrier_code")
    private String carrierCode;

    /**
     * 承运商名称
     */
    @BeanAlias("carrier_name")
    private String carrierName;

    /**
     * 订单地址描述
     */
    @BeanAlias("address_desc")
    private String addressDesc;

    /**
     * 订单地址ID
     */
    @BeanAlias("address_id")
    private String addressId;

    /**
     * 单据备注
     */
    @BeanAlias("remark")
    private String remark;

    /**
     * 删除时间
     */
    @BeanAlias("delete_datetime")
    private Date deleteDatetime;

    /**
     * 删除用户
     */
    @BeanAlias("delete_user")
    private String deleteUser;

    /**
     * 是否启用
     */
    @BeanAlias("is_enable")
    private Boolean isEnable = true;

    /**
     * 版本号
     */
    @BeanAlias("version")
    private Integer version;

    /**
     * 标记是否已修改（非持久化字段）
     */
//    @BeanIgnore
//    private Boolean modified = false;

    /**
     * 主键ID
     */
    @BeanAlias("id")
    private String id;

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

    // ==================== 拣选容器列表 ====================

    /**
     * 容器编号
     */
    private  String containerCode;

    /**
     * 拣选容器/任务列表
     */
    private List<PickItemExtDto> pickContainers;



}