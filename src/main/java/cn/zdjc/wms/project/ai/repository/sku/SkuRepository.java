package cn.zdjc.wms.project.ai.repository.sku;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * SKU 数据访问层 - 负责物料档案和类别查询
 */
@Repository
@Slf4j
public class SkuRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SkuRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * 查询 SKU 列表（支持关键字搜索）
     */
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
            log.error("查询物料档案失败 keyword={}", keyword, e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询 SKU 类别列表（支持关键字搜索）
     */
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
            log.error("查询物料类别失败 keyword={}", keyword, e);
            return Collections.emptyList();
        }
    }
}
