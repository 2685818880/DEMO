package cn.zdjc.wms.project.domain.query.palletize;

import cn.zdjc.warehouse.inventory.infrastructure.enums.QualityStatus;
import cn.zdjc.warehouse.strategy.application.allotPolicy.ExQueryBean;
import cn.zdjc.wms.common.annotation.Comment;
import cn.zdjc.wms.definition.infrastructure.enums.PalletizeFormStatus;
import com.foeris.y.common.bean.BeanAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 组盘明细扩展查询/展示实体
 * <p>
 * 用途：
 * - 组盘任务执行界面（PDA/Web）
 * - 组盘结果报表导出
 * - 与 ASN/订单对账
 *
 * @version 1.1.0 （优化版）
 * @author [您的姓名]
 * @since 2026-01-27
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PalletizeItemExtQuery extends ExQueryBean {

    /**
     * 组盘明细单号（唯一标识，对应数据库字段：item_form_no）
     */
    @Comment("组盘明细单号")
    @BeanAlias("item_form_no")
    private String itemFormNo;

    /**
     * 组盘主单ID（关联主表，对应数据库字段：palletize_form_id）
     */
    @BeanAlias("palletize_form_id")
    private UUID palletizeFormId;

    /**
     * 组盘单号（主单号，对应数据库字段：palletize_form_no）
     */
    @BeanAlias("palletize_form_no")
    private String palletizeFormNo;

    /**
     * 收料单号（ASN 单号，对应数据库字段：asn_form_no）
     */
    @BeanAlias("asn_form_no")
    private String asnFormNo;

    /**
     * 收料明细行号（对应数据库字段：asn_item_no）
     */
    @BeanAlias("asn_item_no")
    private String asnItemNo;

    /**
     * 收料明细详情号（如批次拆分后的子行，对应数据库字段：asn_detail_no）
     */
    @BeanAlias("asn_detail_no")
    private String asnDetailNo;

    /**
     * 拣选详情ID（关联拣选任务，对应数据库字段：pick_detail_id）
     */
    @BeanAlias("pick_detail_id")
    private UUID pickDetailId;

    /**
     * 需求订单号（如销售订单、生产订单，对应数据库字段：order_form_no）
     */
    @BeanAlias("order_form_no")
    private String orderFormNo;

    /**
     * 需求订单行号（对应数据库字段：order_item_no）
     */
    @BeanAlias("order_item_no")
    private String orderItemNo;

    /**
     * 托盘号 / 容器编码（组盘后生成的托盘ID，对应数据库字段：container_code）
     */
    @BeanAlias("container_code")
    private String containerCode;

    /**
     * 物料序列号（唯一标识单品，对应数据库字段：serial_no）
     */
    @BeanAlias("serial_no")
    private String serialNo;

    /**
     * 品类编码（物料大类，对应数据库字段：category_code）
     */
    @BeanAlias("category_code")
    private String categoryCode;

    /**
     * 品类名称（对应数据库字段：category_name）
     */
    @BeanAlias("category_name")
    private String categoryName;

    /**
     * 物料编码（SKU 编码，对应数据库字段：sku_code）
     */
    @BeanAlias("sku_code")
    private String skuCode;

    /**
     * 物料名称（SKU 名称，对应数据库字段：sku_name）
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
     * 主计量单位库存数量（如 120 件，对应数据库字段：primary_qty）
     * <p>
     */
    @BeanAlias("primary_qty")
    private BigDecimal primaryQty;

    /**
     * 主计量单位（如 "EA", "PCS"，对应数据库字段：primary_unit）
     */
    @BeanAlias("primary_unit")
    private String primaryUnit;

    /**
     * 辅助计量数量（如 10 箱，对应数据库字段：auxiliary_qty）
     */
    @BeanAlias("auxiliary_qty")
    private BigDecimal auxiliaryQty;

    /**
     * 辅助计量单位（如 "BOX", "PALLET"，对应数据库字段：auxiliary_unit）
     */
    @BeanAlias("auxiliary_unit")
    private String auxiliaryUnit;

    /**
     * 库存物料ID（关联 wms_storage_material 表，对应数据库字段：storage_material_id）
     */
    @BeanAlias("storage_material_id")
    private UUID storageMaterialId;

    // =============== 新增关键字段（v1.1.0）===============

    /**
     * 组盘完成时间（对应数据库字段：complete_time）
     */
    @BeanAlias("complete_time")
    private LocalDateTime completeTime;

    /**
     * 操作人（执行组盘的用户，对应数据库字段：operator）
     */
    @BeanAlias("operator")
    private String operator;

    /**
     * 目标库位（组盘后存放位置，对应数据库字段：target_location）
     */
    @BeanAlias("target_location")
    private String targetLocation;

    /**
     * 组盘状态（如 CREATED, IN_PROGRESS, COMPLETED，若数据库有该字段）
     */
     private PalletizeFormStatus palletizeFormStatus;


     private List<PalletizeFormStatus> palletizeFormStatusList;

    private List<String> containerCodeList;

    // =============== 非数据库字段（用于前端展示）===============

    /**
     * 是否已上架（非持久化，用于任务看板）
     */
    private Boolean isPutaway;

    /**
     * 总数量（主+辅单位换算后，用于统一展示）
     */
    private BigDecimal totalQtyInPrimaryUnit;
}