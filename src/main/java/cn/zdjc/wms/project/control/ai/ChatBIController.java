package cn.zdjc.wms.project.control.ai;

import cn.zdjc.wms.project.ai.client.LlmClient;
import cn.zdjc.wms.project.ai.dto.AiRequests;
import cn.zdjc.wms.project.ai.dto.AiResponses;
import cn.zdjc.wms.project.ai.service.AiDataService;
import cn.zdjc.wms.project.ai.service.ChatBIBusinessService;
import cn.zdjc.wms.project.common.constant.ProjectConstant;
import cn.zdjc.wms.project.erp.dto.ResultWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * ChatBI - 自然语言智能查询
 * 支持：滞销品查询、临期品预警、积压品分析、库存汇总、
 *       物料档案查询(wms_sku)、物料类别查询(wms_sku_category)
 */
@RestController
@RequestMapping(ProjectConstant.PREFIX_CONTEXT_PATH + "/ai")
@Slf4j
public class ChatBIController {

    private static final String LOG_PREFIX = "[ChatBIController] ";

    @Resource
    private AiDataService aiDataService;

    @Resource
    private ChatBIBusinessService chatBIBusinessService;

    @Resource
    private LlmClient llmClient;

    @PostMapping("/chat-bi/query")
    public ResultWrapper<AiResponses.ChatBIResponse> chatQuery(
            @RequestBody AiRequests.ChatBIQuery request) {
        long start = System.currentTimeMillis();
        log.info("{}ChatBI查询: {}", LOG_PREFIX, request.getQuery());

        try {
            String query = request.getQuery() != null ? request.getQuery() : "";

            // 1. 解析查询意图（优先使用LLM，失败时降级到正则）
            LlmClient.ChatIntent intent = llmClient.parseIntent(query);
            String zoneName;
            String queryType;
            int dwellThreshold;
            if (intent != null) {
                zoneName = intent.getZone();
                queryType = intent.getType();
                dwellThreshold = intent.getDwellDays() != null ? intent.getDwellDays() : 90;
            } else {
                zoneName = parseZone(query);
                queryType = parseType(query);
                dwellThreshold = parseDwellThreshold(query);
            }
            String zonePattern = zoneName != null ? "%" + zoneName + "%" : "%%";

            // 2. 执行查询
            AiResponses.ChatBIResponse response = new AiResponses.ChatBIResponse();
            List<Map<String, Object>> rawItems;

            switch (queryType) {
                case "LOCATION_QUERY": {
                    String kw = parseSearchKeyword(query, "库位", "货位");
                    rawItems = aiDataService.queryLocationList(kw);
                    response = buildLocationResponse(query, kw, rawItems);
                    break;
                }
                case "SCHEDULER_QUERY": {
                    String kw = parseSearchKeyword(query, "定时器", "调度器", "定时任务", "计划任务");
                    rawItems = aiDataService.querySchedulerManageList(kw);
                    response = buildSchedulerResponse(query, kw, rawItems);
                    break;
                }
                case "CONTAINER_QUERY": {
                    String kw = parseSearchKeyword(query, "容器", "托盘");
                    rawItems = aiDataService.queryContainerList(kw);
                    response = buildContainerResponse(query, kw, rawItems);
                    break;
                }
                case "DISPATCH_QUERY": {
                    String kw = parseSearchKeyword(query, "任务调度", "调度信息");
                    rawItems = aiDataService.queryDispatchInfoList(kw);
                    response = buildDispatchInfoResponse(query, kw, rawItems);
                    break;
                }
                case "DISPATCH_JOB_QUERY": {
                    String kw = parseSearchKeyword(query, "任务信息", "作业信息", "作业任务");
                    rawItems = aiDataService.queryDispatchJobList(kw);
                    response = buildDispatchJobResponse(query, kw, rawItems);
                    break;
                }
                case "SKU_QUERY": {
                    String kw = parseSearchKeyword(query, "物料档案", "物料信息", "SKU", "产品档案");
                    rawItems = aiDataService.querySkuList(kw);
                    response = buildSkuResponse(query, kw, rawItems);
                    break;
                }
                case "SKU_CATEGORY_QUERY": {
                    String kw = parseSearchKeyword(query, "物料类别", "物料分类", "产品类别", "产品分类");
                    rawItems = aiDataService.querySkuCategoryList(kw);
                    response = buildSkuCategoryResponse(query, kw, rawItems);
                    break;
                }
                case "INVENTORY_SUMMARY":
                    rawItems = aiDataService.queryInventorySummary(zonePattern);
                    response = buildSummaryResponse(query, zoneName, rawItems);
                    break;
                case "EXPIRY_WARNING":
                    rawItems = aiDataService.queryExpiryWarningProducts(zonePattern, 30);
                    response = buildResponse(query, zoneName, queryType, rawItems);
                    break;
                case "OVERSTOCK":
                    rawItems = aiDataService.queryOverstockProducts(zonePattern, new BigDecimal("1000"));
                    response = buildResponse(query, zoneName, queryType, rawItems);
                    break;
                case "SLOW_MOVING":
                default:
                    rawItems = aiDataService.querySlowMovingProducts(zonePattern, dwellThreshold);
                    response = buildResponse(query, zoneName, queryType, rawItems);
                    break;
            }

            // 3. 构建元数据
            response.getMeta().setQueryTimeMs(System.currentTimeMillis() - start);

            log.info("{}ChatBI查询完成, type={}, zone={}",
                    LOG_PREFIX, queryType, zoneName);
            return ResultWrapper.buildSuccess(response);

        } catch (Exception e) {
            log.error("{}ChatBI处理异常", LOG_PREFIX, e);
            return ResultWrapper.buildFailure("查询处理失败: " + e.getMessage());
        }
    }

    // ==================== Action Endpoints ====================

    @PostMapping("/chat-bi/create-transfer")
    public ResultWrapper<Map<String, Object>> createTransfer(
            @RequestBody AiRequests.ChatBIActionRequest request) {
        log.info("{}创建移库单: houseCode={}, items={}", LOG_PREFIX, request.getHouseCode(),
                request.getItems() != null ? request.getItems().size() : 0);
        try {
            if (request.getItems() == null || request.getItems().isEmpty()) {
                return ResultWrapper.buildFailure("没有可移库的物料");
            }
            Map<String, Object> result = chatBIBusinessService.createTransferOrder(
                    request.getHouseCode(), request.getZoneName(), request.getItems());
            return ResultWrapper.buildSuccess(result);
        } catch (Exception e) {
            log.error("{}创建移库单失败", LOG_PREFIX, e);
            return ResultWrapper.buildFailure("创建移库单失败: " + e.getMessage());
        }
    }

    @PostMapping("/chat-bi/create-stocktake")
    public ResultWrapper<Map<String, Object>> createStocktake(
            @RequestBody AiRequests.ChatBIActionRequest request) {
        log.info("{}创建盘点单: houseCode={}, items={}", LOG_PREFIX, request.getHouseCode(),
                request.getItems() != null ? request.getItems().size() : 0);
        try {
            if (request.getItems() == null || request.getItems().isEmpty()) {
                return ResultWrapper.buildFailure("没有可盘点的物料");
            }
            Map<String, Object> result = chatBIBusinessService.createStocktakeOrder(
                    request.getHouseCode(), request.getZoneName(), request.getItems());
            return ResultWrapper.buildSuccess(result);
        } catch (Exception e) {
            log.error("{}创建盘点单失败", LOG_PREFIX, e);
            return ResultWrapper.buildFailure("创建盘点单失败: " + e.getMessage());
        }
    }

    @PostMapping("/chat-bi/create-asn")
    public ResultWrapper<Map<String, Object>> createAsn(
            @RequestBody AiRequests.ChatBIActionRequest request) {
        log.info("{}创建收货单: houseCode={}, items={}", LOG_PREFIX, request.getHouseCode(),
                request.getItems() != null ? request.getItems().size() : 0);
        try {
            if (request.getItems() == null || request.getItems().isEmpty()) {
                return ResultWrapper.buildFailure("没有可收货的物料");
            }
            Map<String, Object> result = chatBIBusinessService.createAsnOrder(
                    request.getHouseCode(), request.getZoneName(), request.getItems());
            return ResultWrapper.buildSuccess(result);
        } catch (Exception e) {
            log.error("{}创建收货单失败", LOG_PREFIX, e);
            return ResultWrapper.buildFailure("创建收货单失败: " + e.getMessage());
        }
    }

    @PostMapping("/chat-bi/create-requisition")
    public ResultWrapper<Map<String, Object>> createRequisition(
            @RequestBody AiRequests.ChatBIActionRequest request) {
        log.info("{}创建发货单: houseCode={}, items={}", LOG_PREFIX, request.getHouseCode(),
                request.getItems() != null ? request.getItems().size() : 0);
        try {
            if (request.getItems() == null || request.getItems().isEmpty()) {
                return ResultWrapper.buildFailure("没有可发货的物料");
            }
            Map<String, Object> result = chatBIBusinessService.createRequisitionOrder(
                    request.getHouseCode(), request.getZoneName(), request.getItems());
            return ResultWrapper.buildSuccess(result);
        } catch (Exception e) {
            log.error("{}创建发货单失败", LOG_PREFIX, e);
            return ResultWrapper.buildFailure("创建发货单失败: " + e.getMessage());
        }
    }

    @PostMapping("/chat-bi/create-palletize")
    public ResultWrapper<Map<String, Object>> createPalletize(
            @RequestBody AiRequests.ChatBIActionRequest request) {
        log.info("{}创建组盘单: houseCode={}, items={}", LOG_PREFIX, request.getHouseCode(),
                request.getItems() != null ? request.getItems().size() : 0);
        try {
            if (request.getItems() == null || request.getItems().isEmpty()) {
                return ResultWrapper.buildFailure("没有可组盘的物料");
            }
            Map<String, Object> result = chatBIBusinessService.createPalletizeOrder(
                    request.getHouseCode(), request.getZoneName(), request.getItems());
            return ResultWrapper.buildSuccess(result);
        } catch (Exception e) {
            log.error("{}创建组盘单失败", LOG_PREFIX, e);
            return ResultWrapper.buildFailure("创建组盘单失败: " + e.getMessage());
        }
    }

    // ==================== Intent Parsing ====================

    private String parseZone(String query) {
        Pattern p = Pattern.compile("([A-Za-z0-9\\u4e00-\\u9fa5])区");
        Matcher m = p.matcher(query);
        return m.find() ? m.group(1) + "区" : null;
    }

    private String parseType(String query) {
        // 注意: 更具体的关键词需放在前面，避免被通用关键词提前匹配
        if (query.contains("任务调度") || query.contains("调度信息")) {
            return "DISPATCH_QUERY";
        }
        if (query.contains("任务信息") || query.contains("作业信息")
                || query.contains("作业任务")) {
            return "DISPATCH_JOB_QUERY";
        }
        if (query.contains("物料档案") || query.contains("物料信息")
                || query.contains("产品档案") || query.contains("查SKU")
                || query.equals("SKU")) {
            return "SKU_QUERY";
        }
        if (query.contains("物料类别") || query.contains("物料分类")
                || query.contains("产品类别") || query.contains("产品分类")) {
            return "SKU_CATEGORY_QUERY";
        }
        if (query.contains("库位") || query.contains("货位")) {
            return "LOCATION_QUERY";
        }
        if (query.contains("定时器") || query.contains("调度器")
                || query.contains("定时任务") || query.contains("计划任务")) {
            return "SCHEDULER_QUERY";
        }
        if (query.contains("容器") || query.contains("托盘")) {
            return "CONTAINER_QUERY";
        }
        if (query.contains("汇总") || query.contains("按产品") || query.contains("按物料")
                || query.contains("库存分布") || (query.contains("库存") && query.contains("产品"))) {
            return "INVENTORY_SUMMARY";
        }
        if (query.contains("滞销") || query.contains("呆滞") || query.contains("慢动")) {
            return "SLOW_MOVING";
        }
        if (query.contains("过期") || query.contains("临期") || query.contains("效期")) {
            return "EXPIRY_WARNING";
        }
        if (query.contains("积压") || query.contains("过剩") || query.contains("过多")) {
            return "OVERSTOCK";
        }
        // 默认库存汇总查询
        return "INVENTORY_SUMMARY";
    }

    private int parseDwellThreshold(String query) {
        Pattern p = Pattern.compile("(\\d+)\\s*天");
        Matcher m = p.matcher(query);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return 90; // 默认90天
    }

    // ==================== Response Builder ====================

    private AiResponses.ChatBIResponse buildResponse(String query, String zoneName,
                                                      String queryType, List<Map<String, Object>> rawItems) {
        AiResponses.ChatBIResponse response = new AiResponses.ChatBIResponse();
        response.setQuery(query);
        response.setItems(new ArrayList<>());
        response.setSuggestions(new ArrayList<>());
        response.setActionButtons(new ArrayList<>());

        // 构建元数据
        AiResponses.ChatMeta meta = new AiResponses.ChatMeta();
        meta.setParsedZone(zoneName != null ? zoneName : "全部区域");
        meta.setParsedType(queryType);
        meta.setParsedAction("GENERATE_SUGGESTIONS");
        response.setMeta(meta);

        if (rawItems == null || rawItems.isEmpty()) {
            String emptyMsg = zoneName != null
                    ? zoneName + "未发现符合条件的滞销品"
                    : "未发现符合条件的滞销品";
            response.setSummary(emptyMsg);
            return response;
        }

        // 转换数据并生成建议
        int severeCount = 0;
        for (Map<String, Object> row : rawItems) {
            AiResponses.SlowMovingItem item = mapToItem(row);
            response.getItems().add(item);
            if (item.getDwellDays() >= 180) severeCount++;

            // 生成单个SKU建议
            item.setSuggestion(generateItemSuggestion(item));
        }

        // 生成汇总
        String summary = buildSummary(zoneName, rawItems.size(), severeCount);
        response.setSummary(summary);

        // 生成全局建议
        response.setSuggestions(buildGlobalSuggestions(rawItems, zoneName));

        // 生成操作按钮
        response.setActionButtons(buildActionButtons(rawItems, zoneName));

        // 更新元数据
        meta.setTotalItems(rawItems.size());

        return response;
    }

    private AiResponses.SlowMovingItem mapToItem(Map<String, Object> row) {
        AiResponses.SlowMovingItem item = new AiResponses.SlowMovingItem();
        item.setSkuCode(str(row.get("sku_code")));
        item.setSkuName(str(row.get("sku_name")));
        item.setBatchNo(str(row.get("batch_no")));
        item.setLocationCode(str(row.get("location_code")));
        item.setContainerCode(str(row.get("container_code")));
        item.setPrimaryQty(toDecimal(row.get("primary_qty")));
        item.setAvailableQty(toDecimal(row.get("available_qty")));
        item.setPrimaryUnit(str(row.get("primary_unit")));
        item.setZoneName(str(row.get("zone_name")));
        item.setDwellDays(toInt(row.get("dwell_days")));
        item.setQualityStatus(str(row.get("quality_status")));
        return item;
    }

    private String generateItemSuggestion(AiResponses.SlowMovingItem item) {
        if (item.getDwellDays() >= 180) {
            return "超过180天未动，建议报废处理";
        } else if (item.getDwellDays() >= 90) {
            return "超过90天未动，建议移库至待处理区或促销清仓";
        } else if (item.getDwellDays() >= 60) {
            return "超过60天未动，建议关注";
        }
        return "正常库存";
    }

    private String buildSummary(String zoneName, int totalCount, int severeCount) {
        StringBuilder sb = new StringBuilder();
        if (zoneName != null) {
            sb.append("在").append(zoneName).append("发现");
        } else {
            sb.append("发现");
        }
        sb.append(totalCount).append("项滞销品");
        if (severeCount > 0) {
            sb.append("，其中").append(severeCount).append("项超过180天未动，建议优先处理");
        }
        return sb.toString();
    }

    private List<String> buildGlobalSuggestions(List<Map<String, Object>> items, String zoneName) {
        List<String> suggestions = new ArrayList<>();

        long severeCount = items.stream()
                .filter(r -> toInt(r.get("dwell_days")) >= 180)
                .count();
        long over90Count = items.stream()
                .filter(r -> toInt(r.get("dwell_days")) >= 90)
                .count();

        if (severeCount > 0) {
            suggestions.add(severeCount + "种物料库龄超过180天，建议优先安排报废或折扣处理");
        }
        if (over90Count > 0) {
            suggestions.add(over90Count + "种物料库龄超过90天，建议调整采购计划或促销清仓");
        }

        // 统计产品类别分布
        long categories = items.stream()
                .map(r -> str(r.get("sku_code")))
                .distinct()
                .count();
        suggestions.add("共涉及 " + categories + " 种SKU，建议分类制定处理方案");

        if (zoneName != null) {
            suggestions.add("可释放" + zoneName + "约 " + (items.size() * 2) + "% 的存储空间");
        }

        return suggestions;
    }

    private List<AiResponses.ActionButton> buildActionButtons(List<Map<String, Object>> items, String zoneName) {
        List<AiResponses.ActionButton> buttons = new ArrayList<>();

        AiResponses.ActionButton transferBtn = new AiResponses.ActionButton();
        transferBtn.setLabel("生成移库单");
        transferBtn.setAction("CREATE_TRANSFER_ORDER");
        transferBtn.setIcon("el-icon-sort");
        transferBtn.setType("primary");
        buttons.add(transferBtn);

        AiResponses.ActionButton stocktakeBtn = new AiResponses.ActionButton();
        stocktakeBtn.setLabel("生成盘点单");
        stocktakeBtn.setAction("CREATE_STOCKTAKE_ORDER");
        stocktakeBtn.setIcon("el-icon-document-copy");
        stocktakeBtn.setType("warning");
        buttons.add(stocktakeBtn);

        AiResponses.ActionButton asnBtn = new AiResponses.ActionButton();
        asnBtn.setLabel("生成收货单");
        asnBtn.setAction("CREATE_ASN_ORDER");
        asnBtn.setIcon("el-icon-download");
        asnBtn.setType("success");
        buttons.add(asnBtn);

        AiResponses.ActionButton reqBtn = new AiResponses.ActionButton();
        reqBtn.setLabel("生成发货单");
        reqBtn.setAction("CREATE_REQUISITION_ORDER");
        reqBtn.setIcon("el-icon-upload2");
        reqBtn.setType("danger");
        buttons.add(reqBtn);

        AiResponses.ActionButton palletizeBtn = new AiResponses.ActionButton();
        palletizeBtn.setLabel("生成组盘单");
        palletizeBtn.setAction("CREATE_PALLETIZE_ORDER");
        palletizeBtn.setIcon("el-icon-box");
        palletizeBtn.setType("info");
        buttons.add(palletizeBtn);

        return buttons;
    }

    private AiResponses.ChatBIResponse buildSummaryResponse(String query, String zoneName,
                                                              List<Map<String, Object>> rawItems) {
        AiResponses.ChatBIResponse response = new AiResponses.ChatBIResponse();
        response.setQuery(query);
        response.setItems(new ArrayList<>());
        response.setSummaryItems(new ArrayList<>());
        response.setSuggestions(new ArrayList<>());
        response.setActionButtons(new ArrayList<>());

        // 构建元数据
        AiResponses.ChatMeta meta = new AiResponses.ChatMeta();
        meta.setParsedZone(zoneName != null ? zoneName : "全部区域");
        meta.setParsedType("INVENTORY_SUMMARY");
        meta.setParsedAction("GENERATE_SUGGESTIONS");
        response.setMeta(meta);

        if (rawItems == null || rawItems.isEmpty()) {
            String emptyMsg = zoneName != null
                    ? zoneName + "无库存数据"
                    : "无库存数据";
            response.setSummary(emptyMsg);
            return response;
        }

        // 转换汇总数据
        BigDecimal totalQty = BigDecimal.ZERO;
        int totalSkuCount = rawItems.size();
        for (Map<String, Object> row : rawItems) {
            AiResponses.InventorySummaryItem item = mapToSummaryItem(row);
            response.getSummaryItems().add(item);
            totalQty = totalQty.add(item.getTotalAvailableQty());
        }

        // 生成汇总文本
        String summary = buildSummaryText(zoneName, totalSkuCount, totalQty);
        response.setSummary(summary);

        // 生成建议
        response.setSuggestions(buildSummarySuggestions(rawItems, zoneName));

        // 生成操作按钮
        response.setActionButtons(buildSummaryActionButtons());

        meta.setTotalItems(totalSkuCount);
        return response;
    }

    private AiResponses.InventorySummaryItem mapToSummaryItem(Map<String, Object> row) {
        AiResponses.InventorySummaryItem item = new AiResponses.InventorySummaryItem();
        item.setSkuCode(str(row.get("sku_code")));
        item.setSkuName(str(row.get("sku_name")));
        item.setPrimaryUnit(str(row.get("primary_unit")));
        item.setTotalAvailableQty(toDecimal(row.get("total_available_qty")));
        item.setTotalPrimaryQty(toDecimal(row.get("total_primary_qty")));
        item.setBatchCount(toInt(row.get("batch_count")));
        item.setLocationCount(toInt(row.get("location_count")));
        return item;
    }

    private String buildSummaryText(String zoneName, int skuCount, BigDecimal totalQty) {
        StringBuilder sb = new StringBuilder();
        if (zoneName != null) {
            sb.append(zoneName);
        }
        sb.append("库存汇总：共").append(skuCount).append("种SKU，总可用库存").append(totalQty);
        return sb.toString();
    }

    private List<String> buildSummarySuggestions(List<Map<String, Object>> items, String zoneName) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("共 " + items.size() + " 种SKU，建议关注库存量大的物料是否存在积压风险");
        if (zoneName != null) {
            suggestions.add(zoneName + "库存覆盖 " + items.size() + " 种SKU，可按物料分类进一步分析");
        }
        suggestions.add("建议定期核对库存数据，确保账实一致");
        return suggestions;
    }

    private List<AiResponses.ActionButton> buildSummaryActionButtons() {
        List<AiResponses.ActionButton> buttons = new ArrayList<>();

        AiResponses.ActionButton stocktakeBtn = new AiResponses.ActionButton();
        stocktakeBtn.setLabel("生成盘点单");
        stocktakeBtn.setAction("CREATE_STOCKTAKE_ORDER");
        stocktakeBtn.setIcon("el-icon-document-copy");
        stocktakeBtn.setType("primary");
        buttons.add(stocktakeBtn);

        AiResponses.ActionButton asnBtn = new AiResponses.ActionButton();
        asnBtn.setLabel("生成收货单");
        asnBtn.setAction("CREATE_ASN_ORDER");
        asnBtn.setIcon("el-icon-download");
        asnBtn.setType("success");
        buttons.add(asnBtn);

        AiResponses.ActionButton reqBtn = new AiResponses.ActionButton();
        reqBtn.setLabel("生成发货单");
        reqBtn.setAction("CREATE_REQUISITION_ORDER");
        reqBtn.setIcon("el-icon-upload2");
        reqBtn.setType("danger");
        buttons.add(reqBtn);

        AiResponses.ActionButton palletizeBtn = new AiResponses.ActionButton();
        palletizeBtn.setLabel("生成组盘单");
        palletizeBtn.setAction("CREATE_PALLETIZE_ORDER");
        palletizeBtn.setIcon("el-icon-box");
        palletizeBtn.setType("info");
        buttons.add(palletizeBtn);

        return buttons;
    }

    // ==================== Helpers ====================

    /**
     * 从查询中提取搜索关键词：去除已知的触发词后，剩余文本作为关键词
     */
    private String parseSearchKeyword(String query, String... triggerWords) {
        String result = query;
        for (String word : triggerWords) {
            result = result.replace(word, "");
        }
        // 去除"查"、"查询"、"查看"前缀和多余空白
        result = result.replace("查", "")
                .replace("询", "")
                .replace("看", "")
                .trim();
        return result.isEmpty() ? null : result;
    }

    private AiResponses.ChatBIResponse buildSkuResponse(String query, String keyword,
                                                         List<Map<String, Object>> rawItems) {
        AiResponses.ChatBIResponse response = new AiResponses.ChatBIResponse();
        response.setQuery(query);
        response.setItems(new ArrayList<>());
        response.setSkuItems(new ArrayList<>());
        response.setSuggestions(new ArrayList<>());
        response.setActionButtons(new ArrayList<>());

        AiResponses.ChatMeta meta = new AiResponses.ChatMeta();
        meta.setParsedZone("全部区域");
        meta.setParsedType("SKU_QUERY");
        response.setMeta(meta);

        if (rawItems == null || rawItems.isEmpty()) {
            String emptyMsg = keyword != null
                    ? "未找到匹配\"" + keyword + "\"的物料档案"
                    : "暂无物料档案数据";
            response.setSummary(emptyMsg);
            return response;
        }

        for (Map<String, Object> row : rawItems) {
            AiResponses.SkuItem item = mapToSkuItem(row);
            response.getSkuItems().add(item);
        }

        String summary = keyword != null
                ? "共找到 " + rawItems.size() + " 项匹配\"" + keyword + "\"的物料"
                : "共 " + rawItems.size() + " 项物料档案";
        response.setSummary(summary);

        response.getSuggestions().add("共 " + rawItems.size() + " 种物料，可按类别、编码或名称进一步筛选");
        response.getSuggestions().add("如需查看某物料库存详情，请使用\"查库存\"命令");

        meta.setTotalItems(rawItems.size());
        return response;
    }

    private AiResponses.ChatBIResponse buildSkuCategoryResponse(String query, String keyword,
                                                                  List<Map<String, Object>> rawItems) {
        AiResponses.ChatBIResponse response = new AiResponses.ChatBIResponse();
        response.setQuery(query);
        response.setItems(new ArrayList<>());
        response.setSkuCategoryItems(new ArrayList<>());
        response.setSuggestions(new ArrayList<>());
        response.setActionButtons(new ArrayList<>());

        AiResponses.ChatMeta meta = new AiResponses.ChatMeta();
        meta.setParsedZone("全部区域");
        meta.setParsedType("SKU_CATEGORY_QUERY");
        response.setMeta(meta);

        if (rawItems == null || rawItems.isEmpty()) {
            String emptyMsg = keyword != null
                    ? "未找到匹配\"" + keyword + "\"的物料类别"
                    : "暂无物料类别数据";
            response.setSummary(emptyMsg);
            return response;
        }

        for (Map<String, Object> row : rawItems) {
            AiResponses.SkuCategoryItem item = mapToSkuCategoryItem(row);
            response.getSkuCategoryItems().add(item);
        }

        String summary = keyword != null
                ? "共找到 " + rawItems.size() + " 项匹配\"" + keyword + "\"的物料类别"
                : "共 " + rawItems.size() + " 项物料类别";
        response.setSummary(summary);

        response.getSuggestions().add("共 " + rawItems.size() + " 个物料类别，可按类别名称进一步筛选");
        response.getSuggestions().add("如需查看某类别下的物料，请使用\"查物料档案\"命令");

        meta.setTotalItems(rawItems.size());
        return response;
    }

    // ==================== Location/Container/Dispatch Response Builders ====================

    private AiResponses.ChatBIResponse buildLocationResponse(String query, String keyword,
                                                              List<Map<String, Object>> rawItems) {
        AiResponses.ChatBIResponse response = new AiResponses.ChatBIResponse();
        response.setQuery(query);
        response.setItems(new ArrayList<>());
        response.setLocationItems(new ArrayList<>());
        response.setSuggestions(new ArrayList<>());
        response.setActionButtons(new ArrayList<>());

        AiResponses.ChatMeta meta = new AiResponses.ChatMeta();
        meta.setParsedZone("全部区域");
        meta.setParsedType("LOCATION_QUERY");
        response.setMeta(meta);

        if (rawItems == null || rawItems.isEmpty()) {
            response.setSummary(keyword != null
                    ? "未找到匹配\"" + keyword + "\"的库位" : "暂无库位数据");
            return response;
        }
        for (Map<String, Object> row : rawItems) {
            response.getLocationItems().add(mapToLocationItem(row));
        }
        response.setSummary(keyword != null
                ? "共找到 " + rawItems.size() + " 项匹配\"" + keyword + "\"的库位"
                : "共 " + rawItems.size() + " 项库位");
        response.getSuggestions().add("共 " + rawItems.size() + " 个库位，可按库位号或库区进一步筛选");
        meta.setTotalItems(rawItems.size());
        return response;
    }

    private AiResponses.ChatBIResponse buildContainerResponse(String query, String keyword,
                                                               List<Map<String, Object>> rawItems) {
        AiResponses.ChatBIResponse response = new AiResponses.ChatBIResponse();
        response.setQuery(query);
        response.setItems(new ArrayList<>());
        response.setContainerItems(new ArrayList<>());
        response.setSuggestions(new ArrayList<>());
        response.setActionButtons(new ArrayList<>());

        AiResponses.ChatMeta meta = new AiResponses.ChatMeta();
        meta.setParsedZone("全部区域");
        meta.setParsedType("CONTAINER_QUERY");
        response.setMeta(meta);

        if (rawItems == null || rawItems.isEmpty()) {
            response.setSummary(keyword != null
                    ? "未找到匹配\"" + keyword + "\"的容器" : "暂无容器数据");
            return response;
        }
        for (Map<String, Object> row : rawItems) {
            response.getContainerItems().add(mapToContainerItem(row));
        }
        response.setSummary(keyword != null
                ? "共找到 " + rawItems.size() + " 项匹配\"" + keyword + "\"的容器"
                : "共 " + rawItems.size() + " 项容器");
        response.getSuggestions().add("共 " + rawItems.size() + " 个容器，可按编码或类型进一步筛选");
        meta.setTotalItems(rawItems.size());
        return response;
    }

    private AiResponses.ChatBIResponse buildSchedulerResponse(String query, String keyword,
                                                               List<Map<String, Object>> rawItems) {
        AiResponses.ChatBIResponse response = new AiResponses.ChatBIResponse();
        response.setQuery(query);
        response.setItems(new ArrayList<>());
        response.setSchedulerItems(new ArrayList<>());
        response.setSuggestions(new ArrayList<>());
        response.setActionButtons(new ArrayList<>());

        AiResponses.ChatMeta meta = new AiResponses.ChatMeta();
        meta.setParsedZone("全部区域");
        meta.setParsedType("SCHEDULER_QUERY");
        response.setMeta(meta);

        if (rawItems == null || rawItems.isEmpty()) {
            response.setSummary(keyword != null
                    ? "未找到匹配\"" + keyword + "\"的定时器" : "暂无定时器数据");
            return response;
        }
        for (Map<String, Object> row : rawItems) {
            response.getSchedulerItems().add(mapToSchedulerItem(row));
        }
        response.setSummary(keyword != null
                ? "共找到 " + rawItems.size() + " 项匹配\"" + keyword + "\"的定时器"
                : "共 " + rawItems.size() + " 项定时器");
        response.getSuggestions().add("共 " + rawItems.size() + " 个定时器，可按编码或类型进一步筛选");
        meta.setTotalItems(rawItems.size());
        return response;
    }

    private AiResponses.ChatBIResponse buildDispatchInfoResponse(String query, String keyword,
                                                                   List<Map<String, Object>> rawItems) {
        AiResponses.ChatBIResponse response = new AiResponses.ChatBIResponse();
        response.setQuery(query);
        response.setItems(new ArrayList<>());
        response.setDispatchInfoItems(new ArrayList<>());
        response.setSuggestions(new ArrayList<>());
        response.setActionButtons(new ArrayList<>());

        AiResponses.ChatMeta meta = new AiResponses.ChatMeta();
        meta.setParsedZone("全部区域");
        meta.setParsedType("DISPATCH_QUERY");
        response.setMeta(meta);

        if (rawItems == null || rawItems.isEmpty()) {
            response.setSummary(keyword != null
                    ? "未找到匹配\"" + keyword + "\"的调度记录" : "暂无调度记录");
            return response;
        }
        for (Map<String, Object> row : rawItems) {
            response.getDispatchInfoItems().add(mapToDispatchInfoItem(row));
        }
        response.setSummary(keyword != null
                ? "共找到 " + rawItems.size() + " 项匹配\"" + keyword + "\"的调度记录"
                : "共 " + rawItems.size() + " 项调度记录");
        response.getSuggestions().add("共 " + rawItems.size() + " 条调度记录，可按状态、类型进一步筛选");
        meta.setTotalItems(rawItems.size());
        return response;
    }

    private AiResponses.ChatBIResponse buildDispatchJobResponse(String query, String keyword,
                                                                  List<Map<String, Object>> rawItems) {
        AiResponses.ChatBIResponse response = new AiResponses.ChatBIResponse();
        response.setQuery(query);
        response.setItems(new ArrayList<>());
        response.setDispatchJobItems(new ArrayList<>());
        response.setSuggestions(new ArrayList<>());
        response.setActionButtons(new ArrayList<>());

        AiResponses.ChatMeta meta = new AiResponses.ChatMeta();
        meta.setParsedZone("全部区域");
        meta.setParsedType("DISPATCH_JOB_QUERY");
        response.setMeta(meta);

        if (rawItems == null || rawItems.isEmpty()) {
            response.setSummary(keyword != null
                    ? "未找到匹配\"" + keyword + "\"的任务" : "暂无任务数据");
            return response;
        }
        for (Map<String, Object> row : rawItems) {
            response.getDispatchJobItems().add(mapToDispatchJobItem(row));
        }
        response.setSummary(keyword != null
                ? "共找到 " + rawItems.size() + " 项匹配\"" + keyword + "\"的任务"
                : "共 " + rawItems.size() + " 项任务");
        response.getSuggestions().add("共 " + rawItems.size() + " 条任务，可按状态、类型进一步筛选");
        meta.setTotalItems(rawItems.size());
        return response;
    }

    // ==================== Mappers ====================

    private AiResponses.StorageLocationItem mapToLocationItem(Map<String, Object> row) {
        AiResponses.StorageLocationItem item = new AiResponses.StorageLocationItem();
        item.setLocNo(str(row.get("loc_no")));
        item.setHouseCode(str(row.get("house_code")));
        item.setXPos(str(row.get("x_pos")));
        item.setYPos(str(row.get("y_pos")));
        item.setZPos(str(row.get("z_pos")));
        item.setLocType(str(row.get("loc_type")));
        item.setStorageStatus(str(row.get("storage_status")));
        item.setLocUseStatus(str(row.get("loc_use_status")));
        item.setForbidIn(toBool(row.get("forbid_in")));
        item.setForbidOut(toBool(row.get("forbid_out")));
        item.setLocIsError(toBool(row.get("loc_is_error")));
        item.setLocErrorReason(str(row.get("loc_error_reason")));
        item.setActive(toBool(row.get("is_active")));
        return item;
    }

    private AiResponses.ContainerItem mapToContainerItem(Map<String, Object> row) {
        AiResponses.ContainerItem item = new AiResponses.ContainerItem();
        item.setContainerCode(str(row.get("container_code")));
        item.setContainerTypeCode(str(row.get("container_type_code")));
        item.setContainerTypeName(str(row.get("container_type_name")));
        item.setContainerLength(str(row.get("container_length")));
        item.setContainerWidth(str(row.get("container_width")));
        item.setContainerHeight(str(row.get("container_height")));
        item.setContainerWeight(str(row.get("container_weight")));
        item.setUsageCount(toInt(row.get("usage_count")));
        return item;
    }

    private AiResponses.DispatchInfoItem mapToDispatchInfoItem(Map<String, Object> row) {
        AiResponses.DispatchInfoItem item = new AiResponses.DispatchInfoItem();
        item.setDispatchCode(str(row.get("dispatch_code")));
        item.setDispatchName(str(row.get("dispatch_name")));
        item.setDispatchType(str(row.get("dispatch_type")));
        item.setDispatchStatus(str(row.get("dispatch_status")));
        item.setHouseCode(str(row.get("house_code")));
        item.setContainerCode(str(row.get("container_code")));
        item.setBusinessFormNo(str(row.get("business_form_no")));
        item.setBusinessFormType(str(row.get("business_form_type")));
        item.setCreateDatetime(str(row.get("create_datetime")));
        return item;
    }

    private AiResponses.DispatchJobItem mapToDispatchJobItem(Map<String, Object> row) {
        AiResponses.DispatchJobItem item = new AiResponses.DispatchJobItem();
        item.setTaskNo(str(row.get("task_no")));
        item.setTaskType(str(row.get("task_type")));
        item.setTaskStatus(str(row.get("task_status")));
        item.setHouseCode(str(row.get("house_code")));
        item.setContainerCode(str(row.get("container_code")));
        item.setFromPos(str(row.get("from_pos")));
        item.setToPos(str(row.get("to_pos")));
        item.setDispatchId(str(row.get("dispatch_id")));
        item.setSendTime(str(row.get("send_time")));
        item.setInTime(str(row.get("in_time")));
        item.setErrorCode(str(row.get("error_code")));
        item.setErrorDesc(str(row.get("error_desc")));
        item.setBusinessFormNo(str(row.get("business_form_no")));
        item.setBusinessFormType(str(row.get("business_form_type")));
        item.setTaskLevel(toInt(row.get("task_level")));
        item.setGroupCode(str(row.get("group_code")));
        return item;
    }

    private AiResponses.SchedulerManageItem mapToSchedulerItem(Map<String, Object> row) {
        AiResponses.SchedulerManageItem item = new AiResponses.SchedulerManageItem();
        item.setJobCode(str(row.get("job_code")));
        item.setTriggerCode(str(row.get("trigger_code")));
        item.setJobType(str(row.get("job_type")));
        item.setTriggerCorn(str(row.get("trigger_corn")));
        item.setNextTriggerDatetime(str(row.get("next_trigger_datetime")));
        item.setPause(toBool(row.get("is_pause")));
        item.setFinished(toBool(row.get("is_finished")));
        item.setDisable(toBool(row.get("is_disable")));
        item.setDescription(str(row.get("description")));
        item.setStartTimeSeconds(toInt(row.get("start_time_seconds")));
        return item;
    }

    private AiResponses.SkuItem mapToSkuItem(Map<String, Object> row) {
        AiResponses.SkuItem item = new AiResponses.SkuItem();
        item.setSkuCode(str(row.get("sku_code")));
        item.setSkuName(str(row.get("sku_name")));
        item.setCategoryCode(str(row.get("category_code")));
        item.setCategoryName(str(row.get("category_name")));
        item.setBarcode(str(row.get("barcode")));
        item.setPackageType(str(row.get("package_type")));
        item.setDurationOfValidity(str(row.get("duration_of_validity")));
        item.setValidityDateUnit(str(row.get("validity_date_unit")));
        item.setSkuBatchFlag(toBool(row.get("sku_batch_flag")));
        item.setSkuSerialFlag(toBool(row.get("sku_serial_flag")));
        item.setBlockState(toBool(row.get("block_state")));
        item.setActive(toBool(row.get("is_active")));
        return item;
    }

    private AiResponses.SkuCategoryItem mapToSkuCategoryItem(Map<String, Object> row) {
        AiResponses.SkuCategoryItem item = new AiResponses.SkuCategoryItem();
        item.setCategoryCode(str(row.get("category_code")));
        item.setCategoryName(str(row.get("category_name")));
        item.setCategoryDesc(str(row.get("category_desc")));
        item.setParentCategoryName(str(row.get("parent_category_name")));
        item.setTreeLevel(toInt(row.get("tree_level")));
        item.setChildCount(toInt(row.get("child_count")));
        item.setActive(toBool(row.get("is_active")));
        return item;
    }

    private boolean toBool(Object v) {
        if (v == null) return false;
        if (v instanceof Boolean) return (Boolean) v;
        if (v instanceof Number) return ((Number) v).intValue() == 1;
        return "1".equals(v.toString()) || "true".equalsIgnoreCase(v.toString());
    }

    private String str(Object v) {
        return v != null ? v.toString() : "";
    }

    private BigDecimal toDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal) return (BigDecimal) v;
        if (v instanceof Number) return BigDecimal.valueOf(((Number) v).doubleValue());
        return BigDecimal.ZERO;
    }

    private int toInt(Object v) {
        if (v == null) return 0;
        if (v instanceof Number) return ((Number) v).intValue();
        return 0;
    }
}
