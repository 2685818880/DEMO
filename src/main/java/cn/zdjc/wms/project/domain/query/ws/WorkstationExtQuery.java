package cn.zdjc.wms.project.domain.query.ws;

import cn.zdjc.warehouse.ExQuery;
import cn.zdjc.wms.project.domain.entity.ws.WorkstationExtEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.foeris.y.common.bean.BeanAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

/**
 * 工作站扩展查询参数
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WorkstationExtQuery extends ExQuery {

    /**
     * 工作站编号
     */
    private String workstationCode;

    /**
     * 工作站名称
     */
    private String workstationName;

    /**
     * 工作站描述
     */
    private String workstationDescribe;

    /**
     * 是否开启 (0-关闭, 1-开启)
     */
    private String isOpen;

    /**
     * IP地址
     */
    private String workstationIp;

    /**
     * 工作站状态
     */
    private String workstationStatus;

    /**
     * 工作站模式
     */
    private String workstationMode;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 来源库位编码
     */
    private String sourceLocCode;

    /**
     * 理货单号
     */
    private String tallyOrderCode;

    /**
     * 目标库位编码
     */
    private String targetLocCode;

    /**
     * SKU编码
     */
    private String skuCode;

    /**
     * 调拨ID
     */
    private Long transferId;

    /**
     * 计划ID
     */
    private Long planId;

    /**
     * 排序列字段
     */
    private String sidx;

    /**
     * 排序方向 (asc/desc)
     */
    private String sord;

    /**
     * 订单号ID
     */
    private String orderId;
    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 构建查询条件包装器
     *
     * @return QueryWrapper
     */
    public QueryWrapper<WorkstationExtEntity> buildQueryWrapper() {
        QueryWrapper<WorkstationExtEntity> wrapper = new QueryWrapper<>();

        // 基础查询条件
        appendEqCondition(wrapper, "workstation_code", getWorkstationCode());
        appendEqCondition(wrapper, "workstation_name", getWorkstationName());
        appendEqCondition(wrapper, "workstation_ip", getWorkstationIp());
        appendEqCondition(wrapper, "is_open", getIsOpen());
        appendEqCondition(wrapper, "workstation_status", getWorkstationStatus());
        appendEqCondition(wrapper, "workstation_mode", getWorkstationMode());
        appendEqCondition(wrapper, "operator", getOperator());

        // 扩展查询条件
        appendEqCondition(wrapper, "source_loc_code", getSourceLocCode());
        appendEqCondition(wrapper, "tally_order_code", getTallyOrderCode());
        appendEqCondition(wrapper, "target_loc_code", getTargetLocCode());
        appendEqCondition(wrapper, "sku_code", getSkuCode());
        appendEqCondition(wrapper, "plan_id", getPlanId());

        // transfer_id 特殊处理：避免与 plan_id 条件冲突
        if (getTransferId() != null) {
            if (getPlanId() == null) {
                // 未指定 plan_id 时，查询 transfer_id 或 plan_id 匹配
                wrapper.and(w -> w.eq("transfer_id", getTransferId())
                        .or()
                        .eq("plan_id", getTransferId()));
            } else {
                // 已指定 plan_id，只查询 transfer_id
                wrapper.eq("transfer_id", getTransferId());
            }
        }

        // 安全排序：防止SQL注入
        String safeOrder = buildSafeOrderForWrapper(getSidx(), getSord());
        if (StringUtils.isNotBlank(safeOrder)) {
            boolean isAsc = "asc".equalsIgnoreCase(getSord());
            wrapper.orderBy(true, isAsc, safeOrder);
        } else {
            // 默认按创建时间降序
            wrapper.orderByDesc("create_datetime");
        }

        return wrapper;
    }

    /**
     * 安全追加精确匹配条件
     *
     * @param wrapper 查询包装器
     * @param column  数据库列名
     * @param value   条件值
     */
    private void appendEqCondition(QueryWrapper<WorkstationExtEntity> wrapper,
                                   String column, Object value) {
        if (value != null && !StringUtils.isEmpty(String.valueOf(value))) {
            wrapper.eq(column, value);
        }
    }

    /**
     * 构建安全的排序字段（白名单校验，防止SQL注入）
     *
     * @param sidx 排序列字段
     * @param sord 排序方向
     * @return 安全的排序列名，不包含 ORDER BY 前缀
     */
    private String buildSafeOrderForWrapper(String sidx, String sord) {
        // 定义允许排序的字段白名单
        Set<String> allowedFields = Set.of(
                "create_datetime",
                "update_datetime",
                "workstation_code",
                "workstation_name",
                "workstation_ip",
                "source_loc_code",
                "target_loc_code"
        );

        if (StringUtils.isBlank(sidx)) {
            return null;
        }

        String trimmedSidx = sidx.trim();
        if (allowedFields.contains(trimmedSidx)) {
            return trimmedSidx;
        }

        return null;
    }
}