package cn.zdjc.wms.project.ai.service;

import cn.zdjc.wms.project.ai.dto.AiRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ChatBIBusinessService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatBIBusinessService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ChatBIBusinessService(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Transactional
    public Map<String, Object> createTransferOrder(String houseCode, String zoneName,
                                                    List<AiRequests.ChatBIItem> items) {
        LOGGER.info("ChatBI创建移库单: houseCode={}, zoneName={}, items={}", houseCode, zoneName, items.size());

        String transferNo = "AI-TF-" + LocalDate.now().format(DATE_FMT) + "-" + System.currentTimeMillis() % 10000;
        UUID transferId = UUID.randomUUID();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String user = "ChatBI";

        // 构造来源库位描述
        Set<String> locations = new LinkedHashSet<>();
        for (AiRequests.ChatBIItem item : items) {
            if (item.getLocationCode() != null && !item.getLocationCode().isEmpty()) {
                locations.add(item.getLocationCode());
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
                .addValue("remark", "ChatBI智能分析生成移库单，来源: " + zoneName)
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
        for (AiRequests.ChatBIItem item : items) {
            seq++;
            String itemNo = transferNo + "-" + String.format("%03d", seq);
            jdbcTemplate.update(sqlItem, new MapSqlParameterSource()
                    .addValue("id", UUID.randomUUID().toString())
                    .addValue("houseCode", houseCode)
                    .addValue("transferId", transferId.toString())
                    .addValue("transferNo", transferNo)
                    .addValue("transferItemNo", itemNo)
                    .addValue("skuCode", item.getSkuCode())
                    .addValue("skuName", item.getSkuName() != null ? item.getSkuName() : "")
                    .addValue("batchNo", item.getBatchNo() != null ? item.getBatchNo() : "")
                    .addValue("qty", item.getAvailableQty() != null ? item.getAvailableQty().doubleValue() : 0)
                    .addValue("unit", item.getPrimaryUnit() != null ? item.getPrimaryUnit() : "")
                    .addValue("now", now)
                    .addValue("user", user));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("transferNo", transferNo);
        result.put("itemCount", items.size());
        result.put("fromPos", fromPos);
        result.put("toPos", toPos);
        return result;
    }

    @Transactional
    public Map<String, Object> createStocktakeOrder(String houseCode, String zoneName,
                                                     List<AiRequests.ChatBIItem> items) {
        LOGGER.info("ChatBI创建盘点单: houseCode={}, zoneName={}, items={}", houseCode, zoneName, items.size());

        String stockFormNo = "AI-TS-" + LocalDate.now().format(DATE_FMT) + "-" + System.currentTimeMillis() % 10000;
        UUID formId = UUID.randomUUID();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String user = "ChatBI";

        // 1. 插入 wms_take_stock 主单
        String sqlHeader = "INSERT INTO wms_take_stock (id, house_code, stock_form_no, take_stock_type, " +
                "take_stock_status, operator, start_time, form_level, submit_status, audit_status, remark, " +
                "is_active, create_datetime, create_by, last_modify_datetime, last_modify_by) " +
                "VALUES (:id, :houseCode, :stockFormNo, :takeStockType, " +
                ":takeStockStatus, :operator, :startTime, :formLevel, :submitStatus, :auditStatus, :remark, " +
                "1, :now, :user, :now, :user)";

        jdbcTemplate.update(sqlHeader, new MapSqlParameterSource()
                .addValue("id", formId.toString())
                .addValue("houseCode", houseCode)
                .addValue("stockFormNo", stockFormNo)
                .addValue("takeStockType", "REAL_TIME")
                .addValue("takeStockStatus", "Created")
                .addValue("operator", user)
                .addValue("startTime", now)
                .addValue("formLevel", "customize_level")
                .addValue("submitStatus", "Created")
                .addValue("auditStatus", "Created")
                .addValue("remark", "ChatBI智能分析生成盘点单，来源: " + zoneName)
                .addValue("now", now)
                .addValue("user", user));

        // 2. 插入 wms_take_stock_item 明细
        String sqlItem = "INSERT INTO wms_take_stock_item (id, house_code, stock_form_id, stock_form_no, stock_form_type, " +
                "stock_item_no, stock_item_status, container_code, location_code, " +
                "sku_code, sku_name, batch_no, stock_qty, stock_unit, " +
                "is_active, create_datetime, create_by, last_modify_datetime, last_modify_by) " +
                "VALUES (:id, :houseCode, :formId, :formNo, :formType, " +
                ":itemNo, :itemStatus, :containerCode, :locationCode, " +
                ":skuCode, :skuName, :batchNo, :stockQty, :stockUnit, " +
                "1, :now, :user, :now, :user)";

        int seq = 0;
        for (AiRequests.ChatBIItem item : items) {
            seq++;
            String itemNo = stockFormNo + "-" + String.format("%03d", seq);
            jdbcTemplate.update(sqlItem, new MapSqlParameterSource()
                    .addValue("id", UUID.randomUUID().toString())
                    .addValue("houseCode", houseCode)
                    .addValue("formId", formId.toString())
                    .addValue("formNo", stockFormNo)
                    .addValue("formType", "REAL_TIME")
                    .addValue("itemNo", itemNo)
                    .addValue("itemStatus", "Created")
                    .addValue("containerCode", item.getContainerCode() != null ? item.getContainerCode() : "")
                    .addValue("locationCode", item.getLocationCode() != null ? item.getLocationCode() : "")
                    .addValue("skuCode", item.getSkuCode())
                    .addValue("skuName", item.getSkuName() != null ? item.getSkuName() : "")
                    .addValue("batchNo", item.getBatchNo() != null ? item.getBatchNo() : "")
                    .addValue("stockQty", item.getAvailableQty() != null ? item.getAvailableQty().doubleValue() : 0)
                    .addValue("stockUnit", item.getPrimaryUnit() != null ? item.getPrimaryUnit() : "")
                    .addValue("now", now)
                    .addValue("user", user));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("stockFormNo", stockFormNo);
        result.put("itemCount", items.size());
        return result;
    }

    @Transactional
    public Map<String, Object> createAsnOrder(String houseCode, String zoneName,
                                               List<AiRequests.ChatBIItem> items) {
        LOGGER.info("ChatBI创建收货单: houseCode={}, zoneName={}, items={}", houseCode, zoneName, items.size());

        String formNo = "AI-ASN-" + LocalDate.now().format(DATE_FMT) + "-" + System.currentTimeMillis() % 10000;
        UUID formId = UUID.randomUUID();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String user = "ChatBI";

        // 1. 插入 wms_asn 主单
        String sqlHeader = "INSERT INTO wms_asn (id, house_code, form_no, form_type, asn_status, " +
                "start_time, remark, " +
                "is_active, create_datetime, create_by, last_modify_datetime, last_modify_by) " +
                "VALUES (:id, :houseCode, :formNo, :formType, :asnStatus, " +
                ":startTime, :remark, " +
                "1, :now, :user, :now, :user)";

        jdbcTemplate.update(sqlHeader, new MapSqlParameterSource()
                .addValue("id", formId.toString())
                .addValue("houseCode", houseCode)
                .addValue("formNo", formNo)
                .addValue("formType", "AI")
                .addValue("asnStatus", "Created")
                .addValue("startTime", now)
                .addValue("remark", "ChatBI智能分析生成收货单，来源: " + zoneName)
                .addValue("now", now)
                .addValue("user", user));

        // 2. 插入 wms_asn_item 明细
        String sqlItem = "INSERT INTO wms_asn_item (id, item_no, asn_id, asn_no, " +
                "sku_code, sku_name, batch_no, primary_qty, primary_unit, " +
                "is_active, create_datetime, create_by, last_modify_datetime, last_modify_by) " +
                "VALUES (:id, :itemNo, :asnId, :asnNo, " +
                ":skuCode, :skuName, :batchNo, :qty, :unit, " +
                "1, :now, :user, :now, :user)";

        int seq = 0;
        for (AiRequests.ChatBIItem item : items) {
            seq++;
            String itemNo = formNo + "-" + String.format("%03d", seq);
            jdbcTemplate.update(sqlItem, new MapSqlParameterSource()
                    .addValue("id", UUID.randomUUID().toString())
                    .addValue("itemNo", itemNo)
                    .addValue("asnId", formId.toString())
                    .addValue("asnNo", formNo)
                    .addValue("skuCode", item.getSkuCode())
                    .addValue("skuName", item.getSkuName() != null ? item.getSkuName() : "")
                    .addValue("batchNo", item.getBatchNo() != null ? item.getBatchNo() : "")
                    .addValue("qty", item.getAvailableQty() != null ? item.getAvailableQty().doubleValue() : 0)
                    .addValue("unit", item.getPrimaryUnit() != null ? item.getPrimaryUnit() : "")
                    .addValue("now", now)
                    .addValue("user", user));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("formNo", formNo);
        result.put("itemCount", items.size());
        return result;
    }

    @Transactional
    public Map<String, Object> createRequisitionOrder(String houseCode, String zoneName,
                                                       List<AiRequests.ChatBIItem> items) {
        LOGGER.info("ChatBI创建发货单: houseCode={}, zoneName={}, items={}", houseCode, zoneName, items.size());

        String formNo = "AI-REQ-" + LocalDate.now().format(DATE_FMT) + "-" + System.currentTimeMillis() % 10000;
        UUID formId = UUID.randomUUID();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String user = "ChatBI";

        // 1. 插入 wms_requisition_order 主单
        String sqlHeader = "INSERT INTO wms_requisition_order (id, house_code, form_no, form_type, form_status, " +
                "remark, " +
                "is_active, create_datetime, create_by, last_modify_datetime, last_modify_by) " +
                "VALUES (:id, :houseCode, :formNo, :formType, :formStatus, " +
                ":remark, " +
                "1, :now, :user, :now, :user)";

        jdbcTemplate.update(sqlHeader, new MapSqlParameterSource()
                .addValue("id", formId.toString())
                .addValue("houseCode", houseCode)
                .addValue("formNo", formNo)
                .addValue("formType", "AI")
                .addValue("formStatus", "Created")
                .addValue("remark", "ChatBI智能分析生成发货单，来源: " + zoneName)
                .addValue("now", now)
                .addValue("user", user));

        // 2. 插入 wms_requisition_item 明细
        String sqlItem = "INSERT INTO wms_requisition_item (id, item_no, form_id, form_no, house_code, " +
                "sku_code, sku_name, batch_no, primary_qty, primary_unit, " +
                "is_active, create_datetime, create_by, last_modify_datetime, last_modify_by) " +
                "VALUES (:id, :itemNo, :formId, :formNo, :houseCode, " +
                ":skuCode, :skuName, :batchNo, :qty, :unit, " +
                "1, :now, :user, :now, :user)";

        int seq = 0;
        for (AiRequests.ChatBIItem item : items) {
            seq++;
            String itemNo = formNo + "-" + String.format("%03d", seq);
            jdbcTemplate.update(sqlItem, new MapSqlParameterSource()
                    .addValue("id", UUID.randomUUID().toString())
                    .addValue("itemNo", itemNo)
                    .addValue("formId", formId.toString())
                    .addValue("formNo", formNo)
                    .addValue("houseCode", houseCode)
                    .addValue("skuCode", item.getSkuCode())
                    .addValue("skuName", item.getSkuName() != null ? item.getSkuName() : "")
                    .addValue("batchNo", item.getBatchNo() != null ? item.getBatchNo() : "")
                    .addValue("qty", item.getAvailableQty() != null ? item.getAvailableQty().doubleValue() : 0)
                    .addValue("unit", item.getPrimaryUnit() != null ? item.getPrimaryUnit() : "")
                    .addValue("now", now)
                    .addValue("user", user));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("formNo", formNo);
        result.put("itemCount", items.size());
        return result;
    }

    @Transactional
    public Map<String, Object> createPalletizeOrder(String houseCode, String zoneName,
                                                     List<AiRequests.ChatBIItem> items) {
        LOGGER.info("ChatBI创建组盘单: houseCode={}, zoneName={}, items={}", houseCode, zoneName, items.size());

        String formNo = "AI-PAL-" + LocalDate.now().format(DATE_FMT) + "-" + System.currentTimeMillis() % 10000;
        UUID formId = UUID.randomUUID();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String user = "ChatBI";

        // 1. 插入 wms_palletize_form 主单
        String sqlHeader = "INSERT INTO wms_palletize_form (id, house_code, palletize_form_no, palletize_form_type, " +
                "palletize_form_status, start_time, remark, " +
                "is_active, create_datetime, create_by, last_modify_datetime, last_modify_by) " +
                "VALUES (:id, :houseCode, :formNo, :formType, " +
                ":formStatus, :startTime, :remark, " +
                "1, :now, :user, :now, :user)";

        jdbcTemplate.update(sqlHeader, new MapSqlParameterSource()
                .addValue("id", formId.toString())
                .addValue("houseCode", houseCode)
                .addValue("formNo", formNo)
                .addValue("formType", "AI")
                .addValue("formStatus", "Created")
                .addValue("startTime", now)
                .addValue("remark", "ChatBI智能分析生成组盘单，来源: " + zoneName)
                .addValue("now", now)
                .addValue("user", user));

        // 2. 插入 wms_palletize_item 明细
        String sqlItem = "INSERT INTO wms_palletize_item (id, palletize_form_id, palletize_form_no, item_form_no, " +
                "sku_code, sku_name, batch_no, primary_qty, primary_unit, " +
                "is_active, create_datetime, create_by, last_modify_datetime, last_modify_by) " +
                "VALUES (:id, :formId, :formNo, :itemNo, " +
                ":skuCode, :skuName, :batchNo, :qty, :unit, " +
                "1, :now, :user, :now, :user)";

        int seq = 0;
        for (AiRequests.ChatBIItem item : items) {
            seq++;
            String itemNo = formNo + "-" + String.format("%03d", seq);
            jdbcTemplate.update(sqlItem, new MapSqlParameterSource()
                    .addValue("id", UUID.randomUUID().toString())
                    .addValue("formId", formId.toString())
                    .addValue("formNo", formNo)
                    .addValue("itemNo", itemNo)
                    .addValue("skuCode", item.getSkuCode())
                    .addValue("skuName", item.getSkuName() != null ? item.getSkuName() : "")
                    .addValue("batchNo", item.getBatchNo() != null ? item.getBatchNo() : "")
                    .addValue("qty", item.getAvailableQty() != null ? item.getAvailableQty().doubleValue() : 0)
                    .addValue("unit", item.getPrimaryUnit() != null ? item.getPrimaryUnit() : "")
                    .addValue("now", now)
                    .addValue("user", user));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("formNo", formNo);
        result.put("itemCount", items.size());
        return result;
    }
}
