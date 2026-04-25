package cn.zdjc.wms.project.domain.dto.pick;

import lombok.Data;

@Data
public class PickDetailExtDto {

    /**
     *TO行号
     */
    private String toItemNo;

    /**
     *物料号（库存维度）
     */
    private String materialNbr;

    /**
     *工厂号（库存维度）
     * C001，B002-生产工厂，N001-物流
     */
    private String plant;

    /**
     *库存类别（库存维度）
     * 空（UU），Q（质检），S（冻结）
     */
    private String stockCategory;

    /**
     *特殊库存标识（库存维度）
     * 空-普通库存,K-寄售库存，E-销售库存，Q-WBS库存
     */
    private String specialStockIndicator;

    /**
     *特殊库存号（库存维度）
     */
    private String specialStockNbr;

    /**
     *基本单位（库存维度）
     */
    private String baseUnit;

    /**
     *库位，库存地（库存维度）
     */
    private String storageLocation;

    /**
     *存储基本单位
     */
    private String storageUnitType;

    /**
     * 物料类型
     * M-原材料，P-成品
     */
    private String skuType;

    /**
     *是否需要确认
     * 空-不需要  X-需要
     */
    private String confirmationRequired;

    /**
     *已确认标识
     * 空-未确认 X-已确认
     */
    private String confirmed;

    /**
     *证书号
     */
    private String certificateNbr;

    /**
     *SAP收货单号
     */
    private String goodsReceiptNbr;

    /**
     *SAP收货单行号
     */
    private String goodsReceiptItem;

    /**
     *源存储类型
     */
    private String sourceStorageType;

    /**
     *源存储区域
     */
    private String sourceStorageSection;

    /**
     *源存储bin
     */
    private String sourceStorageBin;

    /**
     *数量
     */
    private String qty;

    /**
     *源存储类型
     */
    private String destinationStorageType;

    /**
     *源存储区域
     */
    private String destinationStorageSection;

    /**
     *源存储bin
     */
    private String destinationStorageBin;

    /**
     *检验批号
     */
    private String inspectionLotNumber;

    /**
     *标签内容
     */
    private String labelText;

    /**
     *路线信息
     */
    private String route;

    /**
     * N-忽略
     * KB-kanban料
     * KT-Kitting料
     */
    private String skuCategory;

    private boolean executable;
}
