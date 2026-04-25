// ToolingContainerDto.java
package cn.zdjc.wms.project.domain.dto.inbound;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ToolingContainerDto {
    /**
     * 容器号
     */
    private String containerCode;

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

    /**
     * 批次号
     * 用于追踪物料的生产或到货批次，支持质量追溯和先进先出（FIFO）管理。
     */
    private String batchNo;

    /**
     * 数量
     */
    private BigDecimal qty;

    /**
     * 单位
     */
    private String unit;

    private String storageMaterialId;

    private String serialNo;

    private String id;

}