package cn.zdjc.wms.project.ai.repository.outbound;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 出库数据访问层 - 负责出库/拣货相关查询
 */
@Repository
@Slf4j
public class OutboundRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public OutboundRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * 查询近期出库汇总（按仓库和天数过滤）
     */
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
            log.error("查询出库历史失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询近期拣货数据（用于异常检测）
     */
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
            log.error("查询拣货数据失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询入库汇总（按仓库和天数过滤）
     */
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
            log.error("查询入库历史失败", e);
            return Collections.emptyList();
        }
    }
}
