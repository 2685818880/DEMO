package cn.zdjc.wms.project.ai.repository.location;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 库位数据访问层 - 负责仓库、库位相关查询
 */
@Repository
@Slf4j
public class LocationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public LocationRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * 查询仓库列表
     */
    public List<Map<String, Object>> getWarehouseList() {
        String sql = "SELECT house_no AS house_code, house_name AS warehouse_name " +
                "FROM wms_warehouse WHERE is_active = 1 ORDER BY house_no";
        try {
            return jdbcTemplate.queryForList(sql, new MapSqlParameterSource());
        } catch (Exception e) {
            log.error("查询仓库列表失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询库位列表
     */
    public List<Map<String, Object>> getLocationList(String houseCode) {
        String sql = "SELECT loc_no, x_pos, y_pos, z_pos, house_code, location_type " +
                "FROM wms_storage_location WHERE is_active = 1 " +
                "AND house_code LIKE :houseCode ORDER BY loc_no";
        try {
            return jdbcTemplate.queryForList(sql,
                    new MapSqlParameterSource().addValue("houseCode", houseCode));
        } catch (Exception e) {
            log.error("查询货位列表失败 houseCode={}", houseCode, e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询所有库位坐标（按仓库过滤）
     */
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
            log.error("查询货位坐标失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询库位列表（支持关键字搜索）
     */
    public List<Map<String, Object>> queryLocationList(String keyword) {
        String sql = "SELECT loc_no, house_code, x_pos, y_pos, z_pos, loc_type, " +
                "storage_status, is_active FROM wms_storage_location WHERE is_active = 1";
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (keyword != null && !keyword.isEmpty()) {
            sql += " AND (loc_no LIKE :keyword OR house_code LIKE :keyword)";
            params.addValue("keyword", "%" + keyword + "%");
        }
        sql += " ORDER BY loc_no";
        try {
            return jdbcTemplate.queryForList(sql, params);
        } catch (Exception e) {
            log.error("查询库位列表失败 keyword={}", keyword, e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询容器列表（支持关键字搜索）
     */
    public List<Map<String, Object>> queryContainerList(String keyword) {
        String sql = "SELECT container_code, container_type, container_status, " +
                "house_code, loc_no, is_active FROM wms_container WHERE is_active = 1";
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (keyword != null && !keyword.isEmpty()) {
            sql += " AND (container_code LIKE :keyword OR loc_no LIKE :keyword)";
            params.addValue("keyword", "%" + keyword + "%");
        }
        sql += " ORDER BY container_code";
        try {
            return jdbcTemplate.queryForList(sql, params);
        } catch (Exception e) {
            log.error("查询容器列表失败 keyword={}", keyword, e);
            return Collections.emptyList();
        }
    }
}
