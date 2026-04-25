package cn.zdjc.wms.project.domain.query.requisition;

import cn.zdjc.warehouse.ExQuery;
import cn.zdjc.warehouse.inventory.infrastructure.util.SQLAppendUtil;
import cn.zdjc.warehouse.strategy.application.allotPolicy.ExQueryBean;
import cn.zdjc.wms.common.annotation.Comment;
import cn.zdjc.wms.definition.domain.dto.AsnDetailDto;
import cn.zdjc.wms.definition.domain.entity.AsnDetailEntity;
import cn.zdjc.wms.definition.infrastructure.enums.AllotStatus;
import cn.zdjc.wms.definition.infrastructure.enums.FormStatus;
import cn.zdjc.wms.definition.infrastructure.enums.SubmitStatus;
import cn.zdjc.wms.outbound.requisition.infrastructure.pojo.ReqItemPojo;
import cn.zdjc.wms.outbound.requisition.interfaces.web.dto.ReqItemDto;
import cn.zdjc.wms.project.domain.dto.requisition.RequisitionItemExtDto;
import cn.zdjc.wms.project.domain.entity.requisition.RequisitionItemExtEntity;
import com.foeris.y.common.jdbc.SqlUtl;
import com.foeris.y.common.string.StringUtl;
import com.foreris.eris.common.exception.BusinessException;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 领料单明细扩展查询条件
 * <p>
 * 业务场景：
 * - 领料明细列表查询（工作站/仓库操作）
 * - SAP 收货对账（按 goods_receipt_nbr）
 * - 波次任务监控与追溯
 * - 异常分配分析（库存不足/库位锁定）
 * </p>
 *
 * @author WMS Team
 * @since 2026-02-09
 * @version 2.0.0
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
public class RequisitionItemExtExQuery extends ExQueryBean {

    // ========== 核心业务字段 ==========

    /**
     * 单据状态
     */
    @Comment("单据状态")
    private String formStatus;

    @Comment("分配状态")
    private String allotStatus;


    private List<String> formStatusList;
    /**
     * 工作站编号（精确匹配）
     * <p>示例：WS-001, PICK-AREA-A</p>
     */
    private String workstationCode;

    /**
     * 领料单主单ID（UUID，关联 requisition_header）
     */
    private String formId;

    /**
     * 领料单号（WMS生成，精确匹配）
     * <p>示例：REQ20260209001</p>
     */
    private String formNo;


    /**
     * 出库需求明细id
     */
    private String itemId;
    /**
     * 出库需求明细号
     */
    private String itemNo;


    /**
     * 业务单据号（SAP/ERP单号，支持模糊查询）
     * <p>示例：搜索 %260209% 可匹配 260209001, 260209002</p>
     */
    private String businessFormNo;

    /**
     * 明细行号（唯一标识，精确匹配）
     * <p>示例：0010, 0020（SAP行号规则）</p>
     */
    private String businessItemNo;

    // ========== 物料信息 ==========

    /**
     * SKU编码（精确匹配）
     * <p>示例：SKU-BATT-LFP-100AH</p>
     */
    private String skuCode;

    /**
     * SKU名称（模糊查询）
     * <p>示例：搜索 "磷酸铁锂" 可匹配相关物料</p>
     */
    private String skuName;

    /**
     * 批次号（精确匹配，用于质量追溯）
     */
    private String batchNo;

    /**
     * 检验批号（SAP质检相关，精确匹配）
     */
    private String inspectionLotNumber;

    // ========== SAP集成字段 ==========

    /**
     * SAP收货单号（用于WMS与SAP对账）
     * <p>示例：5000001234</p>
     */
    private String goodsReceiptNbr;

    /**
     * SAP收货单行号（数字字符串，精确匹配）
     * <p>示例："10", "20"（SAP标准行号间隔为10）</p>
     */
    private String goodsReceiptItem;

    // ========== 时间范围 ==========

    /**
     * 波次开始时间（ISO 8601 格式）
     * <p>前端传参示例：2026-02-09T10:00:00</p>
     */
    private LocalDateTime waveStartTime;

    /**
     * 波次结束时间（ISO 8601 格式）
     */
    private LocalDateTime waveEndTime;

    // ========== 业务状态 ==========

    /**
     * 急单标识（Y=急单, N=普通）
     * <p>数据库存储：Y/N</p>
     */
    private String emergencyStatus;

    /**
     * 分配异常标识（Y=存在异常, N=正常）
     * <p>异常类型：库存不足、库位锁定、批次不匹配等</p>
     */
    private String allotExceptionStatus;

    // ========== 扩展维度 ==========

    /**
     * 工厂编码（多工厂场景）
     * <p>示例：CN-WX-FACTORY-A</p>
     */
    private String plantCode;

    /**
     * 库存地点/库区（精确匹配）
     * <p>示例：X01（原材料区）, X102（成品区）</p>
     */
    private String inventoryArea;

    /**
     * 创建人（申请人，模糊查询）
     */
    private String creator;

    /**
     * 物料分类编码（用于按大类筛选）
     * <p>示例：RAW（原材料）, FIN（成品）</p>
     */
    private String categoryCode;

    /**
     * 状态列表（多选过滤）
     * <p>可选值：["PENDING", "ALLOCATED", "PICKED", "SHIPPED", "CANCELLED"]</p>
     */
    private List<String> statusList;

    // ========== 查询构建方法 ==========


    /**
     * 获取查询条件
     *
     * @return .
     */
    @Override
    public ExQueryBean getQuerySql() {
        return null;
    }


    /**
     * 构建领料单明细查询条件
     * <p>
     * 特性：
     * - 精确匹配：workstationCode, formNo, skuCode 等
     * - 模糊查询：businessFormNo, skuName, creator
     * - 时间范围：waveStartTime ~ waveEndTime
     * - 状态列表：IN 查询
     * - 安全排序：防SQL注入
     * </p>
     *
     * @param exQuery 查询条件对象（可为null）
     * @return ExQueryBean 包含完整SQL和参数
     */
    public  ExQueryBean getQuery(RequisitionItemExtExQuery exQuery) {
        // 防御性编程：空对象处理
        if (exQuery == null) {
            exQuery = new RequisitionItemExtExQuery();
            log.debug("getQuery: 传入查询条件为空，使用默认空对象");
        }

        StringBuilder whereSql = new StringBuilder(" WHERE 1 = 1 ");
        StringBuilder orderSql = new StringBuilder();
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(25);

        // ========== 精确匹配条件 ==========
        appendEqCondition(whereSql, param, "workstation_code", exQuery.getWorkstationCode(), "workstationCode");
        appendEqCondition(whereSql, param, "form_id", exQuery.getFormId(), "formId");
        appendEqCondition(whereSql, param, "form_no", exQuery.getFormNo(), "formNo");
        appendEqCondition(whereSql, param, "business_item_no", exQuery.getBusinessItemNo(), "businessItemNo");
        appendEqCondition(whereSql, param, "sku_code", exQuery.getSkuCode(), "skuCode");
        appendEqCondition(whereSql, param, "batch_no", exQuery.getBatchNo(), "batchNo");
        appendEqCondition(whereSql, param, "inspection_lot_number", exQuery.getInspectionLotNumber(), "inspectionLotNumber");
        appendEqCondition(whereSql, param, "goods_receipt_nbr", exQuery.getGoodsReceiptNbr(), "goodsReceiptNbr");
        appendEqCondition(whereSql, param, "goods_receipt_item", exQuery.getGoodsReceiptItem(), "goodsReceiptItem");
        appendEqCondition(whereSql, param, "emergency_status", exQuery.getEmergencyStatus(), "emergencyStatus");
        appendEqCondition(whereSql, param, "allot_exception_status", exQuery.getAllotExceptionStatus(), "allotExceptionStatus");
        appendEqCondition(whereSql, param, "plant_code", exQuery.getPlantCode(), "plantCode");
        appendEqCondition(whereSql, param, "inventory_area", exQuery.getInventoryArea(), "inventoryArea");
        appendEqCondition(whereSql, param, "category_code", exQuery.getCategoryCode(), "categoryCode");

        // ========== 模糊查询条件 ==========
        appendLikeCondition(whereSql, param, "business_form_no", exQuery.getBusinessFormNo(), "businessFormNo");
        appendLikeCondition(whereSql, param, "sku_name", exQuery.getSkuName(), "skuName");
        appendLikeCondition(whereSql, param, "creator", exQuery.getCreator(), "creator");

        // ========== 时间范围查询 ==========
        appendTimeRangeCondition(whereSql, param, "wave_start_time", exQuery.getWaveStartTime(), exQuery.getWaveEndTime());

        // ========== 状态列表 IN 查询 ==========
        appendInCondition(whereSql, param, "status", exQuery.getStatusList(), "statusList");

        // ========== 安全排序处理 ==========
        orderSql.append(SQLAppendUtil.orderByString(exQuery.getSidx(), exQuery.getSort()));

        // ========== 构建完整SQL ==========
        String baseSql = "SELECT " + SqlUtl.getColumns(RequisitionItemExtDto.class)
                + " FROM " + SqlUtl.getTable(RequisitionItemExtEntity.class);

        log.debug("getQuery: 构建SQL完成 - 条件数量: {}, 参数数量: {}",
                countConditions(whereSql), param.size());

        return new ExQueryBean(baseSql, whereSql.toString(), orderSql.toString(), param);
    }

    // ========== 私有辅助方法 ==========

    /**
     * 添加精确匹配条件（自动处理空值）
     */
    private static void appendEqCondition(StringBuilder whereSql, Map<String, Object> param,
                                          String columnName, String value, String paramName) {
        if (!StringUtl.isEmpty(value)) {
            whereSql.append(" AND ").append(columnName).append(" = :").append(paramName);
            param.put(paramName, value.trim());
            log.trace("添加精确条件: {} = '{}'", columnName, value);
        }
    }

    /**
     * 添加模糊查询条件（自动添加通配符）
     */
    private static void appendLikeCondition(StringBuilder whereSql, Map<String, Object> param,
                                            String columnName, String value, String paramName) {
        if (!StringUtl.isEmpty(value)) {
            String likeValue = "%" + value.trim() + "%";
            whereSql.append(" AND ").append(columnName).append(" LIKE :").append(paramName);
            param.put(paramName, likeValue);
            log.trace("添加模糊条件: {} LIKE '{}'", columnName, likeValue);
        }
    }

    /**
     * 添加时间范围条件（自动格式化 LocalDateTime）
     */
    private static void appendTimeRangeCondition(StringBuilder whereSql, Map<String, Object> param,
                                                 String columnName, LocalDateTime startTime, LocalDateTime endTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (startTime != null) {
            String startStr = startTime.format(formatter);
            whereSql.append(" AND ").append(columnName).append(" >= :waveStartTime");
            param.put("waveStartTime", startStr);
            log.trace("添加时间范围起始: {} >= '{}'", columnName, startStr);
        }

        if (endTime != null) {
            String endStr = endTime.format(formatter);
            whereSql.append(" AND ").append(columnName).append(" <= :waveEndTime");
            param.put("waveEndTime", endStr);
            log.trace("添加时间范围结束: {} <= '{}'", columnName, endStr);
        }
    }

    /**
     * 添加 IN 条件（支持列表查询）
     */
    private static void appendInCondition(StringBuilder whereSql, Map<String, Object> param,
                                          String columnName, List<String> values, String baseParamName) {
        if (values != null && !values.isEmpty()) {
            // 过滤空值并去重
            List<String> validValues = values.stream()
                    .filter(v -> !StringUtl.isEmpty(v))
                    .distinct()
                    .collect(Collectors.toList());

            if (!validValues.isEmpty()) {
                whereSql.append(" AND ").append(columnName).append(" IN (");
                for (int i = 0; i < validValues.size(); i++) {
                    String paramName = baseParamName + "_" + i;
                    if (i > 0) whereSql.append(", ");
                    whereSql.append(":").append(paramName);
                    param.put(paramName, validValues.get(i).trim());
                }
                whereSql.append(")");
                log.trace("添加IN条件: {} IN {}", columnName, validValues);
            }
        }
    }

    /**
     * 统计 WHERE 条件数量（用于日志）
     */
    private static int countConditions(StringBuilder whereSql) {
        String sql = whereSql.toString();
        return sql.length() - sql.replace(" AND ", "").length();
    }
}