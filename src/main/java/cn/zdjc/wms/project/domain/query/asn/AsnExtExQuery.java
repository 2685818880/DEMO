package cn.zdjc.wms.project.domain.query.asn;

import cn.zdjc.warehouse.inventory.infrastructure.util.SQLAppendUtil;
import cn.zdjc.warehouse.strategy.application.allotPolicy.ExQueryBean;
import cn.zdjc.wms.definition.infrastructure.enums.AsnStatus;
import cn.zdjc.wms.project.domain.dto.asn.AsnExtDto;
import cn.zdjc.wms.project.domain.entity.asn.AsnExtEntity;
import com.foeris.y.common.jdbc.SqlUtl;
import com.foeris.y.common.string.StringUtl;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Map;

/**
 * ASN 扩展查询条件（用于列表查询、导出等）
 * <p>
 * 业务场景：
 * - ASN 主单列表查询（仓库/供应商维度）
 * - 采购订单对账（按 business_form_no）
 * - 看板物料校验（kanban_multiple 标识）
 * - 异常状态监控（如超期未收货）
 * </p>
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
public class AsnExtExQuery extends ExQueryBean {

    /**
     * 仓库编码（对应 wms_asn.house_code）
     */
    private String houseCode;

    /**
     * 单据号（SAP 单号 或 WMS 流水号，对应 form_no）
     */
    private String formNo;

    /**
     * 明细行号（对应 item_no）
     */
    private String itemNo;

    /**
     * 业务明细号（来源系统行号，对应 business_item_no）
     */
    private String businessItemNo;

    /**
     * 单据类型（如 PO、RETURN 等，对应 form_type）
     */
    private String formType;

    /**
     * 单据状态（如 Created, Confirmed, Finished 等）
     */
    private AsnStatus asnStatus;

    /**
     * 备注信息（模糊查询）
     */
    private String remark;

    /**
     * Kanban 整数倍标识（用于看板物料校验）
     */
    private String kanbanMultiple;

    /**
     * 仓库号（别名字段，部分接口使用 whse 字段名）
     */
    private String whse;

    /**
     * 来源业务单号（如 SAP PO 编号，对应 business_form_no）
     */
    private String businessFormNo;

    /**
     * 供应商名称（模糊查询）
     */
    private String supplierName;

    /**
     * 构建 ASN 主单查询条件
     * <p>
     * 特性：
     * - 精确匹配：houseCode, formNo, formType 等核心字段
     * - 模糊查询：businessFormNo, remark, supplierName
     * - 枚举状态特殊处理：支持 CreatedAndExecuting 复合状态
     * - 仓库编码双重校验：houseCode 与 whse 字段兼容
     * - 安全排序：防SQL注入
     * </p>
     *
     * @param exQuery ASN 查询条件对象（可为null）
     * @return ExQueryBean 包含完整SQL和参数
     */
    public static ExQueryBean getQuery(AsnExtExQuery exQuery) {
        // 防御性编程：空对象处理
        if (exQuery == null) {
            exQuery = new AsnExtExQuery();
            log.debug("getQuery: 传入查询条件为空，使用默认空对象");
        }

        StringBuilder whereSql = new StringBuilder(" WHERE 1 = 1 ");
        StringBuilder orderSql = new StringBuilder();
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(15);

        // ========== 精确匹配条件 ==========

        // 仓库编码（双重字段兼容：house_code / whse）
        if (!StringUtl.isEmpty(exQuery.getHouseCode())) {
            whereSql.append(" AND house_code = :houseCode ");
            param.put("houseCode", exQuery.getHouseCode().trim());
        } else if (!StringUtl.isEmpty(exQuery.getWhse())) {
            whereSql.append(" AND house_code = :whse ");
            param.put("whse", exQuery.getWhse().trim());
        }

        // 单据号（form_no）
        if (!StringUtl.isEmpty(exQuery.getFormNo())) {
            whereSql.append(" AND form_no = :formNo ");
            param.put("formNo", exQuery.getFormNo().trim());
        }

        // 明细行号（item_no）
        if (!StringUtl.isEmpty(exQuery.getItemNo())) {
            whereSql.append(" AND item_no = :itemNo ");
            param.put("itemNo", exQuery.getItemNo().trim());
        }

        // 业务明细号（business_item_no）
        if (!StringUtl.isEmpty(exQuery.getBusinessItemNo())) {
            whereSql.append(" AND business_item_no = :businessItemNo ");
            param.put("businessItemNo", exQuery.getBusinessItemNo().trim());
        }

        // 单据类型（form_type）
        if (!StringUtl.isEmpty(exQuery.getFormType())) {
            whereSql.append(" AND form_type = :formType ");
            param.put("formType", exQuery.getFormType().trim());
        }

        // Kanban 整数倍标识
        if (!StringUtl.isEmpty(exQuery.getKanbanMultiple())) {
            whereSql.append(" AND kanban_multiple = :kanbanMultiple ");
            param.put("kanbanMultiple", exQuery.getKanbanMultiple().trim());
        }

        // ========== 模糊查询条件 ==========

        // 来源业务单号（business_form_no，模糊匹配）
        if (!StringUtl.isEmpty(exQuery.getBusinessFormNo())) {
            whereSql.append(" AND business_form_no LIKE :businessFormNo ");
            param.put("businessFormNo", "%" + exQuery.getBusinessFormNo().trim() + "%");
        }

        // 备注信息（remark，模糊匹配）
        if (!StringUtl.isEmpty(exQuery.getRemark())) {
            whereSql.append(" AND remark LIKE :remark ");
            param.put("remark", "%" + exQuery.getRemark().trim() + "%");
        }

        // 供应商名称（supplier_name，模糊匹配）
        if (!StringUtl.isEmpty(exQuery.getSupplierName())) {
            whereSql.append(" AND supplier_name LIKE :supplierName ");
            param.put("supplierName", "%" + exQuery.getSupplierName().trim() + "%");
        }

        // ========== 枚举状态特殊处理 ==========

        // asn_status（支持复合状态 CreatedAndExecuting）
        if (exQuery.getAsnStatus() != null) {
            if (exQuery.getAsnStatus() == AsnStatus.CreatedAndExecuting) {
                // 复合状态：查询 Created, Executing, Finished 三种状态
                whereSql.append(" AND asn_status IN (:status1, :status2, :status3) ");
                param.put("status1", AsnStatus.Created.getOrder());
                param.put("status2", AsnStatus.Executing.getOrder());
                param.put("status3", AsnStatus.Finished.getOrder());
                log.debug("添加复合状态条件: asn_status IN (Created, Executing, Finished)");
            } else {
                // 单一状态
                whereSql.append(" AND asn_status = :asnStatus ");
                param.put("asnStatus", exQuery.getAsnStatus().getOrder());
                log.debug("添加状态条件: asn_status = {}", exQuery.getAsnStatus().getOrder());
            }
        }

        // ========== 安全排序处理 ==========
        orderSql.append(SQLAppendUtil.orderByString(exQuery.getSidx(), exQuery.getSort()));

        // ========== 构建完整SQL ==========
        String baseSql = "SELECT " + SqlUtl.getColumns(AsnExtDto.class)
                + " FROM " + SqlUtl.getTable(AsnExtEntity.class);

        log.debug("getQuery: 构建ASN查询SQL完成 - 条件数量: {}, 参数数量: {}",
                countConditions(whereSql), param.size());

        return new ExQueryBean(baseSql, whereSql.toString(), orderSql.toString(), param);
    }

    // ========== 私有辅助方法 ==========

    /**
     * 统计 WHERE 条件数量（用于日志）
     */
    private static int countConditions(StringBuilder whereSql) {
        String sql = whereSql.toString();
        return (sql.length() - sql.replace(" AND ", "").length()) / 5; // " AND " 长度为5
    }
}