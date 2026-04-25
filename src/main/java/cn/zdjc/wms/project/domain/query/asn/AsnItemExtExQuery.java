package cn.zdjc.wms.project.domain.query.asn;

import cn.zdjc.warehouse.ExQuery;
import cn.zdjc.warehouse.inventory.infrastructure.util.SQLAppendUtil;
import cn.zdjc.warehouse.strategy.application.allotPolicy.ExQueryBean;
import cn.zdjc.wms.project.domain.dto.asn.AsnItemExtDto;
import cn.zdjc.wms.project.domain.entity.asn.AsnItemExtEntity;
import com.foeris.y.common.jdbc.SqlUtl;
import com.foeris.y.common.string.StringUtl;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * ASN 明细扩展查询条件
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AsnItemExtExQuery extends ExQuery {


    /**
     * 工作站编号
     */
    private String workstationCode;

    /**
     * 工作站名称
     */
    private String workstationName;
    // ===== 字段定义（保持原有结构）=====

    /**
     * ASN ID（主表主键）
     */
    private String asnId;

    private String asnStatus;

    private List<String> asnStatusList;


    /**
     * 存货编码（物料分类编码）
     */
    private String categoryCode;

    /**
     * Kanban 整数倍标识（用于看板物料校验）
     */
    private String kanbanMultiple;

    /**
     * ASN 单据号（WMS 生成的 ASN 编号）
     */
    private String asnNo;

    /**
     * 明细行号
     */
    private String itemNo;

    /**
     * 业务明细号（来源系统行号，如 SAP 行号）
     */
    private String businessItemNo;

    /**
     * 订单号
     */
    private String businessOrderNo;

    /**
     * SKU 编码
     */
    private String skuCode;

    /**
     * SKU 名称
     */
    private String skuName;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 工厂编码
     */
    private String factory;

    private String supplierCode;

    private String createTimeStart;
    private String createTimeEnd;
    /**
     * 获取查询条件
     *
     * @return .
     */
    public ExQueryBean getQuerySql() {
        return getQuery(this);
    }
    /**
     * 构建查询条件（与 AsnExQuery 保持一致的代码风格）
     * @param exQuery 查询条件对象
     * @return ExQueryBean 包含完整SQL查询语句
     */
    public static ExQueryBean getQuery(AsnItemExtExQuery exQuery) {
        StringBuilder whereSql = new StringBuilder(" where 1 = 1 ");
        StringBuilder orderSql = new StringBuilder();
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(15);

        if (exQuery != null) {
            // ===== 精确匹配条件 =====

            // asn_id
            if (!StringUtl.isEmpty(exQuery.getAsnId())) {
                whereSql.append(" and asn_id = :asnId ");
                param.put("asnId", exQuery.getAsnId());
            }

            // asn_status（枚举类型特殊处理）
            if (exQuery.getAsnStatus() != null) {
                whereSql.append(" and asn_status = :asnStatus ");
                param.put("asnStatus", exQuery.getAsnStatus());
            }

            // category_code
            if (!StringUtl.isEmpty(exQuery.getCategoryCode())) {
                whereSql.append(" and category_code = :categoryCode ");
                param.put("categoryCode", exQuery.getCategoryCode());
            }

            // kanban_multiple
            if (!StringUtl.isEmpty(exQuery.getKanbanMultiple())) {
                whereSql.append(" and kanban_multiple = :kanbanMultiple ");
                param.put("kanbanMultiple", exQuery.getKanbanMultiple());
            }

            // asn_no
            if (!StringUtl.isEmpty(exQuery.getAsnNo())) {
                whereSql.append(" and asn_no = :asnNo ");
                param.put("asnNo", exQuery.getAsnNo());
            }

            // item_no
            if (!StringUtl.isEmpty(exQuery.getItemNo())) {
                whereSql.append(" and item_no = :itemNo ");
                param.put("itemNo", exQuery.getItemNo());
            }

            // business_item_no
            if (!StringUtl.isEmpty(exQuery.getBusinessItemNo())) {
                whereSql.append(" and business_item_no = :businessItemNo ");
                param.put("businessItemNo", exQuery.getBusinessItemNo());
            }

            // business_order_no（模糊查询）
            if (!StringUtl.isEmpty(exQuery.getBusinessOrderNo())) {
                whereSql.append(" and business_order_no like :businessOrderNo ");
                param.put("businessOrderNo", "%" + exQuery.getBusinessOrderNo() + "%");
            }

            // sku_code
            if (!StringUtl.isEmpty(exQuery.getSkuCode())) {
                whereSql.append(" and sku_code = :skuCode ");
                param.put("skuCode", exQuery.getSkuCode());
            }

            // sku_name（模糊查询）
            if (!StringUtl.isEmpty(exQuery.getSkuName())) {
                whereSql.append(" and sku_name like :skuName ");
                param.put("skuName", "%" + exQuery.getSkuName() + "%");
            }

            // batch_no
            if (!StringUtl.isEmpty(exQuery.getBatchNo())) {
                whereSql.append(" and batch_no = :batchNo ");
                param.put("batchNo", exQuery.getBatchNo());
            }

            // factory
            if (!StringUtl.isEmpty(exQuery.getFactory())) {
                whereSql.append(" and factory = :factory ");
                param.put("factory", exQuery.getFactory());
            }

            // supplier_code
            if (!StringUtl.isEmpty(exQuery.getSupplierCode())) {
                whereSql.append(" and supplier_code = :supplierCode ");
                param.put("supplierCode", exQuery.getSupplierCode());
            }

            // ===== 时间范围查询 =====

            // create_time 起始时间
            if (!StringUtl.isEmpty(exQuery.getCreateTimeStart())) {
                whereSql.append(" and create_time >= :createTimeStart ");
                param.put("createTimeStart", exQuery.getCreateTimeStart());
            }

            // create_time 结束时间
            if (!StringUtl.isEmpty(exQuery.getCreateTimeEnd())) {
                whereSql.append(" and create_time <= :createTimeEnd ");
                param.put("createTimeEnd", exQuery.getCreateTimeEnd());
            }

            // ===== 排序处理 =====
            orderSql.append(SQLAppendUtil.orderByString(exQuery.getSidx(), exQuery.getSord()));
        }

        // ===== 构建完整SQL =====
        String baseSql = "select " + SqlUtl.getColumns(AsnItemExtDto.class)
                + " from " + SqlUtl.getTable(AsnItemExtEntity.class);

        return new ExQueryBean(baseSql, whereSql.toString(), orderSql.toString(), param);
    }
}