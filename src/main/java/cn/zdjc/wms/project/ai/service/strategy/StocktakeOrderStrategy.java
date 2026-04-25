package cn.zdjc.wms.project.ai.service.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 盘点单创建策略
 */
@Component
@Slf4j
public class StocktakeOrderStrategy implements OrderCreateStrategy {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public StocktakeOrderStrategy(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public String getOrderType() {
        return "STOCKTAKE";
    }

    @Override
    public Map<String, Object> createOrder(String houseCode, String zoneName, List<Map<String, Object>> items) {
        log.info("创建盘点单：houseCode={}, zoneName={}, items={}", houseCode, zoneName, items.size());

        String stocktakeNo = "AI-SC-" + LocalDate.now().format(DATE_FMT) + "-" + System.currentTimeMillis() % 10000;
        UUID stocktakeId = UUID.randomUUID();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String user = "ChatBI";

        // 插入 wms_stocktake 主单
        String sqlHeader = "INSERT INTO wms_stocktake (id, house_code, stocktake_no, stocktake_status, " +
                "zone_name, start_time, stocktake_by, remark, is_active, create_datetime, create_by, " +
                "last_modify_datetime, last_modify_by) " +
                "VALUES (:id, :houseCode, :stocktakeNo, :stocktakeStatus, :zoneName, :startTime, " +
                ":stocktakeBy, :remark, 1, :now, :user, :now, :user)";

        jdbcTemplate.update(sqlHeader, new MapSqlParameterSource()
                .addValue("id", stocktakeId.toString())
                .addValue("houseCode", houseCode)
                .addValue("stocktakeNo", stocktakeNo)
                .addValue("stocktakeStatus", "Created")
                .addValue("zoneName", zoneName)
                .addValue("startTime", now)
                .addValue("stocktakeBy", user)
                .addValue("remark", "ChatBI 智能分析生成盘点单，区域：" + zoneName)
                .addValue("now", now)
                .addValue("user", user));

        Map<String, Object> result = new HashMap<>();
        result.put("stocktakeNo", stocktakeNo);
        result.put("status", "Created");
        result.put("message", "盘点单创建成功");
        return result;
    }
}
