package cn.zdjc.wms.project.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AiDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiDataService.class);

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public AiDataService(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

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
            LOGGER.error("查询库存汇总失败", e);
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getPalletizeInboundSummary(String warehouseId, int days) {
        String start = LocalDate.now().minusDays(days).atStartOfDay()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String sql = "SELECT DATE(pi.create_datetime) as day, pi.sku_code, SUM(pi.primary_qty) as qty " +
                "FROM wms_palletize_item pi " +
                "JOIN wms_palletize_form pf ON pi.palletize_form_id = pf.id " +
                "WHERE pi.is_active = 1 AND pf.house_code = :houseCode " +
                "AND pi.create_datetime >= :startTime " +
                "GROUP BY DATE(pi.create_datetime), pi.sku_code ORDER BY day";
        try {
            return jdbcTemplate.queryForList(sql,
                    new MapSqlParameterSource()
                            .addValue("houseCode", warehouseId)
                            .addValue("startTime", start));
        } catch (Exception e) {
            LOGGER.error("查询入库历史失败", e);
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getPickOutboundSummary(String warehouseId, int days) {
        String start = LocalDate.now().minusDays(days).atStartOfDay()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String sql = "SELECT DATE(create_datetime) as day, sku_code, SUM(primary_qty) as qty " +
                "FROM wms_pick_item WHERE is_active = 1 AND house_code = :houseCode " +
                "AND create_datetime >= :startTime " +
                "GROUP BY DATE(create_datetime), sku_code ORDER BY day";
        try {
            return jdbcTemplate.queryForList(sql,
                    new MapSqlParameterSource()
                            .addValue("houseCode", warehouseId)
                            .addValue("startTime", start));
        } catch (Exception e) {
            LOGGER.error("查询出库历史失败", e);
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getWarehouseList() {
        String sql = "SELECT house_no AS house_code, house_name AS warehouse_name " +
                "FROM wms_warehouse WHERE is_active = 1 ORDER BY house_no";
        try {
            return jdbcTemplate.queryForList(sql, new MapSqlParameterSource());
        } catch (Exception e) {
            LOGGER.error("查询仓库列表失败", e);
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getLocationList(String houseCode) {
        String sql = "SELECT loc_no, x_pos, y_pos, z_pos, house_code, location_type " +
                "FROM wms_storage_location WHERE is_active = 1 " +
                "AND house_code LIKE :houseCode ORDER BY loc_no";
        try {
            return jdbcTemplate.queryForList(sql,
                    new MapSqlParameterSource().addValue("houseCode", houseCode));
        } catch (Exception e) {
            LOGGER.error("查询货位列表失败 houseCode={}", houseCode, e);
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getAllLocationCoordinates(String warehouseId) {
        String sql = "SELECT loc_no, x_pos, y_pos, z_pos, house_code " +
                "FROM wms_storage_location WHERE is_active = 1 AND x_pos IS NOT NULL";
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (warehouseId != null && !warehouseId.isEmpty()) {
            sql += " AND house_code = :houseCode";
            params.addValue("houseCode", warehouseId);
        }
        try {
            return jdbcTemplate.queryForList(sql, params);
        } catch (Exception e) {
            LOGGER.error("查询货位坐标失败", e);
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getRecentPickAnomalies(int minutes) {
        String start = LocalDate.now().atTime(0, 0, 0).minusMinutes(minutes)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String sql = "SELECT p.pick_status, p.sku_code, p.primary_qty, p.create_datetime, " +
                "p.workstation_code, p.container_code " +
                "FROM wms_pick_item p WHERE p.create_datetime >= :startTime " +
                "ORDER BY p.create_datetime DESC";
        try {
            return jdbcTemplate.queryForList(sql,
                    new MapSqlParameterSource().addValue("startTime", start));
        } catch (Exception e) {
            LOGGER.error("查询拣货数据失败", e);
            return Collections.emptyList();
        }
    }

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
            LOGGER.error("查询库存汇总失败 zonePattern={}", zonePattern, e);
            return Collections.emptyList();
        }
    }

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
            LOGGER.error("查询滞销品失败 zonePattern={}", zonePattern, e);
            return Collections.emptyList();
        }
    }

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
            LOGGER.error("查询临期品失败 zonePattern={}", zonePattern, e);
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> querySkuList(String keyword) {
        String sql = "SELECT s.sku_code, s.sku_name, s.category_code, s.category_name, " +
                "s.barcode, s.package_type, s.duration_of_validity, s.validity_date_unit, " +
                "s.sku_batch_flag, s.sku_serial_flag, s.block_state, s.is_active " +
                "FROM wms_sku s WHERE s.is_active = 1";
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (keyword != null && !keyword.isEmpty()) {
            sql += " AND (s.sku_code LIKE :keyword OR s.sku_name LIKE :keyword OR s.barcode LIKE :keyword)";
            params.addValue("keyword", "%" + keyword + "%");
        }
        sql += " ORDER BY s.sku_code";
        try {
            return jdbcTemplate.queryForList(sql, params);
        } catch (Exception e) {
            LOGGER.error("查询物料档案失败 keyword={}", keyword, e);
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> querySkuCategoryList(String keyword) {
        String sql = "SELECT c.category_code, c.category_name, c.category_desc, " +
                "c.tree_level, c.child_count, c.is_active, " +
                "p.category_name AS parent_category_name " +
                "FROM wms_sku_category c " +
                "LEFT JOIN wms_sku_category p ON c.parent_id = p.id " +
                "WHERE c.is_active = 1";
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (keyword != null && !keyword.isEmpty()) {
            sql += " AND (c.category_code LIKE :keyword OR c.category_name LIKE :keyword)";
            params.addValue("keyword", "%" + keyword + "%");
        }
        sql += " ORDER BY c.tree_level, c.category_code";
        try {
            return jdbcTemplate.queryForList(sql, params);
        } catch (Exception e) {
            LOGGER.error("查询物料类别失败 keyword={}", keyword, e);
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> queryOverstockProducts(String zonePattern, BigDecimal qtyThreshold) {
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
            LOGGER.error("查询积压品失败 zonePattern={}", zonePattern, e);
            return Collections.emptyList();
        }
    }

    // ==================== ChatBI 基础数据查询 ====================

    public List<Map<String, Object>> queryLocationList(String keyword) {
        String sql = "SELECT loc_no, house_code, x_pos, y_pos, z_pos, loc_type, " +
                "storage_status, loc_use_status, forbid_in, forbid_out, " +
                "loc_is_error, loc_error_reason, is_active " +
                "FROM wms_storage_location WHERE is_active = 1";
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (keyword != null && !keyword.isEmpty()) {
            sql += " AND (loc_no LIKE :keyword OR house_code LIKE :keyword)";
            params.addValue("keyword", "%" + keyword + "%");
        }
        sql += " ORDER BY house_code, x_pos, y_pos, z_pos";
        try {
            return jdbcTemplate.queryForList(sql, params);
        } catch (Exception e) {
            LOGGER.error("查询库位失败 keyword={}", keyword, e);
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> queryContainerList(String keyword) {
        String sql = "SELECT c.container_code, c.container_type_code, c.container_type_name, " +
                "c.container_length, c.container_width, c.container_height, c.container_weight, " +
                "COALESCE(c.usage_count, 0) AS usage_count " +
                "FROM wms_container c WHERE c.is_active = 1";
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (keyword != null && !keyword.isEmpty()) {
            sql += " AND (c.container_code LIKE :keyword OR c.container_type_code LIKE :keyword OR c.container_type_name LIKE :keyword)";
            params.addValue("keyword", "%" + keyword + "%");
        }
        sql += " ORDER BY c.container_code";
        try {
            return jdbcTemplate.queryForList(sql, params);
        } catch (Exception e) {
            LOGGER.error("查询容器失败 keyword={}", keyword, e);
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> queryDispatchInfoList(String keyword) {
        String sql = "SELECT dispatch_code, dispatch_name, dispatch_type, dispatch_status, " +
                "house_code, container_code, business_form_no, business_form_type, create_datetime " +
                "FROM wms_dispatch_info WHERE is_active = 1";
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (keyword != null && !keyword.isEmpty()) {
            sql += " AND (dispatch_code LIKE :keyword OR dispatch_name LIKE :keyword OR dispatch_type LIKE :keyword)";
            params.addValue("keyword", "%" + keyword + "%");
        }
        sql += " ORDER BY create_datetime DESC";
        try {
            return jdbcTemplate.queryForList(sql, params);
        } catch (Exception e) {
            LOGGER.error("查询任务调度失败 keyword={}", keyword, e);
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> queryDispatchJobList(String keyword) {
        String sql = "SELECT task_no, task_type, task_status, house_code, container_code, " +
                "from_pos, to_pos, dispatch_id, send_time, in_time, " +
                "error_code, error_desc, business_form_no, business_form_type, " +
                "task_level, group_code " +
                "FROM wms_dispatch_job WHERE is_active = 1";
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (keyword != null && !keyword.isEmpty()) {
            sql += " AND (task_no LIKE :keyword OR task_type LIKE :keyword OR task_status LIKE :keyword " +
                    "OR from_pos LIKE :keyword OR to_pos LIKE :keyword OR container_code LIKE :keyword)";
            params.addValue("keyword", "%" + keyword + "%");
        }
        sql += " ORDER BY create_datetime DESC";
        try {
            return jdbcTemplate.queryForList(sql, params);
        } catch (Exception e) {
            LOGGER.error("查询任务信息失败 keyword={}", keyword, e);
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> querySchedulerManageList(String keyword) {
        String sql = "SELECT job_code, trigger_code, job_type, trigger_corn, " +
                "next_trigger_datetime, is_pause, is_finished, is_disable, " +
                "description, start_time_seconds " +
                "FROM sys_scheduler_manage WHERE is_active = 1";
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (keyword != null && !keyword.isEmpty()) {
            sql += " AND (job_code LIKE :keyword OR trigger_code LIKE :keyword " +
                    "OR job_type LIKE :keyword OR description LIKE :keyword)";
            params.addValue("keyword", "%" + keyword + "%");
        }
        sql += " ORDER BY job_code";
        try {
            return jdbcTemplate.queryForList(sql, params);
        } catch (Exception e) {
            LOGGER.error("查询定时器管理失败 keyword={}", keyword, e);
            return Collections.emptyList();
        }
    }
}
