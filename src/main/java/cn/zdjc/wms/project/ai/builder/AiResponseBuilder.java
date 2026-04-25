package cn.zdjc.wms.project.ai.builder;

import cn.zdjc.wms.project.ai.dto.AiResponses;
import cn.zdjc.wms.project.ai.dto.AiRequests;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * AI 响应结果构建器 - 统一构建各类 AI 响应结果
 */
@Component
public class AiResponseBuilder {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 构建空的预测结果
     */
    public AiResponses.PredictionResult buildEmptyPrediction(String message) {
        AiResponses.PredictionResult result = new AiResponses.PredictionResult();
        result.setConfidence(0.0);
        result.setPredictions(new ArrayList<>());
        result.setSuggestions(Collections.singletonList(message));
        return result;
    }

    /**
     * 构建空的路径优化结果
     */
    public AiResponses.PathResult buildEmptyPathResult(String warehouseId) {
        AiResponses.PathResult result = new AiResponses.PathResult();
        result.setPath(new ArrayList<>());
        result.setTotalDistance(0.0);
        result.setTotalTime(0);
        result.setImprovements(Collections.singletonList("暂无可优化路径"));
        return result;
    }

    /**
     * 构建模拟排班结果
     */
    public Map<String, Object> buildMockSchedule(Map<String, Object> request) {
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
            shiftLabels = new String[]{shift.equals("MORNING") ? "早班" : 
                                       shift.equals("AFTERNOON") ? "中班" : "晚班"};
        }

        LocalDate date = scheduleDate != null && !scheduleDate.isEmpty()
                ? LocalDate.parse(scheduleDate)
                : LocalDate.now();

        Random random = new Random();
        for (int i = 0; i < shiftTypes.length; i++) {
            Map<String, Object> s = new HashMap<>();
            s.put("date", date.format(DATE_FORMATTER));
            s.put("shift", shiftTypes[i]);
            s.put("shiftLabel", shiftLabels[i]);
            int staffCount = 2 + random.nextInt(3);
            List<String> staffNames = new ArrayList<>();
            for (int j = 0; j < staffCount; j++) {
                staffNames.add(staffPool[random.nextInt(staffPool.length)]);
            }
            s.put("staff", String.join(",", staffNames));
            s.put("position", positions[random.nextInt(positions.length)]);
            s.put("status", i == 0 ? "CONFIRMED" : "PENDING");
            shifts.add(s);
        }

        List<String> suggestions = new ArrayList<>();
        suggestions.add("建议早班安排 " + (3 + random.nextInt(2)) + " 人，中班 " + (2 + random.nextInt(2)) + " 人");
        suggestions.add("仓库 " + warehouseId + " 当前人力配置合理，无需调整");

        Map<String, Object> result = new HashMap<>();
        result.put("shifts", shifts);
        result.put("suggestions", suggestions);
        return result;
    }

    /**
     * 构建模拟调拨结果
     */
    public Map<String, Object> buildMockAllocation(Map<String, Object> request) {
        String source = (String) request.getOrDefault("sourceWarehouseId", "WH001");
        String target = (String) request.getOrDefault("targetWarehouseId", "WH002");
        String skuCode = (String) request.getOrDefault("skuCode", "SKU001");
        int quantity = request.get("quantity") instanceof Number
                ? ((Number) request.get("quantity")).intValue() : 1;

        String allocationNo = "ALLOC-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Map<String, Object> result = new HashMap<>();
        result.put("allocationNo", allocationNo);
        result.put("skuCode", skuCode);
        result.put("sourceWarehouse", "仓库" + source.replace("WH", ""));
        result.put("targetWarehouse", "仓库" + target.replace("WH", ""));
        result.put("quantity", quantity);
        result.put("status", "COMPLETED");
        result.put("estimatedCompletion", LocalDate.now().plusHours(2)
                .atStartOfDay().plusHours(2).format(DATETIME_FORMATTER));

        List<String> suggestions = new ArrayList<>();
        suggestions.add("调拨 " + quantity + " 件 " + skuCode + " 从仓库" + source.replace("WH", "")
                + " 到仓库" + target.replace("WH", ""));
        suggestions.add("目标仓库现有可用库存充足，可以接收");
        result.put("suggestions", suggestions);
        return result;
    }

    /**
     * 构建预测建议列表
     */
    public List<String> buildPredictionSuggestions(
            String skuCode, 
            double avgDailyOutbound, 
            double currentStock,
            int forecastDays) {
        
        List<String> suggestions = new ArrayList<>();
        
        double totalForecastDemand = avgDailyOutbound * forecastDays;
        double stockCoverageDays = avgDailyOutbound > 0 ? currentStock / avgDailyOutbound : 999;
        
        if (stockCoverageDays < 7) {
            suggestions.add(String.format("警告：%s 库存仅够 %.1f 天，建议立即补货", skuCode, stockCoverageDays));
        } else if (stockCoverageDays < 14) {
            suggestions.add(String.format("提示：%s 库存可维持 %.1f 天，请关注库存变化", skuCode, stockCoverageDays));
        } else if (stockCoverageDays > 60) {
            suggestions.add(String.format("注意：%s 库存过高 (%.1f 天)，可能存在积压风险", skuCode, stockCoverageDays));
        } else {
            suggestions.add(String.format("%s 库存水平正常，可维持 %.1f 天", skuCode, stockCoverageDays));
        }
        
        if (avgDailyOutbound > 0) {
            suggestions.add(String.format("预计未来 %d 天总需求：%.0f 件", forecastDays, totalForecastDemand));
        }
        
        return suggestions;
    }
}
