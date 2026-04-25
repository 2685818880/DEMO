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
 * 移库单创建策略
 */
@Component
@Slf4j
public class TransferOrderStrategy implements OrderCreateStrategy {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public TransferOrderStrategy(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public String getOrderType() {
        return "TRANSFER";
    }

    @Override
    public Map<String, Object> createOrder(String houseCode, String zoneName, List<Map<String, Object>> items) {
        log.info("创建移库单：houseCode={}, zoneName={}, items={}", houseCode, zoneName, items.size());

        String transferNo = "AI-TF-" + LocalDate.now().format(DATE_FMT) + "-" + System.currentTimeMillis() % 10000;
        UUID transferId = UUID.randomUUID();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String user = "ChatBI";

        // 构造来源库位描述
        Set<String> locations = new LinkedHashSet<>();
        for (Map<String, Object> item : items) {
            String locationCode = (String) item.get("locationCode");
            if (locationCode != null && !locationCode.isEmpty()) {
                locations.add(locationCode);
            }
        }
        String fromPos = locations.isEmpty() ? zoneName : String.join(",", locations);
        String toPos = "AI-待处理区";

        // 1. 插入 wms_transfer 主单
        String sqlHeader = "INSERT INTO wms_transfer (id, house_code, out_type, del_inventory, " +
                "transfer_no, transfer_status, from_pos, to_pos, start_time, transfer_by, remark, " +
                "is_active, create_datetime, create_by, last_modify_datetime, last_modify_by) " +
                "VALUES (:id, :houseCode, :outType, :delInventory, :transferNo, :transferStatus, " +
                ":fromPos, :toPos, :startTime, :transferBy, :remark, " +
                "1, :now, :user, :now, :user)";

        jdbcTemplate.update(sqlHeader, new MapSqlParameterSource()
                .addValue("id", transferId.toString())
                .addValue("houseCode", houseCode)
                .addValue("outType", "Transfer")
                .addValue("delInventory", false)
                .addValue("transferNo", transferNo)
                .addValue("transferStatus", "Created")
                .addValue("fromPos", fromPos)
                .addValue("toPos", toPos)
                .addValue("startTime", now)
                .addValue("transferBy", user)
                .addValue("remark", "ChatBI 智能分析生成移库单，来源：" + zoneName)
                .addValue("now", now)
                .addValue("user", user));

        // 2. 插入 wms_transfer_item 明细
        String sqlItem = "INSERT INTO wms_transfer_item (id, house_code, transfer_id, transfer_no, transfer_item_no, " +
                "sku_code, sku_name, batch_no, primary_qty, primary_unit, " +
                "is_active, create_datetime, create_by, last_modify_datetime, last_modify_by) " +
                "VALUES (:id, :houseCode, :transferId, :transferNo, :transferItemNo, " +
                ":skuCode, :skuName, :batchNo, :qty, :unit, " +
                "1, :now, :user, :now, :user)";

        int seq = 0;
        for (Map<String, Object> item : items) {
            seq++;
            String itemNo = transferNo + "-" + String.format("%03d", seq);
            jdbcTemplate.update(sqlItem, new MapSqlParameterSource()
                    .addValue("id", UUID.randomUUID().toString())
                    .addValue("houseCode", houseCode)
                    .addValue("transferId", transferId.toString())
                    .addValue("transferNo", transferNo)
                    .addValue("transferItemNo", itemNo)
                    .addValue("skuCode", item.get("skuCode"))
                    .addValue("skuName", item.get("skuName") != null ? item.get("skuName") : "")
                    .addValue("batchNo", item.get("batchNo") != null ? item.get("batchNo") : "")
                    .addValue("qty", item.get("availableQty") != null ? ((Number) item.get("availableQty")).doubleValue() : 0)
                    .addValue("unit", item.get("primaryUnit") != null ? item.get("primaryUnit") : "")
                    .addValue("now", now)
                    .addValue("user", user));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("transferNo", transferNo);
        result.put("status", "Created");
        result.put("message", "移库单创建成功");
        return result;
    }
}
