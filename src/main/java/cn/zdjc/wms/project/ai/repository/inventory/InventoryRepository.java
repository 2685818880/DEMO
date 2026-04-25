package cn.zdjc.wms.project.ai.repository.inventory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 库存数据访问层 - 负责库存相关查询
 */
@Repository
@Slf4j
public class InventoryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public InventoryRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * 查询当前库存汇总
     */
    public List<Map<String, Object>> getCurrentStockSummary(String warehouseId) {
        String sql = "SELECT sku_code, SUM(available_qty) as total_qty, COUNT(*) as batch_count " +
                "FROM wms_storage_material WHERE is_active = 1 AND available_qty > 0";
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (warehouseId != null && !warehouseId.isEmpty()) {
            sql += " AND house_code = :houseCode";
            params.addValue("houseCode", warehouseId);
        }
        sql += " GROUP BY sku_code ORDER BY total_qty DESC";
        try {
            return jdbcTemplate.queryForList(sql, params);
        } catch (Exception e) {
            log.error("查询库存汇总失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询积压品
     */
    public List<Map<String, Object>> queryOverstockProducts(String zonePattern, java.math.BigDecimal qtyThreshold) {
        boolean hasZoneFilter = zonePattern != null && !"%".equals(zonePattern.trim()) && !"%%".equals(zonePattern.trim());
        String sql = "SELECT sm.sku_code, sm.sku_name, sm.batch_no, sm.primary_qty, sm.available_qty, " +
                "sm.primary_unit, sm.quality_status, sm.create_datetime, " +
                "wcm.container_code, wsl.loc_no AS location_code, " +
                (hasZoneFilter ? "wz.zone_name, " : "NULL AS zone_name, ") +
                "DATEDIFF(NOW(), COALESCE(sm.last_modify_datetime, sm.create_datetime)) AS dwell_days " +
                "FROM wms_storage_material sm " +
                "JOIN wms_container_material wcm ON sm.id = wcm.storage_material_id " +
                "JOIN wms_storage_location_inventory wsli ON wsli.container_code = wcm.container_code " +
                "JOIN wms_storage_location wsl ON wsl.id = wsli.location_id " +
                (hasZoneFilter ?
                        "JOIN wms_relate_zone_and_location rzl ON rzl.location_id = wsl.id " +
                        "JOIN wms_zone wz ON wz.id = rzl.zone_id " : "") +
                "WHERE sm.is_active = 1 AND sm.available_qty > 0 " +
                (hasZoneFilter ? "AND wz.zone_name LIKE :zonePattern " : "") +
                "AND sm.available_qty >= :qtyThreshold " +
                "ORDER BY sm.available_qty DESC";
        try {
            MapSqlParameterSource params = new MapSqlParameterSource().addValue("qtyThreshold", qtyThreshold);
            if (hasZoneFilter) {
                params.addValue("zonePattern", zonePattern);
            }
            return jdbcTemplate.queryForList(sql, params);
        } catch (Exception e) {
            log.error("查询积压品失败 zonePattern={}", zonePattern, e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询滞销品
     */
    public List<Map<String, Object>> querySlowMovingProducts(String zonePattern, int dwellThresholdDays) {
        boolean hasZoneFilter = zonePattern != null && !"%".equals(zonePattern.trim()) && !"%%".equals(zonePattern.trim());
        String sql = "SELECT sm.sku_code, sm.sku_name, sm.batch_no, sm.primary_qty, sm.available_qty, " +
                "sm.primary_unit, sm.quality_status, sm.create_datetime, " +
                "wcm.container_code, wsl.loc_no AS location_code, " +
                (hasZoneFilter ? "wz.zone_name, " : "NULL AS zone_name, ") +
                "DATEDIFF(NOW(), COALESCE(sm.last_modify_datetime, sm.create_datetime)) AS dwell_days " +
                "FROM wms_storage_material sm " +
                "JOIN wms_container_material wcm ON sm.id = wcm.storage_material_id " +
                "JOIN wms_storage_location_inventory wsli ON wsli.container_code = wcm.container_code " +
                "JOIN wms_storage_location wsl ON wsl.id = wsli.location_id " +
                (hasZoneFilter ?
                        "JOIN wms_relate_zone_and_location rzl ON rzl.location_id = wsl.id " +
                        "JOIN wms_zone wz ON wz.id = rzl.zone_id " : "") +
                "WHERE sm.is_active = 1 AND sm.available_qty > 0 " +
                (hasZoneFilter ? "AND wz.zone_name LIKE :zonePattern " : "") +
                "AND DATEDIFF(NOW(), COALESCE(sm.last_modify_datetime, sm.create_datetime)) >= :dwellDays " +
                "ORDER BY dwell_days DESC, sm.available_qty DESC";
        try {
            MapSqlParameterSource params = new MapSqlParameterSource().addValue("dwellDays", dwellThresholdDays);
            if (hasZoneFilter) {
                params.addValue("zonePattern", zonePattern);
            }
            return jdbcTemplate.queryForList(sql, params);
        } catch (Exception e) {
            log.error("查询滞销品失败 zonePattern={}", zonePattern, e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询临期品
     */
    public List<Map<String, Object>> queryExpiryWarningProducts(String zonePattern, int warningDays) {
        boolean hasZoneFilter = zonePattern != null && !"%".equals(zonePattern.trim()) && !"%%".equals(zonePattern.trim());
        String sql = "SELECT sm.sku_code, sm.sku_name, sm.batch_no, sm.primary_qty, sm.available_qty, " +
                "sm.primary_unit, sm.quality_status, sm.expire_date, " +
                "wcm.container_code, wsl.loc_no AS location_code, " +
                (hasZoneFilter ? "wz.zone_name, " : "NULL AS zone_name, ") +
                "DATEDIFF(STR_TO_DATE(sm.expire_date, '%Y-%m-%d'), NOW()) AS remaining_days " +
                "FROM wms_storage_material sm " +
                "JOIN wms_container_material wcm ON sm.id = wcm.storage_material_id " +
                "JOIN wms_storage_location_inventory wsli ON wsli.container_code = wcm.container_code " +
                "JOIN wms_storage_location wsl ON wsl.id = wsli.location_id " +
                (hasZoneFilter ?
                        "JOIN wms_relate_zone_and_location rzl ON rzl.location_id = wsl.id " +
                        "JOIN wms_zone wz ON wz.id = rzl.zone_id " : "") +
                "WHERE sm.is_active = 1 AND sm.available_qty > 0 " +
                (hasZoneFilter ? "AND wz.zone_name LIKE :zonePattern " : "") +
                "AND sm.expire_date IS NOT NULL " +
                "AND DATEDIFF(STR_TO_DATE(sm.expire_date, '%Y-%m-%d'), NOW()) BETWEEN 0 AND :warningDays " +
                "ORDER BY remaining_days ASC";
        try {
            MapSqlParameterSource params = new MapSqlParameterSource().addValue("warningDays", warningDays);
            if (hasZoneFilter) {
                params.addValue("zonePattern", zonePattern);
            }
            return jdbcTemplate.queryForList(sql, params);
        } catch (Exception e) {
            log.error("查询临期品失败 zonePattern={}", zonePattern, e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询库存汇总（按区域）
     */
    public List<Map<String, Object>> queryInventorySummary(String zonePattern) {
        boolean hasZoneFilter = zonePattern != null && !"%".equals(zonePattern.trim()) && !"%%".equals(zonePattern.trim());
        String sql = "SELECT sm.sku_code, MAX(sm.sku_name) AS sku_name, " +
                "sm.primary_unit, " +
                "SUM(sm.available_qty) AS total_available_qty, " +
                "SUM(sm.primary_qty) AS total_primary_qty, " +
                "COUNT(DISTINCT sm.batch_no) AS batch_count, " +
                "COUNT(DISTINCT wcm.container_code) AS location_count " +
                "FROM wms_storage_material sm " +
                "JOIN wms_container_material wcm ON sm.id = wcm.storage_material_id " +
                "JOIN wms_storage_location_inventory wsli ON wsli.container_code = wcm.container_code " +
                "JOIN wms_storage_location wsl ON wsl.id = wsli.location_id " +
                (hasZoneFilter ?
                        "JOIN wms_relate_zone_and_location rzl ON rzl.location_id = wsl.id " +
                        "JOIN wms_zone wz ON wz.id = rzl.zone_id " : "") +
                "WHERE sm.is_active = 1 AND sm.available_qty > 0 " +
                (hasZoneFilter ? "AND wz.zone_name LIKE :zonePattern " : "") +
                "GROUP BY sm.sku_code, sm.primary_unit " +
                "ORDER BY total_available_qty DESC";
        try {
            if (hasZoneFilter) {
                return jdbcTemplate.queryForList(sql,
                        new MapSqlParameterSource().addValue("zonePattern", zonePattern));
            } else {
                return jdbcTemplate.queryForList(sql, new MapSqlParameterSource());
            }
        } catch (Exception e) {
            log.error("查询库存汇总失败 zonePattern={}", zonePattern, e);
            return Collections.emptyList();
        }
    }
}
