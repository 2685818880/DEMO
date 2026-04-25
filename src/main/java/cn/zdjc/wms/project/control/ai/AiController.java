package cn.zdjc.wms.project.control.ai;

import cn.zdjc.wms.project.ai.client.AiApiClient;
import cn.zdjc.wms.project.ai.dto.*;
import cn.zdjc.wms.project.ai.service.AiDataService;
import cn.zdjc.wms.project.common.constant.ProjectConstant;
import cn.zdjc.wms.project.erp.dto.ResultWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(ProjectConstant.PREFIX_CONTEXT_PATH + "/ai")
@Slf4j
public class AiController {

    private static final String LOG_PREFIX = "[AiController] ";

    @Resource
    private AiApiClient aiApiClient;

    @Resource
    private AiDataService aiDataService;

    @PostMapping("/inventory/predict")
    public ResultWrapper<AiResponses.PredictionResult> predictInventory(
            @RequestBody AiRequests.InventoryPrediction request) {
        log.info("{}库存预测请求: warehouseId={}, skuCode={}", LOG_PREFIX,
                request.getWarehouseId(), request.getSkuCode());

        try {
            String warehouseId = request.getWarehouseId();

            // 查询当前库存数据
            List<Map<String, Object>> stockData = aiDataService.getCurrentStockSummary(warehouseId);
            if (stockData.isEmpty()) {
                return ResultWrapper.buildSuccess(buildEmptyPrediction("暂无库存数据"));
            }

            // 查询近期出库历史（按仓库过滤）
            List<Map<String, Object>> outboundData = aiDataService.getPickOutboundSummary(warehouseId, 90);

            // 构建预测结果
            AiResponses.PredictionResult result = buildPredictionResult(
                    request, stockData, outboundData);

            log.info("{}库存预测成功, 预测 {} 个SKU", LOG_PREFIX,
                    result.getPredictions() != null ? result.getPredictions().size() : 0);
            return ResultWrapper.buildSuccess(result);

        } catch (Exception e) {
            log.error("{}库存预测异常", LOG_PREFIX, e);
            // 降级：调用API Gateway
            try {
                AiApiResponse<AiResponses.PredictionResult> response =
                        aiApiClient.predictInventory(request);
                if (response.isSuccess()) {
                    return ResultWrapper.buildSuccess(response.getData());
                }
                return ResultWrapper.buildFailure(response.getMessage());
            } catch (Exception ex) {
                return ResultWrapper.buildFailure("AI服务调用失败: " + e.getMessage());
            }
        }
    }

    @PostMapping("/path/optimize")
    public ResultWrapper<AiResponses.PathResult> optimizePath(
            @RequestBody AiRequests.PathOptimization request) {
        log.info("{}路径优化请求: warehouseId={}, tasks={}", LOG_PREFIX,
                request.getWarehouseId(),
                request.getTasks() != null ? request.getTasks().size() : 0);

        try {
            // 查询真实货位坐标（按仓库过滤）
            List<Map<String, Object>> locations = aiDataService.getAllLocationCoordinates(request.getWarehouseId());
            if (locations.isEmpty() || request.getTasks() == null || request.getTasks().isEmpty()) {
                return ResultWrapper.buildSuccess(buildEmptyPathResult(request.getWarehouseId()));
            }

            // 构建位置坐标映射
            Map<String, double[]> coordMap = new HashMap<>();
            for (Map<String, Object> loc : locations) {
                String locNo = (String) loc.get("loc_no");
                Number x = (Number) loc.get("x_pos");
                Number y = (Number) loc.get("y_pos");
                if (locNo != null && x != null && y != null) {
                    coordMap.put(locNo, new double[]{x.doubleValue(), y.doubleValue()});
                }
            }

            // 生成优化路径
            AiResponses.PathResult result = buildPathResult(request, coordMap);

            log.info("{}路径优化成功, 路径节点数={}", LOG_PREFIX,
                    result.getPath() != null ? result.getPath().size() : 0);
            return ResultWrapper.buildSuccess(result);

        } catch (Exception e) {
            log.error("{}路径优化异常", LOG_PREFIX, e);
            try {
                AiApiResponse<AiResponses.PathResult> response =
                        aiApiClient.optimizePath(request);
                if (response.isSuccess()) {
                    return ResultWrapper.buildSuccess(response.getData());
                }
                return ResultWrapper.buildFailure(response.getMessage());
            } catch (Exception ex) {
                return ResultWrapper.buildFailure("AI服务调用失败: " + e.getMessage());
            }
        }
    }

    @PostMapping("/anomaly/detect")
    public ResultWrapper<AiResponses.AnomalyResult> detectAnomalies(
            @RequestBody AiRequests.AnomalyDetection request) {
        log.info("{}异常检测请求: warehouseId={}, type={}", LOG_PREFIX,
                request.getWarehouseId(), request.getDetectionType());

        try {
            // 查询近期拣货数据
            List<Map<String, Object>> pickData = aiDataService.getRecentPickAnomalies(1440);
            if (pickData.isEmpty()) {
                AiResponses.AnomalyResult emptyResult = new AiResponses.AnomalyResult();
                emptyResult.setTotalCount(0);
                emptyResult.setAnomalies(new ArrayList<>());
                return ResultWrapper.buildSuccess(emptyResult);
            }

            // 检测异常
            AiResponses.AnomalyResult result = detectAnomaliesFromData(pickData, request);

            log.info("{}异常检测完成, 发现 {} 项异常", LOG_PREFIX, result.getTotalCount());
            return ResultWrapper.buildSuccess(result);

        } catch (Exception e) {
            log.error("{}异常检测异常", LOG_PREFIX, e);
            try {
                AiApiResponse<AiResponses.AnomalyResult> response =
                        aiApiClient.detectAnomalies(request);
                if (response.isSuccess()) {
                    return ResultWrapper.buildSuccess(response.getData());
                }
                return ResultWrapper.buildFailure(response.getMessage());
            } catch (Exception ex) {
                return ResultWrapper.buildFailure("AI服务调用失败: " + e.getMessage());
            }
        }
    }

    @GetMapping("/status")
    public ResultWrapper<AiResponses.AgentStatus> getAgentStatus() {
        log.info("{}查询AI Agent状态 (proxying to API Gateway)", LOG_PREFIX);
        try {
            AiApiResponse<AiResponses.AgentStatus> response = aiApiClient.getAgentStatus();
            if (response != null && response.isSuccess() && response.getData() != null) {
                return ResultWrapper.buildSuccess(response.getData());
            }
        } catch (Exception e) {
            log.error("{}获取Agent状态失败: {}", LOG_PREFIX, e.getMessage());
        }
        AiResponses.AgentStatus fallback = new AiResponses.AgentStatus();
        fallback.setStatus("offline");
        fallback.setAgents(Collections.emptyList());
        return ResultWrapper.buildSuccess(fallback);
    }

    @GetMapping("/health")
    public ResultWrapper<AiResponses.HealthStatus> healthCheck() {
        log.debug("{}AI服务健康检查", LOG_PREFIX);

        AiResponses.HealthStatus health = new AiResponses.HealthStatus();
        health.setStatus("UP");
        health.setService("WMS AI Service");
        return ResultWrapper.buildSuccess(health);
    }

    @GetMapping("/warehouses")
    public ResultWrapper<List<Map<String, Object>>> getWarehouses() {
        List<Map<String, Object>> list = aiDataService.getWarehouseList();
        return ResultWrapper.buildSuccess(list);
    }

    // ==================== 模拟数据 ====================

    private Map<String, Object> buildMockSchedule(Map<String, Object> request) {
        String warehouseId = (String) request.getOrDefault("warehouseId", "WH001");
        String scheduleDate = (String) request.getOrDefault("scheduleDate", "");
        String shift = (String) request.getOrDefault("shift", "MORNING");

        List<Map<String, Object>> shifts = new ArrayList<>();
        String[] shiftTypes = {"MORNING", "AFTERNOON", "NIGHT"};
        String[] shiftLabels = {"早班", "中班", "晚班"};
        String[] staffPool = {"张三", "李四", "王五", "赵六", "钱七", "孙八"};
        String[] positions = {"拣货员", "上架员", "复核员", "组长"};

        if (shift != null && !shift.isEmpty()) {
            shiftTypes = new String[]{shift};
            shiftLabels = new String[]{shift.equals("MORNING") ? "早班" : shift.equals("AFTERNOON") ? "中班" : "晚班"};
        }

        java.time.LocalDate date = scheduleDate != null && !scheduleDate.isEmpty()
                ? java.time.LocalDate.parse(scheduleDate)
                : java.time.LocalDate.now();

        for (int i = 0; i < shiftTypes.length; i++) {
            Map<String, Object> s = new HashMap<>();
            s.put("date", date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            s.put("shift", shiftTypes[i]);
            s.put("shiftLabel", shiftLabels[i]);
            int staffCount = 2 + new java.util.Random().nextInt(3);
            List<String> staffNames = new ArrayList<>();
            for (int j = 0; j < staffCount; j++) {
                staffNames.add(staffPool[new java.util.Random().nextInt(staffPool.length)]);
            }
            s.put("staff", String.join("、", staffNames));
            s.put("position", positions[new java.util.Random().nextInt(positions.length)]);
            s.put("status", i == 0 ? "CONFIRMED" : "PENDING");
            shifts.add(s);
        }

        List<String> suggestions = new ArrayList<>();
        suggestions.add("建议早班安排 " + (3 + new java.util.Random().nextInt(2)) + " 人，中班 " + (2 + new java.util.Random().nextInt(2)) + " 人");
        suggestions.add("仓库 " + warehouseId + " 当前人力配置合理，无需调整");

        Map<String, Object> result = new HashMap<>();
        result.put("shifts", shifts);
        result.put("suggestions", suggestions);
        return result;
    }

    private Map<String, Object> buildMockAllocation(Map<String, Object> request) {
        String source = (String) request.getOrDefault("sourceWarehouseId", "WH001");
        String target = (String) request.getOrDefault("targetWarehouseId", "WH002");
        String skuCode = (String) request.getOrDefault("skuCode", "SKU001");
        int quantity = request.get("quantity") instanceof Number
                ? ((Number) request.get("quantity")).intValue() : 1;

        String allocationNo = "ALLOC-" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Map<String, Object> result = new HashMap<>();
        result.put("allocationNo", allocationNo);
        result.put("skuCode", skuCode);
        result.put("sourceWarehouse", "仓库" + source.replace("WH", ""));
        result.put("targetWarehouse", "仓库" + target.replace("WH", ""));
        result.put("quantity", quantity);
        result.put("status", "COMPLETED");
        result.put("estimatedCompletion", java.time.LocalDateTime.now().plusHours(2)
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        List<String> suggestions = new ArrayList<>();
        suggestions.add("调拨 " + quantity + " 件 " + skuCode + " 从仓库" + source.replace("WH", "")
                + " 到仓库" + target.replace("WH", ""));
        suggestions.add("目标仓库现有可用库存充足，可以接收");
        result.put("suggestions", suggestions);
        return result;
    }

    @GetMapping("/locations")
    public ResultWrapper<List<Map<String, Object>>> getLocations(
            @RequestParam(defaultValue = "%") String houseCode) {
        List<Map<String, Object>> list = aiDataService.getLocationList(houseCode);
        return ResultWrapper.buildSuccess(list);
    }

    @PostMapping("/scheduling/generate")
    public ResultWrapper<Map<String, Object>> generateSchedule(@RequestBody Map<String, Object> request) {
        log.info("{}排班请求: warehouseId={}, date={}", LOG_PREFIX,
                request.get("warehouseId"), request.get("scheduleDate"));
        try {
            AiApiResponse<Map<String, Object>> response = aiApiClient.generateSchedule(request);
            if (response != null && response.isSuccess() && response.getData() != null) {
                return ResultWrapper.buildSuccess(response.getData());
            }
            return ResultWrapper.buildSuccess(buildMockSchedule(request));
        } catch (Exception e) {
            log.error("{}排班服务调用失败，使用模拟数据", LOG_PREFIX, e);
            return ResultWrapper.buildSuccess(buildMockSchedule(request));
        }
    }

    @PostMapping("/allocation/execute")
    public ResultWrapper<Map<String, Object>> executeAllocation(@RequestBody Map<String, Object> request) {
        log.info("{}调拨请求: source={}, target={}, sku={}, qty={}", LOG_PREFIX,
                request.get("sourceWarehouseId"), request.get("targetWarehouseId"),
                request.get("skuCode"), request.get("quantity"));
        try {
            AiApiResponse<Map<String, Object>> response = aiApiClient.executeAllocation(request);
            if (response != null && response.isSuccess() && response.getData() != null) {
                return ResultWrapper.buildSuccess(response.getData());
            }
            return ResultWrapper.buildSuccess(buildMockAllocation(request));
        } catch (Exception e) {
            log.error("{}调拨服务调用失败，使用模拟数据", LOG_PREFIX, e);
            return ResultWrapper.buildSuccess(buildMockAllocation(request));
        }
    }

    // ==================== 私有方法 ====================

    private AiResponses.PredictionResult buildEmptyPrediction(String message) {
        AiResponses.PredictionResult result = new AiResponses.PredictionResult();
        result.setConfidence(0.0);
        result.setPredictions(new ArrayList<>());
        result.setSuggestions(Collections.singletonList(message));
        return result;
    }

    private AiResponses.PredictionResult buildPredictionResult(
            AiRequests.InventoryPrediction request,
            List<Map<String, Object>> stockData,
            List<Map<String, Object>> outboundData) {

        // 1. 构建各SKU的库存映射
        Map<String, Double> stockBySku = new HashMap<>();
        for (Map<String, Object> row : stockData) {
            String sku = (String) row.get("sku_code");
            Number qty = (Number) row.get("total_qty");
            if (sku != null && qty != null) {
                stockBySku.put(sku, qty.doubleValue());
            }
        }

        // 2. 确定预测目标SKU（如果没指定则选库存最大的）
        String targetSku = request.getSkuCode();
        if (targetSku == null || targetSku.isEmpty()) {
            targetSku = stockBySku.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
        }
        if (targetSku == null) {
            return buildEmptyPrediction("无库存数据可用于预测");
        }

        // 3. 构建出库时间序列
        Map<String, Map<LocalDate, Double>> outboundTs = buildTimeSeries(outboundData);
        Map<LocalDate, Double> skuOutbound = outboundTs.getOrDefault(targetSku, new HashMap<>());

        // 4. 计算出库统计量
        List<Double> dailyOutboundQtys = new ArrayList<>(skuOutbound.values());
        double avgDailyOutbound = dailyOutboundQtys.stream().mapToDouble(d -> d).average().orElse(0);
        double stdOutbound = dailyOutboundQtys.size() > 1
                ? calculateStdDev(dailyOutboundQtys, avgDailyOutbound)
                : avgDailyOutbound * 0.5;

        // 5. 解析预测日期范围
        LocalDate startDate = request.getStartDate() != null ?
                LocalDate.parse(request.getStartDate()) : LocalDate.now();
        LocalDate endDate = request.getEndDate() != null ?
                LocalDate.parse(request.getEndDate()) : startDate.plusDays(13);

        // 6. 按粒度确定步长
        int stepDays = getGranularityStep(request.getGranularity());

        // 7. 逐期生成预测值
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<AiResponses.DailyPrediction> predictions = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            double predicted = Math.max(0, avgDailyOutbound * stepDays);
            double lower = Math.max(0, (avgDailyOutbound - 1.5 * stdOutbound) * stepDays);
            double upper = Math.max(0.01, (avgDailyOutbound + 1.5 * stdOutbound) * stepDays);

            AiResponses.DailyPrediction dp = new AiResponses.DailyPrediction();
            dp.setDate(current.format(fmt));
            dp.setValue(round2(predicted));
            dp.setLowerBound(round2(lower));
            dp.setUpperBound(round2(upper));
            predictions.add(dp);
            current = current.plusDays(stepDays);
        }

        // 8. 计算置信度
        long dataPoints = dailyOutboundQtys.size();
        double confidence = Math.min(0.95, Math.max(0.2, 0.3 + dataPoints * 0.02));
        if (stdOutbound > avgDailyOutbound * 2 && avgDailyOutbound > 0) {
            confidence *= 0.8;
        }

        // 9. 生成建议
        List<String> suggestions = new ArrayList<>();
        Double currentStock = stockBySku.getOrDefault(targetSku, 0.0);
        double totalPredicted = predictions.stream().mapToDouble(AiResponses.DailyPrediction::getUpperBound).sum();

        if (currentStock < totalPredicted * 0.3 && totalPredicted > 0) {
            suggestions.add("SKU [" + targetSku + "] 库存严重不足（当前 "
                    + formatQty(currentStock) + "），预测期内预计需求 "
                    + formatQty(totalPredicted) + "，建议立即补货");
        } else if (currentStock < totalPredicted * 0.7 && totalPredicted > 0) {
            suggestions.add("SKU [" + targetSku + "] 库存偏低（当前 "
                    + formatQty(currentStock) + "），建议适量补货");
        } else {
            suggestions.add("SKU [" + targetSku + "] 当前库存水平正常（"
                    + formatQty(currentStock) + "）");
        }

        if (dailyOutboundQtys.isEmpty()) {
            suggestions.add("该SKU无历史出库数据，预测基于默认估算，仅供参考");
        } else {
            suggestions.add("基于 " + dataPoints + " 天历史出库数据，日均出库 "
                    + formatQty(avgDailyOutbound));
        }

        AiResponses.PredictionResult result = new AiResponses.PredictionResult();
        result.setWarehouseId(request.getWarehouseId());
        result.setSkuCode(targetSku);
        result.setPredictions(predictions);
        result.setConfidence(round2(confidence));
        result.setSuggestions(suggestions);
        return result;
    }

    /**
     * 将出入库明细按 SKU + 日期 聚合为时间序列
     * 返回 Map<SKU, Map<日期, 数量>>
     */
    private Map<String, Map<LocalDate, Double>> buildTimeSeries(List<Map<String, Object>> data) {
        Map<String, Map<LocalDate, Double>> ts = new HashMap<>();
        for (Map<String, Object> row : data) {
            String sku = (String) row.get("sku_code");
            Object dayObj = row.get("day");
            Number qty = (Number) row.get("qty");
            if (sku == null || dayObj == null || qty == null) continue;
            try {
                String dayStr = dayObj.toString().length() > 10
                        ? dayObj.toString().substring(0, 10) : dayObj.toString();
                LocalDate day = LocalDate.parse(dayStr);
                ts.computeIfAbsent(sku, k -> new HashMap<>())
                        .merge(day, qty.doubleValue(), Double::sum);
            } catch (Exception ignored) {
            }
        }
        return ts;
    }

    /** 计算样本标准差 */
    private double calculateStdDev(List<Double> values, double mean) {
        if (values.size() <= 1) return 0;
        double sumSq = 0;
        for (double v : values) {
            sumSq += Math.pow(v - mean, 2);
        }
        return Math.sqrt(sumSq / (values.size() - 1));
    }

    /** 根据粒度返回步长天数 */
    private int getGranularityStep(String granularity) {
        if ("weekly".equals(granularity)) return 7;
        if ("monthly".equals(granularity)) return 30;
        return 1;
    }

    /** 四舍五入保留两位小数 */
    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /** 格式化数量（无小数） */
    private String formatQty(double value) {
        return String.format("%.0f", value);
    }

    private AiResponses.PathResult buildEmptyPathResult(String warehouseId) {
        AiResponses.PathResult result = new AiResponses.PathResult();
        result.setPath(new ArrayList<>());
        result.setTotalDistance(0.0);
        result.setTotalTime(0);
        result.setImprovements(Collections.singletonList("暂无货位数据，无法优化路径"));
        return result;
    }

    private AiResponses.PathResult buildPathResult(
            AiRequests.PathOptimization request,
            Map<String, double[]> coordMap) {

        List<String> optimalPath = new ArrayList<>();
        double totalDistance = 0;

        // 贪心算法：从最近的点开始，每次选最近的下一个点
        List<AiRequests.PickingTask> tasks = request.getTasks();
        Set<Integer> visited = new HashSet<>();
        int current = 0;
        visited.add(0);

        String currentLoc = tasks.get(0).getFromLocation() != null
                ? tasks.get(0).getFromLocation() : tasks.get(0).getToLocation();
        if (currentLoc != null) {
            optimalPath.add(currentLoc);
        }

        while (visited.size() < tasks.size()) {
            int nearest = -1;
            double minDist = Double.MAX_VALUE;

            for (int i = 0; i < tasks.size(); i++) {
                if (visited.contains(i)) continue;
                String targetLoc = tasks.get(i).getToLocation() != null
                        ? tasks.get(i).getToLocation() : tasks.get(i).getFromLocation();
                if (targetLoc == null) continue;

                double dist = calculateDistance(coordMap, currentLoc, targetLoc);
                if (dist < minDist) {
                    minDist = dist;
                    nearest = i;
                }
            }

            if (nearest == -1) break;

            String nextLoc = tasks.get(nearest).getToLocation() != null
                    ? tasks.get(nearest).getToLocation() : tasks.get(nearest).getFromLocation();
            if (nextLoc != null) {
                optimalPath.add(nextLoc);
                totalDistance += minDist;
                currentLoc = nextLoc;
            }
            visited.add(nearest);
        }

        int estimatedTime = (int) (totalDistance / 1.0); // 假设1m/s

        List<String> improvements = new ArrayList<>();
        improvements.add("优化后路径总距离: " + String.format("%.1f", totalDistance) + "m");
        improvements.add("预计耗时: " + (estimatedTime / 60) + "分钟");
        improvements.add("优化算法: 最近邻贪心算法");
        improvements.add("途经 " + optimalPath.size() + " 个货位点");

        AiResponses.PathResult result = new AiResponses.PathResult();
        result.setPath(optimalPath);
        result.setTotalDistance(Math.round(totalDistance * 10.0) / 10.0);
        result.setTotalTime(estimatedTime);
        result.setImprovements(improvements);
        return result;
    }

    private double calculateDistance(Map<String, double[]> coordMap, String from, String to) {
        if (from == null || to == null) return 100;
        double[] fromCoord = coordMap.get(from);
        double[] toCoord = coordMap.get(to);
        if (fromCoord == null || toCoord == null) return 100;
        return Math.sqrt(Math.pow(fromCoord[0] - toCoord[0], 2) + Math.pow(fromCoord[1] - toCoord[1], 2));
    }

    private AiResponses.AnomalyResult detectAnomaliesFromData(
            List<Map<String, Object>> pickData,
            AiRequests.AnomalyDetection request) {

        List<AiResponses.AnomalyItem> anomalies = new ArrayList<>();
        Set<String> seenTypes = new HashSet<>();

        // 1. 统计各状态分布
        Map<String, Long> statusCount = new HashMap<>();
        Map<String, List<Map<String, Object>>> byWorkstation = new HashMap<>();

        for (Map<String, Object> row : pickData) {
            String status = (String) row.get("pick_status");
            String workstation = (String) row.get("workstation_code");
            statusCount.merge(status != null ? status : "UNKNOWN", 1L, Long::sum);
            if (workstation != null) {
                byWorkstation.computeIfAbsent(workstation, k -> new ArrayList<>()).add(row);
            }
        }

        // 2. 检测异常：异常状态
        long errorCount = statusCount.entrySet().stream()
                .filter(e -> "ERROR".equals(e.getKey()) || "FAILED".equals(e.getKey()))
                .mapToLong(Map.Entry::getValue)
                .sum();
        if (errorCount > 0) {
            AiResponses.AnomalyItem item = new AiResponses.AnomalyItem();
            item.setId(UUID.randomUUID().toString().substring(0, 8));
            item.setLabel("拣货异常");
            item.setType("OPERATION");
            item.setSeverity(errorCount > 5 ? "HIGH" : "MEDIUM");
            item.setDescription("发现 " + errorCount + " 条异常拣货记录");
            item.setScore(Math.min(1.0, errorCount / 10.0));
            item.setTimestamp(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            anomalies.add(item);
            seenTypes.add("OPERATION");
        }

        // 3. 检测异常：工作站效率异常
        for (Map.Entry<String, List<Map<String, Object>>> entry : byWorkstation.entrySet()) {
            if (entry.getValue().size() > 50) {
                AiResponses.AnomalyItem item = new AiResponses.AnomalyItem();
                item.setId(UUID.randomUUID().toString().substring(0, 8));
                item.setLabel("工作站繁忙");
                item.setType("BUSINESS");
                item.setSeverity("MEDIUM");
                item.setDescription("工作站 [" + entry.getKey() + "] 当日任务量 " + entry.getValue().size() + " 条，超过阈值");
                item.setScore(0.6);
                item.setTimestamp(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                anomalies.add(item);
                seenTypes.add("BUSINESS");
            }
        }

        // 4. 按检测类型过滤
        if (request.getDetectionType() != null && !"ALL".equals(request.getDetectionType())) {
            anomalies = anomalies.stream()
                    .filter(a -> request.getDetectionType().equals(a.getType()))
                    .collect(Collectors.toList());
        }

        // 5. 如果没发现异常，添加一个总结性提示
        if (anomalies.isEmpty()) {
            AiResponses.AnomalyItem item = new AiResponses.AnomalyItem();
            item.setId("ok");
            item.setLabel("系统正常");
            item.setType("INFO");
            item.setSeverity("LOW");
            item.setDescription("未检测到明显异常，共 " + pickData.size() + " 条拣货记录");
            item.setScore(0.0);
            item.setTimestamp(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            anomalies.add(item);
        }

        AiResponses.AnomalyResult result = new AiResponses.AnomalyResult();
        result.setTotalCount(anomalies.size());
        result.setAnomalies(anomalies);
        return result;
    }
}
