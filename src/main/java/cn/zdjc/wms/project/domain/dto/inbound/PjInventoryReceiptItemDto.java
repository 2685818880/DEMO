package cn.zdjc.wms.project.domain.dto.inbound;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * 收货单明细数据传输对象（DTO）
 * 用于表示入库收货操作中的单个物料行项信息。
 */
@Data
public class PjInventoryReceiptItemDto {
    /**
     * 业务订单号
     * 触发本次收货的上游业务单据编号，如采购订单号、调拨单号等。
     */
    private String businessOrderNo;

    /**
     * 业务订单明细号
     * 上游业务单据中对应的具体行项目编号，用于精确匹配。
     */
    private String businessItemNo;

    /**
     * ASN 主键 ID（内部系统ID）
     * 对应高级发货通知（ASN）记录的唯一数据库主键。
     */
    private String asnId;

    /**
     * ASN 单号
     * 高级发货通知（Advanced Shipping Notice）的业务单号，用于与供应商协同。
     */
    private String asnNo;

    /**
     * 明细项唯一标识（可选）
     * 用于前端或接口中标识当前行项，便于更新或校验。
     */
    private String id;

    /**
     * 明细行项目编号
     * 在原始业务单据（如采购订单）中的行项目编号。
     */
    private String itemNo;

    /**
     * 收货数量（必须大于0）
     * 表示本次实际收货的物料数量，使用高精度数值类型以支持小数计量。
     */
    @NotNull(message = "收货数量不能为空")
    @Positive(message = "收货数量必须大于0")
    private BigDecimal qty;

    /**
     * 计量单位
     * 例如：件、箱、千克等，与物料主数据中的单位保持一致。
     */
    private String unit;

    /**
     * 业务类型
     * 标识该收货行项所属的业务场景，如：
     * - PURCHASE：采购入库
     * - TRANSFER：调拨入库
     * - RETURN：退货入库
     * - PRODUCTION：生产入库
     */
    private String businessType;

    /**
     * 批次号
     * 用于追踪物料的生产或到货批次，支持质量追溯和先进先出（FIFO）管理。
     */
    private String batchNo;

    /**
     * 客户编码
     * 当前操作涉及的客户唯一标识，适用于代管库或客户专属库存场景。
     */
    private String customerCode;

    /**
     * 货格编号（库位）
     * 指定该物料应上架的具体存储位置，格式如：A-01-02-03。
     */
    private String cellNo;



    /**
     * SKU 编码
     * 物料的标准化库存单位编码，全局唯一，用于精确识别物料。
     */
    private String skuCode;

    /**
     * SKU 名称
     * 物料的描述性名称，便于用户识别，如“磷酸铁锂电池 100Ah”。
     */
    private String skuName;



    private String remark;
    /**
     * 手动校验数据合法性（兼容性保留）
     * 主要用于非 Spring 环境或未启用 Bean Validation 的场景。
     * 当前仅校验收货数量是否有效。
     *
     * @throws IllegalArgumentException 当收货数量为空或小于等于0时抛出
     */
    public void checkData() {
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("收货数量不能为空且必须大于0!");
        }
    }
}